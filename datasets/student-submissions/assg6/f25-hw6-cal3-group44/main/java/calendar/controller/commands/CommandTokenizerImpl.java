package calendar.controller.commands;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the ICommandTokenizer to parse user input into a list.
 */
public class CommandTokenizerImpl implements CommandTokenizer {


  /**
   * Used to parse the user input into a list of input.
   * Parses the user input into words divided by white spaces and.
   * string inside double quotes are considered as single input field.
   *
   * @param command entered but the user.
   * @return Parsed list of the command.
   */
  @Override
  public List<String> parser(String command) {

    List<String> commandTokens = new ArrayList<>();
    boolean inQuotes = false;
    StringBuilder token = new StringBuilder();
    for (int i = 0; i < command.length(); i++) {
      char c = command.charAt(i);
      if (c == '"') {
        inQuotes = !inQuotes;
        continue;
      }
      if (c == ' ' && !inQuotes) {
        if (token.length() > 0) {
          commandTokens.add(token.toString());
          token.setLength(0);
        }
      } else {
        token.append(c);
      }
    }
    if (token.length() > 0) {
      commandTokens.add(token.toString());
    }
    return commandTokens;
  }
}
