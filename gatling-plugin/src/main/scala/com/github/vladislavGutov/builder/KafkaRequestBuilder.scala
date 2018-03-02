package com.github.vladislavGutov.builder

import com.github.vladislavGutov.Message
import com.github.vladislavGutov.action.request.KafkaRequestActionBuilder

object KafkaRequestBuilder {
  def apply[V <: Message](): KafkaRequestBuilder[Nothing, V] = KafkaRequestBuilder("", _ => None)
}

case class KafkaRequestBuilder[K, V <: Message](topic: String, keyExtractor: V => Option[K]) {

  def build = new KafkaRequestActionBuilder[K, V](topic, keyExtractor)

  def withKeyExtractor[KK](f: V => Option[KK]): KafkaRequestBuilder[KK, V] = KafkaRequestBuilder(topic, f)

  def topic(topic: String): KafkaRequestBuilder[K, V] = copy(topic = topic)
}

trait KafkaBuilderImplicits {
  implicit def toRequestActionBuilder[K, V](builder: KafkaRequestBuilder[K, V]): KafkaRequestActionBuilder[K, V] = builder.build
}