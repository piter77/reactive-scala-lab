package actors

import akka.actor.{Actor, ActorRef, Props, Timers}
import akka.event.{Logging, LoggingReceive}

import scala.concurrent.duration._


class Checkout extends Actor with Timers {

  val log = Logging(context.system, this)

  var customer: ActorRef = _
  var paymentService: ActorRef = _

  def ProcessingPayment: Receive = LoggingReceive {

    case ReceivedPayment =>
      log.info("Payment Received. Closing Checkout.")
      context.parent ! CloseCheckout
      context stop self

    case Cancel(PaymentTimer) =>
      cancelCheckout()
  }

  def SelectPaymentMethod: Receive = LoggingReceive {

    case SelectPaymentMethod(paymentMethod) =>
      log.info("Selected Payment method: {}", paymentMethod)
      startTimer(PaymentTimer, 2)
      paymentService = context.actorOf(Props[PaymentService], "paymentService")
      customer ! PaymentServiceStarted(paymentService)
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

    case StartCheckout(numberOfItems, customerRef) if numberOfItems > 0 =>
      log.info("CheckoutStarted")
      customer = customerRef
      startTimer(CheckoutTimer, 2)
      context become SelectingDelivery

    case StartCheckout(numberOfItems, _) if numberOfItems <= 0 =>
      log.info("Can't checkout with number of items {}", numberOfItems)
      cancelCheckout()
  }

  def startTimer(timer: Object, time: Int): Unit = {
    timers.startSingleTimer(timer, Cancel(timer), time.seconds)
  }

  def cancelCheckout(): Unit = {
    log.info("Cancelling Checkout")

    customer ! CancelCheckout
    context.parent ! CancelCheckout
    context stop self
  }

}
