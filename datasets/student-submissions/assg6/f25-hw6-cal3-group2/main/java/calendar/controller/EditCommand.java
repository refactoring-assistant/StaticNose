package calendar.controller;

import calendar.event.EventContext;
import calendar.model.CalendarInterface;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents the process of event edition. Use regex to match edition pattern and update
 * the properties accordingly with the new value.
 */
public class EditCommand extends CommandFactory {
  private String oldProperty;
  private String newPropertyValue;
  private String newSubject = null;
  private String newStartDateTime = null;
  private String newEndDateTime = null;
  private String newDescription = null;
  private String newLocation = null;
  private String newEventStatus = null;


  private static final Pattern E_P_FROM_TO_WNP = Pattern.compile(
      "^edit event (\\S+) (?:(\\S+)|\"([^\"]*)\") from (\\S+) to (\\S+) with (.*?)$");

  private static final Pattern ES_P_FROM_WNP = Pattern.compile(
      "^edit events (\\S+) (?:(\\S+)|\"([^\"]*)\") from (\\S+) with (.*?)$");

  private static final Pattern S_P_FROM_WNP = Pattern.compile(
      "^edit series (\\S+) (?:(\\S+)|\"([^\"]*)\") from (\\S+) with (.*?)$");


  private static final Pattern NEW_VALUE_PARSER =
      Pattern.compile("^(?:(\\S+)|\"([^\"]*)\")$");

  private final CalendarInterface calendar;
  private final String commandLine;

  /**
   * Constructor for EditCommand Class.
   *
   * @param commandLine the input line of a command
   * @param calendar    the calendar object
   */
  public EditCommand(String commandLine, CalendarInterface calendar) {
    this.calendar = calendar;
    this.commandLine = commandLine;
  }

  @Override
  public void execute() throws IllegalArgumentException {
    parseCommand(this.commandLine);
  }

  @Override
  protected void parseCommand(String commandLine) throws IllegalArgumentException {
    Matcher matcher;

    matcher = E_P_FROM_TO_WNP.matcher(commandLine);
    if (matcher.matches()) {
      oldProperty = matcher.group(1).trim();
      subject = ((matcher.group(2) != null) ? matcher.group(2) : matcher.group(3)).trim();
      startDateTime = matcher.group(4).trim();
      endDateTime = matcher.group(5).trim();
      newPropertyValue = matcher.group(6).trim();
      try {
        getNewPropertyValue(oldProperty, newPropertyValue);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
      EventContext newContext = new EventContext(newSubject, newStartDateTime, newEndDateTime,
          newDescription, newLocation, newEventStatus);
      this.calendar.editSingleEvent(subject, startDateTime, endDateTime, newContext);
      return;
    }

    matcher = ES_P_FROM_WNP.matcher(commandLine);
    if (matcher.matches()) {
      oldProperty = matcher.group(1).trim();
      subject = ((matcher.group(2) != null) ? matcher.group(2) : matcher.group(3)).trim();
      startDateTime = matcher.group(4).trim();
      newPropertyValue = matcher.group(5).trim();

      try {
        getNewPropertyValue(oldProperty, newPropertyValue);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
      EventContext newContext = new EventContext(newSubject, newStartDateTime, newEndDateTime,
          newDescription, newLocation, newEventStatus);
      this.calendar.editEventSeries(subject, startDateTime, endDateTime, newContext);
      return;
    }

    matcher = S_P_FROM_WNP.matcher(commandLine);
    if (matcher.matches()) {
      oldProperty = matcher.group(1).trim();
      subject = ((matcher.group(2) != null) ? matcher.group(2) : matcher.group(3)).trim();
      startDateTime = matcher.group(4).trim();
      newPropertyValue = matcher.group(5).trim();

      try {
        getNewPropertyValue(oldProperty, newPropertyValue);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException(e.getMessage());
      }
      EventContext newContext = new EventContext(newSubject, newStartDateTime, newEndDateTime,
          newDescription, newLocation, newEventStatus);
      this.calendar.editEventSeries(subject, startDateTime, endDateTime, newContext);
      return;
    }
    throw new IllegalArgumentException("Edit event failure. Wrong format: " + commandLine);
  }

  private void getNewPropertyValue(String oldProperty, String newPropertyValue)
      throws IllegalArgumentException {
    switch (oldProperty) {
      case "subject":
        {
          Matcher m = NEW_VALUE_PARSER.matcher(newPropertyValue);
          if (m.find()) {
            newSubject = (m.group(1) != null ? m.group(1) : m.group(2)).trim();
          } else {
            throw new IllegalArgumentException("New subject value is invalid");
          }
        }
        break;
      case "location":
        {
          Matcher m = NEW_VALUE_PARSER.matcher(newPropertyValue);
          if (m.find()) {
            newLocation = (m.group(1) != null ? m.group(1) : m.group(2)).trim();
          } else {
            throw new IllegalArgumentException("New location value is invalid");
          }
        }
        break;
      case "description":
        {
          Matcher m = NEW_VALUE_PARSER.matcher(newPropertyValue);
          if (m.find()) {
            newDescription = (m.group(1) != null ? m.group(1) : m.group(2)).trim();
          } else {
            throw new IllegalArgumentException("New description value is invalid");
          }
        }
        break;

      case "start":
        newStartDateTime = newPropertyValue;
        break;
      case "end":
        newEndDateTime = newPropertyValue;
        break;
      case "status":
        newEventStatus = newPropertyValue;
        break;
      default:
    }
  }

}
