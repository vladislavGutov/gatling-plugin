package com.github.vladislavGutov.protocol

import akka.actor.ActorSystem
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.{CoreComponents, protocol}
import io.gatling.core.protocol.{Protocol, ProtocolComponents, ProtocolKey}
import io.gatling.core.session.Session

object KafkaProtocolBuilder {

  implicit def toProtocol(builder: KafkaProtocolBuilder): KafkaProtocol = builder.build

  def apply(configuration: GatlingConfiguration): KafkaProtocolBuilder =
    KafkaProtocolBuilder(KafkaProtocol(configuration))
}

case class KafkaProtocolBuilder(kafkaProtocol: KafkaProtocol) {
  def build = kafkaProtocol
}

object KafkaProtocol {
  def apply(configuration: GatlingConfiguration): KafkaProtocol =
    KafkaProtocol(Map[String, AnyRef]())

  val KafkaProtocolKey = new ProtocolKey {
    type Protocol = KafkaProtocol
    type Components = KafkaComponents

    override def protocolClass: Class[protocol.Protocol] = classOf[KafkaProtocol].asInstanceOf[Class[protocol.Protocol]]

    override def defaultProtocolValue(configuration: GatlingConfiguration): KafkaProtocol = KafkaProtocol(configuration)

    override def newComponents(system: ActorSystem, coreComponents: CoreComponents): KafkaProtocol => KafkaComponents =
      KafkaComponents.apply
  }
}

case class KafkaProtocol(properties: Map[String, AnyRef]) extends Protocol {
  def properties(prop: Map[String, AnyRef]): KafkaProtocol = copy(properties = prop)
}

case class KafkaComponents(protocol: KafkaProtocol) extends ProtocolComponents {
  override def onStart: Option[Session => Session] = None
  override def onExit: Option[Session => Unit] = None
}
