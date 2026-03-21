package calendar.controller;

import calendar.model.*;
import calendar.view.ICalendarView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses text commands and delegates to the calendar model and view.
 *
 * Supported commands:
 *   make event <subject> from <YYYY-MM-DDThh:mm> to <YYYY-MM-DDThh:mm> [repeats <FREQ> <pattern>]
 *   make event <subject> on <YYYY-MM-DD> [repeats <FREQ> <pattern>]
 *   change event  <property> <subject> from <datetime> to <datetime> with <value>
 *   change events <property> <subject> from <datetime> with <value>
 *   change series <property> <subject> from <datetime> with <value>
 *   print events on <YYYY-MM-DD>
 *   print events from <datetime> to <datetime>
 *   export cal <filename.csv>
 *   exit
 */
public class CalendarController implements ICalendarController {

    private final ICalendar model;
    private final ICalendarView view;

    public CalendarController(ICalendar model, ICalendarView view) {
        this.model = model;
        this.view  = view;
    }

    // ---------------------------------------------------------------
    // ICalendarController
    // ---------------------------------------------------------------

    @Override
    public boolean processCommand(String command) {
        if (command == null) return true;
        String trimmed = command.trim();
        if (trimmed.isEmpty()) return true;

        String[] tokens = tokenize(trimmed);
        if (tokens.length == 0) return true;

        try {
            String first = tokens[0].toLowerCase();
            switch (first) {
                case "exit":
                    return false;

                case "make":
                    handleMake(tokens);
                    break;

                case "change":
                    handleChange(tokens);
                    break;

                case "print":
                    handlePrint(tokens);
                    break;

                case "export":
                    handleExport(tokens);
                    break;

                default:
                    view.displayError("Unknown command: " + tokens[0]);
            }
        } catch (CalendarException e) {
            view.displayError(e.getMessage());
        } catch (Exception e) {
            view.displayError("Unexpected error: " + e.getMessage());
        }
        return true;
    }

    @Override
    public void runInteractive() {
        view.displayMessage("Calendar App started. Type 'exit' to quit.");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                boolean cont = processCommand(line);
                if (!cont) {
                    view.displayMessage("Goodbye.");
                    break;
                }
            }
        } catch (IOException e) {
            view.displayError("Failed to read input: " + e.getMessage());
        }
    }

    @Override
    public void runHeadless(String filename) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            view.displayError("Failed to read commands file: " + e.getMessage());
            return;
        }

        // Validate: must contain an "exit" command
        int exitIdx = -1;
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).trim().equalsIgnoreCase("exit")) {
                exitIdx = i;
                break;
            }
        }
        if (exitIdx < 0) {
            view.displayError("Headless mode requires an 'exit' command in the commands file.");
            return;
        }

        if (exitIdx < lines.size() - 1) {
            view.displayWarning("Commands after 'exit' will be ignored.");
        }

        for (int i = 0; i <= exitIdx; i++) {
            String line = lines.get(i);
            boolean cont = processCommand(line);
            if (!cont) {
                break;
            }
        }
    }

    // ---------------------------------------------------------------
    // Command handlers
    // ---------------------------------------------------------------

    /**
     * Handles "make event ..." commands.
     * Tokens: [make, event, <subject>, from|on, ...]
     */
    private void handleMake(String[] tokens) throws CalendarException {
        if (tokens.length < 2 || !tokens[1].equalsIgnoreCase("event")) {
            throw new CalendarException("Invalid make command. Usage: make event ...");
        }
        if (tokens.length < 4) {
            throw new CalendarException("Incomplete make event command");
        }

        // tokens[2] = subject, tokens[3] = from|on
        String subject = tokens[2];
        String dateKeyword = tokens[3].toLowerCase();

        if (dateKeyword.equals("from")) {
            // make event <subject> from <start> to <end> [repeats <FREQ> <pattern...>]
            // tokens: [make, event, subject, from, start, to, end, (repeats, FREQ, ...)]
            requireTokens(tokens, 7, "make event <subject> from <start> to <end>");
            LocalDateTime start = parseDateTime(tokens[4]);
            expectToken(tokens, 5, "to");
            LocalDateTime end   = parseDateTime(tokens[6]);

            if (tokens.length > 7 && tokens[7].equalsIgnoreCase("repeats")) {
                // Series timed event
                requireTokens(tokens, 9, "make event ... repeats <FREQ> ...");
                Frequency freq = Frequency.fromString(tokens[8]);
                RecurrencePattern pattern = RecurrencePattern.parse(freq, tokens, 9);
                validateSeriesEvent(start, end);
                CalendarEvent template = new CalendarEvent(subject, start, end);
                List<CalendarEvent> generated = createSeriesAndCollect(template, pattern);
                String sid = generated.isEmpty() ? "none" : generated.get(0).getSeriesId();
                view.displaySeriesCreated(generated.size(), sid);
            } else {
                CalendarEvent event = new CalendarEvent(subject, start, end);
                model.addEvent(event);
                view.displayEventCreated(event);
            }

        } else if (dateKeyword.equals("on")) {
            // make event <subject> on <YYYY-MM-DD> [repeats <FREQ> <pattern...>]
            requireTokens(tokens, 5, "make event <subject> on <YYYY-MM-DD>");
            LocalDate date = parseDate(tokens[4]);

            if (tokens.length > 5 && tokens[5].equalsIgnoreCase("repeats")) {
                requireTokens(tokens, 7, "make event ... repeats <FREQ> ...");
                Frequency freq = Frequency.fromString(tokens[6]);
                RecurrencePattern pattern = RecurrencePattern.parse(freq, tokens, 7);
                // All-day series: same day start/end
                CalendarEvent template = new CalendarEvent(subject, date);
                List<CalendarEvent> generated = createSeriesAndCollect(template, pattern);
                String sid = generated.isEmpty() ? "none" : generated.get(0).getSeriesId();
                view.displaySeriesCreated(generated.size(), sid);
            } else {
                CalendarEvent event = new CalendarEvent(subject, date);
                model.addEvent(event);
                view.displayEventCreated(event);
            }
        } else {
            throw new CalendarException("Expected 'from' or 'on' after subject, got: " + tokens[3]);
        }
    }

    /**
     * Validate that a series event spans at most one day.
     */
    private void validateSeriesEvent(LocalDateTime start, LocalDateTime end) throws CalendarException {
        if (!start.toLocalDate().equals(end.toLocalDate())) {
            throw new CalendarException("Series events can only span one day (start and end must be on the same day)");
        }
    }

    /**
     * Add event series to the model and return the list of newly-generated events.
     * We snapshot the event count before and after, then identify new events by their
     * shared seriesId (all new events in a single addEventSeries call share one UUID).
     */
    private List<CalendarEvent> createSeriesAndCollect(CalendarEvent template,
                                                        RecurrencePattern pattern)
            throws CalendarException {
        List<CalendarEvent> before = new ArrayList<>(model.getAllEvents());
        model.addEventSeries(template, pattern);
        List<CalendarEvent> after = model.getAllEvents();

        // Collect events present in 'after' but not in 'before' (by identity is unreliable
        // since getAllEvents returns copies; compare by subject+start+seriesId presence).
        // Simplest reliable approach: the new events are those beyond the previous count,
        // after sorting – but sorting may interleave them. Instead, identify the new seriesId.
        // All new events share one seriesId that did not exist before.
        java.util.Set<String> oldSids = new java.util.HashSet<>();
        for (CalendarEvent ev : before) {
            if (ev.getSeriesId() != null) oldSids.add(ev.getSeriesId());
        }

        String newSid = null;
        for (CalendarEvent ev : after) {
            if (ev.getSeriesId() != null && !oldSids.contains(ev.getSeriesId())) {
                newSid = ev.getSeriesId();
                break;
            }
        }

        if (newSid == null) {
            // Fallback: return events beyond previous count (sorted list may interleave,
            // but this handles the edge case where seriesId is somehow already known)
            return after.size() > before.size()
                    ? after.subList(before.size(), after.size())
                    : new ArrayList<>();
        }

        final String sid = newSid;
        List<CalendarEvent> added = new ArrayList<>();
        for (CalendarEvent ev : after) {
            if (sid.equals(ev.getSeriesId())) added.add(ev);
        }
        return added;
    }

    /**
     * Handles "change event|events|series ..." commands.
     */
    private void handleChange(String[] tokens) throws CalendarException {
        if (tokens.length < 2) {
            throw new CalendarException("Incomplete change command");
        }
        String scope = tokens[1].toLowerCase();
        switch (scope) {
            case "event":
                handleChangeEvent(tokens);
                break;
            case "events":
                handleChangeEvents(tokens);
                break;
            case "series":
                handleChangeSeries(tokens);
                break;
            default:
                throw new CalendarException("Unknown change scope: " + tokens[1]
                        + ". Use: event, events, or series");
        }
    }

    /**
     * change event <property> <subject> from <datetime> to <datetime> with <value>
     * tokens: [change, event, property, subject, from, start, to, end, with, value]
     */
    private void handleChangeEvent(String[] tokens) throws CalendarException {
        requireTokens(tokens, 10, "change event <property> <subject> from <start> to <end> with <value>");
        String property = tokens[2];
        String subject  = tokens[3];
        expectToken(tokens, 4, "from");
        LocalDateTime start = parseDateTime(tokens[5]);
        expectToken(tokens, 6, "to");
        LocalDateTime end   = parseDateTime(tokens[7]);
        expectToken(tokens, 8, "with");
        String value = tokens[9];

        model.editSingleEvent(subject, start, end, property, value);
        view.displayMessage("Event updated: " + subject);
    }

    /**
     * change events <property> <subject> from <datetime> with <value>
     * tokens: [change, events, property, subject, from, start, with, value]
     */
    private void handleChangeEvents(String[] tokens) throws CalendarException {
        requireTokens(tokens, 8, "change events <property> <subject> from <datetime> with <value>");
        String property = tokens[2];
        String subject  = tokens[3];
        expectToken(tokens, 4, "from");
        LocalDateTime start = parseDateTime(tokens[5]);
        expectToken(tokens, 6, "with");
        String value = tokens[7];

        model.editEventsFrom(subject, start, property, value);
        view.displayMessage("Events updated from " + start + " for subject: " + subject);
    }

    /**
     * change series <property> <subject> from <datetime> with <value>
     * tokens: [change, series, property, subject, from, start, with, value]
     */
    private void handleChangeSeries(String[] tokens) throws CalendarException {
        requireTokens(tokens, 8, "change series <property> <subject> from <datetime> with <value>");
        String property = tokens[2];
        String subject  = tokens[3];
        expectToken(tokens, 4, "from");
        LocalDateTime start = parseDateTime(tokens[5]);
        expectToken(tokens, 6, "with");
        String value = tokens[7];

        model.editAllInSeries(subject, start, property, value);
        view.displayMessage("Series updated for subject: " + subject);
    }

    /**
     * Handles "print events on|from ..." commands.
     */
    private void handlePrint(String[] tokens) throws CalendarException {
        if (tokens.length < 2 || !tokens[1].equalsIgnoreCase("events")) {
            throw new CalendarException("Invalid print command. Usage: print events on|from ...");
        }
        if (tokens.length < 3) {
            throw new CalendarException("Incomplete print command");
        }
        String dateKeyword = tokens[2].toLowerCase();
        if (dateKeyword.equals("on")) {
            requireTokens(tokens, 4, "print events on <YYYY-MM-DD>");
            LocalDate date = parseDate(tokens[3]);
            List<CalendarEvent> evs = model.getEventsOnDate(date);
            if (evs.isEmpty()) {
                view.displayMessage("No events found on " + date);
            } else {
                view.displayEvents(evs);
            }
        } else if (dateKeyword.equals("from")) {
            requireTokens(tokens, 6, "print events from <datetime> to <datetime>");
            LocalDateTime from = parseDateTime(tokens[3]);
            expectToken(tokens, 4, "to");
            LocalDateTime to = parseDateTime(tokens[5]);
            List<CalendarEvent> evs = model.getEventsInRange(from, to);
            if (evs.isEmpty()) {
                view.displayMessage("No events found in range " + from + " to " + to);
            } else {
                view.displayEvents(evs);
            }
        } else {
            throw new CalendarException("Expected 'on' or 'from' after 'print events', got: " + tokens[2]);
        }
    }

    /**
     * Handles "export cal <filename>" command.
     */
    private void handleExport(String[] tokens) throws CalendarException {
        if (tokens.length < 3 || !tokens[1].equalsIgnoreCase("cal")) {
            throw new CalendarException("Invalid export command. Usage: export cal <filename.csv>");
        }
        String filename = tokens[2];
        String path = model.exportToCSV(filename);
        view.displayExportComplete(path);
    }

    // ---------------------------------------------------------------
    // Tokenizer
    // ---------------------------------------------------------------

    /**
     * Tokenize a command string. Double-quoted strings become single tokens (quotes stripped).
     * Single-word tokens are unquoted.
     */
    static String[] tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        int len = input.length();

        for (int i = 0; i < len; i++) {
            char c = input.charAt(i);
            if (c == '"') {
                if (inQuotes) {
                    // End of quoted token
                    tokens.add(current.toString());
                    current.setLength(0);
                    inQuotes = false;
                } else {
                    // Start of quoted token — flush any pending unquoted chars first
                    if (current.length() > 0) {
                        tokens.add(current.toString());
                        current.setLength(0);
                    }
                    inQuotes = true;
                }
            } else if (c == ' ' && !inQuotes) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            tokens.add(current.toString());
        }
        return tokens.toArray(new String[0]);
    }

    // ---------------------------------------------------------------
    // Parsing helpers
    // ---------------------------------------------------------------

    private LocalDateTime parseDateTime(String s) throws CalendarException {
        try {
            return LocalDateTime.parse(s);
        } catch (DateTimeParseException e) {
            throw new CalendarException("Invalid datetime (expected YYYY-MM-DDThh:mm): " + s);
        }
    }

    private LocalDate parseDate(String s) throws CalendarException {
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            throw new CalendarException("Invalid date (expected YYYY-MM-DD): " + s);
        }
    }

    private void requireTokens(String[] tokens, int minCount, String usage)
            throws CalendarException {
        if (tokens.length < minCount) {
            throw new CalendarException("Too few arguments. Usage: " + usage);
        }
    }

    private void expectToken(String[] tokens, int idx, String expected)
            throws CalendarException {
        if (idx >= tokens.length) {
            throw new CalendarException("Expected '" + expected + "' but ran out of tokens");
        }
        if (!tokens[idx].equalsIgnoreCase(expected)) {
            throw new CalendarException("Expected '" + expected + "' but got '" + tokens[idx] + "'");
        }
    }
}
