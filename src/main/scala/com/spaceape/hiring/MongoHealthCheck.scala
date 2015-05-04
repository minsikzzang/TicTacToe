package com.spaceape.hiring

import com.codahale.metrics.health.HealthCheck
import com.codahale.metrics.health.HealthCheck.Result
import com.mongodb.Mongo

class MongoHealthCheck(mongo: Mongo) extends HealthCheck {

  override def check: Result = {
    mongo.getDatabaseNames
    Result.healthy()
  }

}
