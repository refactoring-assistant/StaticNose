package calendar.model;

import calendar.exception.DuplicateEventException;
import calendar.exception.EventNotFoundException;
import calendar.repository.InEventRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Calendar implementation that manages events through a repository.
 * Provides operations for adding, removing, querying, and checking busy status.
 * - Timezone is managed externally in CalendarDatabase
 * - Added setCalendarName() for calendar renaming
 */
public class Calendar implements InCalendar {

  private String calendarName;
  private final InEventRepository repository;

  /**
   * Constructs a Calendar with a name and repository.
   *
   * @param calendarName the name of the calendar
   * @param repository   the event repository for storage
   */
  public Calendar(String calendarName, InEventRepository repository) {
    if (calendarName == null) {
      throw new IllegalArgumentException("Calendar name cannot be null");
    }
    if (repository == null) {
      throw new IllegalArgumentException("Repository cannot be null");
    }
    if (calendarName.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty");
    }
    this.calendarName = calendarName.trim();
    this.repository = repository;
  }

  @Override
  public String getCalendarName() {
    return calendarName;
  }

  @Override
  public void setCalendarName(String newName) {
    if (newName == null) {
      throw new IllegalArgumentException("Calendar name cannot be null");
    }
    if (newName.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be empty");
    }

    this.calendarName = newName.trim();
  }

  @Override
  public void addEvent(InEvent event) throws DuplicateEventException {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }
    if (repository.exists(event.getSubject(),
        event.getStartDateTime(),
        event.getEndDateTime())) {
      throw new DuplicateEventException(
          "Event with same subject, start, and end already exists: " + event.getSubject());
    }

    repository.save(event);
  }

  @Override
  public void removeEvent(InEvent event) throws EventNotFoundException {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }
    if (!repository.exists(event.getSubject(),
        event.getStartDateTime(),
        event.getEndDateTime())) {
      throw new EventNotFoundException("Event not found in calendar: " + event.getSubject());
    }

    repository.delete(event);
  }

  @Override
  public List<InEvent> getAllEvents() {
    return repository.findAll();
  }

  @Override
  public InEvent findEvent(String subject, LocalDateTime start, LocalDateTime end) {
    if (subject == null || start == null || end == null) {
      return null;
    }
    return repository.findBySubjectAndDateTime(subject, start, end).orElse(null);
  }

  @Override
  public String toString() {
    return "Calendar{name='" + calendarName + "', events=" + repository.findAll().size() + "}";
  }

  /**
   * Filters events using a predicate.
   * Allows flexible querying without adding new methods.
   */
  @Override
  public List<InEvent> filterEvents(Predicate<InEvent> predicate) {
    if (predicate == null) {
      throw new IllegalArgumentException("Predicate cannot be null");
    }
    return repository.findAll().stream()
        .filter(predicate)
        .collect(Collectors.toList());
  }

  @Override
  public List<InEvent> getEventsOnDate(LocalDate date) {
    if (date == null) {
      throw new IllegalArgumentException("Date cannot be null");
    }
    return filterEvents(event -> event.occursOn(date));
  }

  @Override
  public List<InEvent> getEventsBetween(LocalDateTime start, LocalDateTime end) {
    if (start == null) {
      throw new IllegalArgumentException("Start cannot be null");
    }
    if (end == null) {
      throw new IllegalArgumentException("End cannot be null");
    }
    if (start.isAfter(end)) {
      throw new IllegalArgumentException("Start cannot be after end");
    }
    return filterEvents(event -> event.occursBetween(start, end));
  }

  @Override
  public boolean isBusyAt(LocalDateTime dateTime) {
    if (dateTime == null) {
      throw new IllegalArgumentException("DateTime cannot be null");
    }
    return filterEvents(event ->
        !event.getEndDateTime().isBefore(dateTime)
            && !event.getStartDateTime().isAfter(dateTime))
        .size() > 0;
  }
}