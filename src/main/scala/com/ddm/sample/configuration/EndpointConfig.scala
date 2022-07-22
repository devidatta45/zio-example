package com.ddm.sample.configuration

import pureconfig.ConfigSource
import zio.{Has, Layer, Task, URIO, ZIO, ZLayer}

final case class EndpointConfig(endpoint: String, port: Int)

final case class ApplicationConfig(http: EndpointConfig)

object EndpointConfig {
  type Configuration = Has[EndpointConfig]

  val apiConfig: URIO[Has[EndpointConfig], EndpointConfig] = ZIO.access(_.get)

  import pureconfig.generic.auto._
  val live: Layer[Throwable, Configuration] = ZLayer.fromEffectMany(
    Task
      .effect(ConfigSource.default.loadOrThrow[ApplicationConfig])
      .map(c => Has(c.http))
  )
}