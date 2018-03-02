package com.github.vladislavGutov

import com.github.vladislavGutov.builder._
import com.github.vladislavGutov.protocol.{JmsProtocolBuilder, KafkaProtocolBuilder}
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.session.Expression

object Predef extends KafkaBuilderImplicits with JsmBuilderImplicits {
  type KafkaProtocol = protocol.KafkaProtocol
  type JmsProtocol = protocol.JmsProtocol

  def kafkaProtocol(implicit configuration: GatlingConfiguration) = KafkaProtocolBuilder(configuration)
  def jmsProtocol(implicit configuration: GatlingConfiguration) = JmsProtocolBuilder(configuration)

  def kafka[V <: Message] = KafkaRequestBuilder[V]()
  def jms = JmsResponseBuilder
  def generic(requestName: Expression[String]) = new GenericBuilder(requestName)
}
