package calendar.controller;

import java.util.Scanner;

/**
 * An interface for a Parser used by the controller.
 */
public interface Parser {
  /**
   * This method should read the required data from the scanner and create and return the,
   * corresponding command.
   *
   * @param scanner Input source.
   * @return The corresponding command.
   */
  Command parse(Scanner scanner);
}
