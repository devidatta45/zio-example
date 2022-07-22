package com.ddm.sample.persistence

import com.ddm.sample.model.{DomainError, EnergyConsumption}
import zio.{Has, IO, ZIO}

import java.time.ZonedDateTime

object EnergyTariffStorage {
  trait Service[A] {
    def saveEnergyTariff(a: A): IO[DomainError, A]

    def getTariffByTimeRange(timeStart: ZonedDateTime, timeEnd: ZonedDateTime): IO[DomainError, Option[A]]
  }

  type EnergyTariffPersistence = Has[EnergyTariffStorage.Service[EnergyConsumption]]

  def saveEnergyTariff(energyConsumption: EnergyConsumption)
  : ZIO[EnergyTariffPersistence, DomainError, EnergyConsumption] = ZIO.accessM(_.get.saveEnergyTariff(energyConsumption))

  def getEnergyTariffByTimeRange(timeStart: ZonedDateTime, timeEnd: ZonedDateTime)
  : ZIO[EnergyTariffPersistence, DomainError, Option[EnergyConsumption]] = ZIO.accessM(_.get.getTariffByTimeRange(timeStart, timeEnd))

}