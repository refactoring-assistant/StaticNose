package controller.command;


import controller.CommandResult;
import model.Icalendar;

/**
 * Command interface defining the contract for all calendar commands.
 * This interface is part of the Command pattern implementation, allowing
 * commands to be encapsulated as objects.
 */
public interface Command {

  /**
   * Executes this command against the provided calendar.
   *
   * @param calendar the calendar model to operate on
   * @return a CommandResult indicating success/failure and containing
   */
  CommandResult execute(Icalendar calendar);
}
