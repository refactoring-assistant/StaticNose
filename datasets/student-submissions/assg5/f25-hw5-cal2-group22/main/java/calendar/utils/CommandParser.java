package calendar.utils;

import calendar.control.commands.CheckStatusCommand;
import calendar.control.commands.CopyCommandSelect;
import calendar.control.commands.CreateCalendarCommand;
import calendar.control.commands.CreateEventCommand;
import calendar.control.commands.CreateSeriesCommand;
import calendar.control.commands.EditCalendarCommand;
import calendar.control.commands.EditEventCommand;
import calendar.control.commands.ExportCalendarCommand;
import calendar.control.commands.Icommand;
import calendar.control.commands.PrintEventsCommand;
import calendar.control.commands.UseCalendarCommand;
import calendar.model.database.IcalendarDatabase;
import calendar.view.Iview;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Parses user input into specific Command instances.
 * Uses simple prefix checks to route to the correct command type.
 * Throws an IllegalArgumentException for unrecognized input.
 */
public class CommandParser {
  private final Map<Predicate<String>, Function<String, Icommand>> commandMap =
      new LinkedHashMap<>();

  /**
   * Creates a command parser with access to the model and view.
   *
   * @param multipleCalendars calendar model
   * @param view              active view
   */
  public CommandParser(IcalendarDatabase multipleCalendars, Iview view) {

    commandMap.put(
        s -> s.startsWith("create calendar"),
        input -> new CreateCalendarCommand(multipleCalendars, input)
    );

    commandMap.put(
        s -> s.startsWith("use calendar"),
        input -> new UseCalendarCommand(multipleCalendars, input)
    );

    commandMap.put(
        s -> s.startsWith("edit calendar"),
        input -> new EditCalendarCommand(multipleCalendars, input)
    );

    commandMap.put(
        s -> s.startsWith("copy events") || s.startsWith("copy event "),
        input -> CopyCommandSelect.createCopyCommand(multipleCalendars, input)
    );

    commandMap.put(
        s -> s.startsWith("create event") && s.contains("repeats")
            && (s.contains(" for ") || s.contains(" until ")),
        input -> new CreateSeriesCommand(multipleCalendars, input)
    );

    commandMap.put(
        s -> s.startsWith("create event"),
        input -> new CreateEventCommand(multipleCalendars, input)
    );

    commandMap.put(
        s -> s.startsWith("edit"),
        input -> new EditEventCommand(multipleCalendars, input)
    );

    commandMap.put(
        s -> s.startsWith("print events"),
        input -> new PrintEventsCommand(multipleCalendars, input)
    );

    commandMap.put(
        s -> s.startsWith("show status"),
        input -> new CheckStatusCommand(multipleCalendars, input)
    );

    commandMap.put(
        s -> s.startsWith("export cal"),
        input -> new ExportCalendarCommand(multipleCalendars, input)
    );

  }

  /**
   * Parses a raw command string and returns the corresponding Command.
   *
   * @param input user input line
   * @return a command object ready to execute
   */
  public Icommand parse(String input) {
    String s = input.trim().toLowerCase();

    for (Map.Entry<Predicate<String>, Function<String, Icommand>> entry : commandMap.entrySet()) {
      if (entry.getKey().test(s)) {
        Icommand command = entry.getValue().apply(input);
        if (command != null) {
          return command;
        }
      }
    }

    throw new IllegalArgumentException("Invalid command: " + input);
  }
}
