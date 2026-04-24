package calendar.service;

import calendar.model.Calendar;
import calendar.model.Event;
import calendar.model.EventSeriesCreator;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Service for calendar operations.
 * Business logic extracted from CommandController.
 * Shared by text and GUI controllers.
 */
public class CalendarOperations {
  private final EventSeriesCreator creator;

  /**Calendar Operation file.*/
  public CalendarOperations() {
    this.creator = new EventSeriesCreator();
  }

  /**
   * Creates single event in calendar.
   *
   * @param cal calendar to add event to
   * @param subj event subject
   * @param start event start time
   * @param end event end time
   * @param desc event description (nullable)
   * @param loc event location (nullable)
   * @param pub true if public, false if private
   * @return created event
   * @throws IllegalArgumentException if validation fails
   */
  public Event createEvent(Calendar cal, String subj,
                           ZonedDateTime start, ZonedDateTime end,
                           String desc, String loc, boolean pub) {
    validateEventInput(subj, start);
    ZonedDateTime s = start.withZoneSameInstant(cal.getTimezone());
    ZonedDateTime e = end == null ? s.plusHours(1)
        : end.withZoneSameInstant(cal.getTimezone());
    Event evt = buildEvent(subj, s, e, desc, loc, pub, null);
    cal.addEvent(evt);
    return evt;
  }

  /**
   * Creates recurring event series.
   *
   * @param cal calendar to add series to
   * @param subj event subject
   * @param start first occurrence start time
   * @param end first occurrence end time
   * @param weekdays weekday pattern (e.g., MWF)
   * @param occ number of occurrences (null if using until)
   * @param until end date (null if using occurrences)
   * @return list of created events
   * @throws IllegalArgumentException if validation fails
   */
  public List<Event> createRecurringSeries(Calendar cal, String subj,
                                           ZonedDateTime start, ZonedDateTime end,
                                           String weekdays,
                                           Integer occ, LocalDate until) {
    validateEventInput(subj, start);
    validateSeriesInput(weekdays, occ, until);
    ZonedDateTime s = start.withZoneSameInstant(cal.getTimezone());
    ZonedDateTime e = end.withZoneSameInstant(cal.getTimezone());
    List<Event> series = generateSeries(subj, s, e, weekdays, occ, until);
    cal.addEvents(series);
    return series;
  }

  /**
   * Edits single event property.
   *
   * @param cal calendar containing event
   * @param subj event subject
   * @param start event start time
   * @param end event end time (null if not specified)
   * @param prop property to edit
   * @param val new value
   * @throws IllegalArgumentException if event not found
   */
  public void editEvent(Calendar cal, String subj, ZonedDateTime start,
                        ZonedDateTime end, String prop, Object val) {
    Event e = end != null ? cal.findEvent(subj, start, end)
        : cal.findEvent(subj, start);
    cal.editEvent(e, prop, val);
  }

  /**
   * Edits multiple properties of single event.
   *
   * @param cal calendar containing event
   * @param e event to edit
   * @param newSubj new subject
   * @param newStart new start time
   * @param newEnd new end time
   * @param newDesc new description
   * @param newLoc new location
   * @throws IllegalArgumentException if edit fails
   */
  public void editEventMultipleProperties(Calendar cal, Event e,
                                          String newSubj, ZonedDateTime newStart,
                                          ZonedDateTime newEnd, String newDesc,
                                          String newLoc) {
    newStart = newStart.withZoneSameInstant(cal.getTimezone());
    newEnd = newEnd.withZoneSameInstant(cal.getTimezone());

    cal.editEvent(e, "description", newDesc);
    cal.editEvent(e, "location", newLoc);
    cal.editEvent(e, "end", newEnd);
    cal.editEvent(e, "start", newStart);
    cal.editEvent(e, "subject", newSubj);
  }

  /**
   * Edits event and all future occurrences in series.
   *
   * @param cal calendar containing event
   * @param subj event subject
   * @param start event start time
   * @param prop property to edit
   * @param val new value
   * @throws IllegalArgumentException if event not found
   */
  public void editEventsForward(Calendar cal, String subj,
                                ZonedDateTime start, String prop, Object val) {
    Event e = cal.findEvent(subj, start);
    cal.editEventsForward(e, prop, val);
  }

  /**
   * Edits all events in series.
   *
   * @param cal calendar containing event
   * @param subj event subject
   * @param start event start time
   * @param prop property to edit
   * @param val new value
   * @throws IllegalArgumentException if event not found
   */
  public void editEntireSeries(Calendar cal, String subj,
                               ZonedDateTime start, String prop, Object val) {
    Event e = cal.findEvent(subj, start);
    cal.editEntireSeries(e, prop, val);
  }

  private void validateEventInput(String subj, ZonedDateTime start) {
    if (subj == null || subj.trim().isEmpty()) {
      throw new IllegalArgumentException("Subject required");
    }
    if (start == null) {
      throw new IllegalArgumentException("Start time required");
    }
  }

  private void validateSeriesInput(String weekdays, Integer occ, LocalDate until) {
    if (weekdays == null || !weekdays.matches("[MTWRFSU]+")) {
      throw new IllegalArgumentException("Invalid weekdays");
    }
    if (occ == null && until == null) {
      throw new IllegalArgumentException("Series needs 'for N times' or 'until DATE'");
    }
  }

  private Event buildEvent(String subj, ZonedDateTime start, ZonedDateTime end,
                           String desc, String loc, boolean pub, String seriesId) {
    Event.Builder b = new Event.Builder(subj, start).end(end).isPublic(pub);
    if (desc != null && !desc.isEmpty()) {
      b.description(desc);
    }
    if (loc != null && !loc.isEmpty()) {
      b.location(loc);
    }
    if (seriesId != null) {
      b.seriesId(seriesId);
    }
    return b.build();
  }

  private List<Event> generateSeries(String subj, ZonedDateTime start,
                                     ZonedDateTime end, String weekdays,
                                     Integer occ, LocalDate until) {
    if (occ != null) {
      return creator.createSeriesForOccurrences(subj, start, end, weekdays, occ);
    } else {
      return creator.createSeriesUntilDate(subj, start, end, weekdays, until);
    }
  }
}