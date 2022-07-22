package com.ddm.sample

import cats.effect.Async
import com.ddm.sample.configuration.EndpointConfig
import org.http4s.HttpApp
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.middleware.{CORS, Logger}

object Server {
  def startServer[F[_] : Async](api: EndpointConfig, httpApp: HttpApp[F]): F[Unit] = {
    val finalHttpApp = Logger.httpApp(logHeaders = true, logBody = true)(httpApp)
    val cors = CORS.policy
      .withAllowOriginAll
      .withAllowCredentials(false)
      .apply(finalHttpApp)
    BlazeServerBuilder[F]
      .bindHttp(api.port, api.endpoint)
      .withHttpApp(cors)
      .serve
      .compile
      .drain
  }
}