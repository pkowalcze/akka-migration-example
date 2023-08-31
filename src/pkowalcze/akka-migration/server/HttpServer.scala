package pkowalcze.akkamigration.server

import cats.data.NonEmptyList
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.effect.std.Dispatcher
import cats.syntax.all._
import org.http4s.HttpRoutes
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.Server
import org.typelevel.log4cats.LoggerFactory
import pkowalcze.akkamigration.Config
import sttp.tapir.integ.cats.syntax._
import sttp.tapir.server.http4s._

import scala.concurrent.ExecutionContext

trait HttpServer[F[_]] {
  def resource: Resource[F, Server]
}

object HttpServer {
  def instance[F[_]: Async: LoggerFactory](
      router: Router[F]
  ): HttpServer[F] = new HttpServer[F] {
    private val logger = LoggerFactory[F].getLogger

    override def resource: Resource[F, Server] = {
      val routes: HttpRoutes[F] = router.endpoints
        .map(endpoint =>
          Http4sServerInterpreter(Http4sServerOptions.default)
            .toRoutes(endpoint)
        )
        .reduceLeft(_ <+> _)

      BlazeServerBuilder[F]
        .bindHttp(Config.port, Config.host)
        .withHttpApp(routes.orNotFound)
        .resource

    }
  }
}
