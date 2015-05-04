
package com.spaceape.hiring

import javax.ws.rs.core.Response.Status

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.spaceape.hiring.model.{Move, GameState}
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

    println(response.getBody)
    objectMapper.readValue(response.getBody, classOf[GameState])
  }

	@Test
	def testPlayer1Win {
    val gameId = initGame("1", "2")
    runMoves(gameId, Seq(
      Move("1", 0, 0),
      Move("2", 1, 0),
      Move("1", 0, 1),
      Move("2", 1, 1),
      Move("1", 0, 2)))

    val state: GameState = new GameState("1", true)
    getState(gameId).getWinnerId shouldBe state.getWinnerId
    getState(gameId).getGameOver shouldBe state.getGameOver
	}

  @Test
  def testDraw {
    val gameId = initGame("3", "4")
    runMoves(gameId, Seq(
      Move("3", 0, 0),
      Move("4", 1, 0),
      Move("3", 2, 0),
      Move("4", 2, 1),
      Move("3", 0, 1),
      Move("4", 0, 2),
      Move("3", 1, 1),
      Move("4", 2, 2),
      Move("3", 1, 2)))

    val state: GameState = new GameState(null, true)
    getState(gameId).getWinnerId shouldBe state.getWinnerId
    getState(gameId).getGameOver shouldBe state.getGameOver
  }

  @Test
  def testPlayInProgress {
    val gameId = initGame("5", "6")
    runMoves(gameId, Seq(
      Move("5", 0, 0),
      Move("6", 1, 0),
      Move("5", 0, 1),
      Move("6", 1, 1)))

    val state: GameState = new GameState(null, false)
    getState(gameId).getWinnerId shouldBe state.getWinnerId
    getState(gameId).getGameOver shouldBe state.getGameOver
  }

}