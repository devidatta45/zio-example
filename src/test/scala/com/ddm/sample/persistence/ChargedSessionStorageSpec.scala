package com.ddm.sample.persistence

import com.ddm.sample.configuration.EndpointConfig
import com.ddm.sample.model.ChargedSession
import zio.Cause
import zio.blocking.Blocking
import zio.test.Assertion.{equalTo, isRight}
import zio.test.environment.TestEnvironment
import zio.test.{DefaultRunnableSpec, TestFailure, assert}

import java.time.ZonedDateTime

object ChargedSessionStorageSpec extends DefaultRunnableSpec {

  def spec =
    suite("Charged Session Storage should")(
      testM("Save and get all charged sessions correctly") {
        val from = ZonedDateTime.now().minusHours(2)
        val to = ZonedDateTime.now().minusHours(1)
        for {
          session <- ChargedSessionStorage.saveChargedSession(ChargedSession("Mak", from, to, 100, 25)).either
          sessionList <- ChargedSessionStorage.getAllChargedSessions.either
        } yield assert(session)(isRight(equalTo(ChargedSession("Mak", from, to, 100, 25)))) &&
          assert(sessionList)(isRight(equalTo(Vector(ChargedSession("Mak", from, to, 100, 25)))))

      }).provideSomeLayer[TestEnvironment](
      (EndpointConfig.live >+> Blocking.live >+> InMemoryStateStorage.layer >+> InMemoryChargedSessionStorage.layer)
        .mapError(_ => TestFailure.Runtime(Cause.die(new Exception("die"))))
    )
}
