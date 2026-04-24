package calendar.controller;

import calendar.controller.commands.CalendarCommand;
import calendar.controller.commands.CopyEventCommand;
import calendar.controller.commands.CopyEventsBetweenCommand;
import calendar.controller.commands.CopyEventsOnCommand;
import calendar.controller.commands.CreateAllDaySeriesCountCommand;
import calendar.controller.commands.CreateAllDaySeriesUntilCommand;
import calendar.controller.commands.CreateAllDaySingleCommand;
import calendar.controller.commands.CreateCalendarCommand;
import calendar.controller.commands.CreateSeriesCountCommand;
import calendar.controller.commands.CreateSeriesUntilCommand;
import calendar.controller.commands.CreateSingleCommand;
import calendar.controller.commands.EditCalendarCommand;
import calendar.controller.commands.EditFromCommand;
import calendar.controller.commands.EditSeriesCommand;
import calendar.controller.commands.EditSingleCommand;
import calendar.controller.commands.ExitCommand;
import calendar.controller.commands.ExportCommand;
import calendar.controller.commands.PrintOnCommand;
import calendar.controller.commands.PrintRangeCommand;
import calendar.controller.commands.StatusCommand;
import calendar.controller.commands.UseCalendarCommand;
import calendar.model.CalendarManager;

/**
 * Handles commands by delegating to appropriate command objects.
 */
public class CommandController {

  private final CalendarManager manager;

  /**
   * Creates a controller with the given manager.
   *
   * @param manager the calendar manager
   */
  public CommandController(CalendarManager manager) {
    this.manager = manager;
  }

  /**
   * Handles a parsed command.
   *
   * @param cmd the parsed command
   * @return result string
   */
  public String handle(ParsedCommand cmd) {
    CalendarCommand command;

    switch (cmd.kind) {
      case "create-calendar":
        command = new CreateCalendarCommand(manager, cmd);
        break;
      case "edit-calendar":
        command = new EditCalendarCommand(manager, cmd);
        break;
      case "use-calendar":
        command = new UseCalendarCommand(manager, cmd);
        break;
      case "copy-event":
        command = new CopyEventCommand(manager, cmd);
        break;
      case "copy-events-on":
        command = new CopyEventsOnCommand(manager, cmd);
        break;
      case "copy-events-between":
        command = new CopyEventsBetweenCommand(manager, cmd);
        break;
      case "create-single":
        command = new CreateSingleCommand(manager, cmd);
        break;
      case "create-allday-single":
        command = new CreateAllDaySingleCommand(manager, cmd);
        break;
      case "create-series-count":
        command = new CreateSeriesCountCommand(manager, cmd);
        break;
      case "create-series-until":
        command = new CreateSeriesUntilCommand(manager, cmd);
        break;
      case "create-allday-series-count":
        command = new CreateAllDaySeriesCountCommand(manager, cmd);
        break;
      case "create-allday-series-until":
        command = new CreateAllDaySeriesUntilCommand(manager, cmd);
        break;
      case "edit-single":
        command = new EditSingleCommand(manager, cmd);
        break;
      case "edit-from":
        command = new EditFromCommand(manager, cmd);
        break;
      case "edit-series":
        command = new EditSeriesCommand(manager, cmd);
        break;
      case "print-on":
        command = new PrintOnCommand(manager, cmd);
        break;
      case "print-range":
        command = new PrintRangeCommand(manager, cmd);
        break;
      case "status":
        command = new StatusCommand(manager, cmd);
        break;
      case "export":
        command = new ExportCommand(manager, cmd);
        break;
      case "exit":
        command = new ExitCommand(manager, cmd);
        break;
      default:
        throw new IllegalArgumentException("unknown command: " + cmd.kind);
    }

    return command.execute();
  }
}