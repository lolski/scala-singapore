package com.lolski

/**
 * Created by lolski on 8/22/15.
 */

import akka.util.Timeout
import scala.concurrent.duration._

object Settings {
  val port = 8080
  val host = "localhost"
  implicit val defaultTimeout = Timeout(15.seconds)
}