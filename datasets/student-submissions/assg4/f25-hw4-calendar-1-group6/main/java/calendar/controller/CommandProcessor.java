package calendar.controller;

import calendar.view.ConsoleView;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses raw command strings and sends to CommandController.
 * Each command form has an explicit regex.
 */
public final class CommandProcessor {
  private final CommandController controller;
  private final ConsoleView view;

  /**
   * Sets up the processor by linking it to the Controller and the View.
   *
   * @param controller The CommandController that executes the commands.
   * @param view The ConsoleView used to display error messages if parsing fails.
   */
  public CommandProcessor(CommandController controller, ConsoleView view) {
    this.controller = controller;
    this.view = view;
  }

  /**
   * Removes double quotes from around a string if they exist.
   *
   * @param s The raw string possibly wrapped in quotes.
   * @return The string with quotes removed.
   */
  private static String unquote(String s) {
    String t = s.trim();
    if (t.startsWith("\"") && t.endsWith("\"") && t.length() >= 2) {
      return t.substring(1, t.length() - 1);
    }
    return t;
  }

  /**
   * Parses and executes one command line of input.
   * It checks for an 'exit' command first. It uses a series of regex patterns
   * to find a match and extracts the arguments and then calls the Controller.
   *
   * @param line The raw input string from the user or file.
   * @return Returns false only if the command was "exit", otherwise true.
   */
  public boolean process(String line) {
    String s = line.trim();
    if (s.isEmpty()) {
      return true;
    }

    if (s.equalsIgnoreCase("exit")) {
      return false;
    }

    try {
      Matcher m = Pattern.compile(
          "^create\\s+event\\s+(\"[^\"]+\"|\\S+)\\s+from\\s+(\\S+)\\s+to\\s+(\\S+)$",
          Pattern.CASE_INSENSITIVE).matcher(s);
      if (m.find()) {
        controller.createSingle(unquote(m.group(1)), m.group(2), m.group(3));
        return true;
      }

      m = Pattern.compile(
          "^create\\s+event\\s+(\"[^\"]+\"|\\S+)\\s+from\\s+(\\S+)\\s+to\\s+(\\S+)\\s+"
              + "repeats\\s+(\\S+)\\s+for\\s+(\\d+)\\s+times$",
          Pattern.CASE_INSENSITIVE).matcher(s);
      if (m.find()) {
        controller.createSeriesFor(
            unquote(m.group(1)), m.group(2), m.group(3), m.group(4),
            Integer.parseInt(m.group(5)));
        return true;
      }

      m = Pattern.compile(
          "^create\\s+event\\s+(\"[^\"]+\"|\\S+)\\s+from\\s+(\\S+)\\s+to\\s+(\\S+)\\s+"
              + "repeats\\s+(\\S+)\\s+until\\s+(\\S+)$",
          Pattern.CASE_INSENSITIVE).matcher(s);
      if (m.find()) {
        controller.createSeriesUntil(
            unquote(m.group(1)), m.group(2), m.group(3), m.group(4), m.group(5));
        return true;
      }

      m = Pattern.compile(
          "^create\\s+event\\s+(\"[^\"]+\"|\\S+)\\s+on\\s+(\\S+)$",
          Pattern.CASE_INSENSITIVE).matcher(s);
      if (m.find()) {
        controller.createAllDay(unquote(m.group(1)), m.group(2));
        return true;
      }

      m = Pattern.compile(
          "^create\\s+event\\s+(\"[^\"]+\"|\\S+)\\s+on\\s+(\\S+)\\s+repeats\\s+(\\S+)\\s+"
              + "for\\s+(\\d+)\\s+times$",
          Pattern.CASE_INSENSITIVE).matcher(s);
      if (m.find()) {
        controller.createAllDaySeriesFor(
            unquote(m.group(1)), m.group(2), m.group(3), Integer.parseInt(m.group(4)));
        return true;
      }

      m = Pattern.compile(
          "^create\\s+event\\s+(\"[^\"]+\"|\\S+)\\s+on\\s+(\\S+)\\s+repeats\\s+(\\S+)\\s+"
              + "until\\s+(\\S+)$",
          Pattern.CASE_INSENSITIVE).matcher(s);
      if (m.find()) {
        controller.createAllDaySeriesUntil(
            unquote(m.group(1)), m.group(2), m.group(3), m.group(4));
        return true;
      }

      m = Pattern.compile(
          "^edit\\s+event\\s+(\\S+)\\s+(\"[^\"]+\"|\\S+)\\s+from\\s+(\\S+)\\s"
              + "+to\\s+(\\S+)\\s+with\\s+(.+)$",
          Pattern.CASE_INSENSITIVE).matcher(s);
      if (m.find()) {
        controller.editSingle(
            m.group(1), unquote(m.group(2)), m.group(3), m.group(4), unquote(m.group(5)));
        return true;
      }

      m = Pattern.compile(
          "^edit\\s+events\\s+(\\S+)\\s+(\"[^\"]+\"|\\S+)\\s+from\\s+(\\S+)\\s+with\\s+(.+)$",
          Pattern.CASE_INSENSITIVE).matcher(s);
      if (m.find()) {
        controller.editFrom(
            m.group(1), unquote(m.group(2)), m.group(3), unquote(m.group(4)));
        return true;
      }

      m = Pattern.compile(
          "^edit\\s+series\\s+(\\S+)\\s+(\"[^\"]+\"|\\S+)\\s+from\\s+(\\S+)\\s+with\\s+(.+)$",
          Pattern.CASE_INSENSITIVE).matcher(s);
      if (m.find()) {
        controller.editSeries(
            m.group(1), unquote(m.group(2)), m.group(3), unquote(m.group(4)));
        return true;
      }

      m = Pattern.compile(
          "^print\\s+events\\s+on\\s+(\\S+)$",
          Pattern.CASE_INSENSITIVE).matcher(s);
      if (m.find()) {
        controller.printOn(m.group(1));
        return true;
      }

      m = Pattern.compile(
          "^print\\s+events\\s+from\\s+(\\S+)\\s+to\\s+(\\S+)$",
          Pattern.CASE_INSENSITIVE).matcher(s);
      if (m.find()) {
        controller.printBetween(m.group(1), m.group(2));
        return true;
      }

      m = Pattern.compile(
          "^export\\s+cal\\s+(\\S+)$",
          Pattern.CASE_INSENSITIVE).matcher(s);
      if (m.find()) {
        controller.exportCsv(m.group(1));
        return true;
      }

      m = Pattern.compile(
          "^show\\s+status\\s+on\\s+(\\S+)$",
          Pattern.CASE_INSENSITIVE).matcher(s);
      if (m.find()) {
        controller.showStatus(m.group(1));
        return true;
      }

      view.printError("Invalid command: " + s);
      return true;
    } catch (Exception ex) {
      view.printError(ex.getMessage());
      return true;
    }
  }
}
