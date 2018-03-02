package com.github.vladislavGutov.builder

import scala.concurrent.duration._
import akka.util.Timeout
import com.github.vladislavGutov.action.response.JmsResponseActionBuilder

case class JmsResponseBuilder(queue: String, timeout: Timeout = 30 seconds) {

  def build = new JmsResponseActionBuilder(queue, timeout)

  def timeout(duration: FiniteDuration) = copy(timeout = duration)

}

object JmsResponseBuilder {
  def queue(queue: String) = JmsResponseBuilder(queue)
}

trait JsmBuilderImplicits {
  def toActionBuilder(builder: JmsResponseBuilder): JmsResponseActionBuilder = builder.build
}