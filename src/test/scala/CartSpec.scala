import actors._
import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class CartSpec extends TestKit(ActorSystem("ShopSystem"))
  with WordSpecLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.terminate
  }

  val mug =  Item("mug", BigDecimal(20))
  val hat =  Item("hat", BigDecimal(60))
  val bananas =  Item("bananas", BigDecimal(2), 5)

  "A Cart" must {
    "increase number of items" in {
      var cart = Cart.empty

      cart = cart.addItem(bananas)
      cart = cart.addItem(hat)
      cart = cart.addItem(hat)
      cart = cart.addItem(hat)

      assert(cart.items("hat").count == 3)
      assert(cart.items("bananas").count == 5)
      assert(! (cart.items contains "mug"))
    }

    "decrease number of items" in {
      var cart = Cart.empty

      cart = cart.addItem(hat)
      cart = cart.addItem(hat)
      cart = cart.addItem(hat)
      cart = cart.addItem(hat)
      assert(cart.items("hat").count == 4)

      cart = cart.removeItem(hat, 1)
      assert(cart.items("hat").count == 3)

      cart = cart.removeItem(hat, 2)
      assert(cart.items("hat").count == 1)

      cart = cart.removeItem(hat, 1)
      assert(! (cart.items contains "hat"))
    }

    "not go below 0 items" in {
      var cart = Cart.empty

      cart = cart.addItem(mug)
      cart = cart.addItem(mug)
      cart = cart.removeItem(mug, 3)
      assert(! (cart.items contains "mug"))

      cart = cart.addItem(mug)
      assert(cart.items("mug").count == 1)
    }

  }
}
