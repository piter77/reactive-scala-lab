package objects

import actors.Item
import akka.actor.ActorRef

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


sealed trait CheckoutState

case object ProcessingPaymentState extends CheckoutState
case object SelectingPaymentMethodState extends CheckoutState
case object SelectingDeliveryState extends CheckoutState
case class CheckoutStateChangeEvent(state: CheckoutState)

sealed trait CheckoutEvent

case class InitializationEvent(customerRef: ActorRef, state: CheckoutState) extends CheckoutEvent
case class SelectedDeliveryEvent(method: String, state: CheckoutState) extends CheckoutEvent
case class SelectedPaymentMethodEvent(method: String, state: CheckoutState) extends CheckoutEvent