package actors

import akka.actor.{ActorRef, Props, Timers}
import akka.event.{Logging, LoggingReceive}
import akka.persistence.{PersistentActor, RecoveryCompleted}
import objects._

import scala.concurrent.duration._


class Checkout(id: String = "checkout-id") extends PersistentActor with Timers {

  val log = Logging(context.system, this)

  var customer: ActorRef = _
  var paymentService: ActorRef = _
  var deliveryMethod: String = _

  override def persistenceId = id

  def updateState(event: CheckoutEvent): Unit = {
    var newState: CheckoutState = SelectingDeliveryState

    event match {
      case InitializationEvent(customerRef, state) =>
        newState = state
        startTimer(CheckoutTimer, 10)
        customer = customerRef
      case SelectedDeliveryEvent(method, state) =>
        newState = state
        deliveryMethod = method
      case SelectedPaymentMethodEvent(method, state) =>
        newState = state
        startTimer(PaymentTimer, 5)
        paymentService = context.actorOf(Props[PaymentService], "paymentService")
    }

    log.info("Changing context to " + newState.toString)
    context.become(
      newState match {
        case ProcessingPaymentState => ProcessingPayment
        case SelectingPaymentMethodState => SelectingPaymentMethod
        case SelectingDeliveryState => SelectingDelivery
      })
  }


  def ProcessingPayment: Receive = LoggingReceive {

    case ReceivedPayment =>
      log.info("Payment Received. Closing Checkout.")
      context.parent ! CloseCheckout
      context stop self

    case NotReceivedPayment =>
      log.info("Payment Not Received.")
      cancelCheckout()

    case Cancel(PaymentTimer) =>
      cancelCheckout()

    case CheckState => sender ! ProcessingPaymentState

  }

  def SelectingPaymentMethod: Receive = LoggingReceive {

    case SelectPaymentMethod(method) =>
      log.info("Selected Payment method: {}", method)
      persist(SelectedPaymentMethodEvent(method, ProcessingPaymentState)) {
        event => updateState(event)
      }
      customer ! PaymentServiceStarted(paymentService)

    case Cancel(CheckoutTimer) =>
      cancelCheckout()

    case CheckState => sender ! SelectingPaymentMethodState

  }

  def SelectingDelivery: Receive = LoggingReceive {
    case SelectDeliveryMethod(method) =>
      log.info("Selected delivery method: {}", method)
      persist(SelectedDeliveryEvent(method, SelectingPaymentMethodState)) {
        event => updateState(event)
      }

    case Cancel(CheckoutTimer) =>
      cancelCheckout()

    case CheckState => sender ! SelectingDeliveryState
  }

  override def receiveCommand: Receive = {

    case InitCheckout(cart: Cart, _) if cart.items.isEmpty =>
      log.info("Can't checkout with empty cart")
      cancelCheckout()

    case InitCheckout(_, customerRef) =>
      log.info("Checkout started.")
      persist(InitializationEvent(customerRef, SelectingDeliveryState)) {
        event => updateState(event)
      }
  }


  override def receiveRecover: Receive = {
    case event: CheckoutEvent => updateState(event)
    case RecoveryCompleted => log.info("Checkout recovery completed!")
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
