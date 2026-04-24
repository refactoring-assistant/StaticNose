package calendar.controller.commands;

import java.util.List;

/**
 * Command Parser Interface can be used to passed the input String Command into required format.
 */
public interface CommandTokenizer {

  /**
   * Used to parse the user input into a list of input.
   * Parses the user input into words divided by white spaces and.
   * string inside double quotes are considered as single input field.
   *
   * @param command entered but the user.
   * @return Parsed list of the command.
   */
  List<String> parser(String command);
}
