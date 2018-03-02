package com.github.vladislavGutov.client.jms

import javax.jms.{Message, TextMessage}

import akka.actor.{Actor, ActorRef}
import org.json4s.{DefaultReaders, DefaultWriters}

object JmsManagerActor {
  type Id = String
  case class SubscribeForMessage(id: Id)
  case object Ack

  case class NewMessage(msg: Message)
}

class JmsManagerActor(messageParser: MessageParser) extends Actor {
  import JmsManagerActor._
  import scala.collection.mutable

  private val subscriptionMap = mutable.Map[Id, ActorRef]()
  private val msgsBuffer = mutable.Set[Id]()

  def receive: Receive = {
    case SubscribeForMessage(id) if isAlreadyReceived(id) =>
      msgsBuffer.remove(id)
      sender ! Ack

    case SubscribeForMessage(id) =>
      subscriptionMap.update(id, sender())

    case NewMessage(message) =>
      val id = messageParser.parse(message)

      if (isSomeOneWaiting(id)) {
        val waiter = subscriptionMap.remove(id).get
        waiter ! Ack
      } else {
        msgsBuffer += id
      }
  }

  private def isAlreadyReceived(id: JmsManagerActor.Id): Boolean =
    msgsBuffer.contains(id)

  private def isSomeOneWaiting(id: JmsManagerActor.Id): Boolean =
    subscriptionMap.contains(id)
}

trait MessageParser {
  import JmsManagerActor.Id
  def parse(msg: Message): Id
}

class JsonMessageParser extends MessageParser with DefaultReaders with DefaultWriters {
  import JmsManagerActor.Id
  import org.json4s._
  import org.json4s.jackson.JsonMethods.{parse => jsParse}

  def parse(message: Message): Id = {
    message match {
      case t: TextMessage => parseTextMessage(t)
    }
  }

  private def parseTextMessage(message: TextMessage): Id = {
    val msgObj = jsParse(StringInput(message.getText)).as[JObject]

    val id = (msgObj \ "id").as[String]

    id
  }
}