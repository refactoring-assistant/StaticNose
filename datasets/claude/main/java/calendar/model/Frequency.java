package calendar.model;

/**
 * Recurrence frequency for event series.
 */
public enum Frequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY;

    public static Frequency fromString(String s) throws CalendarException {
        if (s == null) throw new CalendarException("Frequency cannot be null");
        switch (s.trim().toUpperCase()) {
            case "DAILY":   return DAILY;
            case "WEEKLY":  return WEEKLY;
            case "MONTHLY": return MONTHLY;
            case "YEARLY":  return YEARLY;
            default: throw new CalendarException("Unknown frequency: " + s);
        }
    }
}
