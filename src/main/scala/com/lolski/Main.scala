package com.lolski

/**
 * Created by lolski on 8/22/15.
 */

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import spray.can.Http

class Main {
  val as = ActorSystem()
  val routeActor: ActorRef = as.actorOf(Props(classOf[Routes]), "routes")
  val attemptStart = IO(Http) ? Http.Bind(routeActor, interface = Settings.host, port = Settings.port)

  attemptStart onComplete { _ => println("started") }
}