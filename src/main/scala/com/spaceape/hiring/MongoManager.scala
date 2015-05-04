package com.spaceape.hiring

import com.mongodb.Mongo
import io.dropwizard.lifecycle.Managed

class MongoManager(mongo: Mongo) extends Managed {

  override def start {

  }

  override def stop {
    mongo.close();
  }

}
