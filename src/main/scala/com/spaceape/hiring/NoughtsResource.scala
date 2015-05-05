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
  
  val TOP_LEADERBOARD_COUNT = 10

  @POST
  def createGame(@QueryParam("player1Id") player1: String, @QueryParam("player2Id") player2: String): String = {
    try {
      val result: WriteResult[Game, String] = Game.create(player1, player2)
      result.getSavedId
    } catch {
      case inGameException: UnfinishedGameException =>
        throw new WebApplicationException(Response.status(422).entity(inGameException.toJson).build)
    }
  }

  @GET
  @Path("/{gameId}")
  def getGame(@PathParam("gameId") gameId: String): GameState = {
    val game = Game.findById(gameId).orNull
    if (game == null) {
      throw new WebApplicationException(Status.NOT_FOUND)
    }

    game.getState
  }

  @PUT
  @Path("/{gameId}")
  def makeMove(@PathParam("gameId") gameId: String, move: Move): Response = {
    try {
      val game = Game.findById(gameId).orNull
      if (game == null) {
        throw new WebApplicationException(Status.NOT_FOUND)
      }

      game.addMove(move)
      Response.status(Status.ACCEPTED).build
    } catch {
      case turnException: InvalidPlayerTurnException => Response.status(422).entity(turnException.toJson).build
      case finishedException: GameHasFinishedException => Response.status(422).entity(finishedException.toJson).build
      case duplicateException: DuplicatedMoveException => Response.status(422).entity(duplicateException.toJson).build
    }
  }

  @GET
  @Path("/leaderboard")
  def leaderBoard(): List[LeaderBoard] = {
    LeaderBoard.findAllTop(TOP_LEADERBOARD_COUNT)
  }
}