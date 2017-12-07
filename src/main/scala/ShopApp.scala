import actors._
import akka.actor._
import objects._

import scala.concurrent.Await
import scala.concurrent.duration._

object ShopApp extends App {
  val shopSystem = ActorSystem("ShopSystem")

  val id = "test-checkout-id-2"
  val notEmptyCart = Cart.empty.addItem(Item("123", BigDecimal(23), 5))

  val checkout = shopSystem.actorOf(Props(new Checkout(id)), "checkout-1")
  val customer = shopSystem.actorOf(Props[Customer], "customer-1")


  checkout ! InitCheckout(notEmptyCart, customer)
  checkout ! SelectDeliveryMethod("with post")
  checkout ! SelectPaymentMethod("cash")

  shopSystem.terminate



  Await.result(shopSystem.whenTerminated, Duration.Inf)
}