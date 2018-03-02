package com.github.vladislavGutov.client

import com.github.vladislavGutov.Message

import scala.concurrent.Future


trait Publisher[V <: Message] {
  def name: String
  def send(value: V): Future[Unit]
  def close(): Unit
}
