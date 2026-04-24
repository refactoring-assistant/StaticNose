package calendar.model;

/**
 * Represents the status of a calendar event.
 */
public enum EventStatus {
    CONFIRMED,
    TENTATIVE,
    CANCELLED;

    /**
     * Parse a status string (case-insensitive).
     */
    public static EventStatus fromString(String s) throws CalendarException {
        if (s == null) throw new CalendarException("Status cannot be null");
        switch (s.trim().toUpperCase()) {
            case "CONFIRMED":  return CONFIRMED;
            case "TENTATIVE":  return TENTATIVE;
            case "CANCELLED":  return CANCELLED;
            default: throw new CalendarException("Unknown status: " + s);
        }
    }
}
