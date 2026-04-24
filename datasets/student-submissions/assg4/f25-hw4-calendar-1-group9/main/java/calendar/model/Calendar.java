package calendar.model;

import calendar.exception.DuplicateEventException;
import calendar.exception.EventNotFoundException;
import calendar.repository.InEventRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Calendar implementation that manages events through a repository.
 * Provides operations for adding, removing, querying, and checking busy status.
 */
public class Calendar implements InCalendar {

  private final String calendarName;
  private final InEventRepository repository;

  /**
   * Constructs a Calendar with a name and repository.
   *
   * @param calendarName the name of the calendar
   * @param repository   the event repository for storage
   */
  public Calendar(String calendarName, InEventRepository repository) {
    if (calendarName == null || calendarName.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    if (repository == null) {
      throw new IllegalArgumentException("Repository cannot be null");
    }
    this.calendarName = calendarName;
    this.repository = repository;
  }

  @Override
  public String getCalendarName() {
    return calendarName;
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
  public List<InEvent> getEventsOnDate(LocalDate date) {
    if (date == null) {
      throw new IllegalArgumentException("Date cannot be null");
    }

    return repository.findAll().stream()
        .filter(e -> e.occursOn(date))
        .collect(Collectors.toList());
  }

  @Override
  public List<InEvent> getEventsBetween(LocalDateTime start, LocalDateTime end) {
    if (start == null || end == null) {
      throw new IllegalArgumentException("Start and end cannot be null");
    }

    if (start.isAfter(end)) {
      throw new IllegalArgumentException("Start cannot be after end");
    }

    return repository.findByDateRange(start, end);
  }

  @Override
  public boolean isBusyAt(LocalDateTime dateTime) {
    if (dateTime == null) {
      throw new IllegalArgumentException("DateTime cannot be null");
    }

    return repository.findAll().stream()
        .anyMatch(e -> !e.getEndDateTime().isBefore(dateTime)
            && !e.getStartDateTime().isAfter(dateTime));
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
}