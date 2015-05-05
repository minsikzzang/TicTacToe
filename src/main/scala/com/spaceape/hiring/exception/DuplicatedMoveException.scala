package com.spaceape.hiring.exception

import com.spaceape.hiring.model.Move

class DuplicatedMoveException(var move: Move)
  extends BaseException("Position(" + move.x + ", " + move.y + ") has already taken") {

  val DUPLICATE_MOVE_ERROR = 103
  override def getCode: Int = { DUPLICATE_MOVE_ERROR }

}
