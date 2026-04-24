package calendar.model;

import java.time.LocalDateTime;

/**
 * Represents a single instance within a recurring event series.
 * Maintains reference to its parent series for group operations.
 * Must start and end on the same day (constraint for recurring events).
 */
public class RecurringEvent extends AbstractEvent {

  private EventSeries parentSeries;

  /**
   * Constructs a RecurringEvent instance.
   * This represents one occurrence in a recurring event series.
   *
   * @param subject       the event subject
   * @param startDateTime the start date/time
   * @param endDateTime   the end date/time (null for all-day)
   * @param description   optional description
   * @param location      optional location
   * @param status        privacy status (PUBLIC or PRIVATE)
   * @param parentSeries  the parent series this event belongs to
   */
  public RecurringEvent(String subject, LocalDateTime startDateTime,
                        LocalDateTime endDateTime, String description,
                        String location, EventStatus status,
                        EventSeries parentSeries) {
    super(subject, startDateTime, endDateTime, description, location, status);
    this.parentSeries = parentSeries;
  }

  @Override
  protected void validateEventConstraints() {

    if (!isAllDay && !startDateTime.toLocalDate().equals(endDateTime.toLocalDate())) {
      throw new IllegalArgumentException(
          "Recurring events must start and end on the same day. "
              + "Start: " + startDateTime.toLocalDate()
              + ", End: " + endDateTime.toLocalDate());
    }
  }

  /**
   * Gets the parent event series.
   *
   * @return the parent EventSeries, or null if not part of a series
   */
  public EventSeries getParentSeries() {
    return parentSeries;
  }

  /**
   * Sets the parent event series.
   * Used when moving events between series or during series splitting.
   *
   * @param parentSeries the new parent series
   */
  public void setParentSeries(EventSeries parentSeries) {
    this.parentSeries = parentSeries;
  }

  /**
   * Checks if this event is part of a series.
   *
   * @return true if part of a series, false otherwise
   */
  public boolean isPartOfSeries() {
    return parentSeries != null;
  }

  /**
   * Gets the series identifier.
   * Useful for grouping events that belong to the same series.
   *
   * @return the series base subject, or null if not part of a series
   */
  public String getSeriesIdentifier() {
    return parentSeries != null ? parentSeries.getBaseSubject() : null;
  }

  @Override
  public InEvent copy() {

    return new RecurringEvent(
        this.subject,
        this.startDateTime,
        this.isAllDay ? null : this.endDateTime,
        this.description,
        this.location,
        this.status,
        this.parentSeries
    );
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("RecurringEvent{");
    sb.append("subject='").append(subject).append("'");
    sb.append(", start=").append(startDateTime);
    sb.append(", end=").append(endDateTime);
    if (parentSeries != null) {
      sb.append(", series='").append(parentSeries.getBaseSubject()).append("'");
    }
    sb.append(", allDay=").append(isAllDay);
    sb.append("}");
    return sb.toString();
  }

  @Override
  public boolean equals(Object obj) {

    return super.equals(obj);
  }

  @Override
  public int hashCode() {

    return super.hashCode();
  }
}
