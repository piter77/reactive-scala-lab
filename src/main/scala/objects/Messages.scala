package objects

import actors.{Cart, Item}
import akka.actor.ActorRef

//Messages generic for all actors
sealed trait GeneralMessages

//Messages that Customer Actor can handle
sealed trait CustomerMessages

//Messages that Cart Actor can handle
sealed trait CartMessages

//Messages and object of Timers used in App
sealed trait MyTimers

//Messages that Checkout Actor can handle
sealed trait CheckoutMessages

//Messages that PaymentService actor can handle
sealed trait PaymentServiceMessages


case object Init extends GeneralMessages
case object Done extends GeneralMessages

case object NewCart extends CustomerMessages
case object CartEmpty extends CustomerMessages
case object CartNonEmpty extends CustomerMessages
case object ConfirmReceivingPayment extends CustomerMessages
case class CheckoutStarted(checkoutRef: ActorRef) extends CustomerMessages
case class PaymentServiceStarted(paymentRef: ActorRef) extends CustomerMessages


case class RemoveItem(item: Item, num: Int) extends CartMessages
case class AddItem(item: Item) extends CartMessages
case object StartCheckout extends CartMessages
case object CancelCheckout extends CartMessages
case object CloseCheckout extends CartMessages
case object CheckState extends CartMessages


case object CartTimer extends MyTimers
case object CartTimerExpired extends MyTimers
case object CheckoutTimer extends MyTimers
case class Cancel(o: Object) extends MyTimers
case object PaymentTimer extends MyTimers


case class SelectPaymentMethod(paymentMethod: String) extends CheckoutMessages
case class SelectDeliveryMethod(deliveryMethod: String) extends CheckoutMessages
case object ReceivedPayment extends CheckoutMessages
case class InitCheckout(cart: Cart, customer: ActorRef) extends CheckoutMessages

case object DoPayment extends PaymentServiceMessages
