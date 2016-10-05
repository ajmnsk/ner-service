package actors

import akka.actor.{Actor, ActorLogging, Props}
import org.aj.ner.{EmbeddedToken, SNer}
import messages.TextEvent
import org.aj.ner.SNerPipeline._

/**
  * NerWorker companion object used to construct NerWorker ActorRef
  *
  */
object NerWorker {
  def props(pipeline: Pipeline) = Props(classOf[NerWorker], pipeline)
}

/**
  * Actor to process Text and return a collection of Tokens if any
  *
  * @param pipeline an instance of StanfordCoreNLP, which ecompasses the conditions set for Named Entity Recognition logic.
  */
class NerWorker(pipeline: Pipeline) extends Actor with ActorLogging {

  //function object instance
  private val processText: (String, Option[Array[String]]) => Set[EmbeddedToken] = (text: String, tagsToCollect: Option[Array[String]]) => {
    tagsToCollect match {
      case Some(array) =>
        if (array.length < 1) SNer().processString(pipeline, text)
        else SNer().processString(pipeline, text, array.toSet)
      case _ =>  SNer().processString(pipeline, text)
    }
  }

  override def preRestart(reason: Throwable, message: Option[Any]) {
    log.error(reason, "Restarting due to [{}] when processing [{}]", reason.getMessage, message.getOrElse(""))
  }

  def receive: Receive = {

    // results of the call are assigned to the promise
    case TextEvent(requestText, promise) => {
      try {
        promise success processText(requestText.text, requestText.tagsToCollect)
      } catch {
        //
        case e: Throwable => promise failure e
      }
    }

    case invalid: Any => {
      log.error("Unexpected message: {}", invalid)
    }
  }

}
