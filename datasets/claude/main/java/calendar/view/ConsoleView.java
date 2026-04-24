package calendar.view;

import calendar.model.CalendarEvent;
import java.util.List;

/**
 * Console-based view implementation. Outputs to standard out/err.
 */
public class ConsoleView implements ICalendarView {

    @Override
    public void displayMessage(String message) {
        System.out.println(message);
    }

    @Override
    public void displayError(String error) {
        System.err.println("ERROR: " + error);
    }

    @Override
    public void displayEvents(List<CalendarEvent> events) {
        if (events == null || events.isEmpty()) {
            System.out.println("No events found.");
            return;
        }
        for (CalendarEvent ev : events) {
            System.out.println(ev.toString());
        }
    }

    @Override
    public void displayEvent(CalendarEvent event) {
        if (event == null) {
            System.out.println("No event found.");
            return;
        }
        System.out.println(event.toString());
    }

    @Override
    public void displayEventCreated(CalendarEvent event) {
        System.out.println("Event created: " + event.getSubject()
                + " on " + event.getStartDateTime().toLocalDate()
                + " at " + String.format("%02d:%02d", event.getStartDateTime().getHour(),
                        event.getStartDateTime().getMinute()));
    }

    @Override
    public void displaySeriesCreated(int count, String seriesId) {
        System.out.println("Event series created: " + count + " occurrence(s) [seriesId=" + seriesId + "]");
    }

    @Override
    public void displayExportComplete(String filePath) {
        System.out.println("Calendar exported to: " + filePath);
    }

    @Override
    public void displayBusyStatus(boolean isBusy, String datetime) {
        if (isBusy) {
            System.out.println("Busy at " + datetime);
        } else {
            System.out.println("Free at " + datetime);
        }
    }

    @Override
    public void displayWarning(String warning) {
        System.out.println("WARNING: " + warning);
    }
}
