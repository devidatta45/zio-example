package com.ddm.sample.model

sealed trait DomainError extends Throwable {
  def code: String

  def message: String
}

case class DuplicateTariff(existingId: String,
                           override val code: String = DomainError.DUPLICATE_TARIFF) extends DomainError {
  override def message: String = s"Tariff for the same time range already exist with id $existingId"
}

case object InvalidTariff extends DomainError {
  override def message: String = s"The date range for the tariff is invalid"

  override val code: String = DomainError.INVALID_TARIFF
}

case object InvalidChargedSession extends DomainError {
  override def message: String = s"The date range for the charged session is invalid"

  override val code: String = DomainError.INVALID_CHARGED_SESSION
}

case object TariffNotFound extends DomainError {
  override def message: String = s"No Tariff found for the provided date range"

  override val code: String = DomainError.TARIFF_NOT_FOUND
}

object DomainError {
  val DUPLICATE_TARIFF = "DUPLICATE_TARIFF"
  val INVALID_TARIFF = "INVALID_TARIFF"
  val INVALID_CHARGED_SESSION = "INVALID_CHARGED_SESSION"
  val TARIFF_NOT_FOUND = "TARIFF_NOT_FOUND"
}