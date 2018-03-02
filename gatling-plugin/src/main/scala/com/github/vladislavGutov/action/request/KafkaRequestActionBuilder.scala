package com.github.vladislavGutov.action.request

import com.github.vladislavGutov.Message
import com.github.vladislavGutov.action.RequestActionBuilder
import com.github.vladislavGutov.client.Publisher
import com.github.vladislavGutov.client.kafka.KafkaPublisher
import com.github.vladislavGutov.protocol.{KafkaComponents, KafkaProtocol}
import io.gatling.core.structure.ScenarioContext
import org.apache.kafka.clients.producer.KafkaProducer

import scala.collection.JavaConverters._

class KafkaRequestActionBuilder[K, V <: Message](topic: String, keyExtractor: V => Option[K]) extends RequestActionBuilder[V] {
  override type PK = KafkaProtocol.KafkaProtocolKey.type
  override def key = KafkaProtocol.KafkaProtocolKey

  override def build(ctx: ScenarioContext, components: KafkaComponents): Publisher[V] = {
    val producer = new KafkaProducer[K, V](components.protocol.properties.asJava)
    new KafkaPublisher[K, V](producer, topic)(keyExtractor)
  }
}
