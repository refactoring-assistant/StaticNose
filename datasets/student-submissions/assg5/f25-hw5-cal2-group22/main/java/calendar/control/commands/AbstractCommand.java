package calendar.control.commands;

import calendar.control.results.CommandResult;
import calendar.model.database.IcalendarDatabase;
import java.util.Set;

/**
 * The abstract class for commands.
 */
public abstract class AbstractCommand implements Icommand {
  protected final IcalendarDatabase multipleCalendar;
  protected final String input;

  /**
   * Creates an abstract command constructor.
   *
   * @param multipleCalendar the calendar model
   * @param input            the raw user command
   */
  public AbstractCommand(IcalendarDatabase multipleCalendar, String input) {
    this.multipleCalendar = multipleCalendar;
    this.input = input;
  }


  /**
   * Executes the command using the model and/or view provided at construction time.
   *
   * @return CommandResult indicating success or failure with appropriate message
   */
  @Override
  public abstract CommandResult execute();

  /**
   * The method used by all commands to return the available calendar names.
   *
   * @return - string of calendar names that are in the database
   */
  protected String getAvailableCalendarNames() {
    Set<String> calendarNames = multipleCalendar.getCalendarNames();
    return String.join(", ", calendarNames);
  }
}
