package com.spaceape.hiring

import javax.validation.constraints.{Max, Min}

import io.dropwizard.Configuration
import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.validator.constraints.NotEmpty

class NoughtsConfiguration extends Configuration {
  @JsonProperty
  @NotEmpty
  val mongohost = "localhost"

  @JsonProperty
  @Min(1)
  @Max(65535)
  val mongoport = 27017

  @JsonProperty
  @NotEmpty
  val mongodb = "tic_tac_toe"
}