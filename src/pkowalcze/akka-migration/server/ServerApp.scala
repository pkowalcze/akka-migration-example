package pkowalcze.akkamigration.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.effect.std.Dispatcher
import cats.implicits._
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import pkowalcze.akkamigration.Akka
import sttp.client3.SttpBackend

import scala.concurrent.Future

object ServerApp extends IOApp.Simple {
  override def run: IO[Unit] = {
    implicit val loggerFactory = Slf4jFactory[IO]

    Akka
      .buildActorSystem[IO]("Server")
      .flatMap { implicit actorSystem =>
        buildServer[IO]
      }
      .use(_ => IO.never)
  }

  private def buildServer[F[_]: Async: LoggerFactory](implicit
      actorSystem: ActorSystem
  ): Resource[F, Http.ServerBinding] = {
    val router = Router.instance[F]
    Dispatcher[F].flatMap { implicit dispatcher: Dispatcher[F] =>
      HttpServer.instance[F](router).resource
    }
  }
}
