package com.github.javicg.tennis.actor

import akka.actor.{Actor, ActorRef, Props}

trait Players {
  def newPlayers(name1: String, name2: String): (ActorRef, ActorRef)
}

trait RealPlayers extends Players { this: Actor =>
  override def newPlayers(name1: String, name2: String): (ActorRef, ActorRef) = {
    val player1 = context.actorOf(Props(classOf[PlayerActor], name1))
    val player2 = context.actorOf(Props(classOf[PlayerActor], name2))
    (player1, player2)
  }
}