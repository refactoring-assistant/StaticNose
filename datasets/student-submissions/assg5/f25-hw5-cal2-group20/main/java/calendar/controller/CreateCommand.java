package calendar.controller;

import calendar.model.CalendarModel;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a class Create Command that implements Command interface and is a part of Command
 * design pattern. Needed to execute commands related to creating events.
 */
public class CreateCommand implements Command {
  private final CalendarModel model;

  /**
   * Constructs the create command given the current calendar model.
   *
   * @param model the current calendar model.
   * @throws IllegalArgumentException if the model is null.
   */
  public CreateCommand(CalendarModel model) {
    if (model == null) {
      throw new IllegalStateException("Invalid command. No active calendar found.");
    }
    this.model = model;
  }

  @Override
  public void execute(Scanner scanner) {
    String command = scanner.nextLine().trim();
    String[] words = command.split("\\s+", 2);
    this.eventKeywordHelper(words[1], model);
  }

  private void eventKeywordHelper(String command, CalendarModel model) {
    Matcher createEventMatch = createEventHelper(command);
    Matcher createAllDayEventMatch = createAllDayEventHelper(command);
    Matcher createRepeatSeriesMatch = createRepeatSeriesHelper(command);
    Matcher createRepeatSeriesAllDayMatch = createRepeatSeriesAllDayEventHelper(command);
    Matcher createUntilSeriesMatch = createUntilSeriesHelper(command);
    Matcher createUntilSeriesAllDayMatch = createUntilSeriesAllDayHelper(command);

    if (createEventMatch.matches()) {
      createEventSet(createEventMatch, model);
    } else if (createAllDayEventMatch.matches()) {
      createAllDayEventSet(createAllDayEventMatch, model);
    } else if (createRepeatSeriesMatch.matches()) {
      createRepeatSeriesSet(createRepeatSeriesMatch, model);
    } else if (createRepeatSeriesAllDayMatch.matches()) {
      createRepeatSeriesAllDaySet(createRepeatSeriesAllDayMatch, model);
    } else if (createUntilSeriesMatch.matches()) {
      createUntilSeriesSet(createUntilSeriesMatch, model);
    } else if (createUntilSeriesAllDayMatch.matches()) {
      createUntilSeriesAllDaySet(createUntilSeriesAllDayMatch, model);
    } else {
      throw new IllegalStateException("Invalid command. Should be one of create event commands");
    }
  }

  private String subjectHelper(String moreWords, String oneWord) {
    String subject;
    if (moreWords != null) {
      subject = moreWords;
    } else {
      subject = oneWord;
    }
    return subject;
  }

  private Matcher createEventHelper(String command) {
    Pattern createEventPattern = Pattern.compile(
          "^(?:\"([^\"]+)\"|(\\S+))\\s+from\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})"
          + "\\s+to\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})$", Pattern.CASE_INSENSITIVE);
    return createEventPattern.matcher(command);
  }

  private void createEventSet(Matcher createEventMatch, CalendarModel model) {
    String subject = subjectHelper(createEventMatch.group(1), createEventMatch.group(2));
    String startDateTime = createEventMatch.group(3);
    String endDateTime = createEventMatch.group(4);
    model.createEvent(subject, startDateTime, endDateTime);
  }

  private Matcher createAllDayEventHelper(String command) {
    Pattern createAllDayEventPattern = Pattern.compile(
        "^(?:\"([^\"]+)\"|(\\S+))\\s+on\\s+(\\d{4}-\\d{2}-\\d{2})$",
        Pattern.CASE_INSENSITIVE);
    return createAllDayEventPattern.matcher(command);

  }

  private void createAllDayEventSet(Matcher  createAllDayEventMatch, CalendarModel model) {
    String subject = subjectHelper(createAllDayEventMatch.group(1),
        createAllDayEventMatch.group(2));
    String startDate = createAllDayEventMatch.group(3);
    model.createAllDayEvent(subject, startDate);
  }

  private Matcher createRepeatSeriesHelper(String command) {
    Pattern createRepeatSeries = Pattern.compile(
        "^(?:\"([^\"]+)\"|(\\S+))\\s+from\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})"
          + "\\s+to\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+repeats\\s+([MTWRFSU]+)"
          + "\\s+for\\s+(\\d+)\\s+times$", Pattern.CASE_INSENSITIVE);
    return createRepeatSeries.matcher(command);
  }

  private void createRepeatSeriesSet(Matcher createRepeatSeriesMatch, CalendarModel model) {
    String subject =
        subjectHelper(createRepeatSeriesMatch.group(1), createRepeatSeriesMatch.group(2));
    String startDateTime = createRepeatSeriesMatch.group(3);
    String endDateTime = createRepeatSeriesMatch.group(4);
    String weekdays = createRepeatSeriesMatch.group(5);
    int occurrences = Integer.parseInt(createRepeatSeriesMatch.group(6));
    model.createEventSeries(subject, startDateTime, endDateTime, weekdays, occurrences);
  }


  private Matcher createRepeatSeriesAllDayEventHelper(String command) {
    Pattern createRepeatSeriesAllDay = Pattern.compile(
        "^(?:\"([^\"]+)\"|(\\S+))\\s+on\\s+(\\d{4}-\\d{2}-\\d{2})\\s+repeats"
          + "\\s+([MTWRFSU]+)\\s+for\\s+(\\d+)\\s+times$", Pattern.CASE_INSENSITIVE);
    return createRepeatSeriesAllDay.matcher(command);
  }

  private void createRepeatSeriesAllDaySet(Matcher createRepeatSeriesAllDayMatch,
                                           CalendarModel model) {
    String subject = subjectHelper(createRepeatSeriesAllDayMatch.group(1),
        createRepeatSeriesAllDayMatch.group(2));
    String startDate = createRepeatSeriesAllDayMatch.group(3);
    String weekdays = createRepeatSeriesAllDayMatch.group(4);
    int occurrences = Integer.parseInt(createRepeatSeriesAllDayMatch.group(5));
    model.createAllDayEventSeries(subject, startDate, weekdays, occurrences);
  }

  private Matcher createUntilSeriesHelper(String command) {
    Pattern createUntilSeries = Pattern.compile(
          "^(?:\"([^\"]+)\"|(\\S+))\\s+from\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})"
            + "\\s+to\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+repeats\\s+([MTWRFSU]+)\\s+until"
            + "\\s+(\\d{4}-\\d{2}-\\d{2})$", Pattern.CASE_INSENSITIVE);
    return createUntilSeries.matcher(command);
  }

  private void createUntilSeriesSet(Matcher createUntilSeriesMatch, CalendarModel model) {
    String subject =
        subjectHelper(createUntilSeriesMatch.group(1), createUntilSeriesMatch.group(2));
    String startDateTime = createUntilSeriesMatch.group(3);
    String endDateTime = createUntilSeriesMatch.group(4);
    String weekdays = createUntilSeriesMatch.group(5);
    String dateUntil = createUntilSeriesMatch.group(6);
    model.createEventSeriesUntil(subject, startDateTime, endDateTime, weekdays, dateUntil);
  }

  private Matcher createUntilSeriesAllDayHelper(String command) {
    Pattern createUntilSeriesAllDay = Pattern.compile(
        "^(?:\"([^\"]+)\"|(\\S+))\\s+on\\s+(\\d{4}-\\d{2}-\\d{2})\\s+repeats\\s+"
            + "([MTWRFSU]+)\\s+until\\s+(\\d{4}-\\d{2}-\\d{2})$", Pattern.CASE_INSENSITIVE);
    return createUntilSeriesAllDay.matcher(command);
  }

  private void createUntilSeriesAllDaySet(Matcher createUntilSeriesAllDayMatch,
                                            CalendarModel model) {
    String subject = subjectHelper(createUntilSeriesAllDayMatch.group(1),
          createUntilSeriesAllDayMatch.group(2));
    String startDate = createUntilSeriesAllDayMatch.group(3);
    String weekdays = createUntilSeriesAllDayMatch.group(4);
    String untilDate = createUntilSeriesAllDayMatch.group(5);
    model.createAllDayEventSeriesUntil(subject, startDate, weekdays, untilDate);
  }
}
