package com.lolski

/**
 * Created by lolski on 8/22/15.
 */
import akka.actor._
import java.io._
import java.nio.file.{Paths, Path}
import scala.util.{Success, Try}
import spray.can.Http.RegisterChunkHandler
import spray.http._

object ChunkedUpload {
  case class UploadFinished(id: String, file: Try[Path], sprayActor: ActorRef)
}

class ChunkedUpload(id: String, dir: Path, sprayActor: ActorRef, manager: ActorRef) extends Actor with ActorLogging {
  val tmp = Paths.get(s"${dir.toAbsolutePath.toString}/$id.tmp")
  val writer = new BufferedOutputStream(new FileOutputStream(tmp.toFile))

  sprayActor ! RegisterChunkHandler(self)

  def receive = {
    case c: MessageChunk => writer.write(c.data.toByteArray)

    case e: ChunkedMessageEnd =>
      manager ! ChunkedUpload.UploadFinished(id, Success(tmp), sprayActor)
      writer.close()
  }

  override def postStop(): Unit =
    log.info("stopping chunked upload instance " + id)
}