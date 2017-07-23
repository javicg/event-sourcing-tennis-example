package com.github.javicg.tennis.actor

import akka.actor.{Actor, ActorLogging}
import com.github.javicg.tennis.actor.PlayerActor._

import scala.util.Random

class PlayerActor(name: String) extends Actor with ActorLogging {
  override def receive: Receive = {
    case Play =>
      waitForReception()
      play()
  }

  private def waitForReception() = {
    log.debug(s"$name is waiting for the ball...")
  }

  private def play() = {
    log.debug(s"$name hits the ball!")
    val r = Random.nextInt(10)
    if (r < 5) {
      sender() ! BallOverNet
    } else {
      sender() ! BallLost
    }
  }
}

object PlayerActor {
  // Protocol
  case object Play
  case object BallOverNet
  case object BallLost
}
