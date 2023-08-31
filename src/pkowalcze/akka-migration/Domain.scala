package pkowalcze.akkamigration

import io.circe.Codec
import io.circe.generic.semiauto._

object Domain {
  final case class Product(id: String, description: String)

  object Product {
    implicit val codec: Codec[Product] = deriveCodec
  }
}