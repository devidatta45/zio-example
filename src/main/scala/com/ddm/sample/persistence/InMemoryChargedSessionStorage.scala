package com.ddm.sample.persistence

import com.ddm.sample.model.{ChargedSession, DomainError}
import com.ddm.sample.persistence.ChargedSessionStorage.ChargedSessionPersistence
import zio.{IO, Ref, ZLayer}

case class InMemoryChargedSessionStorage(chargedSessionMap: Ref[Map[String, ChargedSession]])
  extends ChargedSessionStorage.Service[ChargedSession] {
  override def saveChargedSession(session: ChargedSession): IO[DomainError, ChargedSession] = {
    chargedSessionMap.update(storageMap => storageMap + (session.driverId -> session)).map(_ => session)
  }

  override def getAllChargedSessions: IO[DomainError, Vector[ChargedSession]] = {
    chargedSessionMap.modify(storageMap => (storageMap.values.toVector, storageMap))
  }
}

object InMemoryChargedSessionStorage {
  val layer: ZLayer[Any, DomainError, ChargedSessionPersistence] =
    ZLayer.fromEffect(Ref.make(Map.empty[String, ChargedSession]).map(map => InMemoryChargedSessionStorage(map)))
}