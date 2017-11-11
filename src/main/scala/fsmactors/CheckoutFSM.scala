package fsmactors

import akka.actor.{Actor, FSM, Timers}
import fsmactors.CartFSM.{CancelCheckout, CloseCheckout, StartCheckout}
import fsmactors.CheckoutFSM._

import scala.concurrent.duration._

object CheckoutFSM {

  case object CheckoutTimer
  case object PaymentTimer
  case class Cancelled(o: Object)

  sealed trait CheckoutMessage
  case class SelectPaymentMethod(method: String) extends CheckoutMessage
  case class SelectDeliveryMethod(method: String) extends CheckoutMessage
  case object ReceivedPayment extends CheckoutMessage

  sealed trait State
  case object Unknown extends State
  case object SelectingDelivery extends State
  case object Cancelled extends State
  case object SelectingPaymentMethod extends State
  case object ProcessingPayment extends State
  case object Closed extends State

  sealed trait Data
  case object Uninitialized extends Data
  case class CheckoutData(deliveryMethod: String, paymentMethod: String) extends Data

}

class CheckoutFSM extends Actor with Timers with FSM[State, Data] {

  startWith(Unknown, Uninitialized)

  when(Unknown) {
    case Event(StartCheckout(numberOfItems), Uninitialized) if numberOfItems > 0 =>
      log.debug("Started Checkout: " + numberOfItems)
      goto(SelectingDelivery) using Uninitialized
  }

  when(SelectingDelivery) {
    case Event(SelectDeliveryMethod(delivery), Uninitialized) =>
      log.debug("Selected Delivery: " + delivery)
      goto(SelectingPaymentMethod) using CheckoutData(delivery, null)
  }

  when(SelectingPaymentMethod) {
    case Event(SelectPaymentMethod(payment), CheckoutData(delivery, _)) =>
      log.debug("Selected Payment: " + payment)
      goto(ProcessingPayment) using CheckoutData(delivery, payment)
  }

  when(ProcessingPayment) {
    case Event(ReceivedPayment, CheckoutData(deliveryMethod, paymentMethod)) =>
      log.debug("Received payment")
      timers.cancelAll()
      context.parent ! CloseCheckout(deliveryMethod, paymentMethod)
      stay()
  }

  whenUnhandled {
    case Event(Cancelled(CheckoutTimer), _) =>
      log.debug("CheckoutTimer expired")
      cancel()
      stay
  }

  onTransition {
    case _ -> SelectingDelivery =>
      startTimer(CheckoutTimer, 5)

    case _ -> ProcessingPayment =>
      startTimer(PaymentTimer, 1)

  }

  def startTimer(timer: Object, time: Int): Unit = {
    timers.startSingleTimer(timer, Cancelled(timer), time.seconds)
  }

  def cancel(): Unit = {
    context.parent ! CancelCheckout
    context stop self
  }

}
