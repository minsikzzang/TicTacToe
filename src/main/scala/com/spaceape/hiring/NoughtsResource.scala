package com.spaceape.hiring

import com.mongodb.DB
import javax.ws.rs._
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status

import com.spaceape.hiring.exception._
import com.spaceape.hiring.model.{LeaderBoard, Game, GameState, Move}
import net.vz.mongodb.jackson.WriteResult

@Path("/game")
@Produces(Array(MediaType.APPLICATION_JSON))
@Consumes(Array(MediaType.APPLICATION_JSON))
class NoughtsResource(db: DB) {
  Game.db = db
  LeaderBoard.db = db

  @POST
  def createGame(@QueryParam("player1Id") player1: String, @QueryParam("player2Id") player2: String): String = {
    try {
      val result: WriteResult[Game, String] = Game.create(player1, player2)
      result.getSavedId
    } catch {
      case inGameException: PlayerInLiveGameException =>
        "{\"error\": {\"code\": " + inGameException.getCode + ", \"message\": \"" + inGameException.getMessage + "\"}}"
    }
  }

  @GET
  @Path("/{gameId}")
  def getGame(@PathParam("gameId") gameId: String): GameState = {
    try {
      val game = Game.findById(gameId).orNull
      if (game == null) {
        throw new WebApplicationException(Status.NOT_FOUND)
      }

      game.getState
    } catch {
      case argumentException: IllegalArgumentException => throw new WebApplicationException(Status.NOT_FOUND)
    }
  }

  @PUT
  @Path("/{gameId}")
  def makeMove(@PathParam("gameId") gameId: String, move: Move): Response = {
    println(move.playerId + ", " + move.x + ", "  + move.y)

    try {
      val game = Game.findById(gameId).orNull
      if (game == null) {
        throw new WebApplicationException(Status.NOT_FOUND)
      }

      game.addMove(move)
      Response.status(Status.ACCEPTED).build
    } catch {
      case argumentException: IllegalArgumentException => throw new WebApplicationException(Status.NOT_FOUND)
      case e: InvalidPlayerTurnException =>
        Response.status(422)
          .entity("{\"error\": {\"code\": " + e.getCode + ", \"message\": \"" + e.getMessage + "\"}}")
          .build
      case e: GameHasFinishedException =>
        Response.status(422)
          .entity("{\"error\": {\"code\": " + e.getCode + ", \"message\": \"" + e.getMessage + "\"}}")
          .build
      case e: DuplicatedMoveException =>
        Response.status(422)
          .entity("{\"error\": {\"code\": " + e.getCode + ", \"message\": \"" + e.getMessage + "\"}}")
          .build
    }
  }

  @GET
  @Path("/leaderboard")
  def leaderBoard(): List[LeaderBoard] = {
    LeaderBoard.findAllTop(10)
  }
}