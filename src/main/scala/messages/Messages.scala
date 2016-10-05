package messages

import org.aj.ner.EmbeddedToken
import spray.json.DefaultJsonProtocol
import scala.concurrent.Promise

//type declarations and business domain
case class RequestText(text: String, tagsToCollect: Option[Array[String]] = None)

case class TextEvent(requestText: RequestText, promise: Promise[Set[EmbeddedToken]])

trait ClientEvent

case class ClientTextEvent(id: String, text: TextEvent) extends ClientEvent

//protocols
trait Protocols extends DefaultJsonProtocol {
  implicit val requestTextFormat = jsonFormat2(RequestText.apply)
  implicit val embeddedTokenFormat = jsonFormat2(EmbeddedToken.apply)
}