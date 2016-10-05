//the imports

import actors.{EventBus, EventManager}
import akka.actor.{ActorRef, ActorSystem}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.{Config, ConfigFactory}
import messages._

import scala.concurrent.{ExecutionContextExecutor, Future, Promise}
import org.aj.ner.{EmbeddedToken, SNerPipeline}
import actors.EventBus.Classifier

trait Service extends Protocols {

  implicit val system: ActorSystem
  implicit def executor: ExecutionContextExecutor
  implicit val materializer: Materializer

  def config: Config
  val logger: LoggingAdapter
  val eventManager: ActorRef

  implicit val pipeline = SNerPipeline()
  implicit val eventBus: EventBus = EventBus()

  def subscribe(subscribers: (ActorRef, Classifier)*)(implicit eventBus: EventBus): Unit = subscribers.foreach(r => eventBus.subscribe(r._1, r._2))

  def fetchInfo(id: String, requestText: RequestText): Future[Either[String, Set[EmbeddedToken]]] = {

    val promise = Promise[Set[EmbeddedToken]]()
    val future = promise.future

    eventBus.publish(ClientTextEvent(id, TextEvent(requestText, promise)))

    future map {
      tokens => Right(tokens)
    } recover {
      case e: Throwable => Left(s"${e.getMessage}")
    }

  }

  //routes
  val routes = {

    logRequestResult("akka-http-microservice") {

      get {
        pathPrefix("ner" / Segment) { id =>
          entity(as[RequestText]) { requestText =>
            complete {
              fetchInfo(id, requestText).map[ToResponseMarshallable] {
                case Right(tokens) => tokens
                case Left(errorMessage) => BadRequest -> errorMessage
              }
            }
          }
        }
      }

    }

  }
}

//main App declaration
object AkkaHttpMicroservice extends App with Service {

  override implicit val system = ActorSystem()
  override implicit val executor = system.dispatcher
  override implicit val materializer = ActorMaterializer()

  override val config = ConfigFactory.load()
  override val logger = Logging(system, getClass)

  override val eventManager = system.actorOf(EventManager.props, "eventManager")

  subscribe((eventManager, classOf[ClientEvent]))

  Http().bindAndHandle(routes, config.getString("http.interface"), config.getInt("http.port"))

}
