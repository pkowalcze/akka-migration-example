package pkowalcze.akkamigration.client

import akka.actor.ActorSystem
import cats.Monad
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.effect.std.Dispatcher
import cats.syntax.all._
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import pkowalcze.akkamigration.Akka
import pkowalcze.akkamigration.Config
import pkowalcze.akkamigration.DeferringBackend
import sttp.client3.akkahttp.AkkaHttpBackend
import sttp.model.Uri

object ClientApp extends IOApp.Simple {
  override def run: IO[Unit] = {
    implicit val loggerFactory = Slf4jFactory[IO]
    val baseUri = Uri(Config.host, Config.port)

    Dispatcher[IO]
      .flatMap { implicit dispatcher: Dispatcher[IO] =>
        buildSttpBackend[IO].map { sttpBackend =>
          ProductsClient.instance[IO](sttpBackend, baseUri)
        }
      }
      .use(useClient[IO])

  }

  private def buildSttpBackend[F[_]: Async: Dispatcher: LoggerFactory]
      : Resource[F, DeferringBackend[F, Any]] =
    Akka
      .buildActorSystem("Client")
      .map { actorSystem =>
        DeferringBackend(AkkaHttpBackend.usingActorSystem(actorSystem))
      }

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
