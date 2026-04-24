package calendar.controller.command;

import calendar.exceptions.DuplicateEventException;
import calendar.model.calendar.Icalendar;
import calendar.view.IcalendarView;

/**
 * Interface for Command related classes.
 */
public interface Icommand {
  /**
   * Execute this command on the given calendar & calendar.view.
   *
   * @param calendar the calendar object to operate on
   * @param view the calendar.view object to operate on
   * @throws Exception if command fails to execute
   */
  void execute(Icalendar calendar, IcalendarView view) throws Exception, DuplicateEventException;
}

