package com.lolski

/**
 * Created by lolski on 8/22/15.
 */

import akka.actor.{ActorLogging, Actor, ActorRef, Props}
import java.nio.file.{Files, Path, Paths}
import scala.concurrent.{Promise, Future}
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
  var promises = Map[String, Promise[Path]]()

  val tmp = Paths.get("/tmp/chunked_uploads")
  Files.createDirectories(tmp)

  def receive = {
    case ChunkedUploadManager.NewProcess(id, sprayActor) =>
      spawnActor(id, sprayActor)
      val p = spawnPromise(id)
      sender ! p.future

    case ChunkedUpload.UploadFinished(id, file, sprayActor) =>
      log.info("finishing upload")
      terminateActor(id)
      terminatePromise(id, file)
  }

  private def spawnActor(id: String, sprayActor: ActorRef): Unit = {
    val props = Props(classOf[ChunkedUpload], id, tmp, sprayActor, self)
    val ref = context.actorOf(props, id)
    processes += (id -> ref)
  }

  private def terminateActor(id: String): Unit = {
    context.stop(processes(id))
    processes -= id
  }

  private def spawnPromise(id: String): Promise[Path] = {
    val p = Promise[Path]()
    promises += (id -> p)
    p
  }

  private def terminatePromise(id: String, file: Try[Path]): Unit = {
    log.info("terminating promise")
    promises(id).complete(file)
  }
}