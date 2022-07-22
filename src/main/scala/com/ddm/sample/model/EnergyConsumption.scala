package com.ddm.sample.model

import io.circe.{Decoder, Encoder, HCursor, Json}
import java.time.ZonedDateTime
import Helper._
import io.circe.generic.semiauto._

final case class EnergyConsumption(
                                    id: String,
                                    timeStart: ZonedDateTime,
                                    timeEnd: ZonedDateTime,
                                    costPerKwh: Double
                                  )

object EnergyConsumption {
  implicit val energyConsumptionEncoder: Encoder[Either[DomainError, EnergyConsumption]] = {
    case Right(energy) =>
      Json.obj(
        ("id", Json.fromString(energy.id)),
        ("timeStart", Json.fromString(energy.timeStart.toString)),
        ("timeEnd", Json.fromString(energy.timeEnd.toString)),
        ("costPerKwh", Json.fromDoubleOrNull(energy.costPerKwh))
      )

    case Left(error) => getErrorJson(error)
  }


  implicit val energyConsumptionDecoder: Decoder[EnergyConsumption] = (c: HCursor) => {
    for {
      id <- c.downField("id").as[String]
      timeStart <- c.downField("timeStart").as[String]
      timeEnd <- c.downField("timeEnd").as[String]
      costPerKwh <- c.downField("costPerKwh").as[Double]
    } yield {
      EnergyConsumption(
        id,
        ZonedDateTime.parse(timeStart),
        ZonedDateTime.parse(timeEnd),
        costPerKwh
      )
    }
  }
}

final case class EnergyConsumptionRequest(
                                           id: String,
                                           timeStart: ZonedDateTime,
                                           timeEnd: ZonedDateTime,
                                           costPerKwh: Double
                                         )

object EnergyConsumptionRequest {
  implicit val energyConsumptionEncoder: Encoder[EnergyConsumptionRequest] = deriveEncoder
  implicit val energyConsumptionDecoder: Decoder[EnergyConsumptionRequest] = (c: HCursor) => {
    for {
      id <- c.downField("id").as[String]
      timeStart <- c.downField("timeStart").as[String]
      timeEnd <- c.downField("timeEnd").as[String]
      costPerKwh <- c.downField("costPerKwh").as[Double]
    } yield {
      EnergyConsumptionRequest(
        id,
        ZonedDateTime.parse(timeStart),
        ZonedDateTime.parse(timeEnd),
        costPerKwh
      )
    }
  }
}

final case class ChargedSessionRequest(
                                        driverId: String,
                                        chargeSessionStart: ZonedDateTime,
                                        chargeSessionEnd: ZonedDateTime,
                                        consumedEnergy: Double
                                      )

object ChargedSessionRequest {
  implicit val chargeSessionRequestEncoder: Encoder[ChargedSessionRequest] = deriveEncoder
  implicit val chargeSessionRequestDecoder: Decoder[ChargedSessionRequest] = (c: HCursor) => {
    for {
      driverId <- c.downField("driverId").as[String]
      chargeSessionStart <- c.downField("chargeSessionStart").as[String]
      chargeSessionEnd <- c.downField("chargeSessionEnd").as[String]
      consumedEnergy <- c.downField("consumedEnergy").as[Double]
    } yield {
      ChargedSessionRequest(
        driverId,
        ZonedDateTime.parse(chargeSessionStart),
        ZonedDateTime.parse(chargeSessionEnd),
        consumedEnergy,
      )
    }
  }
}

final case class ChargedSession(
                                 driverId: String,
                                 chargeSessionStart: ZonedDateTime,
                                 chargeSessionEnd: ZonedDateTime,
                                 consumedEnergy: Double,
                                 pricePaid: Double
                               )

object ChargedSession {
  implicit val chargeSessionEncoder: Encoder[Either[DomainError, ChargedSession]] = {
    case Right(session) => getChargedSessionJson(session)
    case Left(error) => getErrorJson(error)
  }

  implicit val chargeSessionVectorEncoder: Encoder[Either[DomainError, Vector[ChargedSession]]] = {
    case Right(sessions) => Json.fromValues(sessions.map(getChargedSessionJson))
    case Left(error) => getErrorJson(error)
  }

  implicit val chargeSessionDecoder: Decoder[ChargedSession] = (c: HCursor) => {
    for {
      driverId <- c.downField("driverId").as[String]
      chargeSessionStart <- c.downField("chargeSessionStart").as[String]
      chargeSessionEnd <- c.downField("chargeSessionEnd").as[String]
      consumedEnergy <- c.downField("consumedEnergy").as[Double]
      pricePaid <- c.downField("pricePaid").as[Double]
    } yield {
      ChargedSession(
        driverId,
        ZonedDateTime.parse(chargeSessionStart),
        ZonedDateTime.parse(chargeSessionEnd),
        consumedEnergy,
        pricePaid
      )
    }
  }
}