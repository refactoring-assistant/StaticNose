package calendar.model;

/**
 * Represents a single occurrence within a recurring event series.
 * Extends AbstractEvent and adds a seriesId field
 * to identify which series this instance belongs to.
 */
public class EventInstance extends AbstractEvent {
  private String seriesId;


  /**
   * Creates a new event instance.
   *
   * @param builder the event instance builder
   */
  public EventInstance(EventInstanceBuilder builder) {
    super(builder);
    this.seriesId = builder.seriesId;
  }

  /**
   * Method which returns series id.
   *
   * @return the series ID of this recurring event instance
   */
  public String getSeriesId() {
    return seriesId;
  }


  /**
   * Assign a new series id (used when splitting a series on start-time edits).
   */
  public void setSeriesId(String newSeriesId) {
    this.seriesId = newSeriesId;
  }


  @Override
  public EventInstanceBuilder toBuilder() {
    return new EventInstanceBuilder().copyFrom(this).seriesId(seriesId);
  }

  /**
   * Builder for EventInstance.
   */
  public static class EventInstanceBuilder extends AbstractEventBuilder<EventInstanceBuilder> {
    private String seriesId;

    /**
     * Sets the series ID.
     *
     * @param seriesId the series ID
     * @return the builder
     */
    public EventInstanceBuilder seriesId(String seriesId) {
      this.seriesId = seriesId;
      return this;
    }

    /**
     * Builds the EventInstance.
     *
     * @return the built EventInstance
     */
    @Override
    public EventInstance build() {
      if (subject == null || start == null || end == null) {
        throw new IllegalStateException("Subject, start, end cannot be null");
      }
      return new EventInstance(this);
    }

    /**
     * Returns the builder instance.
     *
     * @return the builder
     */
    @Override
    protected EventInstanceBuilder returnBuilder() {
      return this;
    }
  }

}
