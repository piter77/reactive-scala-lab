import actors.{Cart, _}
import akka.actor.ActorSystem
import akka.testkit.{TestActorRef, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class CartSpecSync extends TestKit(ActorSystem("ShopSystem"))
  with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate
  }


  "A Cart" must {
    "increase number of items" in {
      val cart = TestActorRef[Cart]
      cart ! Init
      cart ! AddItem
      assert(cart.underlyingActor.itemCount == 1)
      cart ! AddItem
      assert(cart.underlyingActor.itemCount == 2)

    }

    "decrease number of items" in {
      val cart = TestActorRef[Cart]
      cart ! Init
      cart ! AddItem
      assert(cart.underlyingActor.itemCount == 1)
      cart ! AddItem
      assert(cart.underlyingActor.itemCount == 2)
      cart ! RemoveItem
      assert(cart.underlyingActor.itemCount == 1)
      cart ! RemoveItem
      assert(cart.underlyingActor.itemCount == 0)

    }

    "not go below 0 items" in {
      val cart = TestActorRef[Cart]
      cart ! Init
      cart ! AddItem
      assert(cart.underlyingActor.itemCount == 1)
      cart ! RemoveItem
      assert(cart.underlyingActor.itemCount == 0)
      cart ! RemoveItem
      assert(cart.underlyingActor.itemCount == 0)
      cart ! AddItem
      assert(cart.underlyingActor.itemCount == 1)
    }

    "go back to previous item number when checkout cancelled" in {
      val cart = TestActorRef[Cart]
      cart ! Init
      cart ! AddItem
      assert(cart.underlyingActor.itemCount == 1)
      cart ! StartCheckout
      cart ! CancelCheckout
      assert(cart.underlyingActor.itemCount == 1)
    }

    "erase item number when checkout closed" in {
      val cart = TestActorRef[Cart]
      cart ! Init
      cart ! AddItem
      assert(cart.underlyingActor.itemCount == 1)
      cart ! StartCheckout
      cart ! CloseCheckout
      assert(cart.underlyingActor.itemCount == 0)
    }
  }

}
