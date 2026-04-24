package calendar.model;

import calendar.exception.DuplicateEventException;
import calendar.exception.EventNotFoundException;
import calendar.exception.ReadOnlyCalendarException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Read-only wrapper for a calendar used during copy operations.
 * Prevents modifications to the source calendar while allowing read access.
 * THROWS: ReadOnlyCalendarException (RuntimeException) on any modification attempt.
 */
public class ReadOnlyCalendar implements InCalendar {

  private final InCalendar wrappedCalendar;

  /**
   * Constructs a ReadOnlyCalendar wrapper.
   *
   * @param calendar the calendar to wrap
   */
  public ReadOnlyCalendar(InCalendar calendar) {
    if (calendar == null) {
      throw new IllegalArgumentException("Calendar cannot be null");
    }
    this.wrappedCalendar = calendar;
  }

  @Override
  public String getCalendarName() {
    return wrappedCalendar.getCalendarName();
  }

  @Override
  public void setCalendarName(String newName) {
    throw new ReadOnlyCalendarException(
        "Cannot modify calendar name - calendar is read-only");
  }

  @Override
  public void addEvent(InEvent event) throws DuplicateEventException {
    throw new ReadOnlyCalendarException(
        "Cannot add event - calendar is read-only");
  }

  @Override
  public void removeEvent(InEvent event) throws EventNotFoundException {
    throw new ReadOnlyCalendarException(
        "Cannot remove event - calendar is read-only");
  }

  @Override
  public List<InEvent> getEventsOnDate(LocalDate date) {
    return wrappedCalendar.getEventsOnDate(date);
  }

  @Override
  public List<InEvent> getEventsBetween(LocalDateTime start, LocalDateTime end) {
    return wrappedCalendar.getEventsBetween(start, end);
  }

  @Override
  public boolean isBusyAt(LocalDateTime dateTime) {
    return wrappedCalendar.isBusyAt(dateTime);
  }

  @Override
  public List<InEvent> getAllEvents() {
    return wrappedCalendar.getAllEvents();
  }

  @Override
  public InEvent findEvent(String subject, LocalDateTime start, LocalDateTime end) {
    return wrappedCalendar.findEvent(subject, start, end);
  }

  @Override
  public List<InEvent> filterEvents(Predicate<InEvent> predicate) {
    return wrappedCalendar.filterEvents(predicate);
  }

  @Override
  public String toString() {
    return "ReadOnlyCalendar{wrapping=" + wrappedCalendar.getCalendarName() + "}";
  }
}