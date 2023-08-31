package pkowalcze.akkamigration

import cats.effect.kernel.Async
import cats.effect.std.Dispatcher
import cats.~>

import scala.concurrent.Future

object Transactors {
  def futureToF[F[_]: Async]: Future ~> F = new (Future ~> F) {
    override def apply[A](fa: Future[A]): F[A] =
      Async[F].fromFuture(Async[F].delay(fa))
  }

  def fToFuture[F[_]](implicit dispatcher: Dispatcher[F]): F ~> Future =
    new (F ~> Future) {
      override def apply[A](fa: F[A]): Future[A] = dispatcher.unsafeToFuture(fa)
    }
}
