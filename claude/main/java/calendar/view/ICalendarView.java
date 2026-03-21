package calendar.view;

import calendar.model.CalendarEvent;
import java.util.List;

/**
 * Interface for the calendar view. Handles all output (no model/controller logic).
 */
public interface ICalendarView {

    /**
     * Display a general informational message to the user.
     */
    void displayMessage(String message);

    /**
     * Display an error message.
     */
    void displayError(String error);

    /**
     * Display a list of events.
     */
    void displayEvents(List<CalendarEvent> events);

    /**
     * Display a single event.
     */
    void displayEvent(CalendarEvent event);

    /**
     * Display confirmation that an event was created.
     */
    void displayEventCreated(CalendarEvent event);

    /**
     * Display confirmation that events were created (for series).
     */
    void displaySeriesCreated(int count, String seriesId);

    /**
     * Display confirmation that an export was completed.
     */
    void displayExportComplete(String filePath);

    /**
     * Display a busy/free status for a given time.
     */
    void displayBusyStatus(boolean isBusy, String datetime);

    /**
     * Display a warning (non-fatal message).
     */
    void displayWarning(String warning);
}
