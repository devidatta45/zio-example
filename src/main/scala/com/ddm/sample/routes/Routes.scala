package com.ddm.sample.routes

import com.ddm.sample.model.{ChargedSessionRequest, DomainError, EnergyConsumptionRequest}
import com.ddm.sample.persistence.ChargedSessionStorage.ChargedSessionPersistence
import com.ddm.sample.persistence.EnergyTariffStorage.EnergyTariffPersistence
import com.ddm.sample.service.TVIService
import io.circe.{Decoder, Encoder}
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, EntityEncoder, HttpRoutes}
import zio._
import zio.interop.catz._

final case class Routes[R <: EnergyTariffPersistence with ChargedSessionPersistence](rootUri: String) {

  type EnergyTask[A] = RIO[R, A]

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[EnergyTask, A]
  = jsonOf[EnergyTask, A]

  implicit def circeJsonEncoder[A](implicit encoder: Encoder[Either[DomainError, A]]): EntityEncoder[EnergyTask, Either[DomainError, A]]
  = jsonEncoderOf[EnergyTask, Either[DomainError, A]]

  val dsl: Http4sDsl[EnergyTask] = Http4sDsl[EnergyTask]

  import dsl._

  def route: HttpRoutes[EnergyTask] = {

    HttpRoutes.of[EnergyTask] {
      case request@POST -> Root / "energy-tariff" =>
        request.decode[EnergyConsumptionRequest] { energyConsumption =>
          Ok(TVIService.service.saveConsumption(energyConsumption).either)
        }

      case request@POST -> Root / "charged-session" =>
        request.decode[ChargedSessionRequest] { chargedSessionRequest =>
          Ok(TVIService.service.saveChargedSession(chargedSessionRequest).either)
        }

      case _@GET -> Root / "charged-sessions" =>
        Ok(TVIService.service.getAllChargedSessions.either)
    }
  }

}