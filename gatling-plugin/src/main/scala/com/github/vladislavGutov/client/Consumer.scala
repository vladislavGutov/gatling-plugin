package com.github.vladislavGutov.client

import com.github.vladislavGutov.Message

import scala.concurrent.Future

trait Consumer {
  def name: String
  def await(msg: Message): Future[Unit]
  def close(): Unit
}
