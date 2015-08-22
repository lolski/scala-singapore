package com.lolski

import akka.actor.{Actor, ActorLogging}
import spray.can.Http
import spray.http.{ChunkedRequestStart, HttpRequest, HttpMethods, Uri}

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

    case msg @ HttpRequest(_, uri @ Uri.Path("/upload"), _, _, _) =>
      
    case _ =>
  }
}