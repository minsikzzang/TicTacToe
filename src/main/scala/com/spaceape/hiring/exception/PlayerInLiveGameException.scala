package com.spaceape.hiring.exception

class PlayerInLiveGameException(player1Id: String, player2Id: String)
  extends Exception("Player(" + player1Id + " or " + player2Id + ") is now in live game") {

  val PLAYER_IN_LIVE_GAME_ERROR = 100
  def getCode: Int = { PLAYER_IN_LIVE_GAME_ERROR }
}
