package com.github.vladislavGutov.action

import com.github.vladislavGutov.client.Consumer
import io.gatling.core.protocol.ProtocolKey
import io.gatling.core.structure.ScenarioContext

trait ResponseActionBuilder {
  type PK <: ProtocolKey
  val key: PK

  def build(ctx: ScenarioContext, components: PK#Components): Consumer
}
