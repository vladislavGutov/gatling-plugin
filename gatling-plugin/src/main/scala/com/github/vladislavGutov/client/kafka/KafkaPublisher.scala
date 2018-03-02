package com.github.vladislavGutov.client.kafka

import com.github.vladislavGutov.Message
import com.github.vladislavGutov.client.Publisher
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}

import scala.concurrent.{Future, Promise}

class KafkaPublisher[K, V <: Message](
  kafkaProducer: KafkaProducer[K, V],
  topic: String)(keyExtractor: V => Option[K]) extends Publisher[V] {

  override def name: String = "kafka"

  override def send(value: V): Future[Unit] = {
    val promise = Promise[Unit]()

    kafkaProducer.send(buildRecord(value), new Callback {
      override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
        if (exception != null) promise.failure(exception)
        else promise.success(())
      }
    })

    promise.future
  }

  def buildRecord(value: V): ProducerRecord[K, V] = {
    val optKey = keyExtractor(value)

    optKey match {
      case Some(key) => new ProducerRecord(topic, key, value)
      case None => new ProducerRecord(topic, value)
    }
  }

  override def close(): Unit = kafkaProducer.close()
}
