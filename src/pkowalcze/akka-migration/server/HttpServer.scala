package pkowalcze.akkamigration.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.effect.std.Dispatcher
import cats.syntax.all._
import org.typelevel.log4cats.LoggerFactory
import pkowalcze.akkamigration.Config
import pkowalcze.akkamigration.Transactors
import sttp.tapir.integ.cats.syntax._
import sttp.tapir.server.akkahttp._

import scala.concurrent.ExecutionContext

trait HttpServer[F[_]] {
  def resource: Resource[F, Http.ServerBinding]
}

object HttpServer {
  def instance[F[_]: Async: Dispatcher: LoggerFactory](
      router: Router[F]
  )(implicit actorSystem: ActorSystem): HttpServer[F] = new HttpServer[F] {

    private val logger = LoggerFactory[F].getLogger

    override def resource: Resource[F, Http.ServerBinding] = {
      implicit val ec: ExecutionContext = actorSystem.dispatcher

      val routes: Route = router.endpoints
        .map(_.imapK(Transactors.fToFuture)(Transactors.futureToF))
        .map(AkkaHttpServerInterpreter(AkkaHttpServerOptions.default).toRoute)
        .reduceLeft(_ ~ _)

      val server: F[Http.ServerBinding] =
        logger.info("Starting HTTP server") *> Async[F].fromFuture(
          Async[F].delay(Http().newServerAt(Config.host, Config.port).bind(routes))
        )

      Resource.make(server)(server =>
        logger.info("Closing HTTP server") *> Async[F].fromFuture(
          Async[F].delay(server.unbind().void)
        )
      )
    }
  }
}
