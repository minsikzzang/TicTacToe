package com.spaceape.hiring.exception

class BaseException(val msg: String) extends Exception(msg) {
  def toJsonString: String = {
    "{\"error\": {\"code\": " + this.getCode + ", \"message\": \"" + this.getMessage + "\"}}"
  }

  def getCode: Int = { 0 }
}
