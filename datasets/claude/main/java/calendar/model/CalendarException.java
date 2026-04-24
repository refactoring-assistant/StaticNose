package calendar.model;

/**
 * Custom exception for calendar-related errors.
 */
public class CalendarException extends Exception {

    public CalendarException(String message) {
        super(message);
    }

    public CalendarException(String message, Throwable cause) {
        super(message, cause);
    }
}
