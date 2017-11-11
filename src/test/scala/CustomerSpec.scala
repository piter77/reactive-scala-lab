import actors._
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class CustomerSpec extends TestKit(ActorSystem("ShopSystem"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  override def afterAll(): Unit = {
    system.terminate
  }

  "A checkout " must {

    "receive message about closed checkout" in {
      val parent = TestProbe()

      val customerMock = TestProbe()
      val checkout = parent.childActorOf(Props[Checkout])
      parent.send(checkout, InitCheckout(1, customerMock.ref))
      checkout ! SelectDeliveryMethod("with post")
      checkout ! SelectPaymentMethod("cash")
      checkout ! ReceivedPayment
      parent.expectMsg(CloseCheckout)

    }
  }
}
