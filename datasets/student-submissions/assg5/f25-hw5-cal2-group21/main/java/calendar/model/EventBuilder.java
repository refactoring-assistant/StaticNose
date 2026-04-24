package calendar.model;

import calendar.Location;
import calendar.Status;
import java.util.Date;

/**
 * Builder class of Event.
 *
 * @param <T> The type of event builder being used
 */
abstract class EventBuilder<T extends EventBuilder<T>> {

  protected String subject;
  protected Date start;
  protected Date end;
  protected String description;
  protected Location location;
  protected Status status;
  protected Event nextEvent;
  protected Event previousEvent;

  /**
   * Constructor class of EventBuilder.
   */
  public EventBuilder() {
    this.subject = null;
    this.start = null;
    this.end = null;
    this.description = null;
    this.location = null;
    this.status = null;
    this.nextEvent = null;
    this.previousEvent = null;
  }

  /**
   * returns the correct EventBuilder based on which type called the shared method.
   *
   * @return EventBuilder of type T
   */
  abstract T returnBuilder();

  /**
   * Modifies the subject field of the EventBuilder.
   *
   * @param subject subject to save in the field
   * @return EventBuilder of correct type with modified field
   */
  public T addSubject(String subject) {
    this.subject = subject;
    return returnBuilder();
  }

  /**
   * Modifies the start field of the EventBuilder.
   *
   * @param start date to save in the field
   * @return EventBuilder of correct type with modified field
   */
  public T addstart(Date start) {
    this.start = start;
    return returnBuilder();
  }

  /**
   * Modifies the end field of the EventBuilder.
   *
   * @param end date to save in the field
   * @return EventBuilder of correct type with modified field
   */
  public T addEnd(Date end) {
    this.end = end;
    return returnBuilder();
  }

  /**
   * Modifies the description field of the EventBuilder.
   *
   * @param description string to save in the field
   * @return EventBuilder of correct type with modified field
   */
  public T addDescription(String description) {
    this.description = description;
    return returnBuilder();
  }

  /**
   * Modifies the location field of the EventBuilder.
   *
   * @param location location to save in the field
   * @return EventBuilder of correct type with modified field
   */
  public T addLocation(Location location) {
    this.location = location;
    return returnBuilder();
  }

  /**
   * Modifies the status field of the EventBuilder.
   *
   * @param status status to save in the field
   * @return EventBuilder of correct type with modified field
   */
  public T addStatus(Status status) {
    this.status = status;
    return returnBuilder();
  }

  /**
   * Modifies the nextEvent field of the EventBuilder.
   *
   * @param nextEvent event to save in the field
   * @return EventBuilder of correct type with modified field
   */
  public T addNextEvent(Event nextEvent) {
    this.nextEvent = nextEvent;
    return returnBuilder();
  }

  /**
   * Modifies the previousEvent field of the EventBuilder.
   *
   * @param previousEvent event to save in the field
   * @return EventBuilder of correct type with modified field
   */
  public T addPrevEvent(Event previousEvent) {
    this.previousEvent = previousEvent;
    return returnBuilder();
  }


}
