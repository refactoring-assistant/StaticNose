package calendar.controller.text.commandfactory;

import calendar.model.CalendarManagerInterface;
import calendar.view.CalendarViewInterface;
import java.util.Map;

/**
 * Interface which is used to get an executable method according to the input
 * command chain to delegate the model to execute it.
 */
public interface CommandFactory {

  /**
   * Method to get the executable class from the command factory by parsing the input command.
   *
   * @param parameters all the placeholder values with the command name
   * @param calendar   the active calendar
   * @param view       the calendar view
   * @return a command factory class which can execute the command
   */
  CommandInterface getExecutable(Map<String, Object> parameters, CalendarManagerInterface calendar,
                                 CalendarViewInterface view);
}
