package com.lolski

/**
 * Created by lolski on 8/22/15.
 */
import akka.actor._
import java.io._
import java.nio.file.{Paths, Files, Path}
import scala.concurrent.duration._
import scala.util.{Failure, Success, Try}
import spray.can.Http
import spray.can.Http.RegisterChunkHandler
import spray.http._

object ChunkedUpload {
  case class UploadFinished(id: String, path: Try[Path], sprayActor: ActorRef)
  case object CleanUp
}

class ChunkedUpload(id: String, dir: Path, sprayActor: ActorRef, manager: ActorRef) extends Actor with ActorLogging {
  val tmp = Paths.get(s"${dir.toAbsolutePath.toString}/$id.tmp")
  val writer = new BufferedOutputStream(new FileOutputStream(tmp.toFile))

  sprayActor ! RegisterChunkHandler(self)
//  context.setReceiveTimeout(10 seconds) // this timeout

  def receive = {
    case c: MessageChunk => writer.write(c.data.toByteArray)

    case e: ChunkedMessageEnd =>
      context.setReceiveTimeout(Duration.Undefined) // turn off actor timeout
      manager ! ChunkedUpload.UploadFinished(id, Success(tmp), sprayActor)
      writer.close()

//    case e: Http.ConnectionClosed =>
//      log.info("chunked metadata upload: connection closed prematurely")
//      self ! ChunkedUpload.CleanUp
//      manager ! ChunkedUpload.UploadFinished(id, Failure(throw new Exception("chunked metadata upload: connection closed prematurely")), sprayActor)
//
//    case akka.actor.ReceiveTimeout =>
//      log.info("chunked metadata upload: timeout")
//      self ! ChunkedUpload.CleanUp
//      manager ! ChunkedUpload.UploadFinished(id, Failure(throw new Exception("chunked metadata upload: timeout")), sprayActor)

//    case ChunkedUpload.CleanUp =>
//      //      deleteTmpFile(raw)
//      log.info("chunked metadata upload: cleanup performed.")
//      writer.close()
//      context.stop(self)
  }

//  override def postStop(): Unit = log.info("finished.")
}