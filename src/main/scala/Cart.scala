
import Checkout.CheckoutMessage
import akka.actor.{Timers, _}
import akka.event.LoggingReceive

import scala.concurrent.duration._

object Cart {
  case object RemoveItem
  case object AddItem
  case object CartTimer
  case class StartCheckout(bool: Boolean)
  case object CancelCheckout
  case object CloseCheckout
  case object Init
  case object Done
  case object Expired
}

class Cart extends Actor with Timers {

  import Cart._

  var itemCount = 0
  val checkout: ActorRef = context.actorOf(Props[Checkout], "checkout")

  def Empty: Receive = LoggingReceive {

    case AddItem =>
      startTimer(5)
      itemCount += 1
      context become NonEmpty

    case Done =>
      context.system.terminate()
  }


  def NonEmpty: Receive = LoggingReceive {

    case AddItem =>
      startTimer(5)
      itemCount += 1

    case RemoveItem if itemCount > 1 =>
      startTimer(5)
      itemCount -= 1

    case RemoveItem if itemCount == 1 =>
      timers.cancel(CartTimer)
      itemCount -= 1
      context become Empty

    case StartCheckout =>
      timers.cancel(CartTimer)

      checkout ! StartCheckout(itemCount != 0)
      context become InCheckout

    case Expired =>
      itemCount = 0
      context become Empty
  }


  def InCheckout: Receive = LoggingReceive {

    case CancelCheckout =>
      startTimer(5)
      context become NonEmpty

    case CloseCheckout =>
      itemCount = 0
      context become Empty

    case message: CheckoutMessage =>
      checkout forward message
  }


  def receive = LoggingReceive {
    case Init =>
      context become Empty
  }


  def startTimer(timeInSeconds: Int): Unit = {
    timers.startSingleTimer(CartTimer, Expired, timeInSeconds.seconds)
  }

}