package calendar.controller;

import calendar.model.CalendarModel;
import calendar.model.EventProperty;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a class Edit Command that implements Command interface and is a part of Command
 * design pattern. Needed to execute commands related to editing event, events, and event series.
 */
public class EditCommand implements Command {
  private final CalendarModel model;

  /**
   * Constructs the edit command given the current calendar database.
   *
   * @param model the current calendar model.
   * @throws NullPointerException if the database/model is null.
   */
  public EditCommand(CalendarModel model) {
    if (model == null) {
      throw new IllegalStateException("No Calendar in use to execute edit");
    } else {
      this.model = model;
    }
  }

  @Override
  public void execute(Scanner scanner) {
    String command = scanner.nextLine().trim();
    Matcher editOneEventMatch = editOneEventHelper(command);
    Matcher editEventsMatch = editEventsHelper(command);
    Matcher editSeriesMatch = editSeriesHelper(command);
    if (editOneEventMatch.matches()) {
      editOneEventSet(editOneEventMatch, model);
    } else if (editEventsMatch.matches()) {
      editEventsSet(editEventsMatch, model);
    } else if (editSeriesMatch.matches()) {
      editSeriesSet(editSeriesMatch, model);
    } else {
      throw new IllegalStateException("Invalid command. Should be one of the edit commands");
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

  private Matcher editOneEventHelper(String command) {
    Pattern editOneEventPattern = Pattern.compile(
        "^event\\s+(\\S+)\\s+(?:\"([^\"]+)\"|(\\S+))\\s+from\\s+(\\d{4}-\\d{2}-\\d{2}T"
            + "\\d{2}:\\d{2})\\s+to\\s+(\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2})\\s+with\\s+(.+?)\\s*$",
        Pattern.CASE_INSENSITIVE);
    return editOneEventPattern.matcher(command);
  }

  private void editOneEventSet(Matcher editOneEventPatternMatch, CalendarModel model) {
    EventProperty property = createProperty(editOneEventPatternMatch.group(1));
    String subject =
        subjectHelper(editOneEventPatternMatch.group(2), editOneEventPatternMatch.group(3));
    String startDateTime = editOneEventPatternMatch.group(4);
    String endDateTime = editOneEventPatternMatch.group(5);
    String val = editOneEventPatternMatch.group(6);
    model.editEvent(property, subject, startDateTime, endDateTime, val);
  }

  private Matcher editEventsHelper(String command) {
    Pattern editEventsPattern = Pattern.compile(
        "^events\\s+(\\S+)\\s+(?:\"([^\"]+)\"|(\\S+))\\s+from\\s+(\\d{4}-\\d{2}-\\d{2}T"
            + "\\d{2}:\\d{2})\\s+with\\s+(.+?)\\s*$", Pattern.CASE_INSENSITIVE);
    return editEventsPattern.matcher(command);
  }

  private void editEventsSet(Matcher editEventsPatternMatch, CalendarModel model) {
    EventProperty property = createProperty(editEventsPatternMatch.group(1));
    String subject =
        subjectHelper(editEventsPatternMatch.group(2), editEventsPatternMatch.group(3));
    String startDateTime = editEventsPatternMatch.group(4);
    String val = editEventsPatternMatch.group(5);
    model.editEvents(property, subject, startDateTime, val);
  }

  private Matcher editSeriesHelper(String command) {
    Pattern editSeriesPattern = Pattern.compile(
        "^series\\s+(\\S+)\\s+(?:\"([^\"]+)\"|(\\S+))\\s+from\\s+(\\d{4}-\\d{2}-\\d{2}T"
            + "\\d{2}:\\d{2})\\s+with\\s+(.+?)\\s*$", Pattern.CASE_INSENSITIVE);
    return editSeriesPattern.matcher(command);
  }

  private void editSeriesSet(Matcher editSeriesPatternMatch, CalendarModel model) {
    EventProperty property = createProperty(editSeriesPatternMatch.group(1));
    String subject =
        subjectHelper(editSeriesPatternMatch.group(2), editSeriesPatternMatch.group(3));
    String startDateTime = editSeriesPatternMatch.group(4);
    String val = editSeriesPatternMatch.group(5);
    model.editSeries(property, subject, startDateTime, val);
  }

  private EventProperty createProperty(String property) {
    try {
      return EventProperty.valueOf(property.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalStateException("Invalid event property");
    }
  }

}
