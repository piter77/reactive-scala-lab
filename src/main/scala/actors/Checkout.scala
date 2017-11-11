package actors

import akka.actor.{Actor, Timers}
import akka.event.{Logging, LoggingReceive}

import scala.concurrent.duration._


class Checkout extends Actor with Timers {

  val log = Logging(context.system, this)

  def ProcessingPayment: Receive = LoggingReceive {

    case ReceivePayment =>
      log.info("Payment Received. Closing Checkout.")
      context.parent ! CloseCheckout
      context stop self

    case Cancel(PaymentTimer) =>
      cancelCheckout()
  }

  def SelectPaymentMethod: Receive = LoggingReceive {

    case SelectPayment =>
      log.info("Selected Payment method")
      startTimer(PaymentTimer, 2)
      context become ProcessingPayment

    case Cancel(CheckoutTimer) =>
      cancelCheckout()
  }

  def SelectingDelivery: Receive = LoggingReceive {

    case SelectDeliveryMethod =>
      log.info("Selected delivery method")
      context become SelectPaymentMethod

    case Cancel(CheckoutTimer) =>
      cancelCheckout()
  }

  def receive = LoggingReceive {

    case StartCheckout(true) =>
      log.info("CheckoutStarted")
      startTimer(CheckoutTimer, 2)
      context become SelectingDelivery
  }

  def startTimer(timer: Object, time: Int): Unit = {
    timers.startSingleTimer(timer, Cancel(timer), time.seconds)
  }

  def cancelCheckout(): Unit = {
    log.info("Cancelling Checkout")

    context.parent ! CancelCheckout
    context stop self
  }

}
