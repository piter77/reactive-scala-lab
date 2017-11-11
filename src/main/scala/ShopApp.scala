import actors._
import akka.actor._
import fsmactors.{CartFSM, CheckoutFSM}

import scala.concurrent.Await
import scala.concurrent.duration._

object ShopApp extends App {
  val shopSystem = ActorSystem("ShopApp")
  val cart = shopSystem.actorOf(Props[Cart], "Cart")
  val cartFSM = shopSystem.actorOf(Props[CartFSM], "cartFSM")

  val customer = shopSystem.actorOf(Props[Customer], "customer")

  customer ! NewCart
  //customer: Shopping, cart: Empty
  customer ! AddItem
  //customer: Shopping, cart: NonEmpty
  customer ! AddItem
  customer ! RemoveItem
  customer ! RemoveItem
  //customer: Shopping, cart: Empty
  customer ! AddItem
  //customer: Shopping, cart: NonEmpty
  customer ! StartCheckout
  //customer: Finalizing, cart: InCheckout
  customer ! SelectDeliveryMethod("FastAndFurious Courier s.a.")
  customer ! SelectPaymentMethod("On receive")



  //    testFSM()
  //    testFSM2()
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

  def test1(): Unit = {
    cart ! Init
    cart ! AddItem
    cart ! AddItem
    cart ! StartCheckout
    checkoutMessages()
  }

  def test2(): Unit = {
    cart ! Init
    cart ! AddItem
    cart ! AddItem
    cart ! StartCheckout
    checkoutMessages2()
  }

  def checkoutMessages(): Unit = {
    cart ! SelectDeliveryMethod
  }

  def checkoutMessages2(): Unit = {
    cart ! SelectDeliveryMethod
    cart ! SelectPaymentMethod
    cart ! ReceivedPayment
  }

  Await.result(shopSystem.whenTerminated, Duration.Inf)
}