package com.github.vladislavGutov.action

import com.github.vladislavGutov.Message
import com.github.vladislavGutov.client.{Consumer, Publisher}
import com.typesafe.scalalogging.LazyLogging
import io.gatling.commons.stats.{KO, OK}
import io.gatling.commons.util.ClockSingleton.nowMillis
import io.gatling.commons.validation.Validation
import io.gatling.core.CoreComponents
import io.gatling.core.Predef.Session
import io.gatling.core.action.builder.ActionBuilder
import io.gatling.core.action.{Action, ExitableAction}
import io.gatling.core.session.Expression
import io.gatling.core.stats.message.ResponseTimings
import io.gatling.core.structure.ScenarioContext
import io.gatling.core.util.NameGen

import scala.util.{Failure, Success, Try}
import scala.util.control.NonFatal

case class Attributes[V](
  requestName: Expression[String],
  payload: Expression[V]
)

class GenericActionBuilder[V <: Message](
  attributes: Attributes[V],
  requestActionBuilder: RequestActionBuilder[V],
  responseActionBuilder: ResponseActionBuilder
) extends ActionBuilder with LazyLogging {

  override def build(ctx: ScenarioContext, next: Action): Action = {
    import ctx._

    val requestProtocol = protocolComponentsRegistry.components(requestActionBuilder.key)
    val responseProtocol = protocolComponentsRegistry.components(responseActionBuilder.key)

    val publisher = requestActionBuilder.build(ctx, requestProtocol)
    logger.debug(s"Publisher ${publisher.name} has been instantiated")

    val consumer = responseActionBuilder.build(ctx, responseProtocol)
    logger.debug(s"Consumer ${consumer.name} has been instantiated")

    system.registerOnTermination {
      safe(publisher.close())
      safe(consumer.close())
    }

    new GenericAction[V](
      coreComponents,
      attributes,
      publisher,
      consumer,
      throttled,
      next
    )
  }

  private def safe(f: => Unit): Unit = {
    var r: Unit = () //to prevent JIT from skipping execution of f
    try {
      r = f
    } catch {case NonFatal(e) => e.printStackTrace()}
  }

}

class GenericAction[V <: Message](
  val coreComponents: CoreComponents,
  val attributes: Attributes[V],
  val publisher: Publisher[V],
  val consumer: Consumer,
  val throttled: Boolean,
  val next: Action
) extends ExitableAction with NameGen {

  val statsEngine = coreComponents.statsEngine

  def name: String = genName(s"${publisher.name}->${consumer.name}")

  def execute(session: Session): Unit = {
    attributes.requestName(session).flatMap {
      requestName =>
        logger.trace("Executing request {}", requestName)
        val outcome = sendRequest(session, requestName)

        outcome.onFailure { errorMessage =>
          statsEngine.reportUnbuildableRequest(session, requestName, errorMessage)
        }

        outcome
    }
  }

  private def sendRequest(session: Session, requestName: String): Validation[Unit] = {
    val value = attributes.payload(session)

    value.map {value =>
      logger.trace("Sending record id={}", value.id)
      val requestStartDate = nowMillis
      publisher.send(value)
        .onComplete(awaitResult(value, session, requestName, requestStartDate))
    }
  }

  private def awaitResult(value: V, session: Session, requestName: String, startDate: Long): PartialFunction[Try[Unit], Unit] = {
    case f: Failure[Unit] => logResponse(session, requestName, startDate)(f)
    case Success(_) =>
      consumer.await(value)
        .andThen(logResponse(session, requestName, startDate))
        .onComplete { _ =>
          if (throttled) {
            coreComponents.throttler.throttle(session.scenario, () => next ! session)
          } else {
            next ! session
          }
        }
  }

  private def logResponse(session: Session, requestName: String, startDate: Long): PartialFunction[Try[Unit], Unit] = {
    case result: Try[Unit] =>
      val responseEndDate = nowMillis
      statsEngine.logResponse(
        session,
        requestName,
        ResponseTimings(startDate, responseEndDate),
        if (result.isSuccess) OK else KO,
        None,
        if (result.isSuccess) None else Some(result.failed.get.getMessage)
      )
  }

}
