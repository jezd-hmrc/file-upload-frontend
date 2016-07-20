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

package uk.gov.hmrc.fileupload_deprecated.connectors

import play.api.Play
import play.api.libs.iteratee.{Enumerator, Iteratee}
import uk.gov.hmrc.clamav.{ClamAntiVirus, VirusChecker}
import uk.gov.hmrc.clamav.config.ClamAvConfig
import uk.gov.hmrc.play.config.{RunMode, ServicesConfig}

import scala.concurrent.Future
import scala.util.Try

class ClamAvScannerConnector(virusChecker: VirusChecker) extends AvScannerConnector with RunMode {

  import scala.concurrent.ExecutionContext.Implicits.global

  def scan(enumerator: Enumerator[Array[Byte]]): Future[Try[Boolean]] = {
    for {
      scanner <- (enumerator |>>> iteratee) flatMap identity
      result <- scanner.checkForVirus()
    } yield result
  }

  private def iteratee = Iteratee.fold(Future(virusChecker)) { (state, bytes: Array[Byte]) =>
    state flatMap { scanner => scanner.send(bytes) map { _ => scanner } }
  }
}

object ClamAvScannerConnector extends ServicesConfig {

  implicit lazy val clamAvConfig = ClamAvConfig(Play.current.configuration.getConfig(s"$env.clam.antivirus"))

  def virusChecker = ClamAntiVirus(clamAvConfig)
}

trait AvScannerConnector {

  def scan(enumerator: Enumerator[Array[Byte]]): Future[Try[Boolean]]
}