package com.spaceape.hiring

class PlayerInLiveGameException(player1Id: String, player2Id: String)
  extends Exception("Player(" + player1Id + " or " + player2Id + ") is now in live game", null) {

  def getCode: Int = { 100 }
}
