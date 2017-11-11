package actors



//Messages that actors.Cart Actor can handle
sealed trait CartMessages

//Messages for actors.Cart and actors.Checkout actors communication
sealed trait CartAndCheckoutMessages

//Messages and object of Timers used in App
sealed trait MyTimers

//Messages that actors.Checkout Actor can handle
sealed trait CheckoutMessages


case object RemoveItem extends CartMessages
case object AddItem extends CartMessages
case object Init extends CartMessages
case object Done extends CartMessages
case class StartCheckout(bool: Boolean) extends CartMessages

case object CancelCheckout extends CartAndCheckoutMessages
case object CloseCheckout extends CartAndCheckoutMessages


case object CartTimer extends MyTimers
case object CartTimerExpired extends MyTimers
case object CheckoutTimer extends MyTimers
case class Cancel(o: Object) extends MyTimers
case object PaymentTimer extends MyTimers


case object SelectPayment extends CheckoutMessages
case object SelectDeliveryMethod extends CheckoutMessages
case object ReceivePayment extends CheckoutMessages