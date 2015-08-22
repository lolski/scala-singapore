package com.lolski

/**
 * Created by lolski on 8/22/15.
 */

import akka.actor.{ActorLogging, Actor, ActorRef, Props}
import java.nio.file.{Files, Path, Paths}
import scala.util.Try

/**
 * Created by ganeshwara on 24/6/15.
 */

object ChunkedUploadManager {
  case class NewProcess(id: String, sprayActor: ActorRef)
  case class UploadFinished(id: String, file: Try[Path], sprayActor: ActorRef)
}

class ChunkedUploadManager extends Actor with ActorLogging {
  var processes = Map[String, ActorRef]()

  val tmp = Paths.get("/tmp/chunked_uploads")
  Files.createDirectories(tmp)

  def receive = {
    case ChunkedUploadManager.NewProcess(id, sprayActor) =>
      spawn(id, sprayActor)
      
    case ChunkedUpload.UploadFinished(id, file, sprayActor) =>
      terminate(id)
      context.parent ! ChunkedUploadManager.UploadFinished(id, file, sprayActor)
  }

  private def spawn(id: String, sprayActor: ActorRef) = {
    val props = Props(classOf[ChunkedUpload], id, tmp, sprayActor, self)
    val ref = context.actorOf(props, id)
    processes += (id -> ref)
  }

  private def terminate(id: String) = {
    context.stop(processes(id))
    processes -= id
  }
}