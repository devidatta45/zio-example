package com.ddm.sample.persistence

import com.ddm.sample.model.{DomainError, EnergyConsumption}
import com.ddm.sample.persistence.EnergyTariffStorage.EnergyTariffPersistence
import zio.{IO, Ref, ZLayer}

import java.time.ZonedDateTime

case class InMemoryEnergyTariffStorage(energyConsumption: Ref[Map[String, EnergyConsumption]])
  extends EnergyTariffStorage.Service[EnergyConsumption] {
  override def saveEnergyTariff(consumption: EnergyConsumption): IO[DomainError, EnergyConsumption] = {
    energyConsumption.update(storageMap => storageMap + (consumption.id -> consumption)).map(_ => consumption)
  }

  override def getTariffByTimeRange(timeStart: ZonedDateTime, timeEnd: ZonedDateTime): IO[DomainError, Option[EnergyConsumption]] = {
    energyConsumption.modify(storageMap =>
      (storageMap.values.toVector.find(consumption =>
        (timeStart.isAfter(consumption.timeStart) && timeStart.isBefore(consumption.timeEnd)) ||
          consumption.timeStart.isEqual(timeStart) || consumption.timeEnd.isEqual(timeStart)),
        storageMap))
  }
}

object InMemoryStateStorage {
  val layer: ZLayer[Any, DomainError, EnergyTariffPersistence] =
    ZLayer.fromEffect(Ref.make(Map.empty[String, EnergyConsumption]).map(InMemoryEnergyTariffStorage))
}