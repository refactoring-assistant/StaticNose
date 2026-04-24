package calendar.controller.commands;

import calendar.controller.commands.create.CreateFromToCommand;
import calendar.controller.commands.create.CreateFromToRepeatsForCommand;
import calendar.controller.commands.create.CreateFromToRepeatsUntilCommand;
import calendar.controller.commands.create.CreateOnCommand;
import calendar.controller.commands.create.CreateOnRepeatsForCommand;
import calendar.controller.commands.create.CreateOnRepeatsUntilCommand;
import calendar.controller.commands.edit.EditEventCommand;
import calendar.controller.commands.edit.EditEventsCommand;
import calendar.controller.commands.edit.EditSeriesCommand;
import calendar.controller.commands.utils.ExportCommand;
import calendar.controller.commands.utils.PrintFromToCommand;
import calendar.controller.commands.utils.PrintOnCommand;
import calendar.controller.commands.utils.ShowStatusCommand;
import calendar.view.Iview;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Factory for creating command objects from input strings.
 * Combining factory method along with each command function for respective commands.
 * Regex patterns are mapped to command constructors.
 */
public class CommandFactory {
  private final Iview view;
  private final Map<Pattern, BiFunction<String, Matcher, Icommands>> commandMap;

  /**
   * Creating a command factory.
   *
   * @param view the view for displaying messages and errors.
   */
  public CommandFactory(Iview view) {
    this.view = view;
    this.commandMap = new LinkedHashMap<>();

    initializeCommands();
  }

  /**
   * Initializing a command map for passing the model methods for each command.
   */
  private void initializeCommands() {
    String subject = "(?:\"([^\"]+)\"|([\\w\\s]+?))";

    commandMap.put(
        Pattern.compile("create event " + subject + " from (\\S+) to (\\S+)",
            Pattern.CASE_INSENSITIVE),
        (input, m) -> new CreateFromToCommand(view, m)
    );

    commandMap.put(
        Pattern.compile("create event " + subject + " on (\\S+)",
            Pattern.CASE_INSENSITIVE),
        (input, m) -> new CreateOnCommand(view, m)
    );

    commandMap.put(
        Pattern.compile("create event " + subject
                + " from (\\S+) to (\\S+) repeats ([MTWRFSU]+) for (\\d+) times",
            Pattern.CASE_INSENSITIVE),
        (input, m) -> new CreateFromToRepeatsForCommand(view, m)  // ← NO model!
    );

    commandMap.put(
        Pattern.compile("create event " + subject
                + " on (\\S+) repeats ([MTWRFSU]+) for (\\d+) times",
            Pattern.CASE_INSENSITIVE),
        (input, m) -> new CreateOnRepeatsForCommand(view, m)
    );

    commandMap.put(
        Pattern.compile("create event " + subject
                + " from (\\S+) to (\\S+) repeats ([MTWRFSU]+) until (\\S+)",
            Pattern.CASE_INSENSITIVE),
        (input, m) -> new CreateFromToRepeatsUntilCommand(view, m)  // ← NO model!
    );

    commandMap.put(
        Pattern.compile("create event " + subject
                + " on (\\S+) repeats ([MTWRFSU]+) for (\\d+)",
            Pattern.CASE_INSENSITIVE),
        (input, m) -> new CreateOnRepeatsForCommand(view, m)  // ← NO model!
    );

    commandMap.put(
        Pattern.compile("create event " + subject
                + " on (\\S+) repeats ([MTWRFSU]+) until (\\S+)",
            Pattern.CASE_INSENSITIVE),
        (input, m) -> new CreateOnRepeatsUntilCommand(view, m)  // ← NO model!
    );

    commandMap.put(
        Pattern.compile("edit event (\\w+) " + subject
                + " from (\\S+) to (\\S+) with (.+)",
            Pattern.CASE_INSENSITIVE),
        (input, m) -> new EditEventCommand(view, m)  // ← NO model!
    );

    commandMap.put(
        Pattern.compile("edit events (\\w+) " + subject
                + " from (\\S+) with (.+)",
            Pattern.CASE_INSENSITIVE),
        (input, m) -> new EditEventsCommand(view, m)  // ← NO model!
    );

    commandMap.put(
        Pattern.compile("edit series (\\w+) " + subject + " from (\\S+) with (.+)",
            Pattern.CASE_INSENSITIVE),
        (input, m) -> new EditSeriesCommand(view, m)  // ← NO model!
    );

    commandMap.put(
        Pattern.compile("print events on (\\S+)",
            Pattern.CASE_INSENSITIVE),
        (input, m) -> new PrintOnCommand(view, m)  // ← NO model!
    );

    commandMap.put(
        Pattern.compile("print events from (\\S+) to (\\S+)",
            Pattern.CASE_INSENSITIVE),
        (input, m) -> new PrintFromToCommand(view, m)  // ← NO model!
    );

    commandMap.put(
        Pattern.compile("show status on (\\S+)",
            Pattern.CASE_INSENSITIVE),
        (input, m) -> new ShowStatusCommand(view, m)  // ← NO model!
    );

    commandMap.put(
        Pattern.compile("export (.+)",
            Pattern.CASE_INSENSITIVE),
        (input, m) -> new ExportCommand(view, m)  // ← NO model!
    );
  }

  /**
   * Create command from input string.
   *
   * @param input the command string.
   * @return Command object ready to execute, or null if no match.
   */
  public Icommands createCommand(String input) {
    // Try each pattern until one matches
    for (Map.Entry<Pattern, BiFunction<String, Matcher, Icommands>> entry
        : commandMap.entrySet()) {

      java.util.regex.Matcher matcher = entry.getKey().matcher(input.trim());

      if (matcher.matches()) {
        return entry.getValue().apply(input, matcher);
      }
    }

    return null;
  }
}
