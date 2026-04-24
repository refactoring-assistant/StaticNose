package calendar.view.mockview;

import calendar.controller.guicontroller.ViewListener;
import calendar.view.CalendarGuiView;
import calendar.view.ViewEvent;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Mock implementation of CalendarGuiView for testing purposes.
 * Records method calls and parameters for verification in tests.
 */
public class CalendarGuiViewMock implements CalendarGuiView {

  private final Appendable log;
  public LocalDate currentMonth = LocalDate.now();
  public LocalDate selectedDate = LocalDate.now();
  public String timezone = ZoneId.systemDefault().getId();

  /**
   * Constructor for Mock View.
   *
   * @param log appendable to parse the output.
   */
  public CalendarGuiViewMock(Appendable log) {
    this.log = log;
  }

  @Override
  public void display() {
    appendToLog("View Displayed");
  }

  @Override
  public void setCalendars(List<String> calendarNames, String activeCalendarName) {
    if (calendarNames.isEmpty()) {
      throw new IllegalArgumentException("Calendar names list cannot be empty");
    }
    List<String> sorted = new ArrayList<>(calendarNames);
    Collections.sort(sorted);

    if (!calendarNames.equals(sorted)) {
      throw new IllegalArgumentException("Calendar names list sorts are not equal");
    }

    appendToLog("setCalendars called");
  }

  @Override
  public void setMonthEvents(LocalDate month, Map<LocalDate, List<ViewEvent>> events) {
    appendToLog("setMonthEvents called");
  }

  @Override
  public void setDayEvents(LocalDate date, List<ViewEvent> events) {
    appendToLog("setDayEvents called");
  }

  @Override
  public void addViewListener(ViewListener listener) {
    appendToLog("addViewListener called");
  }

  @Override
  public LocalDate getCurrentMonth() {
    appendToLog("getCurrentMonth called");
    return currentMonth;
  }

  @Override
  public LocalDate getSelectedDate() {
    appendToLog("getSelectedDate called");
    return selectedDate;
  }

  @Override
  public void updateTimezone(String timezone) {
    this.timezone = timezone;
    appendToLog("updateTimezone called");
  }

  @Override
  public void updateCurrentMonth(LocalDate month) {
    this.currentMonth = month;
    appendToLog("updateCurrentMonth called");
  }

  @Override
  public void calendarRename(String oldName, String newName) {
    appendToLog("calendarRename called");
  }

  @Override
  public void render(String message) {
    appendToLog("render called: " + message);
  }

  @Override
  public void renderError(String message) {
    appendToLog("renderError called: " + message);
  }

  private void appendToLog(String message) {
    try {
      log.append(message).append(System.lineSeparator());
    } catch (IOException e) {
      throw new IllegalArgumentException("Error: Could not write to log");
    }
  }
}
