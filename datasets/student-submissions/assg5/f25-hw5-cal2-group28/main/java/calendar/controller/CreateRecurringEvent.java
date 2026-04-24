package calendar.controller;

import calendar.model.Calendar;
import calendar.model.CalenderManager;
import calendar.model.EventSeries;
import calendar.model.Events;
import calendar.model.WeekDays;
import calendar.view.ViewConsole;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command to create a recurring event series.
 */
public class CreateRecurringEvent implements CommandInterface {

  private final String textInput;

  /**
   * Constructor to initialise commands class.
   *
   * @param textInput the commands entered by user
   */
  public CreateRecurringEvent(String textInput) {
    this.textInput = textInput;
  }

  /**
   * Function to execute commands.
   *
   * @param manager instance of calendar manager
   * @param view    instance of view console
   */
  @Override
  public void execute(CalenderManager manager, ViewConsole view) {
    Calendar calendar = manager.getCurrentCalender();
    if (calendar == null) {
      view.dispError("No calendar in use. Use 'use calendar' command first");
      return;
    }

    Map<String, String> patterns = Map.of(
        "fromFor", "create event (?:(\"([^\"]+)\")|(\\S+)) from (\\S+) to (\\S+) repeats "
            + "(\\S+) for (\\S+) times",
        "fromUntil", "create event (?:(\"([^\"]+)\")|(\\S+)) from (\\S+) to (\\S+) "
            + "repeats (\\S+) until (\\S+)",
        "onFor", "create event (?:(\"([^\"]+)\")|(\\S+)) on (\\S+) repeats (\\S+) "
            + "for (\\S+) times",
        "onUntil", "create event (?:(\"([^\"]+)\")|(\\S+)) on (\\S+) repeats (\\S+) "
            + "until (\\S+)"
    );

    String key = getKey();
    if (key == null) {
      view.dispError("Invalid recurring event format");
      return;
    }

    Pattern pattern = Pattern.compile(patterns.get(key));
    Matcher matcher = pattern.matcher(textInput);

    if (!matcher.find()) {
      view.dispError("Invalid recurring event command");
      return;
    }

    createSeries(key, matcher, calendar, view);
  }

  /**
   * Function to obtain key for type of event series creation.
   *
   * @return the matching key
   */
  private String getKey() {
    if (textInput.contains(" from ")) {
      return textInput.contains(" for ") ? "fromFor" : "fromUntil";
    } else if (textInput.contains(" on ")) {
      return textInput.contains(" for ") ? "onFor" : "onUntil";
    }
    return null;
  }

  /**
   * Function to create Series.
   *
   * @param seriesType the type of series
   * @param matcher    instance of Matcher
   * @param calendar   instance of calendar
   * @param view       instance of view controller
   */
  private void createSeries(String seriesType, Matcher matcher,
                            Calendar calendar, ViewConsole view) {
    try {
      String subject = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
      Integer occurence = null;
      LocalDate untilDate = null;
      LocalDate beginDate;
      LocalDateTime startDate;
      LocalDateTime endDate;
      Events event;
      String dayCode;

      switch (seriesType) {
        case "fromFor":
          startDate = LocalDateTime.parse(matcher.group(4));
          endDate = LocalDateTime.parse(matcher.group(5));
          dayCode = matcher.group(6);
          occurence = Integer.parseInt(matcher.group(7));

          if (!startDate.toLocalDate().equals(endDate.toLocalDate())) {
            view.dispError("Recurring events must end on the same date");
            return;
          }
          event = new Events(subject, startDate, endDate);
          break;
        case "fromUntil":
          startDate = LocalDateTime.parse(matcher.group(4));
          endDate = LocalDateTime.parse(matcher.group(5));
          dayCode = matcher.group(6);
          untilDate = LocalDate.parse(matcher.group(7));
          if (!startDate.toLocalDate().equals(endDate.toLocalDate())) {
            view.dispError("Recurring events must end on the same date");
            return;
          }
          event = new Events(subject, startDate, endDate);
          break;
        case "onFor":
          beginDate = LocalDate.parse(matcher.group(4));
          dayCode = matcher.group(5);
          occurence = Integer.parseInt(matcher.group(6));
          event = new Events(subject, beginDate);
          break;
        default:
          beginDate = LocalDate.parse(matcher.group(4));
          dayCode = matcher.group(5);
          untilDate = LocalDate.parse(matcher.group(6));
          event = new Events(subject, beginDate);
          break;
      }

      String seriesId = UUID.randomUUID().toString();
      EventSeries series = new EventSeries(seriesId, event);
      series.setRepeatDays(parseDayCode(dayCode));

      if (occurence != null) {
        series.setOccur(occurence);
      } else {
        series.setEndDate(untilDate);
      }

      for (Events events : series.genEvents(calendar)) {
        calendar.addEvent(events);
      }

      view.dispSuccess("Successfully created series with subject: " + subject);
    } catch (Exception e) {
      view.dispError("Error creating recurring event: " + e.getMessage());
    }
  }

  /**
   * Function to get day of week based on its code.
   *
   * @param dayCode the code assigned with the day
   * @return the days of the week
   */
  private Set<WeekDays> parseDayCode(String dayCode) {
    Set<WeekDays> days = new HashSet<>();
    for (char c : dayCode.toCharArray()) {
      days.add(WeekDays.fromCode(c));
    }
    return days;
  }
}