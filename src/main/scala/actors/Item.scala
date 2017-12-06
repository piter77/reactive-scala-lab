package actors

case class Item(name: String, price: BigDecimal, count: Int = 1) {
  override def toString: String = name + " " + price.toString
}
