package controller;

import controller.command.Command;
import controller.parser.CommandParser;
import model.Icalendar;

/**
 * Main controller implementation for the calendar system using the Command pattern.
 * This controller acts as a facade, coordinating between the command parser
 * and the calendar model to execute user commands.
 */
public class CalendarController implements IcalendarController {
  private final Icalendar model;
  private final CommandParser parser;

  /**
   * Constructs a CalendarController with the specified calendar model.
   *
   * @param model the calendar model to operate on (cannot be null)
   * @throws IllegalArgumentException if model is null
   */
  public CalendarController(Icalendar model) {
    if (model == null) {
      throw new IllegalArgumentException("Model cannot be null");
    }
    this.model = model;
    this.parser = new CommandParser();
  }

  @Override
  public CommandResult executeCommand(String commandString) {
    try {
      Command command = parser.parse(commandString);
      return command.execute(model);
    } catch (CommandParser.InvalidCommandException e) {
      return new CommandResult(false, e.getMessage());
    } catch (Exception e) {
      return new CommandResult(false, "Error: " + e.getMessage());
    }
  }
}