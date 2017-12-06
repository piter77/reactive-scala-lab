package actors

import akka.actor.Actor
import akka.event.LoggingReceive
import objects.{ConfirmReceivingPayment, DoPayment, ReceivedPayment}

class PaymentService extends Actor {
  def receive: Receive = LoggingReceive {
    case DoPayment =>
      sender ! ConfirmReceivingPayment
      context.parent ! ReceivedPayment
      context stop self
  }
}
