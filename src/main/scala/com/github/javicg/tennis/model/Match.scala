package com.github.javicg.tennis.model

import akka.actor.ActorRef
import com.github.javicg.tennis.actor.TennisUmpire.PlayerScored

final case class Match(scores: Map[Player, Int],
                       players: Map[Player, ActorRef],
                       nextServe: Player) {

  def init(ref1: ActorRef, ref2: ActorRef): Match = {
    copy(
      scores = Map(Player1 -> 0, Player2 -> 0),
      players = Map(Player1 -> ref1, Player2 -> ref2)
    )
  }

  /*
   * Simplification of tennis rules:
   * - players play one after another (alternating)
   * - Points are just summed, no games/sets
   */
  def +(event: PlayerScored): Match = {
    copy(
      scores = scores + (event.player -> (scores(event.player) + 1)),
      nextServe = event.serving.opponent
    )
  }

  def isFinished: Boolean = scores.exists(p => p._2 == 10)

  def getRef(player: Player): ActorRef = players(player)
}

object Match {
  def apply(): Match = Match(
    scores = Map.empty,
    players = Map.empty,
    nextServe = Player1
  )
}