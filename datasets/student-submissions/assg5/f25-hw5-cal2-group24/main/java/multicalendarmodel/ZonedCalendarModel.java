package multicalendarmodel;

import calendarmodel.CalendarModel;
import calendarmodel.Event;
import calendarmodel.enums.Location;
import calendarmodel.exceptions.DuplicateEventException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * An extended calendar model that is time-zone aware.
 *
 * <p>This interface extends the original {@link CalendarModel} by giving a
 * clear time zone context to all operations. Methods inheriting from the
 * parent interface (e.g., {@code getEventsOn(LocalDate)}) are now
 * understood to operate within this calendar's specific time zone.</p>
 *
 * <p>It also adds new, unambiguous methods that use modern Java time
 * classes like {@link ZonedDateTime} and {@link Instant}.</p>
 */
public interface ZonedCalendarModel extends CalendarModel {

  /**
   * Gets the time zone this calendar instance operates in.
   *
   * @return The calendar's non-null {@link ZoneId}.
   */
  ZoneId getZone();

  /**
   * Sets the time zone for this calendar.
   *
   * <p>Changing the zone will affect how all event times are interpreted
   * and displayed when retrieved. The underlying universal time (Instant)
   * of each event is preserved.</p>
   *
   * @param zone The new {@link ZoneId} for this calendar.
   */
  void setZone(ZoneId zone);

  /**
   * Creates a single event using a {@link ZonedDateTime}.
   * This is the new, preferred way to create a time-zone-aware event.
   *
   * @param subject     The event subject.
   * @param startTime   The exact start moment (with time zone).
   * @param endTime     The exact end moment (with time zone).
   * @param description The event's description, or null.
   * @param location    The event's location, or null.
   * @param status      The event's status, or null.
   * @throws DuplicateEventException if the event conflicts with an existing one.
   */
  void createZonedEvent(String subject, ZonedDateTime startTime, ZonedDateTime endTime,
                        String description, Location location, String status)
      throws DuplicateEventException;

  /**
   * (Overridden) Gets events on a specific date, interpreted
   * in this calendar's local time zone.
   *
   * @param date The date to query (e.g., "Nov 5th"). This will be
   *             interpreted in the calendar's {@link ZoneId}.
   * @return A list of {@link Event} objects, with start/end times
   *         in the calendar's local time.
   */
  @Override
  List<Event> getEventsOn(LocalDate date);

  /**
   * (Overridden) Checks if the user is busy at a specific "wall time".
   *
   * @param dateTime The wall time (e.g., "9:00 AM"). This will be
   *                 interpreted in the calendar's {@link ZoneId}.
   * @return true if the user is busy, false otherwise.
   */
  @Override
  boolean isBusy(LocalDateTime dateTime);

  /**
   * Checks if the user is busy at a specific, unambiguous instant in time.
   *
   * @param instant The {@link Instant} to check.
   * @return true if the user is busy at that moment, false otherwise.
   */
  boolean isBusy(Instant instant);

  /**
   * (Overridden) Gets a defensive copy of all events in this calendar.
   *
   * <p>The events are returned with {@link LocalDateTime} fields
   * converted to this calendar's {@link ZoneId}.</p>
   *
   * @return A new list containing all events in the calendar's local zone.
   */
  @Override
  List<Event> getAllEvents();
}