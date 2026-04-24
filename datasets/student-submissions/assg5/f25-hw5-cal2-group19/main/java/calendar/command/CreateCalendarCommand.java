package calendar.command;

import calendar.exception.CalendarException;
import calendar.model.Calendar;
import calendar.model.CalendarDatabase;
import calendar.model.InCalendar;
import calendar.repository.InMemoryEventRepository;
import calendar.view.InCalendarView;
import java.time.ZoneId;
import java.util.Objects;

/**
 * Command to create a new calendar in the database.
 * - Creates calendar with unique name and timezone
 * - Adds to CalendarDatabase (not storing timezone in Calendar object)
 * - Validates timezone format (IANA Time Zone Database format)
 */
public class CreateCalendarCommand implements InCommand {

  private final CalendarDatabase calendarDatabase;
  private final InCalendarView view;
  private final String calendarName;
  private final ZoneId timezone;

  /**
   * Constructs a CreateCalendarCommand.
   *
   * @param calendarDatabase the database to add calendar to
   * @param view the view for displaying messages
   * @param calendarName the unique name for the new calendar
   * @param timezone the timezone for this calendar
   */
  public CreateCalendarCommand(CalendarDatabase calendarDatabase,
                               InCalendarView view,
                               String calendarName,
                               ZoneId timezone) {
    Objects.requireNonNull(calendarDatabase, "CalendarDatabase cannot be null");
    Objects.requireNonNull(view, "View cannot be null");
    Objects.requireNonNull(calendarName, "Calendar name cannot be null");
    Objects.requireNonNull(timezone, "Timezone cannot be null");

    this.calendarDatabase = calendarDatabase;
    this.view = view;
    this.calendarName = calendarName.trim();
    this.timezone = timezone;
  }

  @Override
  public void execute() throws CalendarException {
    InCalendar newCalendar = new Calendar(calendarName, new InMemoryEventRepository());

    calendarDatabase.addCalendar(calendarName, newCalendar, timezone);

    view.displaySuccess("Calendar created: " + calendarName
        + " (Timezone: " + timezone.getId() + ")");
  }

  @Override
  public String getDescription() {
    return "Create calendar: " + calendarName + " with timezone " + timezone.getId();
  }
}