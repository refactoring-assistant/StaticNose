package calendar.controller.command;

import calendar.model.calendar.Icalendar;
import calendar.view.IcalendarView;
import java.time.LocalDateTime;

/**
 * Command to check if the user is busy at a specific date and time.
 */
public class ShowStatusCommand implements Icommand {

  private final LocalDateTime dateTime;

  /**
   * Constructor for show status command.
   *
   * @param dateTime the date and time to check availability for
   */
  public ShowStatusCommand(LocalDateTime dateTime) {
    this.dateTime = dateTime;
  }

  /**
   * Executes the show status command.
   *
   * @param calendar the calendar to check for busy status
   * @param view the view to display the status to
   * @throws Exception if the status cannot be determined
   */
  @Override
  public void execute(Icalendar calendar, IcalendarView view) throws Exception {
    boolean isBusy = calendar.isBusyAt(dateTime);
    view.displayStatus(isBusy);
  }
}