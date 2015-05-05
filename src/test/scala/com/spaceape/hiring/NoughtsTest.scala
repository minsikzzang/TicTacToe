
package com.spaceape.hiring

import javax.ws.rs.core.Response.Status

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.spaceape.hiring.model.{LeaderBoard, Move, GameState}
import io.dropwizard.testing.junit.DropwizardAppRule
import org.scalatest.junit.JUnitSuite

import org.junit.Test
import org.junit.ClassRule
import com.mashape.unirest.http.Unirest
import org.scalatest.Matchers


object NoughtsTest {
	@ClassRule def rule = new DropwizardAppRule[NoughtsConfiguration](classOf[NoughtsApplication], "test.yml")
}

class NoughtsTest extends JUnitSuite with Matchers {

  val baseUrl = "http://localhost:8080/game"

  val objectMapper = new ObjectMapper()
  objectMapper.registerModule(DefaultScalaModule)

  def initGame(player1Id: String, player2Id: String) = {
    val response = Unirest.post(baseUrl)
      .queryString("player1Id", player1Id)
      .queryString("player2Id", player2Id)
      .asString()

    if(response.getStatus != Status.OK.getStatusCode) {
      throw new RuntimeException(s"${response.getStatus} when creating game: ${response.getBody}")
    }

    response.getBody
  }

  def runMoves(gameId: String, moves: Seq[Move]) = {
    moves.foreach(move => {
      val response = Unirest.put(s"$baseUrl/$gameId")
        .header("Content-Type", "application/json")
        .body(objectMapper.writeValueAsString(move))
        .asString()

      if(response.getStatus != Status.ACCEPTED.getStatusCode) {
        throw new RuntimeException(s"${response.getStatus} when making move: ${response.getBody}")
      }
    })
  }

  def getState(gameId: String) = {
    val response = Unirest.get(s"$baseUrl/$gameId").asString()

    if(response.getStatus != Status.OK.getStatusCode) {
      throw new RuntimeException(s"${response.getStatus} when getting state: ${response.getBody}")
    }

    objectMapper.readValue(response.getBody, classOf[GameState])
  }

  def getLeaderboard = {
    val response = Unirest.get(s"$baseUrl/leaderboard").asString()

    if(response.getStatus != Status.OK.getStatusCode) {
      throw new RuntimeException(s"${response.getStatus} when getting state: ${response.getBody}")
    }

    objectMapper.readValue(response.getBody, classOf[List[Map[String, String]]])
  }

  @Test
  def testCreatAGameWhenUnfinishedGameExists {
    val player1: String = "10"
    val player2: String = "11"
    val gameId = initGame(player1, player2)

    val response = Unirest.post(baseUrl)
      .queryString("player1Id", player1)
      .queryString("player2Id", player2)
      .asString()

    response.getStatus shouldBe 422
    response.getBody shouldBe "{\"error\": {\"code\": 100, \"message\": \"Player(" + player1 +
      " or " + player2 + ") has unfinished games\"}}"
  }

  @Test
  def testMoveWhenGameNoExist {
    val player1: String = "12"
    val gameId = "55492b610364e6b2d4ec53e1"
    val response = Unirest.put(s"$baseUrl/$gameId")
      .header("Content-Type", "application/json")
      .body(objectMapper.writeValueAsString(Move(player1, 0, 1)))
      .asString()

    response.getStatus shouldBe 404
  }

  @Test
  def testOutOfTurn = {
    val player1: String = "12"
    val player2: String = "13"
    val gameId = initGame(player1, player2)
    runMoves(gameId, Seq(Move(player1, 0, 0)))

    val response = Unirest.put(s"$baseUrl/$gameId")
      .header("Content-Type", "application/json")
      .body(objectMapper.writeValueAsString(Move(player1, 0, 1)))
      .asString()

    response.getStatus shouldBe 422
    response.getBody shouldBe "{\"error\": {\"code\": 101, \"message\": \"It's not Player(" + player1 +
      ")'s turn yet\"}}"
  }

  @Test
  def testFinishedGame {
    val player1: String = "3"
    val player2: String = "4"
    val gameId = initGame(player1, "4")
    runMoves(gameId, Seq(
      Move(player1, 0, 0),
      Move(player2, 1, 0),
      Move(player1, 2, 0),
      Move(player2, 2, 1),
      Move(player1, 0, 1),
      Move(player2, 0, 2),
      Move(player1, 1, 1),
      Move(player2, 2, 2),
      Move(player1, 1, 2)))

    val response = Unirest.put(s"$baseUrl/$gameId")
      .header("Content-Type", "application/json")
      .body(objectMapper.writeValueAsString(Move(player2, 0, 1)))
      .asString()

    response.getStatus shouldBe 422
    response.getBody shouldBe "{\"error\": {\"code\": 102, \"message\": \"Game(" + gameId + ") has already finished\"}}"
  }

  @Test
  def testDuplicateMove = {
    val player1: String = "14"
    val player2: String = "15"
    val gameId = initGame(player1, player2)
    runMoves(gameId, Seq(Move(player1, 0, 0)))

    val response = Unirest.put(s"$baseUrl/$gameId")
      .header("Content-Type", "application/json")
      .body(objectMapper.writeValueAsString(Move(player2, 0, 0)))
      .asString()

    response.getStatus shouldBe 422
    response.getBody shouldBe "{\"error\": {\"code\": 103, \"message\": \"Position(0, 0) has already taken\"}}"
  }

  @Test
  def testGetState {
    val player1: String = "7"
    val player2: String = "8"
    val gameId = initGame(player1, player2)

    val state: GameState = new GameState(null, false)
    val gameState = getState(gameId)
    gameState.getWinnerId shouldBe state.getWinnerId
    gameState.getGameOver shouldBe state.getGameOver
  }

  @Test
  def testGetStateWhenGameNotExist {
    val gameId = "55492b610364e6b2d4ec53e1"
    val response = Unirest.get(s"$baseUrl/$gameId").asString()

    response.getStatus shouldBe 404
  }

  @Test
	def testPlayer1Win {
    val player1: String = "1"
    val player2: String = "2"
    val gameId = initGame(player1, player2)
    runMoves(gameId, Seq(
      Move(player1, 0, 0),
      Move(player2, 1, 0),
      Move(player1, 0, 1),
      Move(player2, 1, 1),
      Move(player1, 0, 2)))

    val state: GameState = new GameState("1", true)
    val gameState = getState(gameId)
    gameState.getWinnerId shouldBe state.getWinnerId
    gameState.getGameOver shouldBe state.getGameOver
	}

  @Test
  def testDraw {
    val player1: String = "3"
    val player2: String = "4"
    val gameId = initGame(player1, "4")
    runMoves(gameId, Seq(
      Move(player1, 0, 0),
      Move(player2, 1, 0),
      Move(player1, 2, 0),
      Move(player2, 2, 1),
      Move(player1, 0, 1),
      Move(player2, 0, 2),
      Move(player1, 1, 1),
      Move(player2, 2, 2),
      Move(player1, 1, 2)))

    val state: GameState = new GameState(null, true)
    val gameState = getState(gameId)
    gameState.getWinnerId shouldBe state.getWinnerId
    gameState.getGameOver shouldBe state.getGameOver
  }

  @Test
  def testGameInProgress {
    val player1: String = "5"
    val player2: String = "6"
    val gameId = initGame(player1, player2)
    runMoves(gameId, Seq(
      Move(player1, 0, 0),
      Move(player2, 1, 0),
      Move(player1, 0, 1),
      Move(player2, 1, 1)))

    val state: GameState = new GameState(null, false)
    val gameState = getState(gameId)
    gameState.getWinnerId shouldBe state.getWinnerId
    gameState.getGameOver shouldBe state.getGameOver
  }

  @Test
  def testGetLeaderboard {
    val player1: String = "100"
    val player2: String = "101"
    val player3: String = "102"
    var gameId = initGame(player1, player2)
    runMoves(gameId, Seq(
      Move(player1, 0, 0),
      Move(player2, 1, 0),
      Move(player1, 0, 1),
      Move(player2, 1, 1),
      Move(player1, 0, 2)))

    gameId = initGame(player1, player2)
    runMoves(gameId, Seq(
      Move(player1, 0, 0),
      Move(player2, 1, 0),
      Move(player1, 0, 1),
      Move(player2, 1, 1),
      Move(player1, 0, 2)))

    gameId = initGame(player1, player2)
    runMoves(gameId, Seq(
      Move(player1, 0, 0),
      Move(player2, 1, 0),
      Move(player1, 0, 1),
      Move(player2, 1, 1),
      Move(player1, 0, 2)))

    gameId = initGame(player3, player2)
    runMoves(gameId, Seq(
      Move(player3, 0, 0),
      Move(player2, 1, 0),
      Move(player3, 0, 1),
      Move(player2, 1, 1),
      Move(player3, 0, 2)))

    val leaderBoards: List[Map[String, String]] = getLeaderboard
    leaderBoards(0)("playerId") shouldBe player1
    leaderBoards(1)("playerId") shouldBe player3
  }

}