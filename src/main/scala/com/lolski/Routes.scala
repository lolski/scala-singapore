package com.lolski

import akka.actor.{Actor, ActorLogging}
import spray.can.Http
import spray.http._
import spray.http.HttpMethods.POST

/**
 * Created by lolski on 8/22/15.
 */

class Routes extends Actor with ActorLogging {
  implicit def actorRefFactory = context

  def receive = {
    case _: Http.Connected    => sender ! Http.Register(self)
    case Http.PeerClosed      =>
    case Http.Aborted         =>
    case Http.ConfirmedClosed =>

    // curl -X POST localhost:8080/upload --data @large.bin
    case msg @ HttpRequest(POST, uri @ Uri.Path("/upload"), _, entity: HttpEntity, _) =>
      entity.toOption map { nonempty =>
        val data = nonempty.data.toByteArray
        log.info("request body length: " + data.length)
      } getOrElse {
        log.info("request body must not be empty")
        sender ! HttpResponse(StatusCodes.BadRequest)
      }

    case _ =>
      log.warning("unknown routes")
  }
}