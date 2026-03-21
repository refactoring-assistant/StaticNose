package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface for the calendar model. No UI dependencies.
 */
public interface ICalendar {

    /**
     * Add a single (non-recurring) event.
     *
     * @param event the event to add
     * @throws CalendarException if a duplicate exists
     */
    void addEvent(CalendarEvent event) throws CalendarException;

    /**
     * Create a series of recurring events based on the given recurrence pattern.
     * Events are generated eagerly (up to 500 if no end condition).
     *
     * @param event   template event (subject, start, end, etc.)
     * @param pattern recurrence pattern
     * @throws CalendarException if any generated event duplicates an existing one
     */
    void addEventSeries(CalendarEvent event, RecurrencePattern pattern) throws CalendarException;

    /**
     * Edit a single event uniquely identified by subject + start + end.
     * Fails if multiple events match.
     *
     * @param subject   event subject
     * @param start     event start datetime
     * @param end       event end datetime
     * @param property  property to edit
     * @param value     new value
     * @throws CalendarException on validation or duplicate errors
     */
    void editSingleEvent(String subject, LocalDateTime start, LocalDateTime end,
                         String property, String value) throws CalendarException;

    /**
     * Edit all series events whose start >= given start (and same subject).
     *
     * @param subject   event subject
     * @param start     lower bound for event start datetime (inclusive)
     * @param property  property to edit
     * @param value     new value
     * @throws CalendarException on validation or duplicate errors
     */
    void editEventsFrom(String subject, LocalDateTime start, String property, String value)
            throws CalendarException;

    /**
     * Edit all events in the same series as identified by subject + start.
     *
     * @param subject   event subject
     * @param start     any event's start datetime to locate the series
     * @param property  property to edit
     * @param value     new value
     * @throws CalendarException on validation or duplicate errors
     */
    void editAllInSeries(String subject, LocalDateTime start, String property, String value)
            throws CalendarException;

    /**
     * Get all events on a given date (any overlap counts).
     */
    List<CalendarEvent> getEventsOnDate(LocalDate date);

    /**
     * Get all events that overlap the given datetime range.
     */
    List<CalendarEvent> getEventsInRange(LocalDateTime from, LocalDateTime to);

    /**
     * Check if any event is active at the exact given datetime.
     */
    boolean isBusy(LocalDate date, LocalDateTime time);

    /**
     * Export all events to a CSV file in Google Calendar format.
     *
     * @param filename the output filename (written to current directory)
     * @return the absolute path of the written file
     * @throws CalendarException if writing fails
     */
    String exportToCSV(String filename) throws CalendarException;

    /**
     * Return all events sorted by startDateTime.
     */
    List<CalendarEvent> getAllEvents();
}
