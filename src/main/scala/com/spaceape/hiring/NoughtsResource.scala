package com.spaceape.hiring

import com.mongodb.DB
import javax.ws.rs._
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response.Status

import com.spaceape.hiring.model.{Game, GameState, Move}
import net.vz.mongodb.jackson.{WriteResult, JacksonDBCollection}
;

@Path("/game")
@Produces(Array(MediaType.APPLICATION_JSON))
@Consumes(Array(MediaType.APPLICATION_JSON))
class NoughtsResource(db: DB) {
  Game.db = db

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
    GameState(None, false)
  }


  @PUT
  @Path("/{gameId}")
  def makeMove(@PathParam("gameId") gameId: String, move: Move) {
    throw new WebApplicationException(Status.NOT_IMPLEMENTED)
  }
}