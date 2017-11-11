import actors.{Cart, _}
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class CartSpec extends TestKit(ActorSystem("ShopSystem"))
  with WordSpecLike with BeforeAndAfterAll  with ImplicitSender{

  override def afterAll(): Unit = {
    system.terminate
  }


  "A Cart" must {
    "increase number of items" in {
      val cart = TestActorRef[Cart]
      cart ! Init
      cart ! AddItem
      assert (cart.underlyingActor.itemCount == 1)
      cart ! AddItem
      assert (cart.underlyingActor.itemCount == 2)

    }

    "decrease number of items" in {
      val cart = TestActorRef[Cart]
      cart ! Init
      cart ! AddItem
      assert (cart.underlyingActor.itemCount == 1)
      cart ! AddItem
      assert (cart.underlyingActor.itemCount == 2)
      cart ! RemoveItem
      assert (cart.underlyingActor.itemCount == 1)
      cart ! RemoveItem
      assert (cart.underlyingActor.itemCount == 0)

    }

    "not go below 0 items" in {
      val cart = TestActorRef[Cart]
      cart ! Init
      cart ! AddItem
      assert (cart.underlyingActor.itemCount == 1)
      cart ! RemoveItem
      assert (cart.underlyingActor.itemCount == 0)
      cart ! RemoveItem
      assert (cart.underlyingActor.itemCount == 0)
      cart ! AddItem
      assert (cart.underlyingActor.itemCount == 1)
    }

    "go back to previous item number when checkout cancelled" in {
      val cart = TestActorRef[Cart]
      cart ! Init
      cart ! AddItem
      assert (cart.underlyingActor.itemCount == 1)
      cart ! StartCheckout
      cart ! CancelCheckout
      assert (cart.underlyingActor.itemCount == 1)
    }

    "erase item number when checkout closed" in {
      val cart = TestActorRef[Cart]
      cart ! Init
      cart ! AddItem
      assert (cart.underlyingActor.itemCount == 1)
      cart ! StartCheckout
      cart ! CloseCheckout
      assert (cart.underlyingActor.itemCount == 0)
    }

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
