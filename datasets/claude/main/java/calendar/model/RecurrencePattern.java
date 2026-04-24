package calendar.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Holds recurrence configuration for an event series.
 * Supports INTERVAL, COUNT, UNTIL, and DAY (weekly only) patterns.
 */
public class RecurrencePattern {

    private final Frequency frequency;
    private int interval = 1;           // default interval is 1
    private Integer count = null;       // null = unlimited (capped at 500)
    private LocalDate until = null;     // null = no end date
    private Set<DayOfWeek> days = Collections.emptySet(); // for WEEKLY/DAY

    public RecurrencePattern(Frequency frequency) {
        this.frequency = frequency;
    }

    // ---- Getters ----

    public Frequency getFrequency() { return frequency; }

    public int getInterval() { return interval; }

    public Integer getCount() { return count; }

    public LocalDate getUntil() { return until; }

    public Set<DayOfWeek> getDays() { return days; }

    // ---- Setters (with validation) ----

    public void setInterval(int interval) throws CalendarException {
        if (interval < 1) throw new CalendarException("Interval must be >= 1");
        this.interval = interval;
    }

    public void setCount(int count) throws CalendarException {
        if (this.until != null) {
            throw new CalendarException("Cannot combine COUNT and UNTIL");
        }
        if (count < 1) throw new CalendarException("Count must be >= 1");
        this.count = count;
    }

    public void setUntil(LocalDate until) throws CalendarException {
        if (this.count != null) {
            throw new CalendarException("Cannot combine UNTIL and COUNT");
        }
        this.until = until;
    }

    public void setDays(Set<DayOfWeek> days) throws CalendarException {
        if (frequency != Frequency.WEEKLY) {
            throw new CalendarException("DAY pattern is only valid with WEEKLY frequency");
        }
        this.days = days;
    }

    /**
     * Parse a day-of-week character set string (e.g. "MW", "MTWRF").
     * M=Mon, T=Tue, W=Wed, R=Thu, F=Fri, S=Sat, U=Sun
     */
    public static Set<DayOfWeek> parseDayChars(String chars) throws CalendarException {
        Set<DayOfWeek> result = EnumSet.noneOf(DayOfWeek.class);
        for (char c : chars.toUpperCase().toCharArray()) {
            switch (c) {
                case 'M': result.add(DayOfWeek.MONDAY);    break;
                case 'T': result.add(DayOfWeek.TUESDAY);   break;
                case 'W': result.add(DayOfWeek.WEDNESDAY); break;
                case 'R': result.add(DayOfWeek.THURSDAY);  break;
                case 'F': result.add(DayOfWeek.FRIDAY);    break;
                case 'S': result.add(DayOfWeek.SATURDAY);  break;
                case 'U': result.add(DayOfWeek.SUNDAY);    break;
                default:
                    throw new CalendarException("Unknown day character: " + c);
            }
        }
        if (result.isEmpty()) {
            throw new CalendarException("DAY pattern must specify at least one day");
        }
        return result;
    }

    /**
     * Parse token stream (already split) and populate this pattern.
     * Tokens after frequency keyword have already been consumed by caller.
     * Expected token pairs: INTERVAL N | COUNT N | UNTIL date | DAY chars
     */
    public static RecurrencePattern parse(Frequency frequency, String[] tokens, int startIdx)
            throws CalendarException {
        RecurrencePattern pattern = new RecurrencePattern(frequency);
        int i = startIdx;
        while (i < tokens.length) {
            String tok = tokens[i].toUpperCase();
            switch (tok) {
                case "INTERVAL":
                    i++;
                    if (i >= tokens.length) throw new CalendarException("INTERVAL missing value");
                    try {
                        pattern.setInterval(Integer.parseInt(tokens[i]));
                    } catch (NumberFormatException e) {
                        throw new CalendarException("INTERVAL value must be an integer");
                    }
                    break;
                case "COUNT":
                    i++;
                    if (i >= tokens.length) throw new CalendarException("COUNT missing value");
                    try {
                        pattern.setCount(Integer.parseInt(tokens[i]));
                    } catch (NumberFormatException e) {
                        throw new CalendarException("COUNT value must be an integer");
                    }
                    break;
                case "UNTIL":
                    i++;
                    if (i >= tokens.length) throw new CalendarException("UNTIL missing date");
                    try {
                        pattern.setUntil(LocalDate.parse(tokens[i]));
                    } catch (DateTimeParseException e) {
                        throw new CalendarException("UNTIL date must be YYYY-MM-DD: " + tokens[i]);
                    }
                    break;
                case "DAY":
                    i++;
                    if (i >= tokens.length) throw new CalendarException("DAY missing characters");
                    pattern.setDays(parseDayChars(tokens[i]));
                    break;
                default:
                    throw new CalendarException("Unknown recurrence token: " + tokens[i]);
            }
            i++;
        }
        return pattern;
    }
}
