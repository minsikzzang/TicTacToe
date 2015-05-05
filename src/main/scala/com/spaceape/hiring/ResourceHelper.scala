package com.spaceape.hiring

import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status

import com.spaceape.hiring.exception.BaseException

object ResourceHelper {

  def errorResponse(code: Int, exception: BaseException) = {
    Response.status(code).entity(exception.toJson).build
  }

  def ifNullThrowErrorNotFound(obj: Any) {
    if (obj == null) {
      throw new WebApplicationException(Status.NOT_FOUND)
    }
  }
}
