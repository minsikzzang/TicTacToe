package com.spaceape.hiring.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.mongodb.{BasicDBObject, DB}
import net.vz.mongodb.jackson._
import scala.collection.JavaConversions._

import scala.beans.BeanProperty

class LeaderBoard(@BeanProperty @JsonProperty("playerId") val playerId: String,
                  @BeanProperty @JsonProperty("scores") var scores: Int) {
  def this(@ObjectId @Id id: String) {
    this(null, 0)
    this.id = id
  }

  @ObjectId @Id var id: String = null
  @ObjectId @Id def getId: String = id

  def updateScore(point: Int) {
    scores += point
  }
}

object LeaderBoard {
  var db: DB = null

  private lazy val coll = JacksonDBCollection.wrap(db.getCollection("leaderBoard"), classOf[LeaderBoard], classOf[String])

  def findByPlayerId(playerId: String): LeaderBoard = {
    val cursor = coll.find(DBQuery.is("playerId", playerId))
    if (cursor != null && cursor.hasNext) {
      cursor.next
    } else {
      null
    }
  }

  def findAndModifyOrCreate(playerId: String, point: Int): WriteResult[LeaderBoard, String] = {
    val lb: LeaderBoard = findByPlayerId(playerId)
    if (lb == null) {
      coll.save(new LeaderBoard(playerId, point))
    } else {
      lb.updateScore(point)
      coll.updateById(lb.id, lb)
    }
  }

  def findAllTop(nTop: Int): List[LeaderBoard] = {
    coll.find.sort(new BasicDBObject("scores", -1)).toArray(nTop).toList
  }
}
