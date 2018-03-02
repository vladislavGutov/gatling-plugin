package com.github.vladislavGutov.action

import com.github.vladislavGutov.Message
import com.github.vladislavGutov.client.Publisher
import io.gatling.core.protocol.ProtocolKey
import io.gatling.core.structure.ScenarioContext

trait RequestActionBuilder[V <: Message] {
  type PK <: ProtocolKey
  def key: PK

  def build(ctx: ScenarioContext, components: PK#Components): Publisher[V]
}
