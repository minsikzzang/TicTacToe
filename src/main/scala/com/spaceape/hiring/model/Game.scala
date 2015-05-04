package com.spaceape.hiring.model

import javax.persistence.Id
import com.fasterxml.jackson.annotation.JsonProperty

import com.mongodb.DB
import com.spaceape.hiring.PlayerInLiveGameException
import net.vz.mongodb.jackson.{DBQuery, WriteResult, JacksonDBCollection, ObjectId}
import scala.beans.BeanProperty

class Game (@ObjectId @JsonProperty("_id") val id: String,
            @BeanProperty @JsonProperty("player1Id") val player1Id: String,
            @BeanProperty @JsonProperty("player2Id") val player2Id: String,
            @BeanProperty @JsonProperty("state") val state: GameState = new GameState(None, false)) {
  @ObjectId @Id def getId = id
}

// Assume player1 is the one who creates a game, and player is a person who gets invited.
object Game {
  var db: DB = null

  private lazy val coll = JacksonDBCollection.wrap(db.getCollection("games"), classOf[Game], classOf[String])

  def findLiveGameByPlayer1IdOr2Id(playerId: String) = {
    Option(coll.find.or(DBQuery.is("player1Id", playerId), DBQuery.is("player2Id", playerId))
      .is("state.gameOver", false))
  }

  def findById(id: String) = {
    Option(coll.findOneById(id))
  }

  def create(player1Id: String, player2Id: String): WriteResult[Game, String] = {
    val joinedGames = Game.findLiveGameByPlayer1IdOr2Id(player1Id).orNull
    println(joinedGames.explain())
    if (joinedGames != null && joinedGames.count() > 0) {
      throw new PlayerInLiveGameException(player1Id, player2Id)
    }

    coll.save(new Game(null, player1Id, player2Id))
  }
}