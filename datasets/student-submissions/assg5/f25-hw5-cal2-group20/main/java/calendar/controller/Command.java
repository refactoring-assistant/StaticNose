package calendar.controller;

import java.util.Scanner;

/**
 * Represents a Command interface which is a part of the Command Design pattern for the Controller.
 */
public interface Command {

  /**
   * Executes the command, supplied with a Scanner.
   *
   * @param scanner scanner to parse the command from string.
   */
  void execute(Scanner scanner);
}
