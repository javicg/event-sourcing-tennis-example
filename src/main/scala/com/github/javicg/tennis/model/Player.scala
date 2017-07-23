package com.github.javicg.tennis.model

sealed trait Player {
  def opponent: Player
}

case object Player1 extends Player {
  val opponent = Player2
}

case object Player2 extends Player {
  val opponent = Player1
}