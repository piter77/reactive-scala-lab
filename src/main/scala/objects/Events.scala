package objects

import actors.Item

// states
sealed trait CartManagerState

case object EmptyState extends CartManagerState
case object NonEmptyState extends CartManagerState
case object InCheckoutState extends CartManagerState
case class CartManagerStateChangeEvent(state: CartManagerState)


sealed trait ItemEvent

case class AddItemEvent(item: Item) extends ItemEvent
case class RemoveItemEvent(item: Item, num: Int) extends ItemEvent
case class EraseItemsEvent() extends ItemEvent

