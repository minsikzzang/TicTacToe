package com.spaceape.hiring.exception

class GameHasFinishedException(var id: String)
  extends Exception("Game(" + id + ") has already finished") {

  val GAME_HAS_FINISHED_ERROR = 102
  def getCode: Int = { GAME_HAS_FINISHED_ERROR }

}
