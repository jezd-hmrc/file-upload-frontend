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

import sbt._

object FrontendBuild extends Build with MicroService {

  val appName = "file-upload-frontend"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {

  import play.core.PlayVersion

  private val frontendBootstrapVersion = "12.4.0"
  private val playPartialsVersion = "6.5.0"
  private val authClient = "2.17.0-play-25"
  private val playConfigVersion = "7.3.0"
  private val hmrcTestVersion = "3.3.0"
  private val clamAvClientVersion = "6.9.0"
  private val catsVersion = "0.6.0"
  private val awsJavaSdkVersion = "1.11.97"

  val compile = Seq(
    "uk.gov.hmrc" %% "frontend-bootstrap" % frontendBootstrapVersion,
    "uk.gov.hmrc" %% "play-partials" % playPartialsVersion,
    "uk.gov.hmrc" %% "auth-client" % authClient,
    "uk.gov.hmrc" %% "clamav-client" % clamAvClientVersion,
    "org.typelevel" %% "cats" % catsVersion,
    "com.amazonaws" % "aws-java-sdk" % awsJavaSdkVersion,
    "com.lightbend.akka" %% "akka-stream-alpakka-file" % "2.0.1",
    // ensure all akka versions are the same
    "com.typesafe.akka" %% "akka-slf4j" % "2.5.31"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = null
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % "3.0.5" % scope,
        "org.scalamock" %% "scalamock-scalatest-support" % "3.5.0" % scope,
        "com.github.tomakehurst" % "wiremock" % "1.58" % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "org.jsoup" % "jsoup" % "1.11.3" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "com.typesafe.akka" %% "akka-testkit" % "2.3.2" % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {
      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % "3.0.1" % scope,
        "org.scalamock" %% "scalamock-scalatest-support" % "3.6.0" % scope,
        "com.github.tomakehurst" % "wiremock" % "1.58" % scope,
        "org.pegdown" % "pegdown" % "1.6.0" % scope,
        "org.jsoup" % "jsoup" % "1.8.3" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "com.typesafe.akka" %% "akka-testkit" % "2.3.2" % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.1" % scope,
        "io.findify" %% "s3mock" % "0.2.5" % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}
