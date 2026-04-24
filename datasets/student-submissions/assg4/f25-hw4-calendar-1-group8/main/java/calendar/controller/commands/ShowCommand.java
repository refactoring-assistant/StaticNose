package calendar.controller.commands;

import calendar.controller.utils.CommandParserUtils;
import calendar.model.InterfaceCalendarModel;
import calendar.view.InterfaceCalendarView;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Command to handle showing user status on a particular datetime.
 */
public class ShowCommand implements CommandInterface {

  private final CommandParserUtils utils;

  /**
   * Sets up the show command with parser utilities.
   *
   * @param utils the parser utilities to use
   */
  public ShowCommand(CommandParserUtils utils) {
    this.utils = utils;
  }

  @Override
  public void execute(InterfaceCalendarModel model, InterfaceCalendarView view, List<String> args)
      throws Exception {
    if (args.size() != 3 || !args.get(0).equals("status") || !args.get(1).equals("on")) {
      throw new Exception("Usage: show status on YYYY-MM-DDTHH:MM");
    }
    ZonedDateTime dateTime = utils.parseDateTimeToZonedDateTime(args.get(2));
    boolean isBusy = model.isBusy(dateTime);
    view.displayStatus(isBusy ? "Busy" : "Available");
  }
}