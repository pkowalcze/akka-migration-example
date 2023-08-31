package pkowalcze.akkamigration

import cats.effect.kernel.Async
import cats.effect.kernel.Sync
import cats.effect.std.Dispatcher
import cats.syntax.all._
import org.typelevel.log4cats.LoggerFactory
import pkowalcze.akkamigration.Transactors
import sttp.capabilities.Effect
import sttp.client3._
import sttp.client3.akkahttp.AkkaHttpBackend
import sttp.client3.impl.cats.implicits._

import scala.concurrent.Future

final class DeferringBackend[F[_]: Sync, +P] private (
    underlying: SttpBackend[F, P]
) extends DelegateSttpBackend[F, P](underlying) {

  override def send[T, R >: P with Effect[F]](
      request: Request[T, R]
  ): F[Response[T]] = Sync[F].defer(underlying.send(request))

  override def close(): F[Unit] = Sync[F].defer(underlying.close())
}

object DeferringBackend {
  def apply[F[_]: Async: LoggerFactory, P](
      underlying: SttpBackend[Future, P]
  )(implicit dispatcher: Dispatcher[F]): DeferringBackend[F, P] = {
    new DeferringBackend[F, P](underlying.mapK(Transactors.futureToF, Transactors.fToFuture))
  }
}
