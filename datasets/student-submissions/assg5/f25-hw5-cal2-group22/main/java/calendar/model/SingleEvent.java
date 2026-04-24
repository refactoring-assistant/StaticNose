package calendar.model;

/**
 * Represents a single, non-recurring calendar event.
 * Extends AbstractEvent to store standard event
 * details such as subject, start and end times, location,
 * description, and visibility status.
 */
public class SingleEvent extends AbstractEvent {
  /**
   * Creates a new single (non-recurring) event. Assigning default values for description, location
   * and status when creating the event.
   *
   * @param builder the single event builder
   */
  public SingleEvent(SingleEventBuilder builder) {
    super(builder);
  }

  @Override
  public SingleEventBuilder toBuilder() {
    return new SingleEventBuilder().copyFrom(this);
  }

  /**
   * Builder for SingleEvent.
   */

  public static class SingleEventBuilder extends AbstractEventBuilder<SingleEventBuilder> {
    /**
     * Builds the SingleEvent.
     *
     * @return the built SingleEvent
     */
    @Override
    public SingleEvent build() {
      if (subject == null || start == null || end == null) {
        throw new IllegalStateException("Subject, start, and end cannot be null");
      }
      return new SingleEvent(this);
    }

    /**
     * Returns the builder instance.
     *
     * @return the builder
     */
    @Override
    protected SingleEventBuilder returnBuilder() {
      return this;
    }
  }
}
