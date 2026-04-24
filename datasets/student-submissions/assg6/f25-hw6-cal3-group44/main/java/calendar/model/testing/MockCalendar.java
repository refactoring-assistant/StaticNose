package calendar.model.testing;

import calendar.model.interfaces.CalendarEditable;
import calendar.model.interfaces.EventReadOnly;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * A mock implementation of CalendarEditable used for testing method calls and interactions.
 */
class MockCalendar implements CalendarEditable {


  private final Appendable log;

  /**
   * Creates a mock calendar that logs method calls to the given Appendable.
   *
   * @param log the output to record called methods
   */
  public MockCalendar(Appendable log) {
    this.log = log;
  }


  @Override
  public EventReadOnly addEvent(EventReadOnly event) {
    appendToLog("addEvent");
    return event;
  }

  @Override
  public List<EventReadOnly> editEvent(List<EventReadOnly> events,
                                       String property, String newValue) {
    appendToLog("editEvent");
    return events;
  }

  @Override
  public void removeEvent(EventReadOnly event) {
    appendToLog("removeEvent");
  }

  @Override
  public boolean isBusy(LocalDateTime dateTime) {
    appendToLog("isBusy");
    return false;
  }

  @Override
  public void forEachEvent(Consumer<EventReadOnly> consumer) {
    appendToLog("forEachEvent");
  }

  @Override
  public List<EventReadOnly> getEvents(LocalDateTime startDateTime, LocalDateTime endDateTime) {
    appendToLog("getEvents");
    return new ArrayList<>();
  }

  @Override
  public Map<LocalDate, List<EventReadOnly>> getAllEvents() {
    appendToLog("getAllEvents");
    return Map.of();
  }

  private void appendToLog(String message) {
    try {
      log.append(message);
    } catch (IOException e) {
      throw new IllegalArgumentException("Error: Could not write to log");
    }
  }
}
