package actors

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.routing.FromConfig
import messages.ClientTextEvent
import org.aj.ner.SNerPipeline._

/**
  * EventManager companion object used to construct EventManager ActorRef
  *
  */
object EventManager {

  /**
    * Method is used to construct and return ActorRef object
    *
    * @param pipeline an instance of StanfordCoreNLP, which ecompasses the conditions set for Named Entity Recognition logic.
    * @return
    */
  def props(implicit pipeline: Pipeline) = Props(classOf[EventManager], pipeline)
}

/**
  *  EventManager is a parent for all ClientManager ActorRef(s).
  *  It creates a ClientManager for each user / client id passed.
  *
  * @param pipeline an instance of StanfordCoreNLP, which ecompasses the conditions set for Named Entity Recognition logic.
  */
class EventManager(pipeline: Pipeline) extends Actor with ActorLogging {

  private val nerWorkersRouter: ActorRef = context.actorOf(FromConfig.props(NerWorker.props(pipeline)), "nerWorkersRouter")

  private def getClientManager(id: String): ActorRef = {
    context.child(id).getOrElse {
      val child = context.actorOf(ClientManager.props(nerWorkersRouter), id)
      child
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    log.error(reason, "Restarting due to [{}] when processing [{}]", reason.getMessage, message.getOrElse(""))
  }

  def receive: Receive = {
    case ClientTextEvent(id, text) => {
      try {
        getClientManager(id) ! text
      } catch {
        case e: Throwable => text.promise failure e
      }
    }
    case invalid: Any => {
      log.error("Unexpected message: {}", invalid)
    }
  }

}
