import actors.EventManager
import akka.event.NoLogging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import messages.{ClientEvent, RequestText}
import org.aj.ner.EmbeddedToken
import org.scalatest._
import akka.testkit.TestActorRef

class ServiceSpec extends FlatSpec with Matchers with ScalatestRouteTest with Service {

  override def testConfigSource = "akka.loglevel = WARNING"
  override def config = testConfig
  override val logger = NoLogging

  override val eventManager = TestActorRef(EventManager.props, "eventManager")

  subscribe((eventManager, classOf[ClientEvent]))

  val requestText = RequestText(
    "Marcora doesn't even have to talk in his mother tongue to spark a reaction: In his adopted hometown of Chatham in Kent, southeast of London, just speaking English with an Italian accent can be enough to provoke a reaction. This is post-Brexit referendum Britain. And it's a place Marcora, who has lived and worked in the UK for 18 years, barely recognizes.",
    Some(Array("MISC"))
  )

  val tokens = Set(
      EmbeddedToken("MISC", "English"),
      EmbeddedToken("MISC", "Italian"),
      EmbeddedToken("MISC", "post-Brexit")
  )

  "Service" should "respond to query with MISC tokens only" in {
    Get("/ner/aj", requestText) ~> routes ~> check {
      status shouldBe OK
      contentType shouldBe `application/json`
      responseAs[Set[EmbeddedToken]] shouldBe tokens
    }
  }

}
