package calendar.manager;

import calendar.model.CalendarModel;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation of CalendarManager.
 * Maintains a set of calendars uniquely identified by name.
 */
public class CalendarManagerImpl implements CalendarManager {

  private final Map<String, CalendarModel> calendars = new LinkedHashMap<>();
  private String selectedCalendar = null;

  @Override
  public void createCalendar(String name, CalendarModel model) {
    validateName(name);

    if (calendars.containsKey(name)) {
      throw new IllegalArgumentException("A calendar already exists with name: " + name);
    }

    calendars.put(name, model);
    if (selectedCalendar == null) {
      selectedCalendar = name;
    }
  }

  @Override
  public void selectCalendar(String name) {
    if (!calendars.containsKey(name)) {
      throw new IllegalArgumentException("No calendar exists with name: " + name);
    }
    selectedCalendar = name;
  }

  @Override
  public void renameCalendar(String oldName, String newName) {
    validateName(newName);

    if (!calendars.containsKey(oldName)) {
      throw new IllegalArgumentException("No calendar exists with name: " + oldName);
    }

    if (calendars.containsKey(newName)) {
      throw new IllegalArgumentException("A calendar already exists with name: " + newName);
    }

    CalendarModel model = calendars.remove(oldName);
    calendars.put(newName, model);
    if (oldName.equals(selectedCalendar)) {
      selectedCalendar = newName;
    }
  }

  @Override
  public void deleteCalendar(String name) {
    if (!calendars.containsKey(name)) {
      throw new IllegalArgumentException("No calendar exists with name: " + name);
    }

    calendars.remove(name);
    if (name.equals(selectedCalendar)) {
      selectedCalendar = null;
    }
  }

  @Override
  public CalendarModel getSelectedCalendar() {
    if (selectedCalendar == null) {
      throw new IllegalStateException("No calendar currently selected.");
    }
    return calendars.get(selectedCalendar);
  }

  @Override
  public List<String> listCalendars() {
    return calendars.keySet().stream().collect(Collectors.toList());
  }

  /**
   * This method validates calendar names.
   */
  private void validateName(String name) {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
  }

  /**
   * Retrieves a calendar by its name.
   *
   * @param name the name of the calendar
   * @return the calendar associated with the given name
   * @throws IllegalArgumentException if no calendar with the given name exists
   */
  public CalendarModel getCalendar(String name) {
    CalendarModel cal = calendars.get(name);
    if (cal == null) {
      throw new IllegalArgumentException("Calendar not found: " + name);
    }
    return cal;
  }
}