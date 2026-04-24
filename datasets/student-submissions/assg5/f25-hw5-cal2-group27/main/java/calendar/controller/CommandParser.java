package calendar.controller;

import calendar.command.Command;
import calendar.command.EditProperty;
import calendar.command.EditScope;
import calendar.command.calendar.CreateCalendar;
import calendar.command.calendar.EditCalendar;
import calendar.command.calendar.UseCalendar;
import calendar.command.event.CopyEvent;
import calendar.command.event.CopyEvents;
import calendar.command.event.Create;
import calendar.command.event.Edit;
import calendar.command.event.Print;
import calendar.command.event.Status;
import calendar.command.export.ExportRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Parses raw string commands into executable Command objects.
 */
public class CommandParser {

  private static final List<CommandPattern> COMMAND_PATTERNS = new ArrayList<>();

  static {
    COMMAND_PATTERNS.add(new CommandPattern(
        "^create calendar --name (\\S+) --timezone (\\S+)$",
        matcher -> Optional.of(new CreateCalendar(matcher.group(1), matcher.group(2)))
    ));

    COMMAND_PATTERNS.add(new CommandPattern(
        "^use calendar --name (\\S+)$",
        matcher -> Optional.of(new UseCalendar(matcher.group(1)))
    ));

    COMMAND_PATTERNS.add(new CommandPattern(
        "^edit calendar --name (\\S+) --property (\\w+) (.*)$",
        matcher -> Optional.of(new EditCalendar(matcher.group(1), matcher.group(2),
                matcher.group(3)))
    ));

    COMMAND_PATTERNS.add(new CommandPattern(
        "^copy event \"?(.*?)\"? on (\\S+) --target (\\S+) to (\\S+)$",
        matcher -> Optional.of(new CopyEvent(matcher.group(1), matcher.group(2),
                matcher.group(3), matcher.group(4)))
    ));

    COMMAND_PATTERNS.add(new CommandPattern(
        "^copy events on (\\S+) --target (\\S+) to (\\S+)$",
        matcher -> Optional.of(new CopyEvents(matcher.group(1), null,
                matcher.group(2), matcher.group(3), false))
    ));

    COMMAND_PATTERNS.add(new CommandPattern(
        "^copy events between (\\S+) and (\\S+) --target (\\S+) to (\\S+)$",
        matcher -> Optional.of(new CopyEvents(matcher.group(1), matcher.group(2),
                matcher.group(3), matcher.group(4), true))
    ));

    COMMAND_PATTERNS.add(new CommandPattern(
        "^show status on (\\S+)$",
        matcher -> Optional.of(new Status(matcher.group(1)))
    ));

    COMMAND_PATTERNS.add(new CommandPattern(
        "^export cal (\\S+)$",
        matcher -> Optional.of((Command) ExportRegistry.resolve(matcher.group(1)))
    ));

    COMMAND_PATTERNS.add(new CommandPattern(
        "^create event \"?(.*?)\"? from (\\S+) to (\\S+)"
            + "(?: repeats (\\S+) (for (\\d+) times|until (\\S+)))?$",
        matcher -> {
          String subject = matcher.group(1);
          String startStr = matcher.group(2);
          String endStr = matcher.group(3);
          String weekdaysStr = matcher.group(4);
          String countStr = matcher.group(6);

          if (weekdaysStr == null) {
            return Optional.of(Create.single(subject, startStr, endStr));
          } else {
            if (countStr != null) {
              return Optional.of(Create.recurringForCount(subject, startStr, endStr, weekdaysStr,
                  Integer.parseInt(countStr)));
            } else {
              String untilDateStr = matcher.group(7);
              return Optional.of(Create.recurringUntilDate(subject, startStr, endStr, weekdaysStr,
                      untilDateStr));
            }
          }
        }
    ));

    COMMAND_PATTERNS.add(new CommandPattern(
        "^create event \"?(.*?)\"? on (\\S+)(?: repeats (\\S+) (for (\\d+)"
                + " times|until (\\S+)))?$",
        matcher -> {
          String subject = matcher.group(1);
          String dateStr = matcher.group(2);
          String weekdaysStr = matcher.group(3);
          String countStr = matcher.group(5);

          if (weekdaysStr == null) {
            return Optional.of(Create.allDay(subject, dateStr));
          } else {
            if (countStr != null) {
              return Optional.of(Create.allDayRecurringForCount(subject, dateStr, weekdaysStr,
                  Integer.parseInt(countStr)));
            } else {
              String untilDateStr = matcher.group(6);
              return Optional.of(Create.allDayRecurringUntilDate(subject, dateStr, weekdaysStr,
                      untilDateStr));
            }
          }
        }
    ));

    COMMAND_PATTERNS.add(new CommandPattern(
        "^edit (event|events|series) (\\w+) \"?(.*?)\"? from (\\S+)(?:"
            + " to (\\S+))? with (.*)$",
        matcher -> {
          try {
            EditScope scope = EditScope.fromString(matcher.group(1));
            EditProperty property = EditProperty.fromString(matcher.group(2));
            String subject = matcher.group(3);
            String startStr = matcher.group(4);
            String endStr = matcher.group(5);
            String newValueStr = matcher.group(6);

            if (scope == EditScope.EVENT && endStr == null) {
              throw new IllegalArgumentException("'edit event' requires a 'to <date>' clause.");
            }
            if ((scope == EditScope.EVENTS || scope == EditScope.SERIES) && endStr != null) {
              throw new IllegalArgumentException(
                  "'edit events/series' does not support a 'to <date>' clause.");
            }

            return Optional.of(new Edit(scope, property, subject, startStr, endStr, newValueStr));
          } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
          }
        }
    ));

    COMMAND_PATTERNS.add(new CommandPattern(
        "^print events (on (\\S+)|from (\\S+) to (\\S+))$",
        matcher -> {
          String onDateStr = matcher.group(2);
          String fromDateStr = matcher.group(3);

          if (onDateStr != null) {
            return Optional.of(new Print(onDateStr));
          } else {
            String toDateStr = matcher.group(4);
            return Optional.of(new Print(fromDateStr, toDateStr));
          }
        }
    ));
  }

  /**
   * Default constructor.
   */
  public CommandParser() {
  }

  /**
   * Parses a raw command string into a Command object.
   *
   * @param commandString The raw command string from the user.
   * @return An Optional containing the Command if parsing is successful, otherwise empty.
   */
  Optional<Command> parse(String commandString) {
    for (CommandPattern commandPattern : COMMAND_PATTERNS) {
      Optional<Command> command = commandPattern.matchAndCreate(commandString);
      if (command.isPresent()) {
        return command;
      }
    }
    return Optional.empty();
  }
}
