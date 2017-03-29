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

package uk.gov.hmrc.fileupload.utils

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.ByteString
import play.api.libs.iteratee.Iteratee
import play.api.libs.streams.{Accumulator, Streams}
import uk.gov.hmrc.fileupload.fileupload.ByteStream

object StreamsConverter {
  def iterateeToAccumulator[T](iteratee: Iteratee[ByteStream, T]): Accumulator[ByteString, T] = {
    val sink = Streams.iterateeToAccumulator(iteratee).toSink
    Accumulator(sink.contramap[ByteString](_.toArray[Byte]))
  }
}

object StreamImplicits {
  implicit val system = ActorSystem()
  implicit val materializer: Materializer = ActorMaterializer()
}