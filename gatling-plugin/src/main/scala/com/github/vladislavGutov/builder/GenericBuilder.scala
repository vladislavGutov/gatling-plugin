package com.github.vladislavGutov.builder

import com.github.vladislavGutov.Message
import com.github.vladislavGutov.action.{Attributes, GenericActionBuilder, RequestActionBuilder, ResponseActionBuilder}
import io.gatling.core.session.Expression

class GenericBuilder(requestName: Expression[String]) {
  def send[V <: Message](value: Expression[V]) = new ViaStep(Attributes(requestName, value))

  class ViaStep[V <: Message](attributes: Attributes[V]) {
    def via(requestActionBuilder: RequestActionBuilder[V]) = new ReceiveStep(attributes, requestActionBuilder)
  }

  class ReceiveStep[V <: Message](attributes: Attributes[V], request: RequestActionBuilder[V]) {
    def receive(responseActionBuilder: ResponseActionBuilder): GenericActionBuilder[V] =
      new GenericActionBuilder[V](attributes, request, responseActionBuilder)
  }
}
