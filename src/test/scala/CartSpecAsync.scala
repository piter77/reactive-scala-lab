import actors.{Cart, _}
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class CartSpecAsync extends TestKit(ActorSystem("ShopSystem"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  override def afterAll(): Unit = {
    system.terminate
  }


  "A Cart" must {

    "receive confirmation that checkout started" in {
      val cart = system.actorOf(Props[Cart])
      cart ! Init
      cart ! AddItem
      cart ! StartCheckout
      expectMsg(CheckoutStarted)
    }

    "receive info about empty cart" in {
      val cart = system.actorOf(Props[Cart])
      cart ! Init
      cart ! AddItem
      cart ! RemoveItem
      expectMsg(CartEmpty)

      cart ! AddItem
      cart ! StartCheckout
      expectMsg(CheckoutStarted)
      cart ! CloseCheckout
      expectMsg(CartEmpty)
    }
  }

}
