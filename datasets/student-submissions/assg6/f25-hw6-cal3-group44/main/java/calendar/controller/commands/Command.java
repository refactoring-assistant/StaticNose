package calendar.controller.commands;

import java.util.List;

/**
 * Interface used to delegate the functionality based on the user input.
 * example
 */
public interface Command {
  /**
   * Performs the parsing to extract the information from the user input list.
   * pass the information to the model to handel all the operations.
   *
   * @param parsedCommand list of user input values.
   * @return a string based on the operation performed.
   */
  String execute(List<String> parsedCommand);
}
