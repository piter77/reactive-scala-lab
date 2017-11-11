package actors

import akka.actor.{Timers, _}
import akka.event.{Logging, LoggingReceive}

import scala.concurrent.duration._


class Cart extends Actor with Timers {

  val log = Logging(context.system, this)
  var itemCount = 0
  val checkout: ActorRef = context.actorOf(Props[Checkout], "checkout")

  def Empty: Receive = LoggingReceive {

    case AddItem =>
      startCartTimer(5)
      itemCount += 1
      log.info("Added new item. Current item number: {}", itemCount)
      context become NonEmpty

    case Done =>
      log.info("Done")
      context.system.terminate()
  }


  def NonEmpty: Receive = LoggingReceive {

    case AddItem =>
      startCartTimer(5)
      itemCount += 1
      log.info("Added new item. Current item number: {}", itemCount)

    case RemoveItem if itemCount > 1 =>
      startCartTimer(5)
      itemCount -= 1
      log.info("Item removed. Current item number: {}", itemCount)

    case RemoveItem if itemCount == 1 =>
      timers.cancel(CartTimer)
      log.info("Removing last item from cart.")
      zeroItemsAndBecomeEmpty()

    case StartCheckout =>
      timers.cancel(CartTimer)
      checkout ! InitCheckout(itemCount, context.parent)
      sender ! CheckoutStarted
      log.info("Starting checkout. Current item number: {}", itemCount)
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
      log.info("Initializing new Cart with Empty context")
      context become Empty
  }

  def zeroItemsAndBecomeEmpty(): Unit = {
    itemCount = 0
    context become Empty
    sender ! CartEmpty
  }

  def startCartTimer(timeInSeconds: Int): Unit = {
    timers.startSingleTimer(CartTimer, CartTimerExpired, timeInSeconds.seconds)
  }

}