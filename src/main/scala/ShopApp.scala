import akka.actor._

import scala.concurrent.Await
import scala.concurrent.duration._

object ShopApp extends App {
  val shopSystem = ActorSystem("ShopApp")
  val cart = shopSystem.actorOf(Props[Cart], "Cart")

  val cartFSM = shopSystem.actorOf(Props[CartFSM], "cartFSM")


//    testFSM()
    testFSM2()

  def testFSM(): Unit = {
    cartFSM ! CartFSM.AddItem
    cartFSM ! CartFSM.AddItem
    cartFSM ! CartFSM.RemoveItem
    cartFSM ! CartFSM.RemoveItem
    cartFSM ! CartFSM.AddItem
    cartFSM ! CartFSM.StartCheckout

  }

  def testFSM2(): Unit = {
    cartFSM ! CartFSM.AddItem
    cartFSM ! CartFSM.AddItem
    cartFSM ! CartFSM.RemoveItem
    cartFSM ! CartFSM.RemoveItem
    cartFSM ! CartFSM.AddItem
    cartFSM ! CartFSM.StartCheckout
    cartFSM ! CheckoutFSM.SelectDeliveryMethod("method")
    cartFSM ! CheckoutFSM.SelectPaymentMethod("payment")
    cartFSM ! CheckoutFSM.ReceivedPayment

  }

  //test1()

  def test1(): Unit = {

    cart ! Cart.Init
    cart ! Cart.AddItem
    cart ! Cart.AddItem

    cart ! Cart.StartCheckout
    checkoutMessages()

  }

  def test2(): Unit = {

    cart ! Cart.Init
    cart ! Cart.AddItem
    cart ! Cart.AddItem
    cart ! Cart.StartCheckout
    checkoutMessages2()

  }


  def checkoutMessages(): Unit = {
    cart ! Checkout.SelectDeliveryMethod
  }

  def checkoutMessages2(): Unit = {
    cart ! Checkout.SelectDeliveryMethod
    cart ! Checkout.SelectPayment
    cart ! Checkout.ReceivePayment
  }


  Await.result(shopSystem.whenTerminated, Duration.Inf)
}