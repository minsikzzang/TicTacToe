package com.spaceape.hiring.model

import com.fasterxml.jackson.annotation.{JsonProperty, JsonCreator}

import com.mongodb.DB
import com.spaceape.hiring.exception._
import net.vz.mongodb.jackson.Id
import net.vz.mongodb.jackson.ObjectId
import net.vz.mongodb.jackson.JacksonDBCollection
import net.vz.mongodb.jackson.DBQuery
import net.vz.mongodb.jackson.WriteResult
import scala.beans.BeanProperty

class Game (@BeanProperty @JsonProperty("player1Id") val player1Id: String,
            @BeanProperty @JsonProperty("player2Id") val player2Id: String,
            @BeanProperty @JsonProperty("state") val state: GameState = new GameState(null, false),
            @BeanProperty @JsonProperty("scores") var scores: Array[Int] = Array(0, 0, 0, 0, 0, 0, 0, 0),
            @BeanProperty @JsonProperty("points") var points: Array[Int] = Array(0, 0, 0, 0, 0, 0, 0, 0, 0)) {

  def this(@ObjectId @Id id: String) {
    this(null, null, null)
    this.id = id
  }

  @ObjectId @Id var id: String = null
  @BeanProperty @JsonProperty("turn") var turn: String = player1Id
  @ObjectId @Id def getId: String = id
  val GRID_N = 3

  def addMove(move: Move) {
    if (state.getGameOver) {
      throw new GameHasFinishedException(id)
    }

    if (getTurn != move.playerId) {
      throw new InvalidPlayerTurnException(move.playerId)
    }

    makeMove(move, scores)
    switchTurn

    val winner: Int = hasWinner(scores)
    if (winner != 0) {
      state.setWinnerId(getWinnerId(winner, player1Id, player2Id))
      updateLeaderboard(state.getWinnerId, getLoserId(winner, player1Id, player2Id))
    }

    if (hasDrawn(points) || winner != 0) {
      state.setGameOver(true)
    }

    Game.updateById(id, this)
  }

  def updateLeaderboard(winnerId: String, loserId: String) {
    LeaderBoard.findAndModifyOrCreate(winnerId, 1)
    LeaderBoard.findAndModifyOrCreate(loserId, -1)
  }

  def getWinnerId(winner: Int, player1: String, player2: String): String = {
    if (winner == 1) player1Id else player2Id
  }

  def getLoserId(winner: Int, player1: String, player2: String): String = {
    if (winner == 1) player2Id else player1Id
  }

  def hasWinner(scores: Array[Int]): Int = {
    var result: Int = 0
    if (scores.indexOf(GRID_N) >= 0 || scores.indexOf(-1 * GRID_N) >= 0) {
      result = if (scores.indexOf(GRID_N) >= 0) 1 else -1
    }
    result
  }

  def hasDrawn(points: Array[Int]): Boolean = {
    points.indexOf(0) < 0
  }

  def makeMove(move: Move, scores: Array[Int]) {
    if (points(move.x * GRID_N + move.y) != 0) {
      throw new DuplicatedMoveException(move)
    }
    points(move.x * GRID_N + move.y) = 1

    val point: Int = { if (turn.compareTo(player1Id) == 0) 1 else -1 }
    scores(move.x) += point
    scores(move.y + GRID_N) += point
    if (move.x == move.y) scores(2 * GRID_N) += point
    if (GRID_N - 1 - move.y == move.x) scores(2 * GRID_N + 1) += point
  }

  def switchTurn {
    turn = if (turn.compareTo(player1Id) == 0) player2Id else player1Id
  }
}

object Game {
  var db: DB = null

  private lazy val coll = JacksonDBCollection.wrap(db.getCollection("games"), classOf[Game], classOf[String])

  def findLiveGameByPlayer1IdOr2Id(playerId: String) = {
    Option(coll.find
      .or(DBQuery.is("player1Id", playerId), DBQuery.is("player2Id", playerId))
        .is("state.gameOver", false))
  }

  def findById(id: String) = Option(coll.findOneById(id))

  def updateById(id: String, game: Game) { coll.updateById(id, game) }

  def create(player1Id: String, player2Id: String): WriteResult[Game, String] = {
    val joinedGames = Game.findLiveGameByPlayer1IdOr2Id(player1Id).orNull
    if (joinedGames != null && joinedGames.count() > 0) {
      throw new UnfinishedGameException(player1Id, player2Id)
    }

    coll.save(new Game(player1Id, player2Id))
  }

}