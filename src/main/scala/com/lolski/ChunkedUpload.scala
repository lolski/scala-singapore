package com.lolski

/**
 * Created by lolski on 8/22/15.
 */
import akka.actor._
import java.io._
import java.nio.file.{Paths, Path}
import spray.can.Http
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import spray.can.Http.RegisterChunkHandler
import spray.http._

object ChunkedUpload {
  case class UploadFinished(id: String, file: Try[Path], sprayActor: ActorRef)
}

class ChunkedUpload(id: String, dir: Path, sprayActor: ActorRef, manager: ActorRef) extends Actor with ActorLogging {
  val tmp = Paths.get(s"${dir.toAbsolutePath.toString}/$id.tmp")
  val writer = new BufferedOutputStream(new FileOutputStream(tmp.toFile))

  sprayActor ! RegisterChunkHandler(self)
  context.setReceiveTimeout(10 seconds) // this timeout

  def receive = {
    case c: MessageChunk => writer.write(c.data.toByteArray)

    case e: ChunkedMessageEnd =>
      manager ! ChunkedUpload.UploadFinished(id, Try(tmp), sprayActor)
      cleanup()

    case e: Http.ConnectionClosed =>
      manager ! ChunkedUpload.UploadFinished(id, Try(throw new Exception("chunked upload: connection closed prematurely")), sprayActor)
      cleanup()

    case akka.actor.ReceiveTimeout =>
      manager ! ChunkedUpload.UploadFinished(id, Try(throw new Exception("chunked upload: timeout")), sprayActor)
      cleanup()
  }

  def cleanup() = {
    context.setReceiveTimeout(Duration.Undefined) // turn off actor timeout
    writer.close()
  }

  override def postStop(): Unit =
    log.info("stopping chunked upload instance " + id)
}