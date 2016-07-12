/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.fileupload

import java.io.{File, FileOutputStream}

import play.api.libs.iteratee.{Enumerator, Iteratee}
import play.api.mvc.MultipartFormData
import play.api.mvc.MultipartFormData.{BadPart, MissingFilePart}
import play.api.test.{FakeHeaders, FakeRequest}
import uk.gov.hmrc.clamav.VirusChecker
import uk.gov.hmrc.fileupload.Errors.EnvelopeValidationError
import uk.gov.hmrc.fileupload.connectors._
import uk.gov.hmrc.fileupload.controllers.FileUploadController
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.http.{HeaderCarrier, HttpGet}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import org.scalatest.time.SpanSugar._

object UploadFixtures {
  import scala.concurrent.ExecutionContext.Implicits.global

  val tmpDir = System.getProperty("java.io.tmpdir")
  val validEnvelopeId = "fea8cc15-f2d1-4eb5-b10e-b892bcbe94f8"
  val validFileId = "0987654321"
  val fileController = new TestFileUploadController {}

  trait TestFileUploadController extends FileUploadController with TestFileUploadConnector with TmpFileQuarantineStoreConnector with TestAvScannerConnector

  trait TestAvScannerConnector extends AvScannerConnector {
    val fail: Try[Boolean] = Success(true)
    val pause = (0 seconds) toMillis
    def sentData = virusChecker.asInstanceOf[DelayCheckingVirusChecker].sentData

    lazy val virusChecker: VirusChecker = new DelayCheckingVirusChecker {
      override val response = fail
      override val delay = pause
    }
  }

  trait DelayCheckingVirusChecker extends VirusChecker {
    var sentData: Array[Byte] = Array()
    val response: Try[Boolean] = Success(true)
    val delay = (0 seconds) toMillis

    var scanInitiated = false
    var scanCompleted = false

    override def send(bytes: Array[Byte])(implicit ec: ExecutionContext) = {
      Future {
        scanInitiated = true
        sentData = sentData ++ bytes
      }
    }

    override def finish()(implicit ec : ExecutionContext) = {
      Future {
        Thread.sleep(delay)
        response
      }.map { r =>
        scanCompleted = true
        r
      }
    }
  }

  trait TestServicesConfig extends ServicesConfig {
    override def baseUrl(serviceName: String): String = null
  }

  trait TestFileUploadConnector extends FileUploadConnector with TestServicesConfig {

    override def validate(envelopeId: String)(implicit hc: HeaderCarrier): Future[Try[String]] = {
      Future.successful(envelopeId match {
        case "INVALID" => Failure(EnvelopeValidationError(envelopeId))
        case _ => Success(envelopeId)
      })
    }

    override val http: HttpGet = null
  }

  def toStringIteratee = Iteratee.fold[Array[Byte], String]("") { (s, d) => s ++ d.toString }

  def toFileIteratee(filename: String) = {
    val fos: FileOutputStream = new FileOutputStream(new File(filename))

    Iteratee.fold[Array[Byte], FileOutputStream](fos) { (f, d) => f.write(d); f } map { fos => fos.close() }
  }

  trait TmpFileQuarantineStoreConnector extends QuarantineStoreConnector {
    def deleteFileBeforeWrite(file: FileData) = Future.successful(())

    override def writeFile(file: FileData) = {
      val it = toFileIteratee(s"$tmpDir/${file.envelopeId}-${file.fileId}.Unscanned") map { _ => Success(file.envelopeId) }
      file.data |>>> it
    }

    override def list(state: FileState): Future[Seq[FileData]] = {
      Future.successful {
        new File(s"$tmpDir").listFiles.filter(_.getName.endsWith(s".$state")).toList.map { f =>
          FileData(Enumerator.fromFile(f), f.getName, "n/a", f.getName.split("-").head, f.getName.split("-").tail.head)
        }
      }
    }
  }

  def file(name:String) = Try(Enumerator.fromFile(new File(s"test/resources/$name"))).getOrElse(Enumerator.empty)

  def filePart(name:String) = MultipartFormData.FilePart(name, name, Some("text/plain"), file(name))

  def createUploadRequest(successRedirectURL:Option[String] = Some("http://somewhere.com/success"),
                          failureRedirectURL:Option[String] = Some("http://somewhere.com/failure"),
                          envelopeId:Option[String] = Some(validEnvelopeId),
                          fileIds:Seq[String] = Seq("testUpload.txt"),
                          headers:Seq[(String, Seq[String])] = Seq()) = {
    var params = Map[String, Seq[String]]()

    def addParam(paramName: String)(value:String) = params = params + (paramName -> Seq(value))

    successRedirectURL.foreach(addParam("successRedirect"))
    failureRedirectURL.foreach(addParam("failureRedirect"))
    envelopeId.foreach(addParam("envelopeId"))

    val multipartBody = MultipartFormData[Enumerator[Array[Byte]]](params, fileIds.map(filePart), Seq[BadPart](), Seq[MissingFilePart]())

    FakeRequest(method = "POST", uri = "/upload", headers = FakeHeaders(headers), body = multipartBody)
  }
}
