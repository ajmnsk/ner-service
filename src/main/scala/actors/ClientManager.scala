package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import messages.TextEvent
import org.aj.ner.{EmbeddedToken, SNer}
import org.aj.ner.SNerPipeline.Pipeline

/**
  * ClientManager companion object used to construct ClientManager ActorRef
  *
  */
object ClientManager {

  def props(nerWorkersRouter: ActorRef) = Props(classOf[ClientManager], nerWorkersRouter)
}

/**
  * ClientManager ActorRef would normally be created on per user / client id basis.
  * It is taking router to a pool of NerWorker actors
  *
  * @param nerWorkersRouter router to a NerWorker(s) pool.
  */
class ClientManager(nerWorkersRouter: ActorRef) extends Actor with ActorLogging {

  override def preRestart(reason: Throwable, message: Option[Any]) {
    log.error(reason, "Restarting due to [{}] when processing [{}]", reason.getMessage, message.getOrElse(""))
  }

  def receive: Receive = {

    case msg: TextEvent => {
      try {
        nerWorkersRouter ! msg
      } catch {
        case e: Throwable => msg.promise failure e
      }
    }

    case invalid: Any => {
      log.error("Unexpected message: {}", invalid)
    }
  }

}
