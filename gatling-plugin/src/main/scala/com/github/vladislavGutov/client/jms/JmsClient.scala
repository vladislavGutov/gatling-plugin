package com.github.vladislavGutov.client.jms

import javax.jms._

import akka.actor.ActorRef

class JmsClient(url: String, user: Option[String], password: Option[String]) {

  import JmsManagerActor._

  private val connection = {
    val connectionFactory = new FakeConnectionFactory
    val connection = user match {
      case Some(u) => connectionFactory.createConnection(u, password.getOrElse(""))
      case None => connectionFactory.createConnection()
    }
    connection.start()
    connection
  }

  def listen(queueName: String, listener: ActorRef): Unit = {
    val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
    val queue = session.createQueue(queueName)

    val consumer = session.createConsumer(queue)

    consumer.setMessageListener((message) => listener ! NewMessage(message))
  }

  def cleanQueue(queueName: String): Unit = {
    val session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)

    val queue = session.createQueue(queueName)

    val consumer = session.createConsumer(queue)

    def drainConsumer(consumer: MessageConsumer, numOfMsgs: Int): Int = {
      val msg = consumer.receive(100)
      if (msg != null) drainConsumer(consumer, numOfMsgs + 1)
      else numOfMsgs
    }

    val cleaned = drainConsumer(consumer, 0)

    println(s"received $cleaned messages")

    consumer.close()
    session.close()
  }

  def close() = connection.close()

}

class FakeConnectionFactory extends ConnectionFactory {
  override def createConnection(): Connection = ???
  override def createConnection(userName: String, password: String): Connection = ???
  override def createContext(): JMSContext = ???
  override def createContext(userName: String, password: String): JMSContext = ???
  override def createContext(userName: String, password: String, sessionMode: Int): JMSContext = ???
  override def createContext(sessionMode: Int): JMSContext = ???
}
