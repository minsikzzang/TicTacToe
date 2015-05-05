package com.spaceape.hiring.exception

class UnfinishedGameException(player1Id: String, player2Id: String)
  extends BaseException("Player(" + player1Id + " or " + player2Id + ") has unfinished games") {

  val UNFINISHED_GAME_ERROR = 100
  override def getCode: Int = { UNFINISHED_GAME_ERROR }

}
