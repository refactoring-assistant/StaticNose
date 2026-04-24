package calendar.model.testing;

import calendar.model.interfaces.AdvancedCalendar;
import calendar.model.interfaces.CalendarContainer;
import java.io.IOException;
import java.util.Map;

/**
 * A mock implementation of CalendarContainer used for testing controller interactions.
 */
public class MockCalendarContainer implements CalendarContainer {

  private final Appendable log;

  /**
   * Creates a mock calendar container that logs called methods to the given Appendable.
   *
   * @param log the output to record method calls
   */
  public MockCalendarContainer(Appendable log) {
    this.log = log;
  }

  @Override
  public void addCalendar(String name, AdvancedCalendar advancedCalendar) {
    appendToLog("Call reached addCalendar");
  }

  @Override
  public void updateCalendar(String name, String property, String newValue) {
    appendToLog("Call reached updateCalendar");
  }

  @Override
  public AdvancedCalendar getActiveCalendar() {
    appendToLog("Call reached getActiveCalendar");
    return null;
  }

  @Override
  public void setActiveCalendar(String name) {
    appendToLog("Call reached setActiveCalendar");
  }

  @Override
  public Map<String, AdvancedCalendar> getCalendars() {
    appendToLog("Call reached getCalendars");
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
