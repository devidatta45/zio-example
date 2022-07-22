package com.ddm.sample.model

import io.circe.Json

object Helper {
  def getErrorJson(error: DomainError): Json = {
    Json.obj(
      ("errorMessage", Json.fromString(error.message)),
      ("errorCode", Json.fromString(error.code))
    )
  }

  def getChargedSessionJson(session: ChargedSession): Json = {
    Json.obj(
      ("driverId", Json.fromString(session.driverId)),
      ("chargeSessionStart", Json.fromString(session.chargeSessionStart.toString)),
      ("chargeSessionEnd", Json.fromString(session.chargeSessionEnd.toString)),
      ("consumedEnergy", Json.fromDoubleOrNull(session.consumedEnergy)),
      ("pricePaid", Json.fromDoubleOrNull(session.pricePaid))
    )
  }
}
