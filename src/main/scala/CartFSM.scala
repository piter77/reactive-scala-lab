
import CartFSM._
import akka.actor.{Actor, FSM, Timers, _}

import scala.concurrent.duration._

object CartFSM {

  case object CartTimerExpired
  case object CartTimer

  sealed trait CartMessages
  case object RemoveItem extends CartMessages
  case object AddItem extends CartMessages
  case class StartCheckout(numberOfItems: Int) extends CartMessages

  sealed trait MessagesFromCheckout
  case object CancelCheckout extends MessagesFromCheckout
  case class CloseCheckout(deliveryMethod: String, paymentMethod: String) extends MessagesFromCheckout

  sealed trait State
  case object Empty extends State
  case object NonEmpty extends State
  case object InCheckout extends State

  sealed trait Data
  case object Uninitialized extends Data
  case class ItemsNumber(numberOfItems: Int) extends Data
  case class ItemsNumberAndCheckoutRef(numberOfItems: Int, checkout: ActorRef) extends Data

}


class CartFSM extends Actor with Timers with FSM[State, Data] {

  import CartFSM._

  startWith(Empty, ItemsNumber(0))

  when(Empty) {

    case Event(AddItem, ItemsNumber(numberOfItems)) =>
      log.debug("In state: {};  Data: {};", stateName, stateData)
      goto(NonEmpty) using ItemsNumber(numberOfItems + 1)
  }

  when(NonEmpty) {
    case Event(AddItem, ItemsNumber(numberOfItems)) =>
      log.debug("In state: {};  Data: {};", stateName, stateData)
      stay using ItemsNumber(numberOfItems + 1)

    case Event(RemoveItem, ItemsNumber(numberOfItems)) if numberOfItems > 1 =>
      log.debug("In state: {};  Data: {};", stateName, stateData)
      stay using ItemsNumber(numberOfItems - 1)

    case Event(RemoveItem, ItemsNumber(numberOfItems)) if numberOfItems == 1 =>
      log.debug("In state: {};  Data: {};", stateName, stateData)
      goto(Empty) using ItemsNumber(0)

    case Event(StartCheckout, ItemsNumber(numberOfItems)) =>
      log.debug("In state: {};  Data: {};", stateName, stateData)
      val checkoutActor = context.actorOf(Props[CheckoutFSM], "checkout")
      log.debug("Starting next actor: {}", checkoutActor.toString())
      checkoutActor ! StartCheckout(numberOfItems)
      goto(InCheckout) using ItemsNumberAndCheckoutRef(numberOfItems, checkoutActor)

    case Event(CartTimerExpired, _) =>
      goto(Empty) using ItemsNumber(0)

  }

  when(InCheckout) {
    case Event(CancelCheckout, ItemsNumberAndCheckoutRef(numberOfItems, _)) =>
      log.debug("Cancelling checkout and going to NonEmpty state.")
      goto(NonEmpty) using ItemsNumber(numberOfItems)

    case Event(CloseCheckout(_, _), _) =>
      log.debug("Closing checkout and going to Empty state.")
      goto(Empty) using ItemsNumber(0)

    case Event(msg, data@ItemsNumberAndCheckoutRef(numberOfItems, checkoutActorRef)) if numberOfItems > 0 =>
      log.debug("Sending message to Checkout: " + msg.toString)
      checkoutActorRef ! msg
      stay using data
  }

  onTransition {
    case _ -> NonEmpty =>
      startCartTimer()

    case NonEmpty -> _ =>
      timers.cancel(CartTimer)

  }

  def startCartTimer(): Unit = {
    timers.startSingleTimer(CartTimer, CartTimerExpired, 5.seconds)
  }
}