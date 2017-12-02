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

  def SelectingPaymentMethod: Receive = LoggingReceive {

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

    case SelectDeliveryMethod(deliveryMethod) =>
      log.info("Selected delivery method: {}", deliveryMethod)
      context become SelectingPaymentMethod

    case Cancel(CheckoutTimer) =>
      cancelCheckout()
  }

  def receive = LoggingReceive {

    case InitCheckout(cart: Cart, _) if cart.items.isEmpty =>
      log.info("Can't checkout with empty cart")
      cancelCheckout()

    case InitCheckout(_, customerRef) =>
      log.info("Checkout started.")
      customer = customerRef
      startTimer(CheckoutTimer, 2)
      context become SelectingDelivery


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
