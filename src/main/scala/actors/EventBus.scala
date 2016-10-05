package actors

import akka.event.{ActorEventBus, SubchannelClassification}
import akka.util.Subclassification
import messages.ClientEvent

/**
  * An EventBus companion object
  */
object EventBus {

  type Classifier = Class[_ <: ClientEvent]
  type Event = ClientEvent

  /**
    * Gets an EventBus instance.
    *
    * @return An EventBus instance.
    */
  def apply() = new EventBus
}

/**
  * An EventBus implementation which uses a class based lookup classification.
  * Events to be executed are published onto bus.
  *
  */
class EventBus extends ActorEventBus with SubchannelClassification {

  override type Classifier = EventBus.Classifier
  override type Event = EventBus.Event

  /**
    * The logic to form sub-class hierarchy
    */
  override protected implicit val subclassification = new Subclassification[Classifier] {
    def isEqual(x: Classifier, y: Classifier): Boolean = x == y
    def isSubclass(x: Classifier, y: Classifier): Boolean = y.isAssignableFrom(x)
  }

  /**
    * Publishes the given Event to the given Subscriber.
    *
    * @param event The Event to publish.
    * @param subscriber The Subscriber to which the Event should be published.
    */
  override protected def publish(event: Event, subscriber: Subscriber): Unit = subscriber ! event

  /**
    * Returns the Classifier associated with the given Event.
    *
    * @param event The event for which the Classifier should be returned.
    * @return The Classifier for the given Event.
    */
  override protected def classify(event: Event): Classifier = event.getClass
}
