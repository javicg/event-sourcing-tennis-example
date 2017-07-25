package com.github.javicg.tennis.actor

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import com.github.javicg.tennis.actor.PlayerActor.{BallLost, BallOverNet, Play}
import com.github.javicg.tennis.actor.TennisUmpire.{MatchInProgress, NewMatch, PlayPoint}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class TennisUmpireTest extends TestKit(ActorSystem("tennis-umpire-test"))
  with WordSpecLike with Matchers with BeforeAndAfterAll with ImplicitSender {

  private class Fixture(umpireId: String) {
    val player1 = TestProbe()
    val player2 = TestProbe()

    def newUmpire(): ActorRef = {
      system.actorOf(Props(new TennisUmpire(umpireId) with Players {
        override def newPlayers(name1: String, name2: String): (ActorRef, ActorRef) = {
          (player1.ref, player2.ref)
        }
      }))
    }
  }

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A TennisUmpire" should {
    "set the match in progress after the first point is played" in new Fixture("umpire-001") {
      val umpire = newUmpire()
      umpire ! NewMatch("Rafa Nadal", "Roger Federer")
      umpire ! PlayPoint

      player1.expectMsg(Play)
      player1.reply(BallLost)

      expectMsg(MatchInProgress(0, 1))
    }

    "alternate players after a point is played" in new Fixture("umpire-002") {
      val umpire = newUmpire()
      umpire ! NewMatch("Rafa Nadal", "Roger Federer")
      umpire ! PlayPoint

      player1.expectMsg(Play)
      player1.reply(BallLost)

      expectMsg(MatchInProgress(0, 1))
      umpire ! PlayPoint

      player2.expectMsg(Play)
    }

    "keep score and serving order after a restart" in new Fixture("umpire-003") {
      val umpire = newUmpire()
      umpire ! NewMatch("Rafa Nadal", "Roger Federer")
      umpire ! PlayPoint

      player1.expectMsg(Play)
      player1.reply(BallLost)
      expectMsg(MatchInProgress(0, 1))

      umpire ! PoisonPill

      val umpire2 = newUmpire()
      umpire2 ! PlayPoint

      player2.expectMsg(Play)
      player2.reply(BallLost)
      expectMsg(MatchInProgress(1, 1))
    }

    "maintain rally until a player loses the ball" in new Fixture("umpire-004") {
      val umpire = newUmpire()
      umpire ! NewMatch("Rafa Nadal", "Roger Federer")
      umpire ! PlayPoint

      player1.expectMsg(Play)
      player1.reply(BallOverNet)

      player2.expectMsg(Play)
      player2.reply(BallLost)
      expectMsg(MatchInProgress(1, 0))
    }
  }

}
