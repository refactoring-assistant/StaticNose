package calendar.control.commands;

import calendar.control.results.CommandResult;
import calendar.model.AbstractEvent;
import calendar.model.Imodel;
import calendar.model.database.IcalendarDatabase;
import calendar.utils.StringUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Command to copy all events on a specific date.
 */
public class CopyEventsOnDateCommand extends AbstractCopyCommand {

  private static final Pattern PATTERN = Pattern.compile(
      "copy\\s+events\\s+on\\s+(\\d{4}-\\d{2}-\\d{2})\\s+--target\\s+(?:\"([^\"]+)\"|([^\\s\"]+))"
          + "\\s+to\\s+(\\d{4}-\\d{2}-\\d{2})",
      Pattern.CASE_INSENSITIVE
  );

  private LocalDate sourceDate;
  private LocalDate targetDate;

  /**
   * Constructor for the class.
   *
   * @param multipleCalendar database of calendars.
   * @param input            string of input.
   */
  public CopyEventsOnDateCommand(IcalendarDatabase multipleCalendar, String input) {
    super(multipleCalendar, input);
  }

  @Override
  protected ParsedInput parseInput() {
    Matcher matcher = PATTERN.matcher(input);
    if (!matcher.find()) {
      return ParsedInput.error(
          "Invalid syntax. Use: copy events on <date> --target <calendar> to <date>");
    }

    this.sourceDate = LocalDate.parse(matcher.group(1));
    String targetCalendarName = (matcher.group(2)) != null ? matcher.group(2) : matcher.group(3);
    this.targetDate = LocalDate.parse(matcher.group(4));

    return ParsedInput.success(StringUtils.removeQuotes(targetCalendarName));
  }

  @Override
  protected List<AbstractEvent> getSourceEvents(Imodel sourceCalendar) {
    return sourceCalendar.getAllEvents().stream()
        .filter(e -> e.getStart().toLocalDate().equals(sourceDate)
            || e.getEnd().toLocalDate().equals(sourceDate)
            || (e.getStart().toLocalDate().isBefore(sourceDate)
            && e.getEnd().toLocalDate().isAfter(sourceDate)))
        .collect(Collectors.toList());
  }

  @Override
  protected CommandResult copyToTarget(List<AbstractEvent> events, Imodel targetCalendar,
                                       String targetCalendarName) {
    ZoneId sourceTimezone = getSourceTimezone();
    ZoneId targetTimezone = getCalendarTimezone(targetCalendarName);

    int successCount = 0;
    boolean allCopied = true;
    StringBuilder warnings = new StringBuilder();

    for (AbstractEvent event : events) {
      try {
        LocalTime sourceStartTime = event.getStart().toLocalTime();
        LocalTime sourceEndTime = event.getEnd().toLocalTime();

        LocalDateTime targetStart = convertEventTime(
            targetDate, sourceStartTime, sourceTimezone, targetTimezone);
        LocalDateTime targetEnd = convertEventTime(
            targetDate, sourceEndTime, sourceTimezone, targetTimezone);

        if (targetEnd.isBefore(targetStart) || targetEnd.equals(targetStart)) {
          targetEnd = targetEnd.plusDays(1);
        }

        boolean success = targetCalendar.createEvent(
            event.getSubject(), targetStart, targetEnd);

        if (success) {
          successCount++;
        } else {
          allCopied = false;
        }
      } catch (Exception e) {
        warnings.append("Error copying '").append(event.getSubject())
            .append("': ").append(e.getMessage()).append(". ");
      }
    }
    String message = "Successfully copied " + successCount + " event(s) from " + sourceDate
        + " to " + targetDate + " in calendar '" + targetCalendarName + "'";

    if (!allCopied) {
      return CommandResult.warning(message,  "Some events copying failed."
          + warnings);
    } else {
      return CommandResult.success(message);
    }
  }
}