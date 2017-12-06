import actors.{CartManager, _}
import akka.actor.{ActorSystem, PoisonPill, Props}
import akka.testkit.{ImplicitSender, TestKit}
import objects._
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class CartManagerSpec extends TestKit(ActorSystem("ShopSystem"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  override def afterAll(): Unit = {
    system.terminate
  }

  val mug = Item("mug", BigDecimal(20))

  "A Cart Manager" must {

    "receive confirmation that checkout started" in {
      val id = "test-id-01"
      val cartManager = system.actorOf(Props(new CartManager(id)), "manager-11")
      cartManager ! Init
      cartManager ! AddItem(mug)
      cartManager ! StartCheckout
      expectMsgType[CheckoutStarted]
    }

    "Handle timeouts" in {
      val id = "test-id-02"
      val cartManager = system.actorOf(Props(new CartManager(id)), "manager-21")
      cartManager ! Init
      cartManager ! AddItem(mug)
      cartManager ! CheckState
      expectMsg(CartNonEmpty)

      Thread.sleep(4 * 1000)

      cartManager ! CheckState
      expectMsg(CartEmpty)

    }

    "Continue transaction after restart" in {
      val id = "test-id-03"
      val cartManager = system.actorOf(Props(new CartManager(id)), "manager-31")
      cartManager ! Init
      cartManager ! AddItem(mug)
      cartManager ! AddItem(mug)
      cartManager ! CheckState

      expectMsg(CartNonEmpty)

      cartManager ! PoisonPill

      val cartManager2 = system.actorOf(Props(new CartManager(id)), "manager-41")
      cartManager2 ! CheckState
      expectMsg(CartNonEmpty)

      Thread.sleep(4 * 1000)

      cartManager2 ! CheckState
      expectMsg(CartEmpty)

    }
  }
}
