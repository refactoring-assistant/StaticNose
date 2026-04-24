package calendar.control.commands;

import calendar.control.results.CommandResult;
import calendar.model.AbstractEvent;
import calendar.model.Imodel;
import calendar.model.database.IcalendarDatabase;
import calendar.utils.StringUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Concrete class extending the abstract class for copy command.
 * This one takes care of copying single events.
 */

public class CopySingleEventCommand extends AbstractCopyCommand {

  private static final Pattern PATTERN = Pattern.compile(
      "copy\\s+event\\s+\"?([^\"]+)\"?\\s+on\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+"
          + "--target\\s+(?:\"([^\"]+)\"|([^\\s\"]+))\\s+to\\s"
          + "+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})",
      Pattern.CASE_INSENSITIVE
  );

  private String eventName;
  private LocalDateTime sourceDateTime;
  private LocalDateTime targetDateTime;

  /**
   * Constructor for Abstract class for Copy Command.
   *
   * @param multipleCalendar the calendar database
   * @param input            the raw command input
   */
  public CopySingleEventCommand(IcalendarDatabase multipleCalendar,
                                String input) {
    super(multipleCalendar, input);
  }

  @Override
  protected ParsedInput parseInput() {
    Matcher matcher = PATTERN.matcher(input);
    if (!matcher.find()) {
      return ParsedInput.error(
          "Invalid syntax. Use: copy event \"<name>\" on <datetime> "
              + "--target <calendar> to <datetime>");
    }

    this.eventName = StringUtils.removeQuotes(matcher.group(1));
    this.sourceDateTime = LocalDateTime.parse(matcher.group(2));
    String targetCalendarName = matcher.group(3) != null ? matcher.group(3) : matcher.group(4);
    this.targetDateTime = LocalDateTime.parse(matcher.group(5));

    return ParsedInput.success(StringUtils.removeQuotes(targetCalendarName));
  }

  @Override
  protected List<AbstractEvent> getSourceEvents(Imodel sourceCalendar) {
    return sourceCalendar.getAllEvents().stream()
        .filter(e -> e.getSubject().equalsIgnoreCase(eventName)
            && e.getStart().equals(sourceDateTime))
        .collect(Collectors.toList());
  }

  @Override
  protected CommandResult copyToTarget(List<AbstractEvent> events, Imodel targetCalendar,
                                       String targetCalendarName) {
    if (events.isEmpty()) {
      return CommandResult.error("Event '" + eventName + "' not found at " + sourceDateTime);
    }

    int successCount = 0;
    StringBuilder errors = new StringBuilder();
    boolean allCopied = true;

    for (AbstractEvent event : events) {
      try {
        long durationMinutes = java.time.temporal.ChronoUnit.MINUTES.between(
            event.getStart(), event.getEnd());
        LocalDateTime targetEnd = targetDateTime.plusMinutes(durationMinutes);

        boolean success = targetCalendar.createEvent(
            event.getSubject(), targetDateTime, targetEnd);

        if (success) {
          successCount++;
        } else {
          allCopied = false;
        }
      } catch (Exception e) {
        errors.append("Error copying '").append(eventName)
            .append("': ").append(e.getMessage()).append(". ");
      }
    }
    if (allCopied) {
      return CommandResult.success(
          "Successfully copied '" + eventName + "' to calendar '" + targetCalendarName + "'");
    } else {
      return CommandResult.error(
          "Failed to copy event(s). " + errors);
    }
  }
}