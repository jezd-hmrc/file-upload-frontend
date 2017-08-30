/*
 * Copyright 2017 HM Revenue & Customs
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

package uk.gov.hmrc.fileupload.s3

import java.io.InputStream

import akka.NotUsed
import akka.stream.IOResult
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.amazonaws.services.s3.model.{CopyObjectResult, S3ObjectSummary}
import com.amazonaws.services.s3.transfer.model.UploadResult
import play.api.libs.json.JsValue
import uk.gov.hmrc.fileupload.quarantine.FileData
import uk.gov.hmrc.fileupload.s3.S3Service.{DownloadFromBucket, StreamResult, UploadToQuarantine}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait S3Service {
  def awsConfig: AwsConfig

  def download(bucketName: String, key: S3KeyName): Option[StreamWithMetadata]

  def download(bucketName: String, key: S3KeyName, versionId: String): Option[StreamWithMetadata]

  def retrieveFileFromQuarantine(key: String, versionId: String)(implicit ec: ExecutionContext): Future[Option[FileData]]

  def upload(bucketName: String, key: String, file: InputStream, fileSize: Int): Future[UploadResult]

  def uploadToQuarantine: UploadToQuarantine = upload(awsConfig.quarantineBucketName, _, _, _)

  def downloadFromTransient: DownloadFromBucket = download(awsConfig.transientBucketName, _)

  def downloadFromQuarantine: DownloadFromBucket = download(awsConfig.quarantineBucketName, _)

  def listFilesInBucket(bucketName: String): Source[Seq[S3ObjectSummary], NotUsed]

  def listFilesInQuarantine: Source[Seq[S3ObjectSummary], NotUsed] =
    listFilesInBucket(awsConfig.quarantineBucketName)

  def listFilesInTransient: Source[Seq[S3ObjectSummary], NotUsed] =
    listFilesInBucket(awsConfig.transientBucketName)

  def copyFromQtoT(key: String, versionId: String): Try[CopyObjectResult]

  def getFileLengthFromQuarantine(key: String, versionId: String): Long

  def getBucketProperties(bucketName: String): JsValue

  def getQuarantineBucketProperties = getBucketProperties(awsConfig.quarantineBucketName)

  def getTransientBucketProperties = getBucketProperties(awsConfig.transientBucketName)
}

object S3Service {
  type StreamResult = Source[ByteString, Future[IOResult]]

  type UploadToQuarantine = (String, InputStream, Int) => Future[UploadResult]

  type DownloadFromBucket = (S3KeyName) => Option[StreamWithMetadata]
}

case class Metadata(
                     contentType: String,
                     contentLength: Long,
                     versionId: String = "",
                     ETag: String = "",
                     s3Metadata: Option[Map[String, String]] = None)

case class StreamWithMetadata(stream: StreamResult, metadata: Metadata)