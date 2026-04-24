package calendar.model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * A concrete implementation of the CalendarModel interface.
 * Stores events and provides methods to create, edit, retrieve, and export events.
 */
public class CalendarModelImpl implements CalendarModel {

  private final List<Event> events;

  /**
   * Creates a new CalendarModelImpl.
   */
  public CalendarModelImpl() {
    this.events = new ArrayList<>();
  }

  /**
   * Creates an event in the calendar.
   *
   * @param event the event to create
   * @throws IllegalArgumentException if the event conflicts or duplicates with an existing event
   */
  @Override
  public void createEvent(Event event) throws IllegalArgumentException {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }

    if (hasDuplicate(event)) {
      throw new IllegalArgumentException("Event conflicts with an existing event");
    }

    events.add(event);
    sortEvents();
  }


  /**
   * Creates a series of events in the calendar.
   *
   * @param series the series of events to create
   * @throws IllegalArgumentException if the series conflicts or duplicates with an existing event
   */
  @Override
  public void createSeries(EventSeries series) throws IllegalArgumentException {
    if (series == null) {
      throw new IllegalArgumentException("Series cannot be null");
    }

    List<Event> instances = series.generateInstances();

    for (Event event : instances) {
      if (hasDuplicate(event)) {
        throw new IllegalArgumentException("Series already exists");
      }
    }

    events.addAll(instances);
    sortEvents();

  }

  /**
   * Edits an event in the calendar.
   *
   * @param property      the property to edit ("subject", "start", "end", "description",
   *                      "location", "status")
   * @param subject       the subject of the event
   * @param startDateTime the start date and time of the event
   * @param newValue      the new value of the property
   * @param mode          the edit mode: "event", "events" (this and future), or "series"
   *                      (all in series)
   * @throws IllegalArgumentException if the edit conflicts or duplicates with an existing event
   */
  @Override
  public void editEvent(String property, String subject, LocalDateTime startDateTime,
                        String newValue, String mode) throws IllegalArgumentException {
    if (property == null || subject == null || startDateTime == null || newValue == null
        || mode == null) {
      throw new IllegalArgumentException("Invalid arguments");
    }

    List<Event> matchingEvents = events.stream()
        .filter(e -> e.getSubject().equalsIgnoreCase(subject)
            && e.getStart().equals(startDateTime)).collect(Collectors.toList());

    if (matchingEvents.isEmpty()) {
      throw new IllegalArgumentException("No matching events found");
    }

    Event target = matchingEvents.get(0);
    String seriesId = target.getSeriesId();

    List<Event> eventsToEdit = new ArrayList<Event>();

    if (mode.equalsIgnoreCase("event")) {
      eventsToEdit.add(target);
    } else if ("events".equalsIgnoreCase(mode) && seriesId != null) {
      for (Event e : events) {
        if (seriesId.equals(e.getSeriesId())
            && !e.getStart().isBefore(startDateTime)) {
          eventsToEdit.add(e);
        }
      }
    } else if ("series".equalsIgnoreCase(mode) && seriesId != null) {
      for (Event e : events) {
        if (seriesId.equals(e.getSeriesId())) {
          eventsToEdit.add(e);
        }
      }
    } else if (!"event".equalsIgnoreCase(mode)) {
      throw new IllegalArgumentException("Invalid edit mode: " + mode);
    }

    for (Event e : eventsToEdit) {
      Event updated = e.editProperty(property, newValue);
      if (hasDuplicateExcluding(updated, e)) {
        throw new IllegalArgumentException("Edit creates duplicate conflict.");
      }
      events.remove(e);
      events.add(updated);
    }
    sortEvents();
  }

  /**
   * Gets all events on a given date.
   *
   * @param date the date to get events for
   * @return a list of events on the given date
   */
  @Override
  public List<Event> getEventsOn(LocalDate date) {
    return events.stream().filter(e -> e.occursOn(date))
        .sorted()
        .collect(Collectors.toList());
  }

  /**
   * Gets all events between two dates.
   *
   * @param start the start date
   * @param end   the end date
   * @return a list of events between the two dates
   */
  @Override
  public List<Event> getEventsBetween(LocalDateTime start, LocalDateTime end) {
    return events.stream()
        .filter(e -> e.overlaps(start, end))
        .sorted()
        .collect(Collectors.toList());
  }

  /**
   * Checks if a given time is busy.
   *
   * @param time the time to check
   * @return true if the time is busy, false otherwise
   */
  @Override
  public boolean isBusy(LocalDateTime time) {
    for (Event e : events) {
      if (e.contains(time)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Exports the calendar to a CSV file.
   *
   * @param filename the filename to export to
   * @throws IOException if there is an error writing to the file
   */
  @Override
  public void exportToCsv(String filename) throws IOException {
    Path path = Path.of(filename).toAbsolutePath();
    BufferedWriter writer = new BufferedWriter(new FileWriter(path.toFile()));
    try {
      writer.write(
          "Subject, Start Date, Start Time, End Date, End Time, Description, Location, Status\n");
      for (Event e : events) {
        String[] csv = {
            quote(e.getSubject()),
            e.getStart().toLocalDate().toString(),
            e.getStart().toLocalTime().toString(),
            e.getEnd().toLocalDate().toString(),
            e.getEnd().toLocalTime().toString(),
            quoteOrEmpty(e.getDescription()),
            quoteOrEmpty(e.getLocation()),
            quoteOrEmpty(e.getStatus())
        };
        writer.write(String.join(",", csv));
        writer.newLine();
      }
    } finally {
      writer.close();
    }
    System.out.println("Exporting " + events.size() + " events...");
    System.out.println("Calendar exported to " + path);
  }

  /**
   * Gets all events in the calendar.
   *
   * @return a list of all events in the calendar
   */
  @Override
  public List<Event> getAllEvents() {
    return Collections.unmodifiableList(events);
  }


  private boolean hasDuplicate(Event event) {
    return events.stream().anyMatch(e ->
        e.getSubject().equalsIgnoreCase(event.getSubject())
            && e.getStart().equals(event.getStart())
            && e.getEnd().equals(event.getEnd()));
  }

  private void sortEvents() {
    events.sort((e1, e2) -> e1.getStart().compareTo(e2.getStart()));
  }

  private boolean hasDuplicateExcluding(Event newEvent, Event oldEvent) {
    for (Event e : events) {
      if (e != oldEvent
          && e.getSubject().equalsIgnoreCase(newEvent.getSubject())
          && e.getStart().equals(newEvent.getStart())
          && e.getEnd().equals(newEvent.getEnd())) {
        return true;
      }
    }
    return false;
  }

  private String quote(String s) {
    return "\"" + s.replace("\"", "\"\"") + "\"";
  }

  private String quoteOrEmpty(String s) {
    return (s == null || s.trim().isEmpty()) ? "" : quote(s);
  }
}
