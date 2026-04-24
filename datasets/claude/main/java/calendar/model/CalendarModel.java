package calendar.model;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Core calendar model implementation. Holds all events in memory.
 * Events are kept sorted by startDateTime.
 */
public class CalendarModel implements ICalendar {

    private static final int SERIES_CAP = 500;
    private static final DateTimeFormatter CSV_DATE_FMT  = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter CSV_TIME_FMT  = DateTimeFormatter.ofPattern("h:mm a");

    private final List<CalendarEvent> events = new ArrayList<>();

    // ---------------------------------------------------------------
    // ICalendar implementation
    // ---------------------------------------------------------------

    @Override
    public void addEvent(CalendarEvent event) throws CalendarException {
        checkDuplicate(event, null);
        events.add(event);
        sortEvents();
    }

    @Override
    public void addEventSeries(CalendarEvent template, RecurrencePattern pattern)
            throws CalendarException {
        List<CalendarEvent> generated = generateSeries(template, pattern);
        // Check all generated events for duplicates before committing any
        for (CalendarEvent ev : generated) {
            checkDuplicate(ev, null);
        }
        events.addAll(generated);
        sortEvents();
    }

    @Override
    public void editSingleEvent(String subject, LocalDateTime start, LocalDateTime end,
                                String property, String value) throws CalendarException {
        List<CalendarEvent> matches = events.stream()
                .filter(e -> e.getSubject().equals(subject)
                        && e.getStartDateTime().equals(start)
                        && objectEquals(e.getEndDateTime(), end))
                .collect(Collectors.toList());

        if (matches.isEmpty()) {
            throw new CalendarException("No event found with subject='" + subject
                    + "', start=" + start + ", end=" + end);
        }
        if (matches.size() > 1) {
            throw new CalendarException("Multiple events match criteria — cannot edit");
        }

        CalendarEvent ev = matches.get(0);
        String oldSeriesId = ev.getSeriesId();

        // Validate on a copy before committing
        CalendarEvent copy = ev.copy();
        applyEdit(copy, property, value);
        checkDuplicateExcluding(copy, ev);

        // Commit
        applyEdit(ev, property, value);

        // If start was changed and this event was part of a series, assign new seriesId
        if ("start".equalsIgnoreCase(property) && oldSeriesId != null) {
            ev.setSeriesId(UUID.randomUUID().toString());
        }
    }

    @Override
    public void editEventsFrom(String subject, LocalDateTime start, String property, String value)
            throws CalendarException {
        // First find the anchor event (exact match on subject+start)
        List<CalendarEvent> anchors = events.stream()
                .filter(e -> e.getSubject().equals(subject)
                        && e.getStartDateTime().equals(start))
                .collect(Collectors.toList());
        if (anchors.isEmpty()) {
            throw new CalendarException("No event found with subject='" + subject + "' at " + start);
        }
        if (anchors.size() > 1) {
            throw new CalendarException("Multiple events match — cannot edit");
        }

        CalendarEvent anchor = anchors.get(0);
        List<CalendarEvent> candidates;
        if (anchor.getSeriesId() == null) {
            // Standalone event: treat same as editSingleEvent
            candidates = List.of(anchor);
        } else {
            // Series: all events in the same series starting from the given start
            final String sid = anchor.getSeriesId();
            candidates = events.stream()
                    .filter(e -> sid.equals(e.getSeriesId())
                            && !e.getStartDateTime().isBefore(start))
                    .collect(Collectors.toList());
        }

        if (candidates.isEmpty()) {
            throw new CalendarException("No events found for subject='" + subject + "' from " + start);
        }

        // Validate edits on copies first
        List<CalendarEvent> copies = copyListForEdit(candidates);
        String newSeriesId = ("start".equalsIgnoreCase(property)) ? UUID.randomUUID().toString() : null;

        for (CalendarEvent copy : copies) {
            applyEdit(copy, property, value);
            if (newSeriesId != null) copy.setSeriesId(newSeriesId);
        }

        // Check duplicates for edited copies
        for (int i = 0; i < copies.size(); i++) {
            checkDuplicateExcluding(copies.get(i), candidates.get(i));
        }

        // Commit
        for (int i = 0; i < candidates.size(); i++) {
            applyEdit(candidates.get(i), property, value);
            if (newSeriesId != null) candidates.get(i).setSeriesId(newSeriesId);
        }
        sortEvents();
    }

    @Override
    public void editAllInSeries(String subject, LocalDateTime start, String property, String value)
            throws CalendarException {
        // Find one event matching subject+start
        List<CalendarEvent> anchors = events.stream()
                .filter(e -> e.getSubject().equals(subject)
                        && e.getStartDateTime().equals(start))
                .collect(Collectors.toList());

        if (anchors.isEmpty()) {
            throw new CalendarException("No event found for subject='" + subject + "' at start=" + start);
        }
        if (anchors.size() > 1) {
            throw new CalendarException("Multiple events match — cannot edit");
        }

        CalendarEvent anchor = anchors.get(0);
        List<CalendarEvent> seriesEvents;
        if (anchor.getSeriesId() == null) {
            // Standalone: treat as single-event edit
            seriesEvents = List.of(anchor);
        } else {
            String seriesId = anchor.getSeriesId();
            seriesEvents = events.stream()
                    .filter(e -> seriesId.equals(e.getSeriesId()))
                    .collect(Collectors.toList());
        }

        List<CalendarEvent> copies = copyListForEdit(seriesEvents);
        for (CalendarEvent copy : copies) {
            applyEdit(copy, property, value);
        }
        for (int i = 0; i < copies.size(); i++) {
            checkDuplicateExcluding(copies.get(i), seriesEvents.get(i));
        }

        // Commit
        for (CalendarEvent ev : seriesEvents) {
            applyEdit(ev, property, value);
        }
        sortEvents();
    }

    @Override
    public List<CalendarEvent> getEventsOnDate(LocalDate date) {
        return events.stream()
                .filter(e -> e.overlapsDate(date))
                .collect(Collectors.toList());
    }

    @Override
    public List<CalendarEvent> getEventsInRange(LocalDateTime from, LocalDateTime to) {
        return events.stream()
                .filter(e -> e.overlapsRange(from, to))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isBusy(LocalDate date, LocalDateTime time) {
        return events.stream().anyMatch(e -> e.isActiveAt(time));
    }

    @Override
    public String exportToCSV(String filename) throws CalendarException {
        String path = Paths.get(filename).toAbsolutePath().toString();
        try (PrintWriter pw = new PrintWriter(new FileWriter(path))) {
            pw.println("Subject,Start Date,Start Time,End Date,End Time,All Day Event,Description,Location,Private");
            for (CalendarEvent ev : events) {
                pw.println(formatCSVRow(ev));
            }
        } catch (IOException e) {
            throw new CalendarException("Failed to export CSV: " + e.getMessage(), e);
        }
        return path;
    }

    @Override
    public List<CalendarEvent> getAllEvents() {
        return new ArrayList<>(events);
    }

    // ---------------------------------------------------------------
    // Private helpers
    // ---------------------------------------------------------------

    private void sortEvents() {
        events.sort(Comparator.comparing(CalendarEvent::getStartDateTime));
    }

    /** Throw if event would be a duplicate of any existing event, skipping `exclude`. */
    private void checkDuplicate(CalendarEvent candidate, CalendarEvent exclude)
            throws CalendarException {
        for (CalendarEvent ev : events) {
            if (ev == exclude) continue;
            if (ev.isDuplicateOf(candidate)) {
                throw new CalendarException("Duplicate event: subject='" + candidate.getSubject()
                        + "' at " + candidate.getStartDateTime());
            }
        }
    }

    /** For editing: exclude the original event that is being replaced. */
    private void checkDuplicateExcluding(CalendarEvent edited, CalendarEvent original)
            throws CalendarException {
        for (CalendarEvent ev : events) {
            if (ev == original) continue;
            if (ev.isDuplicateOf(edited)) {
                throw new CalendarException("Edit would create duplicate: subject='"
                        + edited.getSubject() + "' at " + edited.getStartDateTime());
            }
        }
    }

    private boolean objectEquals(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    /** Apply a property edit to an event in-place. */
    private void applyEdit(CalendarEvent ev, String property, String value)
            throws CalendarException {
        switch (property.toLowerCase()) {
            case "subject":
                ev.setSubject(value);
                break;
            case "start": {
                LocalDateTime newStart = parseDateTime(value);
                LocalDateTime oldStart = ev.getStartDateTime();
                LocalDateTime oldEnd   = ev.getEndDateTime();
                long offsetMinutes = 0;
                if (oldEnd != null) {
                    offsetMinutes = java.time.Duration.between(oldStart, oldEnd).toMinutes();
                }
                ev.setStartDateTime(newStart);
                if (oldEnd != null) {
                    ev.setEndDateTime(newStart.plusMinutes(offsetMinutes));
                }
                break;
            }
            case "end":
                ev.setEndDateTime(parseDateTime(value));
                break;
            case "description":
                ev.setDescription(value);
                break;
            case "location":
                ev.setLocation(value);
                break;
            case "status":
                ev.setStatus(EventStatus.fromString(value));
                break;
            case "participants":
                ev.setParticipants(parseParticipants(value));
                break;
            default:
                throw new CalendarException("Unknown property: " + property);
        }
    }

    private LocalDateTime parseDateTime(String s) throws CalendarException {
        try {
            return LocalDateTime.parse(s);
        } catch (DateTimeParseException e) {
            throw new CalendarException("Invalid datetime: " + s);
        }
    }

    private List<Participant> parseParticipants(String value) throws CalendarException {
        List<Participant> list = new ArrayList<>();
        for (String token : value.split(",")) {
            token = token.trim();
            if (!token.isEmpty()) {
                list.add(Participant.parse(token));
            }
        }
        return list;
    }

    /** Create shallow copies of events for pre-validation of edits. */
    private List<CalendarEvent> copyListForEdit(List<CalendarEvent> source) throws CalendarException {
        List<CalendarEvent> copies = new ArrayList<>(source.size());
        for (CalendarEvent ev : source) {
            copies.add(ev.copy());
        }
        return copies;
    }

    // ---------------------------------------------------------------
    // Series generation
    // ---------------------------------------------------------------

    private List<CalendarEvent> generateSeries(CalendarEvent template, RecurrencePattern pattern)
            throws CalendarException {

        String seriesId = UUID.randomUUID().toString();
        List<CalendarEvent> result = new ArrayList<>();

        Frequency freq   = pattern.getFrequency();
        int interval     = pattern.getInterval();
        Integer count    = pattern.getCount();
        LocalDate until  = pattern.getUntil();
        Set<DayOfWeek> days = pattern.getDays();

        LocalDateTime templateStart = template.getStartDateTime();
        LocalDateTime templateEnd   = template.getEndDateTime();
        long durationMinutes = (templateEnd != null)
                ? java.time.Duration.between(templateStart, templateEnd).toMinutes()
                : 0;

        // For WEEKLY with DAY pattern: generate occurrences on specified days of week
        if (freq == Frequency.WEEKLY && !days.isEmpty()) {
            result = generateWeeklyDayEvents(template, pattern, seriesId, durationMinutes);
        } else {
            result = generateRegularEvents(template, pattern, seriesId, durationMinutes);
        }

        return result;
    }

    private List<CalendarEvent> generateRegularEvents(CalendarEvent template,
                                                       RecurrencePattern pattern,
                                                       String seriesId,
                                                       long durationMinutes)
            throws CalendarException {
        List<CalendarEvent> result = new ArrayList<>();

        Frequency freq  = pattern.getFrequency();
        int interval    = pattern.getInterval();
        Integer count   = pattern.getCount();
        LocalDate until = pattern.getUntil();

        LocalDateTime current = template.getStartDateTime();
        int generated = 0;
        int cap = (count != null) ? count : (until != null ? Integer.MAX_VALUE : SERIES_CAP);

        while (generated < cap && generated < SERIES_CAP) {
            if (until != null && current.toLocalDate().isAfter(until)) break;

            CalendarEvent ev = template.copy();
            ev.setStartDateTime(current);
            if (template.getEndDateTime() != null) {
                ev.setEndDateTime(current.plusMinutes(durationMinutes));
            }
            ev.setSeriesId(seriesId);
            result.add(ev);
            generated++;

            current = advance(current, freq, interval);
        }

        return result;
    }

    private List<CalendarEvent> generateWeeklyDayEvents(CalendarEvent template,
                                                          RecurrencePattern pattern,
                                                          String seriesId,
                                                          long durationMinutes)
            throws CalendarException {
        List<CalendarEvent> result = new ArrayList<>();

        int interval    = pattern.getInterval();
        Integer count   = pattern.getCount();
        LocalDate until = pattern.getUntil();
        Set<DayOfWeek> days = pattern.getDays();

        LocalDateTime templateStart = template.getStartDateTime();
        LocalTime startTime = templateStart.toLocalTime();

        // Find the Monday of the starting week
        LocalDate startDate = templateStart.toLocalDate();
        LocalDate weekStart = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        int cap = (count != null) ? count : (until != null ? Integer.MAX_VALUE : SERIES_CAP);
        int generated = 0;
        int weekOffset = 0;

        outerLoop:
        while (generated < SERIES_CAP) {
            LocalDate currentWeekStart = weekStart.plusWeeks((long) weekOffset * interval);

            for (DayOfWeek day : sortedDays(days)) {
                // Find the date of this day in the current week
                LocalDate occurrenceDate = currentWeekStart.with(TemporalAdjusters.nextOrSame(day));

                // Skip dates before the template start date
                if (occurrenceDate.isBefore(startDate)) continue;

                if (until != null && occurrenceDate.isAfter(until)) break outerLoop;

                LocalDateTime occStart = occurrenceDate.atTime(startTime);
                LocalDateTime occEnd   = (template.getEndDateTime() != null)
                        ? occStart.plusMinutes(durationMinutes)
                        : null;

                CalendarEvent ev = template.copy();
                ev.setStartDateTime(occStart);
                if (occEnd != null) ev.setEndDateTime(occEnd);
                ev.setSeriesId(seriesId);
                result.add(ev);
                generated++;

                if (generated >= cap || generated >= SERIES_CAP) break outerLoop;
            }
            weekOffset++;
        }

        return result;
    }

    /** Sort DayOfWeek values Monday-first. */
    private List<DayOfWeek> sortedDays(Set<DayOfWeek> days) {
        List<DayOfWeek> list = new ArrayList<>(days);
        list.sort(Comparator.comparingInt(DayOfWeek::getValue));
        return list;
    }

    private LocalDateTime advance(LocalDateTime dt, Frequency freq, int interval) {
        switch (freq) {
            case DAILY:   return dt.plusDays(interval);
            case WEEKLY:  return dt.plusWeeks(interval);
            case MONTHLY: return dt.plusMonths(interval);
            case YEARLY:  return dt.plusYears(interval);
            default:      return dt.plusDays(interval);
        }
    }

    // ---------------------------------------------------------------
    // CSV export helper
    // ---------------------------------------------------------------

    private String formatCSVRow(CalendarEvent ev) {
        String subject    = csvEscape(ev.getSubject());
        String startDate  = ev.getStartDateTime().format(CSV_DATE_FMT);
        String startTime  = ev.getStartDateTime().format(CSV_TIME_FMT);
        String endDate    = ev.getEndDateTime() != null
                ? ev.getEndDateTime().format(CSV_DATE_FMT) : startDate;
        String endTime    = ev.getEndDateTime() != null
                ? ev.getEndDateTime().format(CSV_TIME_FMT) : startTime;
        String allDay     = ev.isAllDay() ? "True" : "False";
        String desc       = csvEscape(ev.getDescription() != null ? ev.getDescription() : "");
        String loc        = csvEscape(ev.getLocation() != null ? ev.getLocation() : "Online");

        return String.join(",",
                subject, startDate, startTime, endDate, endTime, allDay, desc, loc, "False");
    }

    private String csvEscape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
