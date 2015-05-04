package com.spaceape.hiring.model

import com.fasterxml.jackson.annotation.JsonProperty
import scala.beans.BeanProperty

class GameState (@BeanProperty @JsonProperty("winnerId") var winnerId: String,
                 @BeanProperty @JsonProperty("gameOver") var gameOver: Boolean) {
  def this() {
    this(null, false)
  }
}
