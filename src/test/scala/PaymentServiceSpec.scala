import actors.PaymentService
import akka.actor.{ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit, TestProbe}
import objects._
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class PaymentServiceSpec extends TestKit(ActorSystem("ShopSystem"))
  with WordSpecLike with BeforeAndAfterAll with ImplicitSender {

  override def afterAll(): Unit = {
    system.terminate
  }

  "A payment service " must {

    "proceed payment with payu" in {
      val parent = TestProbe()
      val paymentService = parent.childActorOf(Props[PaymentService], "payment-1")

      parent.send(paymentService, DoPayment("PayU"))
      parent.expectMsg(ReceivedPayment)

    }

    "proceed payment with visa" in {
      val parent = TestProbe()
      val paymentService = parent.childActorOf(Props[PaymentService], "payment-2")

      parent.send(paymentService, DoPayment("VISA"))
      parent.expectMsg(ReceivedPayment)
    }

    "proceed payment with bad type" in {
      val parent = TestProbe()
      val paymentService = parent.childActorOf(Props[PaymentService], "payment-3")

      parent.send(paymentService, DoPayment("LOLZ"))
      parent.expectMsg(NotReceivedPayment)
    }
  }
}



