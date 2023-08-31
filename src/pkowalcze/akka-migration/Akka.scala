package pkowalcze.akkamigration

import akka.actor.ActorSystem
import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.syntax.all._
import org.typelevel.log4cats.LoggerFactory

object Akka {
  def buildActorSystem[F[_]: Async: LoggerFactory](name: String): Resource[F, ActorSystem] = {
    val logger = LoggerFactory[F].getLogger

    Resource.make(
      logger.info(s"Starting ActorSystem $name") *>
        Async[F].delay(ActorSystem(name))
    )(actorSystem =>
      logger.info(s"Terminating ActorSystem $name") *>
        Async[F].fromFuture(Async[F].delay(actorSystem.terminate())).void
    )
  }
}
