package com.ddm.sample.persistence

import com.ddm.sample.configuration.EndpointConfig
import com.ddm.sample.model.EnergyConsumption
import zio.Cause
import zio.blocking.Blocking
import zio.test.Assertion.{equalTo, isRight}
import zio.test.environment.TestEnvironment
import zio.test.{DefaultRunnableSpec, TestFailure, assert}

import java.time.ZonedDateTime

object EnergyTariffStorageSpec extends DefaultRunnableSpec {

  def spec =
    suite("Energy Tariff Storage should")(
      testM("Save and get tariffs correctly") {
        val from = ZonedDateTime.now().plusHours(1)
        val to = ZonedDateTime.now().plusHours(2)
        for {
          consumption <- EnergyTariffStorage.saveEnergyTariff(EnergyConsumption("tariff-1", from, to, 1.25)).either
          fetchedTariff <- EnergyTariffStorage.getEnergyTariffByTimeRange(from.plusMinutes(5), from.plusMinutes(10)).either
        } yield assert(consumption)(isRight(equalTo(EnergyConsumption("tariff-1", from, to, 1.25)))) &&
          assert(fetchedTariff)(isRight(equalTo(Some(EnergyConsumption("tariff-1", from, to, 1.25)))))

      }).provideSomeLayer[TestEnvironment](
      (EndpointConfig.live >+> Blocking.live >+> InMemoryStateStorage.layer >+> InMemoryChargedSessionStorage.layer)
        .mapError(_ => TestFailure.Runtime(Cause.die(new Exception("die"))))
    )
}
