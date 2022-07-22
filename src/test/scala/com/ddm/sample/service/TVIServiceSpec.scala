package com.ddm.sample.service

import com.ddm.sample.configuration.EndpointConfig
import com.ddm.sample.model._
import com.ddm.sample.persistence.{InMemoryChargedSessionStorage, InMemoryStateStorage}
import zio.Cause
import zio.blocking.Blocking
import zio.test.Assertion._
import zio.test.environment.TestEnvironment
import zio.test.{DefaultRunnableSpec, TestFailure, assert}

import java.time.ZonedDateTime

object TVIServiceSpec extends DefaultRunnableSpec {

  def spec =
    suite("TVI Service should")(
      testM("Save tariff in case of proper request") {
        val from = ZonedDateTime.now().plusHours(1)
        val to = ZonedDateTime.now().plusHours(2)
        for {
          consumption <- TVIService.service.saveConsumption(EnergyConsumptionRequest("tariff-1", from, to, 1.25)).either
        } yield assert(consumption)(isRight(equalTo(EnergyConsumption("tariff-1", from, to, 1.25))))

      },
      testM("Fail if the date is in the past in the tariff request") {
        val from = ZonedDateTime.now().minusHours(1)
        for {
          consumption <- TVIService.service.saveConsumption(EnergyConsumptionRequest("tariff-1", from, ZonedDateTime.now(), 1.25)).either
        } yield assert(consumption)(isLeft(equalTo(InvalidTariff)))

      },
      testM("Fail if the end is before the start in the tariff request") {
        val from = ZonedDateTime.now().minusHours(1)
        val to = ZonedDateTime.now().minusHours(2)
        for {
          consumption <- TVIService.service.saveConsumption(EnergyConsumptionRequest("tariff-1", from, to, 1.25)).either
        } yield assert(consumption)(isLeft(equalTo(InvalidTariff)))

      },
      testM("Fail if duplicate tariff exists with exact dates") {
        val from = ZonedDateTime.now().plusHours(1)
        val to = ZonedDateTime.now().plusHours(2)
        for {
          consumption <- TVIService.service.saveConsumption(EnergyConsumptionRequest("tariff-1", from, to, 1.25)).either
          duplicate <- TVIService.service.saveConsumption(EnergyConsumptionRequest("tariff-1", from, to, 1.25)).either
        } yield assert(consumption)(isRight(equalTo(EnergyConsumption("tariff-1", from, to, 1.25)))) &&
          assert(duplicate)(isLeft(equalTo(DuplicateTariff("tariff-1"))))
      },
      testM("Fail if duplicate tariff exists within the date range") {
        val from = ZonedDateTime.now().plusHours(1)
        val to = ZonedDateTime.now().plusHours(2)
        for {
          consumption <- TVIService.service.saveConsumption(EnergyConsumptionRequest("tariff-1", from, to, 1.25)).either
          duplicate <- TVIService.service.saveConsumption(EnergyConsumptionRequest("tariff-1", from.plusMinutes(5), to, 1.25)).either
        } yield assert(consumption)(isRight(equalTo(EnergyConsumption("tariff-1", from, to, 1.25)))) &&
          assert(duplicate)(isLeft(equalTo(DuplicateTariff("tariff-1"))))
      },
      testM("Fail to save charged session in case no active Tariff") {
        val from = ZonedDateTime.now().minusHours(2)
        val to = ZonedDateTime.now().minusHours(1)
        for {
          error <- TVIService.service.saveChargedSession(ChargedSessionRequest("driver-1", from, to, 100)).either
        } yield assert(error)(isLeft(equalTo(TariffNotFound)))

      },
      testM("Fail to save charged session in case the dates are in the future") {
        val from = ZonedDateTime.now().plusHours(1)
        val to = ZonedDateTime.now().plusHours(2)
        for {
          error <- TVIService.service.saveChargedSession(ChargedSessionRequest("driver-1", from, to, 100)).either
        } yield assert(error)(isLeft(equalTo(InvalidChargedSession)))

      }).provideSomeLayer[TestEnvironment](
      (EndpointConfig.live >+> Blocking.live >+> InMemoryStateStorage.layer >+> InMemoryChargedSessionStorage.layer)
        .mapError(_ => TestFailure.Runtime(Cause.die(new Exception("die"))))
    )
}