package com.spaceape.hiring.exception

class GameHasFinishedException(var id: String)
  extends BaseException("Game(" + id + ") has already finished") {

  val GAME_HAS_FINISHED_ERROR = 102
  override def getCode: Int = { GAME_HAS_FINISHED_ERROR }

}
