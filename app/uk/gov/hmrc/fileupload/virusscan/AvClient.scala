/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.fileupload.virusscan

import java.io.InputStream

import play.api.{Configuration, Environment}
import uk.gov.hmrc.clamav.{ClamAntiVirus, ClamAntiVirusFactory}
import uk.gov.hmrc.clamav.config.ClamAvConfig
import uk.gov.hmrc.clamav.model.ScanningResult

import scala.concurrent.{ExecutionContext, Future}

// ClamAntiVirus for some reason has a private constructor and no interface...
trait AvClient {
    def sendAndCheck(inputStream: InputStream, length: Int)(implicit ec: ExecutionContext): Future[ScanningResult]
}

object AvClient {
  def apply(configuration: Configuration, environment: Environment)(implicit ec: ExecutionContext): AvClient =
    new AvClient {
      val clamAvConfig =
        new ClamAvConfig {
          def getString(key: String) =
            configuration.getString(key).getOrElse(sys.error(s"No config for key `$key` defined"))

          def getInt(key: String) =
            configuration.getInt(key).getOrElse(sys.error(s"No config for key `$key` defined"))


          override val host   : String = getString(s"${environment.mode}.clam.antivirus.host")
          override val port   : Int    = getInt(s"${environment.mode}.clam.antivirus.port")
          override val timeout: Int    = getInt(s"${environment.mode}.clam.antivirus.timeout")
        }

      val clamAntiVirus: ClamAntiVirus = new ClamAntiVirusFactory(clamAvConfig).getClient

      override def sendAndCheck(inputStream: InputStream, length: Int)(implicit ec: ExecutionContext): Future[ScanningResult] =
        clamAntiVirus.sendAndCheck(inputStream, length)
    }
}
