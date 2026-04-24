package calendar.model;

import calendar.model.utils.EventStatus;

/**
 * Abstract base class for calendar events, implementing the Event interface
 * and holding common properties AND common builder logic.
 */
public abstract class AbstractEvent implements Event {
  protected final String subject;
  protected final String description;
  protected final String location;
  protected final EventStatus status;

  /**
   * Constructor for common event properties.
   */
  protected AbstractEvent(String subject, String description, String location, EventStatus status) {
    if (subject == null || subject.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject cannot be null or empty.");
    }
    this.subject = subject;
    this.description = description;
    this.location = location;
    this.status = (status != null) ? status : EventStatus.PUBLIC;
  }

  @Override
  public String getSubject() {
    return subject;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getLocation() {
    return location;
  }

  @Override
  public EventStatus getStatus() {
    return status;
  }

  /**
   * Abstract Builder to handle common event properties.
   * Concrete builders (EventSingle.Builder, EventSeries.Builder) will extend this.
   * We use generics to allow method chaining.
   */
  public abstract static class AbstractBuilder<T extends AbstractBuilder<T>> {
    protected String subject;
    protected String description = null;
    protected String location = null;
    protected EventStatus status = EventStatus.PUBLIC;

    /**
     * Sets the subject for the event.
     *
     * @param subject The event subject.
     * @return This builder instance for chaining.
     */
    public T withSubject(String subject) {
      this.subject = subject;
      return (T) this;
    }

    /**
     * Sets the description for the event.
     *
     * @param description The event description.
     * @return This builder instance for chaining.
     */
    public T withDescription(String description) {
      this.description = description;
      return (T) this;
    }

    /**
     * Sets the location for the event.
     *
     * @param location The event location.
     * @return This builder instance for chaining.
     */
    public T withLocation(String location) {
      this.location = location;
      return (T) this;
    }

    /**
     * Sets the status for the event.
     *
     * @param status The event status (e.g., PUBLIC or PRIVATE).
     * @return This builder instance for chaining.
     */
    public T withStatus(EventStatus status) {
      this.status = status;
      return (T) this;
    }
  }
}