package calendar.control.commands;


import calendar.control.results.CommandResult;
import calendar.model.AbstractEvent;
import calendar.model.EventInstance;
import calendar.model.Imodel;
import calendar.model.database.IcalendarDatabase;
import calendar.utils.StringUtils;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Command to copy all events within a date range.
 */
public class CopyEventsBetweenCommand extends AbstractCopyCommand {

  private static final Pattern PATTERN = Pattern.compile(
      "copy\\s+events\\s+between\\s+(\\d{4}-\\d{2}-\\d{2})\\s+and\\s+"
          + "(\\d{4}-\\d{2}-\\d{2})\\s+--target\\s+(?:\"([^\"]+)\"|([^\\s\"]+))\\s"
          + "+to\\s+(\\d{4}-\\d{2}-\\d{2})",
      Pattern.CASE_INSENSITIVE
  );

  private LocalDate sourceFromDate;
  private LocalDate sourceToDate;
  private LocalDate targetStartDate;

  /**
   * Constructor for the class.
   *
   * @param multipleCalendar is the database of calendars.
   * @param input            is the string input.
   */
  public CopyEventsBetweenCommand(IcalendarDatabase multipleCalendar, String input) {
    super(multipleCalendar, input);
  }

  @Override
  protected ParsedInput parseInput() {
    Matcher matcher = PATTERN.matcher(input);
    if (!matcher.find()) {
      return ParsedInput.error(
          "Invalid syntax. Use: copy events between <date> and <date> "
              + "--target <calendar> to <date>");
    }

    this.sourceFromDate = LocalDate.parse(matcher.group(1));
    this.sourceToDate = LocalDate.parse(matcher.group(2));
    String targetCalendarName = matcher.group(3) != null ? matcher.group(3) : matcher.group(4);
    this.targetStartDate = LocalDate.parse(matcher.group(5));

    return ParsedInput.success(StringUtils.removeQuotes(targetCalendarName));
  }

  @Override
  protected List<AbstractEvent> getSourceEvents(Imodel sourceCalendar) {
    return sourceCalendar.getAllEvents().stream()
        .filter(e -> {
          LocalDate eventStart = e.getStart().toLocalDate();
          return !eventStart.isBefore(sourceFromDate) && !eventStart.isAfter(sourceToDate);
        })
        .collect(Collectors.toList());
  }

  @Override
  protected CommandResult copyToTarget(List<AbstractEvent> events, Imodel targetCalendar,
                                       String targetCalendarName) {
    ZoneId sourceTimezone = getSourceTimezone();
    ZoneId targetTimezone = getCalendarTimezone(targetCalendarName);

    EventGroups groups = separateEventsBySeries(events);

    int successCount = 0;
    boolean allCopied = true;

    for (AbstractEvent event : groups.singleEvents) {
      if (copySingleEvent(event, targetCalendar, sourceTimezone, targetTimezone)) {
        successCount++;
      } else {
        allCopied = false;
      }
    }

    for (Map.Entry<String, List<EventInstance>> entry : groups.seriesMap.entrySet()) {
      int seriesSize = entry.getValue().size();
      if (copySeriesEvents(entry.getValue(), targetCalendar, sourceTimezone, targetTimezone)) {
        successCount += seriesSize;
      } else {
        allCopied = false;
      }
    }

    String message = "Successfully copied " + successCount + " event(s) from " + sourceFromDate
        + " to " + sourceToDate + " starting at " + targetStartDate
        + " in calendar '" + targetCalendarName + "'";

    if (!allCopied) {
      return CommandResult.warning(message,  "Copying all events failed.");
    } else {
      return CommandResult.success(message);
    }
  }

  private boolean copySingleEvent(AbstractEvent event, Imodel targetCalendar,
                                  ZoneId sourceTimezone, ZoneId targetTimezone) {
    try {
      LocalDate eventDate = event.getStart().toLocalDate();
      long daysFromStart = ChronoUnit.DAYS.between(sourceFromDate, eventDate);
      LocalDate targetEventDate = targetStartDate.plusDays(daysFromStart);

      LocalTime sourceStartTime = event.getStart().toLocalTime();
      LocalTime sourceEndTime = event.getEnd().toLocalTime();

      LocalDateTime targetStart = convertEventTime(
          targetEventDate, sourceStartTime, sourceTimezone, targetTimezone);
      LocalDateTime targetEnd = convertEventTime(
          targetEventDate, sourceEndTime, sourceTimezone, targetTimezone);

      if (targetEnd.isBefore(targetStart) || targetEnd.equals(targetStart)) {
        targetEnd = targetEnd.plusDays(1);
      }

      return targetCalendar.createEvent(event.getSubject(), targetStart, targetEnd);
    } catch (Exception e) {
      return false;
    }
  }

  private boolean copySeriesEvents(List<EventInstance> instances, Imodel targetCalendar,
                                   ZoneId sourceTimezone, ZoneId targetTimezone) {
    try {
      if (instances.isEmpty()) {
        return false;
      }

      EventInstance firstInstance = instances.get(0);
      String subject = StringUtils.removeQuotes(firstInstance.getSubject());
      LocalTime startTime = firstInstance.getStart().toLocalTime();
      LocalTime endTime = firstInstance.getEnd().toLocalTime();

      Set<DayOfWeek> weekdays = instances.stream()
          .map(e -> e.getStart().getDayOfWeek())
          .collect(Collectors.toSet());

      LocalDate firstTargetDate = findFirstMatchingWeekday(targetStartDate, weekdays);

      LocalDateTime firstTargetStart = convertEventTime(
          firstTargetDate, startTime, sourceTimezone, targetTimezone);
      LocalDateTime firstTargetEnd = convertEventTime(
          firstTargetDate, endTime, sourceTimezone, targetTimezone);

      LocalTime targetStartTime = firstTargetStart.toLocalTime();
      LocalTime targetEndTime = firstTargetEnd.toLocalTime();

      int count = instances.size();
      return targetCalendar.createEventSeries(
          subject, firstTargetDate, targetStartTime,
          count, targetEndTime, weekdays);
    } catch (Exception e) {
      return false;
    }
  }
}
