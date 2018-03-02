package com.github.vladislavGutov.client.jms

import akka.actor.{ActorSystem, Kill, Props}
import akka.util.Timeout
import com.github.vladislavGutov.Message
import com.github.vladislavGutov.client.Consumer
import akka.pattern.ask

import scala.concurrent.Future

class JmsConsumer(
  actorSystem: ActorSystem,
  url: String,
  queue: String,
  user: Option[String],
  password: Option[String],
  awaitTimeout: Timeout) extends Consumer {

  import JmsManagerActor._

  private val managerActor = actorSystem.actorOf(Props(classOf[JmsManagerActor], new JsonMessageParser))

  private val jmsClient = {
    val client = new JmsClient(url, user, password)
    client.listen(queue, managerActor)
    client
  }

  implicit val timeout: Timeout = awaitTimeout

  import actorSystem.dispatcher

  override def name: String = "jsm"

  override def await(msg: Message): Future[Unit] = {
    managerActor ? SubscribeForMessage(msg.id) map (_ => ())
  }

  override def close(): Unit = {
    jmsClient.close()
    managerActor ! Kill
  }
}
