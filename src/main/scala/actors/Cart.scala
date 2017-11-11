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
      itemCount -= 1
      log.info("Item removed. Current item number: {}", itemCount)
      context become Empty

    case StartCheckout =>
      timers.cancel(CartTimer)
      checkout ! StartCheckout(itemCount, context.parent)
      context.parent ! CheckoutStarted
      log.info("Starting checkout. Current item number: {}", itemCount)
      context become InCheckout

    case CartTimerExpired =>
      itemCount = 0
      log.info("Cart Timer expired. Resetting cart to empty")
      context become Empty
  }


  def InCheckout: Receive = LoggingReceive {

    case CancelCheckout =>
      log.info("Checkout canceled.")
      startCartTimer(5)
      context become NonEmpty

    case CloseCheckout =>
      log.info("Checkout Closed. Resetting cart to Empty")
      itemCount = 0
      context become Empty

    case message: CheckoutMessages =>
      checkout forward message
  }


  def receive: Receive = LoggingReceive {
    case Init =>
      log.info("Initializing new Cart with Empty context")
      context become Empty
  }


  def startCartTimer(timeInSeconds: Int): Unit = {
    timers.startSingleTimer(CartTimer, CartTimerExpired, timeInSeconds.seconds)
  }

}