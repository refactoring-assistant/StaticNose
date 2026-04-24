package calendar.controller.command;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple parser for command arguments.
 * Parses flags like --name and --timezone.
 */
class ArgumentParser {
  private final Map<String, String> options = new HashMap<>();
  private final String commandPrefix;

  /**
   * Constructor.
   *
   * @param commandPrefix the command prefix
   */
  public ArgumentParser(String commandPrefix) {
    this.commandPrefix = commandPrefix;
  }

  /**
   * Parses the command and extracts flags.
   *
   * @param command the command string
   * @return this parser
   */
  public ArgumentParser parse(String command) {
    if (!command.toLowerCase().startsWith(commandPrefix.toLowerCase())) {
      throw new IllegalArgumentException("Invalid command format.");
    }

    String remaining = command.substring(commandPrefix.length()).trim();
    String[] tokens = remaining.split("\\s+");

    for (int i = 0; i < tokens.length; i++) {
      if (tokens[i].startsWith("--") && i + 1 < tokens.length) {
        String flag = tokens[i].substring(2).toLowerCase();
        String val = tokens[i + 1];
        // Remove quotes
        if (val.length() >= 2 && val.startsWith("\"") && val.endsWith("\"")) {
          val = val.substring(1, val.length() - 1);
        }
        options.put(flag, val);
        i++;
      }
    }

    if (tokens.length > 0 && !tokens[tokens.length - 1].startsWith("--")
        && options.containsKey("property") && !options.containsKey("value")) {
      String val = tokens[tokens.length - 1];
      if (val.length() >= 2 && val.startsWith("\"") && val.endsWith("\"")) {
        val = val.substring(1, val.length() - 1);
      }
      options.put("value", val);
    }

    return this;
  }

  /**
   * Gets a required flag value.
   *
   * @param flag the flag name
   * @return the value
   */
  public String getRequired(String flag) {
    String value = options.get(flag.toLowerCase());
    if (value == null || value.isEmpty()) {
      throw new IllegalArgumentException("Missing required flag: --" + flag);
    }
    return value;
  }

  /**
   * Gets an optional flag value.
   *
   * @param flag the flag name
   * @return the value or null
   */
  public String getOptional(String flag) {
    return options.get(flag.toLowerCase());
  }
}

