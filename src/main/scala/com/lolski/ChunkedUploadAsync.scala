package com.lolski

import akka.actor.{ActorContext, Actor, ActorRef, Props}
import akka.pattern.{ask}
import akka.util.Timeout
import scala.concurrent.{Future}
import scala.concurrent.duration._
import java.nio.file.Path

/**
 * Created by lolski on 8/22/15.
 */


class ChunkedUploadAsync(name: String, val tmp: String)(implicit val to: Timeout = Timeout(10.seconds), implicit val ctx: ActorContext) {
  import ctx.dispatcher
  val chunkedActor = ctx.actorOf(Props(classOf[ChunkedUploadManager], tmp), name)

  def upload(id: String, sprayActor: ActorRef): Future[Path] = {
    val async = (chunkedActor ? ChunkedUploadManager.NewProcess(id, sprayActor)).mapTo[Future[Path]]
    async flatMap( e => e ) // why doesn't this variant work: async flatMap(_)
  }
}
