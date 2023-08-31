package pkowalcze.akkamigration.server

import cats.effect.IO
import cats.effect.IOApp
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import org.http4s.server.Server
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory

import scala.concurrent.Future

object ServerApp extends IOApp.Simple {
  override def run: IO[Unit] = {
    implicit val loggerFactory = Slf4jFactory[IO]

    buildServer[IO].use(_ => IO.never)
  }

  private def buildServer[F[_]: Async: LoggerFactory]: Resource[F, Server] = {
    val router = Router.instance[F]
    HttpServer.instance[F](router).resource
  }
}
