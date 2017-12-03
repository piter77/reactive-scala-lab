package actors

import akka.actor.{Timers, _}
import akka.event.Logging
import akka.persistence.{PersistentActor, RecoveryCompleted}

import scala.concurrent.duration._


// states
sealed trait CartManagerState

case object EmptyState extends CartManagerState

case object NonEmptyState extends CartManagerState

case object InCheckoutState extends CartManagerState

case class CartManagerStateChangeEvent(state: CartManagerState)


sealed trait ItemEvent

case class AddItemEvent(item: Item) extends ItemEvent

case class RemoveItemEvent(item: Item, num: Int) extends ItemEvent

case class EraseItemsEvent() extends ItemEvent


class CartManager(id: String = "sample-id") extends PersistentActor with Timers {

  var state = Cart()

  val log = Logging(context.system, this)
  val checkout: ActorRef = context.actorOf(Props[Checkout], "checkout")

  override def persistenceId = id

  def updateState(event: CartManagerStateChangeEvent): Unit = {
    log.info("Changing context to " + event.state.toString)
    context.become(
      event.state match {
        case EmptyState => Empty
        case NonEmptyState => NonEmpty
        case InCheckoutState => InCheckout
      })
  }

  def updateCart(event: ItemEvent): Unit = {
    startCartTimer(2)
    event match {
      case AddItemEvent(item: Item) =>
        state = state.addItem(item)
      case RemoveItemEvent(item, num) =>
        state = state.removeItem(item, num)
      case EraseItemsEvent() =>
        state = Cart()
    }
  }


  def Empty: Receive = {

    case AddItem(item: Item) =>
      persist(AddItemEvent(item)) {
        event => updateCart(event)
      }
      log.info("Added new item: {}", item.toString)
      persist(CartManagerStateChangeEvent(NonEmptyState)) {
        event => updateState(event)
      }

    case Done =>
      log.info("Done")
      context.system.terminate()

    case CheckState => sender ! CartEmpty

  }


  def NonEmpty: Receive = {

    case AddItem(item: Item) =>
      persist(AddItemEvent(item)) {
        event => updateCart(event)
      }
      log.info("Added new item: {}.", item.toString)

    case RemoveItem(item: Item, num: Int) =>
      persist(RemoveItemEvent(item, num)) {
        event => updateCart(event)
      }

      log.info("Items removed.")
      if (state.items.isEmpty) {
        log.info("Removing last item from cart.")
        zeroItemsAndBecomeEmpty()
      }

    case StartCheckout =>
      log.info("Starting checkout.")
      persist(CartManagerStateChangeEvent(InCheckoutState)) {
        event =>
          updateState(event)
          checkout ! InitCheckout(state, context.parent)
          sender ! CheckoutStarted(checkout)
      }
    case CartTimerExpired =>
      log.info("Cart Timer expired. Resetting cart to empty")
      zeroItemsAndBecomeEmpty()

    case CheckState => sender ! CartNonEmpty

  }


  def InCheckout: Receive = {

    case CancelCheckout =>
      log.info("Checkout canceled.")
      persist(CartManagerStateChangeEvent(NonEmptyState)) {
        event =>
          startCartTimer(2)
          updateState(event)
      }
    case CloseCheckout =>
      log.info("Checkout Closed. Resetting cart to Empty")
      zeroItemsAndBecomeEmpty()
  }


  override def receiveCommand: Receive = {
    case Init =>
      log.info("Initializing new CartManager with Empty context")
      persist(EraseItemsEvent()) {
        event => updateCart(event)
      }
      persist(CartManagerStateChangeEvent(EmptyState)) {
        event => updateState(event)
      }
  }

  override def receiveRecover: Receive = {
    case event: CartManagerStateChangeEvent => updateState(event)
    case event: ItemEvent => updateCart(event)
    case RecoveryCompleted => log.info("Recovery completed!")
  }


  // My methods

  def zeroItemsAndBecomeEmpty(): Unit = {
    persist(EraseItemsEvent()) { event =>
      updateCart(event)
    }
    persist(CartManagerStateChangeEvent(EmptyState)) { event =>
      updateState(event)
      sender ! CartEmpty
    }
  }

  def startCartTimer(timeInSeconds: Int): Unit = {
    timers.startSingleTimer(CartTimer, CartTimerExpired, timeInSeconds.seconds)
  }

}