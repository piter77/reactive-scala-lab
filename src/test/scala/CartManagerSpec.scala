import actors.{CartManager, _}
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class CartManagerSpec extends TestKit(ActorSystem("ShopSystem"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  override def afterAll(): Unit = {
    system.terminate
  }

  val mug =  Item("mug", BigDecimal(20))
  val hat =  Item("hat", BigDecimal(60))
  val bananas =  Item("bananas", BigDecimal(2), 5)


  "A Cart" must {

    "receive confirmation that checkout started" in {
      val cartManager = system.actorOf(Props[CartManager])
      cartManager ! Init
      cartManager ! AddItem(mug)
      cartManager ! StartCheckout
      expectMsg(CheckoutStarted)
    }

    "receive info about empty cart" in {
      val cart = system.actorOf(Props[CartManager])
      cart ! Init
      cart ! AddItem(mug)
      cart ! RemoveItem(mug, 1)
      expectMsg(CartEmpty)

      cart ! AddItem(mug)
      cart ! StartCheckout
      expectMsg(CheckoutStarted)
      cart ! CloseCheckout
      expectMsg(CartEmpty)
    }
  }

}
