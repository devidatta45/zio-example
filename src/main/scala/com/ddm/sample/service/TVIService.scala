package com.ddm.sample.service

import cats.implicits._
import com.ddm.sample.model._
import com.ddm.sample.persistence.ChargedSessionStorage.ChargedSessionPersistence
import com.ddm.sample.persistence.EnergyTariffStorage.EnergyTariffPersistence
import com.ddm.sample.persistence.{ChargedSessionStorage, EnergyTariffStorage}
import zio.ZIO

import java.time.ZonedDateTime

trait TVIService {
  def saveConsumption(energyConsumption: EnergyConsumptionRequest): ZIO[EnergyTariffPersistence, DomainError, EnergyConsumption]

  def saveChargedSession(session: ChargedSessionRequest): ZIO[EnergyTariffPersistence with ChargedSessionPersistence, DomainError, ChargedSession]

  def getAllChargedSessions: ZIO[ChargedSessionPersistence, DomainError, Vector[ChargedSession]]
}

object TVIService {
  def service: TVIService = new TVIService {
    override def saveConsumption(energyConsumption: EnergyConsumptionRequest): ZIO[EnergyTariffPersistence, DomainError, EnergyConsumption] = {
      for {
        validEnergyConsumption <- ZIO.fromEither(validateEnergyConsumption(energyConsumption))
        existingTariff <- EnergyTariffStorage.getEnergyTariffByTimeRange(validEnergyConsumption.timeStart, validEnergyConsumption.timeEnd)
        validatedTariff = existingTariff match {
          case Some(consumption) => DuplicateTariff(consumption.id).asLeft
          case None => validEnergyConsumption.asRight
        }
        finalEnergyConsumption <- ZIO.fromEither(validatedTariff)
        result <- EnergyTariffStorage.saveEnergyTariff(finalEnergyConsumption)
      } yield result
    }

    override def saveChargedSession(session: ChargedSessionRequest): ZIO[EnergyTariffPersistence with ChargedSessionPersistence, DomainError, ChargedSession] = {
      for {
        sessionRequest <- ZIO.fromEither(validateChargeSession(session))
        existingTariff <- EnergyTariffStorage.getEnergyTariffByTimeRange(session.chargeSessionStart, session.chargeSessionEnd)
        price = existingTariff match {
          case Some(consumption) => (consumption.costPerKwh * session.consumedEnergy).asRight
          case None => TariffNotFound.asLeft
        }
        pricePaid <- ZIO.fromEither(price)
        finalChargedSession = ChargedSession(
          sessionRequest.driverId,
          sessionRequest.chargeSessionStart,
          sessionRequest.chargeSessionEnd,
          sessionRequest.consumedEnergy,
          pricePaid
        )
        result <- ChargedSessionStorage.saveChargedSession(finalChargedSession)
      } yield result
    }

    override def getAllChargedSessions: ZIO[ChargedSessionPersistence, DomainError, Vector[ChargedSession]] =
      ChargedSessionStorage.getAllChargedSessions

    private def validateEnergyConsumption(energyConsumption: EnergyConsumptionRequest): Either[DomainError, EnergyConsumption] = {
      Either.cond(energyConsumption.timeStart.isAfter(ZonedDateTime.now())
        && energyConsumption.timeEnd.isAfter(ZonedDateTime.now())
        && energyConsumption.timeEnd.isAfter(energyConsumption.timeStart),
        EnergyConsumption(energyConsumption.id, energyConsumption.timeStart, energyConsumption.timeEnd, energyConsumption.costPerKwh), InvalidTariff)
    }

    private def validateChargeSession(session: ChargedSessionRequest): Either[DomainError, ChargedSessionRequest] = {
      Either.cond(session.chargeSessionStart.isBefore(ZonedDateTime.now())
        && session.chargeSessionEnd.isBefore(ZonedDateTime.now())
        && session.chargeSessionEnd.isAfter(session.chargeSessionStart),
        session, InvalidChargedSession)
    }
  }
}