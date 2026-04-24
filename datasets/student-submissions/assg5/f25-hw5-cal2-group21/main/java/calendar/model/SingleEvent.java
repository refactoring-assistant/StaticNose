package calendar.model;

import calendar.Location;
import calendar.Property;
import calendar.Status;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Class implementing attributes and methods for SingleEvents.
 */
public class SingleEvent implements Event {

  protected String subject;
  protected Date start;
  protected Date end;
  protected String description;
  protected Location location;
  protected Status status;
  protected Event nextEvent;
  protected Event previousEvent;

  /**
   * Constructor class of SingleEvent.
   *
   * @param builder the SingleEventBuilder or ReoccurringEventBuilder used to build the SingleEvent
   */
  SingleEvent(EventBuilder<?> builder) {
    this.subject = builder.subject;
    this.start = builder.start;
    this.end = builder.end;
    this.description = builder.description;
    this.location = builder.location;
    this.status = builder.status;
    this.nextEvent = builder.nextEvent;
    this.previousEvent = builder.previousEvent;
  }

  @Override
  public Event getNextEvent() {
    return this.nextEvent;
  }

  @Override
  public void setNextEvent(Event newEvent) {
    this.nextEvent = newEvent;
  }

  @Override
  public void setPreviousEvent(Event currentEvent) {
    this.previousEvent = currentEvent;
  }

  @Override
  public Event createEditedCopy(Property property, Object newProperty)
      throws IllegalArgumentException {
    SingleEvent copy = new SingleEventBuilder()
        .addstart(this.start)
        .addEnd(this.end)
        .addSubject(this.subject)
        .build();

    copy.editPropertyOfCopy(property, newProperty);

    return copy;
  }

  @Override
  public Date getStart() {
    return this.start;
  }

  @Override
  public String getSubject() {
    return this.subject;
  }

  @Override
  public String getDescription() {
    return this.description;
  }

  @Override
  public Location getLocation() {
    return this.location;
  }

  @Override
  public Status getStatus() {
    return this.status;
  }

  @Override
  public Date getEnd() {
    return this.end;
  }

  @Override
  public Event getPreviousEvent() {
    return this.previousEvent;
  }

  @Override
  public void editProperty(Property property, Object newProperty) throws IllegalArgumentException {
    switch (property) {
      case START:

        this.start = (Date) newProperty;
        break;

      case END:

        this.end = (Date) newProperty;
        break;

      case STATUS:
        if (!(newProperty instanceof Status)) {
          throw new IllegalArgumentException("New property value not of correct type for "
              + "specified property to change.");
        }

        this.status = (Status) newProperty;
        break;

      case SUBJECT:


        this.subject = newProperty.toString();
        break;

      case LOCATION:
        if (!(newProperty instanceof Location)) {
          throw new IllegalArgumentException("New property value not of correct type for "
              + "specified property to change.");
        }

        this.location = (Location) newProperty;
        break;

      default:
        if (!(newProperty instanceof String)) {
          throw new IllegalArgumentException("New property value not of correct type for "
              + "specified property to change.");
        }

        this.description = newProperty.toString();
        break;
    }
  }

  @Override
  public void editPropertyOfCopy(Property property, Object newProperty) {
    switch (property) {
      case START:
        if (!(newProperty instanceof Date)) {
          throw new IllegalArgumentException("New property value not of correct type for "
              + "specified property to change.");
        }
        this.start = (Date) newProperty;
        break;

      case END:
        if (!(newProperty instanceof Date)) {
          throw new IllegalArgumentException("New property value not of correct type for "
              + "specified property to change.");
        }
        this.end = (Date) newProperty;
        break;

      case SUBJECT:
        if (!(newProperty instanceof String)) {
          throw new IllegalArgumentException("New property value not of correct type for "
              + "specified property to change.");
        }
        this.subject = (String) newProperty;
        break;

      default:
        break;
    }
  }

  @Override
  public List<Event> addThisAndAllAfter(List<Event> allAfterInSeries) {
    allAfterInSeries.add(this);
    return allAfterInSeries;
  }

  @Override
  public Event getFirstEventInSeries() {
    return this;
  }

  @Override
  public Object getCorrectStartProperty(Object newProperty, Date targetStartDate) {
    return newProperty;
  }

  @Override
  public Object getCorrectEndProperty(Object newProperty, Date targetEndDate) {
    return newProperty;
  }


  /**
   * Builder sub-class implementing the attributes and methods unique to the SingleEventBuilder.
   */
  public static class SingleEventBuilder extends EventBuilder<SingleEventBuilder> {

    /**
     * Constructor for SingleEventBuilder sub-class.
     */
    public SingleEventBuilder() {
      super();
    }

    @Override
    protected SingleEventBuilder returnBuilder() {
      return this;
    }

    /**
     * Calls the constructor of the SingleEvent to build using values specified from the
     * manufactured SingleEventBuilder.
     *
     * @return SingleEvent object with values determined by this SingleEventBuilder
     * @throws IllegalArgumentException when required fields are null, start is after end, or event
     *                                  spans more than one day
     */
    public SingleEvent build() throws IllegalArgumentException {
      if (this.start == null || this.subject == null || this.end == null) {
        throw new IllegalArgumentException("An event is required to have a subject, "
            + "start, and end.");
      }
      if (!this.start.before(this.end)) {
        throw new IllegalArgumentException("An start must be before the event end.");
      }
      return new SingleEvent(this);
    }


  }


}
