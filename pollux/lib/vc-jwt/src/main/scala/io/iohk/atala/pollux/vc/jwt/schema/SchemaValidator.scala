package io.iohk.atala.pollux.vc.jwt.schema

import zio.prelude.Validation;
import io.circe.{Decoder, Encoder, HCursor, Json}

trait SchemaValidator {
  def validate(payloadToValidate: Json): Validation[String, Json]
}
