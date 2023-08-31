package pkowalcze.akkamigration.server

import cats.Applicative
import cats.data.NonEmptyList
import cats.syntax.applicative._
import pkowalcze.akkamigration.Endpoints
import pkowalcze.akkamigration.Domain.Product
import sttp.tapir.server.ServerEndpoint

trait Router[F[_]] {
  def endpoints: NonEmptyList[ServerEndpoint[Any, F]]
}

object Router {
  def instance[F[_]: Applicative]: Router[F] = new Router[F] {

    val endpoints: NonEmptyList[ServerEndpoint[Any, F]] = NonEmptyList.of(
      Endpoints.getProducts.serverLogicSuccess(getProducts)
    )

    def getProducts(id: String): F[Product] = Product(id, description = s"$id-description").pure[F]
  }
}