import actors._
import akka.actor._

import scala.concurrent.Await
import scala.concurrent.duration._

object ShopApp extends App {
  val shopSystem = ActorSystem("ShopSystem")
  val cartManager = shopSystem.actorOf(Props[CartManager], "CartManager")

  val customer = shopSystem.actorOf(Props[Customer], "customer")

  customer ! NewCart

  customer ! AddItem(Item("mug", BigDecimal(20)))
  customer ! AddItem(Item("mug", BigDecimal(20)))

  customer ! StartCheckout

  customer ! SelectDeliveryMethod("FastAndFurious Courier s.a.")
  customer ! SelectPaymentMethod("On receive")

  Await.result(shopSystem.whenTerminated, Duration.Inf)
}