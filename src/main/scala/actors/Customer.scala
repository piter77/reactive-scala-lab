package actors

import akka.actor.{Actor, ActorRef, Props}
import akka.event.{Logging, LoggingReceive}

class Customer extends Actor {

  var cart: ActorRef = _
  var checkout: ActorRef = _
  var paymentService: ActorRef = _

  val log = Logging(context.system, this)


  def receive: Receive = LoggingReceive {
    case NewCart =>
      cart = context.actorOf(Props[Cart], "cart")
      cart ! Init
      context become Shopping
  }

  def Shopping: Receive = LoggingReceive {
    case CheckoutStarted(checkoutRef) =>
      checkout = checkoutRef
      context become InCheckout

    case CartEmpty =>
      log.info("Cart is empty.")

    case message: CartMessages =>
      cart ! message

  }

  def InCheckout: Receive = LoggingReceive {
    case message: CheckoutMessages =>
      checkout ! message

    case PaymentServiceStarted(paymentServiceRef) =>
      paymentService = paymentServiceRef
      context become DoingPayment

    case CancelCheckout =>
      checkout = null
      context become Shopping

  }
  def DoingPayment: Receive = LoggingReceive {
    case message: PaymentServiceMessages =>
      paymentService ! message

    case ConfirmReceivingPayment =>
      paymentService = null
      context become InCheckout

  }
}
