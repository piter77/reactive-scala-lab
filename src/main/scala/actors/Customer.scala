package actors

import akka.actor.{Actor, ActorRef, Props}
import akka.event.{Logging, LoggingReceive}
import objects._

class Customer extends Actor {

  var cartManager: ActorRef = _
  var checkout: ActorRef = _
  var paymentService: ActorRef = _

  val log = Logging(context.system, this)


  def receive: Receive = LoggingReceive {
    case NewCart =>
      log.info("This is customer. gonna create cart")
      cartManager = context.actorOf(Props[CartManager], "cartManager")
      cartManager ! Init
      log.info("Going into shopping state")
      context become Shopping
  }

  def Shopping: Receive = LoggingReceive {
    case CheckoutStarted(checkoutRef) =>
      log.info("received checkout started...")
      checkout = checkoutRef
      context become InCheckout

    case CartEmpty =>
      log.info("Cart is empty.")

    case message: CartMessages =>
      cartManager ! message

  }

  def InCheckout: Receive = LoggingReceive {
    case message: CheckoutMessages =>
      log.info("forwarding checkout message...")
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
