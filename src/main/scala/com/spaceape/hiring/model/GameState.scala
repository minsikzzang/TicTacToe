package com.spaceape.hiring.model

import com.fasterxml.jackson.annotation.JsonProperty
import scala.beans.BeanProperty

case class GameState(@BeanProperty @JsonProperty val winnerId: Option[String],
                     @BeanProperty @JsonProperty val gameOver: Boolean)