import actors.{Cart, Checkout, Item}
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import objects._
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class CheckoutSpec extends TestKit(ActorSystem("ShopSystem"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {
  override def afterAll(): Unit = {
    system.terminate
  }

  "A checkout " must {

    "change states correctly" in {
      val id = "test-checkout-id-1"
      val parent = TestProbe()
      val customerMock = TestProbe()

      val notEmptyCart = Cart.empty.addItem(Item("123", BigDecimal(23), 5))

      val checkout = parent.childActorOf(Props(new Checkout(id)), "checkout-1")

      parent.send(checkout, InitCheckout(notEmptyCart, customerMock.ref))
      checkout ! CheckState
      expectMsg(SelectingDeliveryState)
      checkout ! SelectDeliveryMethod("with post")
      checkout ! CheckState
      expectMsg(SelectingPaymentMethodState)
      checkout ! SelectPaymentMethod("cash")
      checkout ! CheckState
      expectMsg(ProcessingPaymentState)
      checkout ! ReceivedPayment
      parent.expectMsg(CloseCheckout)

    }
  }
}