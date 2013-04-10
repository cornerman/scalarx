

import annotation.tailrec
import concurrent.Future
import java.util.concurrent.atomic.{AtomicBoolean, AtomicReference}

/**
 * '''Scala.Rx''' is an experimental change propagation library for [[http://www.scala-lang.org/ Scala]].
 * Scala.Rx gives you Reactive variables (`Rx`s), which are smart variables who auto-update themselves
 * when the values they depend on change. The underlying implementation is push-based
 * [[http://en.wikipedia.org/wiki/Functional_reactive_programming FRP]] based on the
 * ideas in
 * [[http://infoscience.epfl.ch/record/176887/files/DeprecatingObservers2012.pdf Deprecating the Observer Pattern]].
 *
 * A simple example which demonstrates its usage is:
 *
 * {{{
 * import rx._
 * val a = Var(1); val b = Var(2)
 * val c = Rx{ a() + b() }
 * println(c()) // 3
 * a() = 4
 * println(c()) // 6
 * }}}
 *
 * See [[https://github.com/lihaoyi/scala.rx the github page]] for more
 * instructions on how to use this package, or browse the classes on the left.
 */
package object rx {

  /**
   * Shorthand for constructing instances of [[Dynamic]]
   */
  val Rx = Dynamic
  /**
   * Shorthand for [[Signal]][T]
   */
  type Rx[T] = Signal[T]
  val Timer = utility.Timer

  implicit def pimpedFutureSignal[T](source: Signal[Future[T]]) = utility.pimpedFutureSignal(source)

  private[rx] case class Atomic[T](t: T) extends AtomicReference[T](t){
    def apply() = get
    def update(t: T) = set(t)
    @tailrec final def spinSet(transform: T => T): Unit = {
      val oldV = this()
      val newV = transform(oldV)
      if (!compareAndSet(oldV, newV)) {
        spinSet(transform)
      }
    }
    @tailrec final def spinSetOpt(transform: T => Option[T]): Boolean = {
      val oldV = this()
      val newVOpt = transform(oldV)
      newVOpt match{
        case Some(newV) => if (!compareAndSet(oldV, newV)) {
          spinSetOpt(transform)
        } else true
        case None => false
      }

    }
  }
}

