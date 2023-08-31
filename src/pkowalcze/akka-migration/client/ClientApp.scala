package pkowalcze.akkamigration.client

import cats.Monad
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.syntax.all._
import org.http4s.blaze.client.BlazeClientBuilder
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import pkowalcze.akkamigration.Config
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3.SttpBackend
import sttp.client3.http4s.Http4sBackend
import sttp.model.Uri

object ClientApp extends IOApp.Simple {
  override def run: IO[Unit] = {
    implicit val loggerFactory = Slf4jFactory[IO]
    val baseUri = Uri(Config.host, Config.port)

    buildSttpBackend[IO]
      .map { sttpBackend =>
        ProductsClient.instance[IO](sttpBackend, baseUri)
      }
      .use(useClient[IO])

  }

  private def buildSttpBackend[F[_]: Async: LoggerFactory]
      : Resource[F, SttpBackend[F, Fs2Streams[F]]] =
    BlazeClientBuilder[F].resource.map(Http4sBackend.usingClient(_))

  private def useClient[F[_]: Monad: LoggerFactory](
      client: ProductsClient[F]
  ) = {
    val logger = LoggerFactory[F].getLogger

    logger.info(s"Fetching some product") *>
      client
        .getProducts("test")
        .flatMap(result => logger.info(s"got result $result"))
  }
}
