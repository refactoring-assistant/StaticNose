package calendar.command;

import calendar.exception.CalendarException;
import calendar.exception.CalendarNotFoundException;
import calendar.model.CalendarDatabase;
import calendar.model.InCalendar;
import calendar.view.InCalendarView;
import java.util.Objects;

/**
 * Command to set the active calendar context.
 * After executing this command, all event operations (create, edit, query, export)
 * will operate on this calendar until another calendar is made active.
 * Active calendar is tracked in CalendarDatabase using
 * an activeCalendar reference.
 */
public class UseCalendarCommand implements InCommand {

  private final CalendarDatabase calendarDatabase;
  private final InCalendarView view;
  private final String calendarName;

  /**
   * Constructs a UseCalendarCommand.
   *
   * @param calendarDatabase the calendar database
   * @param view the view for displaying messages
   * @param calendarName the name of calendar to make active
   */
  public UseCalendarCommand(CalendarDatabase calendarDatabase,
                            InCalendarView view,
                            String calendarName) {
    Objects.requireNonNull(calendarDatabase, "CalendarDatabase cannot be null");
    Objects.requireNonNull(view, "View cannot be null");
    Objects.requireNonNull(calendarName, "Calendar name cannot be null");

    this.calendarDatabase = calendarDatabase;
    this.view = view;
    this.calendarName = calendarName.trim();
  }

  @Override
  public void execute() throws CalendarException {
    try {
      calendarDatabase.setActiveCalendar(calendarName);
      InCalendar calendar = calendarDatabase.getActiveCalendar();

      view.displaySuccess("Now using calendar: " + calendarName);
      view.displayMessage("Events: " + calendar.getAllEvents().size());
    } catch (CalendarNotFoundException e) {
      throw new CalendarException(
          "Calendar not found: " + calendarName
              + ". Use 'create calendar --name " + calendarName
              + " --timezone <timezone>' to create it first.", e);
    }
  }

  @Override
  public String getDescription() {
    return "Use calendar: " + calendarName;
  }
}