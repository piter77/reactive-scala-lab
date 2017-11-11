import Cart.{CancelCheckout, CloseCheckout, StartCheckout}
import akka.actor.{Actor, Timers}
import akka.event.LoggingReceive

import scala.concurrent.duration._


object Checkout {

  sealed trait CheckoutMessage
  case object SelectPayment extends CheckoutMessage
  case object SelectDeliveryMethod extends CheckoutMessage
  case object ReceivePayment extends CheckoutMessage

  case object CheckoutTimer
  case class Cancel(o: Object)
  case object PaymentTimer
}

class Checkout extends Actor with Timers {

  import Checkout._

  def ProcessingPayment: Receive = LoggingReceive {

    case ReceivePayment =>
      context.parent ! CloseCheckout
      context stop self

    case Cancel(PaymentTimer) =>
      cancelCheckout()
  }

  def SelectPaymentMethod: Receive = LoggingReceive {

    case SelectPayment =>
      startTimer(PaymentTimer, 2)
      context become ProcessingPayment

    case Cancel(CheckoutTimer) =>
      cancelCheckout()
  }

  def SelectingDelivery: Receive = LoggingReceive {

    case SelectDeliveryMethod =>
      context become SelectPaymentMethod

    case Cancel(CheckoutTimer) =>
      cancelCheckout()
  }

  def receive = LoggingReceive {

    case StartCheckout(true) =>
      startTimer(CheckoutTimer, 2)
      context become SelectingDelivery
  }

  def startTimer(timer: Object, time: Int): Unit = {
    timers.startSingleTimer(timer, Cancel(timer), time.seconds)
  }

  def cancelCheckout(): Unit = {
    context.parent ! CancelCheckout
    context stop self
  }

}
