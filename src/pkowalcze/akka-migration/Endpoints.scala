package pkowalcze.akkamigration

import pkowalcze.akkamigration.Domain.Product
import sttp.tapir._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

object Endpoints {

  val getProducts: PublicEndpoint[String, Unit, Product, Any] =
    endpoint.get.in("products").in(path[String]("id")).out(jsonBody[Product])

}
