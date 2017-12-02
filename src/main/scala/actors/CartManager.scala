package actors

import akka.actor.{Timers, _}
import akka.event.{Logging, LoggingReceive}

import scala.concurrent.duration._



case class Item(name: String, price: BigDecimal, count: Int=1){
  override def toString: String = name + " " + price.toString
}

case class Cart(items: Map[String, Item]) {

  def addItem(newItem: Item): Cart = {
    val currentCount = if (items contains newItem.name) items(newItem.name).count else 0
    copy(items = items.updated(newItem.name, newItem.copy(count = currentCount + newItem.count)))
  }

  def removeItem(toDelete: Item, cnt: Int): Cart = {
    if (items(toDelete.name).count <= cnt) {
      copy(items = items - toDelete.name)
    } else {
      val currentCount = items(toDelete.name).count
      copy(items = items.updated(toDelete.name, toDelete.copy(count = currentCount - cnt)))
    }
  }
}

object Cart {
  val empty = Cart(Map.empty)
}


class CartManager(var shoppingCart: Cart) extends Actor with Timers {

  def this() = this(Cart.empty)

  val log = Logging(context.system, this)
  val checkout: ActorRef = context.actorOf(Props[Checkout], "checkout")

  def Empty: Receive = LoggingReceive {

    case AddItem(item: Item) =>
      startCartTimer(5)
      shoppingCart = shoppingCart.addItem(item)
      log.info("Added new item: {}. Current item quantity in cart: {}", item.toString, shoppingCart.items(item.name).count)
      context become NonEmpty

    case Done =>
      log.info("Done")
      context.system.terminate()
  }


  def NonEmpty: Receive = LoggingReceive {

    case AddItem(item: Item) =>
      startCartTimer(5)
      shoppingCart = shoppingCart.addItem(item)
      log.info("Added new item: {}. Current item quantity in cart: {}", item.toString, shoppingCart.items(item.name).count)

    case RemoveItem(item: Item, num: Int) =>
      startCartTimer(5)
      shoppingCart = shoppingCart.removeItem(item, num)
      log.info("Items removed.")
      if (shoppingCart.items.isEmpty) {
        timers.cancel(CartTimer)
        log.info("Removing last item from cart.")
        zeroItemsAndBecomeEmpty()
      }

    case StartCheckout =>
      timers.cancel(CartTimer)
      checkout ! InitCheckout(shoppingCart, context.parent)
      sender ! CheckoutStarted
      log.info("Starting checkout.")
      context become InCheckout

    case CartTimerExpired =>
      log.info("Cart Timer expired. Resetting cart to empty")
      zeroItemsAndBecomeEmpty()
  }



  def InCheckout: Receive = LoggingReceive {

    case CancelCheckout =>
      log.info("Checkout canceled.")
      startCartTimer(5)
      context become NonEmpty

    case CloseCheckout =>
      log.info("Checkout Closed. Resetting cart to Empty")
      zeroItemsAndBecomeEmpty()

  }


  def receive: Receive = LoggingReceive {
    case Init =>
      log.info("Initializing new CartManager with Empty context")
      context become Empty
  }

  def zeroItemsAndBecomeEmpty(): Unit = {
    shoppingCart = Cart.empty
    context become Empty
    sender ! CartEmpty
  }

  def startCartTimer(timeInSeconds: Int): Unit = {
    timers.startSingleTimer(CartTimer, CartTimerExpired, timeInSeconds.seconds)
  }

}