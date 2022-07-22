package com.ddm.sample.persistence

import com.ddm.sample.model.{ChargedSession, DomainError}
import zio.{Has, IO, ZIO}

object ChargedSessionStorage {

  trait Service[A] {
    def saveChargedSession(a: A): IO[DomainError, A]

    def getAllChargedSessions: IO[DomainError, Vector[A]]
  }

  type ChargedSessionPersistence = Has[ChargedSessionStorage.Service[ChargedSession]]

  def saveChargedSession(chargedSession: ChargedSession)
  : ZIO[ChargedSessionPersistence, DomainError, ChargedSession] = ZIO.accessM(_.get.saveChargedSession(chargedSession))

  def getAllChargedSessions
  : ZIO[ChargedSessionPersistence, DomainError, Vector[ChargedSession]] = ZIO.accessM(_.get.getAllChargedSessions)

}
