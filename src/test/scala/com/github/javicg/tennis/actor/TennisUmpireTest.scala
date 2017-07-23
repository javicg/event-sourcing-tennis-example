package com.github.javicg.tennis.actor

import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.github.javicg.tennis.actor.TennisUmpire.{MatchInProgress, NewMatch, PlayPoint}
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

class TennisUmpireTest extends TestKit(ActorSystem("tennis-umpire-test"))
    with WordSpecLike with Matchers with BeforeAndAfterAll with ImplicitSender {

  override def afterAll: Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "A TennisUmpire" should {
    "set match in progress after the first point is played" in {
      val umpire = newUmpire("umpire-001")
      umpire ! NewMatch("Rafa Nadal", "Roger Federer")
      umpire ! PlayPoint

      val state = expectMsgType[MatchInProgress]
      assert(state.score1 + state.score2 == 1)
    }

    "keep score after a restart" in {
      val umpire = newUmpire("umpire-002")
      umpire ! NewMatch("Rafa Nadal", "Roger Federer")
      umpire ! PlayPoint
      expectMsgType[MatchInProgress]

      umpire ! PoisonPill

      val umpire2 = newUmpire("umpire-002")
      umpire2 ! PlayPoint
      val state = expectMsgType[MatchInProgress]
      assert(state.score1 + state.score2 == 2)
    }
  }

  private def newUmpire(id: String) = {
    system.actorOf(Props(classOf[TennisUmpire], id))
  }

}
