package com.lolski

import akka.actor.{Props, Actor, ActorLogging}
import akka.pattern.{pipe}
import java.util.UUID
import spray.can.Http
import spray.http._
import spray.http.HttpMethods.POST

/**
 * Created by lolski on 8/22/15.
 */

class Routes extends Actor with ActorLogging {
  import context.dispatcher

  val asyncUpload = new ChunkedUploadAsync()

  def receive = {
    case _: Http.Connected    => sender ! Http.Register(self)
    case Http.PeerClosed      =>
    case Http.Aborted         =>
    case Http.ConfirmedClosed =>

    // curl -X POST localhost:8080/upload --data-binary @large.bin --limit-rate 1m
    case msg @ HttpRequest(POST, uri @ Uri.Path("/upload"), _, entity: HttpEntity, _) =>
      entity.toOption map { nonempty =>
        val data = nonempty.data.toByteArray
        log.info("data received")
        sender ! HttpResponse(StatusCodes.OK, "data received")
      } getOrElse {
        log.info("request body must not be empty")
        sender ! HttpResponse(StatusCodes.BadRequest, "request body must not be empty")
      }

    // chunked messages
    case msg @ ChunkedRequestStart(HttpRequest(POST, uri @ Uri.Path("/upload"), _, _, _)) =>
      log.info("starting chunked msg requests")
      val id = UUID.randomUUID().toString
      val upload = asyncUpload.upload(id, sender)

      upload map { path =>
        log.info("data received by chunking: " + path)
        HttpResponse(StatusCodes.OK, "data received by chunking: " + path)
      } pipeTo sender

    case x =>
      log.warning("unknown message: " + x)
  }
}