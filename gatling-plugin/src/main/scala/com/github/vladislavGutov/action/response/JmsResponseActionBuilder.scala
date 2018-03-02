package com.github.vladislavGutov.action.response

import akka.util.Timeout
import com.github.vladislavGutov.action.ResponseActionBuilder
import com.github.vladislavGutov.client.Consumer
import com.github.vladislavGutov.client.jms.JmsConsumer
import com.github.vladislavGutov.protocol.{JmsComponents, JmsProtocol}
import io.gatling.core.structure.ScenarioContext

class JmsResponseActionBuilder(queue: String, timeout: Timeout) extends ResponseActionBuilder {
  override type PK = JmsProtocol.JmsProtocolKey.type
  override val key = JmsProtocol.JmsProtocolKey

  override def build(ctx: ScenarioContext, components: JmsComponents): Consumer = {
    val protocol = components.jmsProtocol

    new JmsConsumer(ctx.system, protocol.url, queue, protocol.user, protocol.password, timeout)
  }
}
