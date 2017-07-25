package com.github.javicg.tennis

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.github.javicg.tennis.actor.TennisUmpire
import com.github.javicg.tennis.actor.TennisUmpire._

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.language.postfixOps
import scala.util.{Failure, Success}

object Main extends App {
  val system = ActorSystem("TennisMatch")
  implicit val dispatcher: ExecutionContext = system.dispatcher
  implicit val timeout: Timeout = 5 seconds

  val umpire = system.actorOf(Props(classOf[TennisUmpire], "tennis-umpire"))

  umpire ? GetMatchState foreach {
    case MatchNotStarted =>
      umpire ! NewMatch("Rafa Nadal", "Roger Federer")
      playNextPoint()
    case m: MatchInProgress =>
      playNextPoint()
    case MatchFinished =>
      system.terminate()
  }

  Await.ready(system.whenTerminated, 10 seconds)

  private def playNextPoint(): Unit = {
    umpire ? PlayPoint onComplete {
      case Success(state) => state match {
        case _: MatchInProgress => playNextPoint()
        case MatchFinished => system.terminate()
      }
      case Failure(ex) => ex.printStackTrace()
    }
  }
}
