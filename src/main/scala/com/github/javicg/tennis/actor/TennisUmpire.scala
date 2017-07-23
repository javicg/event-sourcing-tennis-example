package com.github.javicg.tennis.actor

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.persistence.PersistentActor
import com.github.javicg.tennis.actor.PlayerActor._
import com.github.javicg.tennis.actor.TennisUmpire._
import com.github.javicg.tennis.model._

class TennisUmpire(id: String) extends PersistentActor with ActorLogging {
  override val persistenceId: String = id

  private var _match = Match()

  override def receiveRecover: Receive = {
    case event: MatchStarted =>
      update(event)

    case event: PlayerScored =>
      update(event)
  }

  override def receiveCommand: Receive = {
    case NewMatch(name1, name2) =>
      persist(MatchStarted(name1, name2)) { event =>
        log.info(s"New tennis match! $name1 vs $name2")
        update(event)
      }

    case GetMatchState =>
      sendMatchStateTo(sender())

    case PlayPoint =>
      serve(_match.nextServe, sender())
  }

  private def serve(player: Player, controller: ActorRef) = {
    log.debug(s"$player is serving")
    rally(player, player, controller)
  }

  private def rally(player: Player, serving: Player, controller: ActorRef) = {
    log.debug(s"$player is playing next")
    _match.getRef(player) ! Play
    context.become(waitingForPlayer(player, serving, controller))
  }

  private def waitingForPlayer(player: Player, serving: Player, controller: ActorRef): Receive = {
    case BallOverNet =>
      log.debug(s"Ball over net")
      rally(player.opponent, serving, controller)

    case BallLost =>
      log.debug(s"Ball lost!")
      persist(PlayerScored(player.opponent, serving)) { event =>
        log.info(s"Point for ${player.opponent}")
        update(event)
        sendMatchStateTo(controller)
        context.become(receiveCommand)
      }
  }

  private def sendMatchStateTo(ref: ActorRef) = {
    if (!_match.isStarted) {
      log.debug("Match has not started yet!")
      ref ! MatchNotStarted
    } else if (_match.isFinished) {
      log.info("Match finished!")
      log.info(s"Player 1 [${_match.scores(Player1)}] - Player 2[${_match.scores(Player2)}]")
      ref ! MatchFinished
    } else {
      log.debug(s"Match in progress. Player 1 [${_match.scores(Player1)}] - Player 2[${_match.scores(Player2)}]")
      ref ! MatchInProgress(_match.scores(Player1), _match.scores(Player2))
    }
  }

  private def update(event: MatchStarted) = {
    _match = _match.init(
      context.actorOf(Props(classOf[PlayerActor], event.name1), "player1"),
      context.actorOf(Props(classOf[PlayerActor], event.name2), "player2"))
  }

  private def update(event: PlayerScored) = {
    _match += event
  }

}

object TennisUmpire {
  // Protocol
  case class NewMatch(name1: String, name2: String)
  case object PlayPoint
  case object GetMatchState

  sealed trait MatchState
  case object MatchNotStarted extends MatchState
  case class MatchInProgress(score1: Int, score2: Int) extends MatchState
  case object MatchFinished extends MatchState

  // Events
  sealed trait Event
  final case class MatchStarted(name1: String, name2: String) extends Event
  final case class PlayerScored(player: Player, serving: Player) extends Event
}