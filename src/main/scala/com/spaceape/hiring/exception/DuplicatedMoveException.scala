package com.spaceape.hiring.exception

import com.spaceape.hiring.model.Move

class DuplicatedMoveException(var move: Move)
  extends Exception("Position(" + move.x + ", " + move.y + ") has already taken") {

  val DUPLICATE_MOVE_ERROR = 103
  def getCode: Int = { DUPLICATE_MOVE_ERROR }
}
