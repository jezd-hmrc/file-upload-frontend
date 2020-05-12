package uk.gov.hmrc.fileupload.support

import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterEach, Suite}
import uk.gov.hmrc.clamav.ClamAntiVirus
import uk.gov.hmrc.clamav.config.ClamAvConfig

trait ITTestAppComponentsWithStubbedClamAV extends IntegrationTestApplicationComponents with BeforeAndAfterEach with MockFactory{
  this: Suite =>

  protected val stubbedClamAVClient: ClamAntiVirus = stub[ClamAntiVirus]

  override lazy val disableAvScanning: Boolean = false
  override lazy val numberOfTimeoutAttempts: Int = 3
  override lazy val clamAntiVirusTestClient: ClamAvConfig => ClamAntiVirus = _ => stubbedClamAVClient
}
