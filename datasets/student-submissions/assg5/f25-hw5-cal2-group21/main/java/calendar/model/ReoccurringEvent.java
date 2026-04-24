package calendar.model;

import calendar.Location;
import calendar.Property;
import calendar.Status;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.security.auth.Subject;

/**
 * Class implementing attributes and methods for ReoccurringEvents.
 */
public class ReoccurringEvent extends SingleEvent {

  protected ReoccurringEvent nextEventInSeries;
  protected ReoccurringEvent previousEventInSeries;

  /**
   * Constructor class for ReoccurringEvent.
   *
   * @param builder ReoccurringEventBuilder used to build the ReoccurringEvent
   */
  private ReoccurringEvent(ReoccurringEventBuilder builder) {
    super(builder);
    this.nextEventInSeries = builder.nextEventInSeries;
    this.previousEventInSeries = builder.previousEventInSeries;
  }

  /**
   * Set method to set next event in series.
   *
   * @param nextEvent event to set
   */
  public void setNextEventInSeries(ReoccurringEvent nextEvent) {
    this.nextEventInSeries = nextEvent;
  }

  @Override
  public Event createEditedCopy(Property property, Object newProperty)
      throws IllegalArgumentException {
    ReoccurringEvent copy = new ReoccurringEventBuilder()
        .addstart(this.start)
        .addEnd(this.end)
        .addSubject(this.subject)
        .build();

    copy.editPropertyOfCopy(property, newProperty);

    return copy;
  }

  @Override
  public void editProperty(Property property, Object newProperty)
      throws IllegalArgumentException {
    switch (property) {
      case START:

        if (this.start.getDate() != ((Date) newProperty).getDate()
            || this.start.getMonth() != ((Date) newProperty).getMonth()
            || this.start.getYear() != ((Date) newProperty).getYear()) {
          throw new IllegalArgumentException("Can not edit reoccurring events to be multiple days");
        }

        if (!this.start.equals(newProperty)) {
          this.start = (Date) newProperty;
          if (this.nextEventInSeries != null) {
            this.nextEventInSeries.setPreviousEventInSeries(this.previousEventInSeries);
          }
          if (this.previousEventInSeries != null) {
            this.previousEventInSeries.setNextEventInSeries(this.nextEventInSeries);
          }
          this.nextEventInSeries = null;
          this.previousEventInSeries = null;
        }
        break;

      case END:
        if (((Date) newProperty).getDate() != this.end.getDate()
            || ((Date) newProperty).getMonth() != this.end.getMonth()
            || ((Date) newProperty).getYear() != this.end.getYear()) {
          throw new IllegalArgumentException("Can not edit reoccurring events to be multiple days");
        }

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
        if (!(newProperty instanceof String)) {
          throw new IllegalArgumentException("New property value not of correct type for "
              + "specified property to change.");
        }

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
  public void editPropertyOfCopy(Property property, Object newProperty)
      throws IllegalArgumentException {
    switch (property) {
      case START:
        this.start = (Date) newProperty;
        break;

      case END:
        this.end = (Date) newProperty;
        break;

      case SUBJECT:
        this.subject = newProperty.toString();
        break;

      default:
        break;
    }
  }

  /**
   * Set method to set previous event in series.
   *
   * @param previousEventInSeries event to set
   */
  void setPreviousEventInSeries(ReoccurringEvent previousEventInSeries) {
    this.previousEventInSeries = previousEventInSeries;
  }

  @Override
  public List<Event> addThisAndAllAfter(List<Event> allAfterInSeries) {
    allAfterInSeries.add(this);
    if (this.nextEventInSeries != null) {
      allAfterInSeries = this.nextEventInSeries.addThisAndAllAfter(allAfterInSeries);
    }
    return allAfterInSeries;
  }

  @Override
  public Event getFirstEventInSeries() {
    if (this.previousEventInSeries != null) {
      return this.previousEventInSeries.getFirstEventInSeries();
    }
    return this;
  }

  @Override
  public Object getCorrectStartProperty(Object newProperty, Date targetStartDate) {
    if (!(newProperty instanceof Date)) {
      throw new IllegalArgumentException("New property value not of correct type for "
          + "specified property to change.");
    }

    Date updatedProperty = (Date) ((Date) newProperty).clone();
    if (updatedProperty.getDate() != targetStartDate.getDate()) {
      throw new IllegalArgumentException("Series event can not be edited to last multiple days");
    }
    updatedProperty.setDate(this.start.getDate());
    updatedProperty.setMonth(this.start.getMonth());
    updatedProperty.setYear(this.start.getYear());
    return updatedProperty;
  }

  @Override
  public Object getCorrectEndProperty(Object newProperty, Date targetEndDate) {
    if (!(newProperty instanceof Date)) {
      throw new IllegalArgumentException("New property value not of correct type for "
          + "specified property to change.");
    }

    Date updatedProperty = (Date) ((Date) newProperty).clone();
    if (updatedProperty.getDate() != targetEndDate.getDate()) {
      throw new IllegalArgumentException("Series event can not be edited to last multiple days");
    }
    updatedProperty.setDate(this.end.getDate());
    updatedProperty.setMonth(this.end.getMonth());
    updatedProperty.setYear(this.end.getYear());
    return updatedProperty;
  }


  /**
   * Builder sub-class implementing the attributes and methods unique to the
   * ReoccurringEventBuilder.
   */
  public static class ReoccurringEventBuilder extends EventBuilder<ReoccurringEventBuilder> {

    protected ReoccurringEvent nextEventInSeries;
    protected ReoccurringEvent previousEventInSeries;

    /**
     * Constructor for ReoccurringEventBuilder sub-class.
     */
    public ReoccurringEventBuilder() {
      super();
      this.nextEventInSeries = null;
      this.previousEventInSeries = null;
    }

    @Override
    ReoccurringEventBuilder returnBuilder() {
      return this;
    }

    /**
     * Calls the constructor of the ReoccurringEvent to build using values specified from the
     * manufactured ReoccurringEventBuilder.
     *
     * @return ReoccurringEvent object with values determined by this ReoccurringEventBuilder
     * @throws IllegalArgumentException when required fields are null, start is after end, or event
     *                                  spans more than one day
     */
    public ReoccurringEvent build() throws IllegalArgumentException {
      if (!this.start.before(this.end)) {
        throw new IllegalArgumentException("A start must be before the event end.");
      }
      if (this.start.getDate() != this.end.getDate()
          || this.start.getMonth() != this.end.getMonth()
          || this.start.getYear() != this.end.getYear()) {
        throw new IllegalArgumentException("Reoccurring event can not span several days.");
      }
      return new ReoccurringEvent(this);
    }

    /**
     * Add an event to the nextEventInSeries field of the ReoccurringEventBuilder.
     *
     * @param event the event to be added
     * @return the modified ReoccurringEventBuilder
     */
    public ReoccurringEventBuilder addNextEventInSeries(ReoccurringEvent event) {
      this.nextEventInSeries = event;
      return this;
    }

    /**
     * Add an event to the previousEventInSeries field of the ReoccurringEventBuilder.
     *
     * @param event the event to be added
     * @return the modified ReoccurringEventBuilder
     */
    public ReoccurringEventBuilder addPreviousEventInSeries(ReoccurringEvent event) {
      this.previousEventInSeries = event;
      return this;
    }
  }
}
