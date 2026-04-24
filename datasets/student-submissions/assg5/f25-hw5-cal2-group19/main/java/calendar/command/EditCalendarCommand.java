package calendar.command;

import calendar.exception.CalendarException;
import calendar.exception.CalendarNotFoundException;
import calendar.exception.DuplicateCalendarException;
import calendar.model.CalendarDatabase;
import calendar.view.InCalendarView;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Command to edit calendar properties (name or timezone).
 * Supported properties:
 * - name: Renames the calendar
 * - timezone: Changes the calendar's timezone
 */
public class EditCalendarCommand implements InCommand {

  private final CalendarDatabase calendarDatabase;
  private final InCalendarView view;
  private final String calendarName;
  private final String property;
  private final String newValue;

  /**
   * Constructs an EditCalendarCommand.
   *
   * @param calendarDatabase the calendar database
   * @param view the view for displaying messages
   * @param calendarName the name of calendar to edit
   * @param property the property to edit ("name" or "timezone")
   * @param newValue the new value for the property
   */
  public EditCalendarCommand(CalendarDatabase calendarDatabase,
                             InCalendarView view,
                             String calendarName,
                             String property,
                             String newValue) {
    Objects.requireNonNull(calendarDatabase, "CalendarDatabase cannot be null");
    Objects.requireNonNull(view, "View cannot be null");
    Objects.requireNonNull(calendarName, "Calendar name cannot be null");
    Objects.requireNonNull(property, "Property cannot be null");
    Objects.requireNonNull(newValue, "New value cannot be null");

    this.calendarDatabase = calendarDatabase;
    this.view = view;
    this.calendarName = calendarName.trim();
    this.property = property.toLowerCase().trim();
    this.newValue = newValue.trim();
  }

  @Override
  public void execute() throws CalendarException {
    switch (property) {
      case "name":
        editName();
        break;
      case "timezone":
        editTimezone();
        break;
      default:
        throw new CalendarException(
            "Invalid property: " + property + ". Valid properties: name, timezone");
    }
  }

  /**
   * Edits the calendar name.
   */
  private void editName() throws CalendarException {
    try {
      calendarDatabase.renameCalendar(calendarName, newValue);
      view.displaySuccess("Calendar renamed from '" + calendarName
          + "' to '" + newValue + "'");
    } catch (CalendarNotFoundException e) {
      throw new CalendarException("Calendar not found: " + calendarName, e);
    } catch (DuplicateCalendarException e) {
      throw new CalendarException("Calendar name '" + newValue
          + "' already exists", e);
    }
  }

  /**
   * Edits the calendar timezone.
   */
  private void editTimezone() throws CalendarException {
    try {
      ZoneId newTimezone = ZoneId.of(newValue);
      calendarDatabase.setTimezone(calendarName, newTimezone);
      view.displaySuccess("Calendar '" + calendarName
          + "' timezone changed to: " + newTimezone.getId());
    } catch (java.time.DateTimeException e) {
      throw new CalendarException("Invalid timezone: " + newValue
          + ". Must be in IANA format (e.g., America/New_York)", e);
    } catch (CalendarNotFoundException e) {
      throw new CalendarException("Calendar not found: " + calendarName, e);
    }
  }

  @Override
  public String getDescription() {
    return "Edit calendar '" + calendarName + "' property: " + property;
  }
}