package actors

case class Item(name: String, price: BigDecimal, count: Int=1){
  override def toString: String = name + " " + price.toString
}

case class Cart(items: Map[String, Item] = Map.empty) {

  def addItem(newItem: Item): Cart = {
    val currentCount = if (items contains newItem.name) items(newItem.name).count else 0
    copy(items = items.updated(newItem.name, newItem.copy(count = currentCount + newItem.count)))
  }

  def removeItem(toDelete: Item, cnt: Int): Cart = {
    if (items(toDelete.name).count <= cnt) {
      copy(items = items - toDelete.name)
    } else {
      val currentCount = items(toDelete.name).count
      copy(items = items.updated(toDelete.name, toDelete.copy(count = currentCount - cnt)))
    }
  }
}

object Cart {
  val empty = Cart(Map.empty)
}
