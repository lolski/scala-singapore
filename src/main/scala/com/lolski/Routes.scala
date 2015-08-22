package com.lolski

import java.io.{OutputStream, BufferedOutputStream}
import java.nio.file.{Path, Files, Paths}

import akka.actor.{Actor, ActorLogging}
import spray.can.Http
import spray.can.Http.RegisterChunkHandler
import spray.http._
import spray.http.HttpMethods.POST

/**
 * Created by lolski on 8/22/15.
 */

class Routes extends Actor with ActorLogging {
  implicit def actorRefFactory = context

  var tmp: Option[Path] = None
  var writer: Option[OutputStream] = None

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
      sender ! RegisterChunkHandler(self)
      tmp = Some(Paths.get("/tmp/upload.mkv"))
      writer = Some(new BufferedOutputStream(Files.newOutputStream(tmp.get)))
      log.info("starting chunked msg requests")

    case chunk: MessageChunk =>
      writer.get.write(chunk.data.toByteArray)

    case msg @ ChunkedMessageEnd =>
      log.info("chunked msg requests finished!")
      sender ! HttpResponse(StatusCodes.OK, "data received (by chunking)")
      writer.get.close()

    case x =>
      log.warning("unknown message: " + x)
  }
}