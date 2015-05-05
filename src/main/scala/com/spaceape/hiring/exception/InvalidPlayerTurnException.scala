package com.spaceape.hiring.exception

class InvalidPlayerTurnException(playerId: String)
  extends BaseException("It's not Player(" + playerId + ")'s turn yet") {

  val INVALID_PLAYER_TURN_ERROR = 101
  override def getCode: Int = { INVALID_PLAYER_TURN_ERROR }

}
