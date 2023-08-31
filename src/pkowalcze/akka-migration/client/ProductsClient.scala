package pkowalcze.akkamigration.client

import cats.effect.kernel.Sync
import cats.syntax.all._
import org.typelevel.log4cats.LoggerFactory
import pkowalcze.akkamigration.Domain.Product
import pkowalcze.akkamigration.Endpoints
import sttp.client3.SttpBackend
import sttp.client3._
import sttp.model.Uri
import sttp.tapir.DecodeResult
import sttp.tapir.PublicEndpoint
import sttp.tapir.client.sttp.SttpClientInterpreter
import sttp.tapir.client.sttp.SttpClientOptions

trait ProductsClient[F[_]] {
  def getProducts(id: String): F[Product]
}

object ProductsClient {
  def instance[F[_]: Sync: LoggerFactory](
      sttpBackend: SttpBackend[F, Any],
      baseUri: Uri
  ): ProductsClient[F] =
    new ProductsClient[F] {
      private val logger = LoggerFactory[F].getLogger

      override def getProducts(id: String): F[Product] = {
        infallibleRequest(sttpBackend, baseUri, Endpoints.getProducts)(id)
      }

    }

  private def infallibleRequest[F[_]: Sync, I, E, O](
      sttpBackend: SttpBackend[F, Any],
      baseUri: Uri,
      endpoint: PublicEndpoint[I, E, O, Any]
  )(params: I): F[O] = {
    val request =
      SttpClientInterpreter(SttpClientOptions.default).toRequest(
        endpoint,
        Some(baseUri)
      )(implicitly)(params)

    request
      .response(asBoth(asStringAlways, request.response))
      .send(sttpBackend)
      .flatMap { response =>
        response.body match {
          case (_, DecodeResult.Value(Right(value))) =>
            value.pure[F]
          case (_, failure) =>
            Sync[F].raiseError(
              new RuntimeException(s"Request failed: $failure")
            )
        }
      }
  }
}
