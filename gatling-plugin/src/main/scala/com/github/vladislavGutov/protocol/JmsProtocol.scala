package com.github.vladislavGutov.protocol

import akka.actor.ActorSystem
import io.gatling.core.config.GatlingConfiguration
import io.gatling.core.{CoreComponents, protocol}
import io.gatling.core.protocol.{Protocol, ProtocolComponents, ProtocolKey}
import io.gatling.core.session.Session

object JmsProtocolBuilder {
  implicit def toProtocol(builder: JmsProtocolBuilder): JmsProtocol = builder.build

  def apply(configuration: GatlingConfiguration): JmsProtocolBuilder =
    JmsProtocolBuilder(JmsProtocol(configuration))
}

case class JmsProtocolBuilder(jmsProtocol: JmsProtocol) {
  def build: JmsProtocol = jmsProtocol
}

object JmsProtocol {
  def apply(configuration: GatlingConfiguration): JmsProtocol = JmsProtocol(
    "",
    None,
    None
  )

  val JmsProtocolKey = new ProtocolKey {
    type Protocol = JmsProtocol
    type Components = JmsComponents

    override def protocolClass: Class[protocol.Protocol] = classOf[JmsProtocol].asInstanceOf[Class[protocol.Protocol]]

    override def defaultProtocolValue(configuration: GatlingConfiguration): JmsProtocol = JmsProtocol(configuration)

    override def newComponents(system: ActorSystem, coreComponents: CoreComponents): JmsProtocol => JmsComponents = JmsComponents.apply
  }
}

case class JmsProtocol(
  url: String,
  user: Option[String],
  password: Option[String]) extends Protocol {

  def url(url: String) = copy(url = url)

  def user(user: Option[String]) = copy(user = user)

  def password(password: Option[String]) = copy(password = password)
}

case class JmsComponents(jmsProtocol: JmsProtocol) extends ProtocolComponents {
  override def onStart: Option[Session => Session] = None

  override def onExit: Option[Session => Unit] = None
}
