package com.ddm.sample

import com.ddm.sample.configuration.EndpointConfig
import com.ddm.sample.persistence.ChargedSessionStorage.ChargedSessionPersistence
import com.ddm.sample.persistence.EnergyTariffStorage.EnergyTariffPersistence
import com.ddm.sample.persistence.{InMemoryChargedSessionStorage, InMemoryStateStorage}
import com.ddm.sample.routes.Routes
import org.http4s.implicits._
import org.http4s.server.Router
import zio._
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.putStrLn
import zio.interop.catz._

object Main extends CatsApp {

  type AppEnvironment = EndpointConfig.Configuration with Clock with EnergyTariffPersistence with ChargedSessionPersistence with zio.blocking.Blocking

  type AppTask[A] = RIO[AppEnvironment, A]

  val appEnvironment =
    EndpointConfig.live >+> Blocking.live >+> InMemoryStateStorage.layer >+> InMemoryChargedSessionStorage.layer

  override def run(args: List[String]): ZIO[ZEnv, Nothing, ExitCode] = {
    val program: ZIO[AppEnvironment, Throwable, Unit] =
      for {
        api <- EndpointConfig.apiConfig
        httpApp = Router[AppTask](
          "/energy" -> Routes(s"${api.endpoint}/energy").route
        ).orNotFound

        server <- ZIO.runtime[AppEnvironment].flatMap { _ =>
          Server.startServer(api, httpApp)
        }
      } yield server

    program
      .provideSomeLayer[ZEnv](appEnvironment)
      .tapError(err => putStrLn(s"Execution failed with: $err"))
      .exitCode
  }
}