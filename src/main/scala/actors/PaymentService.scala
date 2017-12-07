package actors

import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.stream.{ActorMaterializer, ActorMaterializerSettings}
import akka.util.ByteString
import objects.{ConfirmReceivingPayment, DoPayment, NotReceivedPayment, ReceivedPayment}

import scala.concurrent.Await
import scala.concurrent.duration.Duration


class PaymentService extends Actor with ActorLogging {

  import akka.pattern.pipe
  import context.dispatcher

  final implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(context.system))

  val http = Http(context.system)

  def receive: Receive = LoggingReceive {
    case DoPayment(method) =>
      method match {
        case "VISA" =>
          http.singleRequest(HttpRequest(uri = "http://localhost:8080/visa"))
            .pipeTo(self)
        case "PayU" =>
          http.singleRequest(HttpRequest(uri = "http://localhost:8080/payu"))
            .pipeTo(self)
        case _ =>
          http.singleRequest(HttpRequest(uri = "http://localhost:8080/fake"))
            .pipeTo(self)
      }
      context become WaitForResponse
  }

  def WaitForResponse: Receive = {

    case resp@HttpResponse(StatusCodes.OK, headers, entity, _) =>
      entity.dataBytes.runFold(ByteString(""))(_ ++ _).foreach { body =>
        println("Got response, body: " + body.utf8String)
        resp.discardEntityBytes()
        shutdown()
      }
    case resp@HttpResponse(code, _, _, _) =>
      println("Request failed, response code: " + code)
      resp.discardEntityBytes()
      shutdownWithFailure()

  }

  def shutdown() = {
    Await.result(http.shutdownAllConnectionPools(), Duration.Inf)

    sender ! ConfirmReceivingPayment
    context.parent ! ReceivedPayment
    context stop self
  }
  def shutdownWithFailure() = {
    Await.result(http.shutdownAllConnectionPools(), Duration.Inf)

    sender ! ConfirmReceivingPayment
    context.parent ! NotReceivedPayment
    context stop self
  }
}