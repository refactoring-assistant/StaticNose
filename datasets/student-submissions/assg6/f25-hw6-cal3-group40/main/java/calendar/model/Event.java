package calendar.model;

import java.time.Instant;

/**
 * Represents a single event in the calendar.
 * This class is a data-centric class, holding the state
 * for a calendar event.
 */
public interface Event {

  /**
   * Gets the event's subject.
   *
   * @return The subject line.
   */
  String getSubject();

  /**
   * Gets the event's start date and time in UTC.
   *
   * @return The start {@link Instant}.
   */
  Instant getStart();

  /**
   * Gets the event's end date and time in UTC.
   *
   * @return The end {@link Instant}.
   */
  Instant getEnd();

  /**
   * Gets the event's description.
   *
   * @return The description string.
   */
  String getDescription();

  /**
   * Gets the event's location.
   *
   * @return The location string.
   */
  String getLocation();

  /**
   * Checks the event's privacy status.
   *
   * @return true if the event is private, false otherwise.
   */
  boolean isPrivate();

  /**
   * Gets the event's series ID.
   *
   * @return The series ID, or null if not part of a series.
   */
  String getSeriesId();

  /**
   * Checks if this event is part of a series.
   *
   * @return true if the seriesId is not null, false otherwise.
   */
  boolean isSeries();

  /**
   * Sets the event's subject.
   *
   * @param subject The new subject.
   */
  void setSubject(String subject);

  /**
   * Sets the event's start date and time in UTC.
   *
   * @param start The new start time.
   */
  void setStart(Instant start);

  /**
   * Sets the event's end date and time in UTC.
   *
   * @param end The new end time.
   */
  void setEnd(Instant end);

  /**
   * Sets the event's description.
   *
   * @param description The new description.
   */
  void setDescription(String description);

  /**
   * Sets the event's location.
   *
   * @param location The new location.
   */
  void setLocation(String location);

  /**
   * Sets the event's privacy status.
   *
   * @param isPrivate true to set as private, false for public.
   */
  void setPrivate(boolean isPrivate);

  /**
   * Sets the event's series ID.
   *
   * @param seriesId The new series ID.
   */
  void setSeriesId(String seriesId);

  /**
   * Creates a deep copy of this event.
   *
   * @return A new {@link Event} instance with the same data.
   */
  Event copy();

  /**
   * Checks for equality based on subject, start, and end time.
   * This is used for conflict detection.
   *
   * @param o The object to compare with.
   * @return true if the subject, start, and end are identical, false otherwise.
   */
  @Override
  boolean equals(Object o);

  /**
   * Generates a hash code based on subject, start, and end time.
   *
   * @return The hash code.
   */
  @Override
  int hashCode();
}