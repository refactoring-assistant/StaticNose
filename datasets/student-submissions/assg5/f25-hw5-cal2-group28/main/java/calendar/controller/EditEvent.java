package calendar.controller;

import calendar.model.Calendar;
import calendar.model.CalenderManager;
import calendar.model.Events;
import calendar.view.ViewConsole;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to control edit event command.
 */
public class EditEvent implements CommandInterface {
  private final String textInput;

  /**
   * Constructor to initialise commands class.
   *
   * @param textInput the commands entered by user
   */
  public EditEvent(String textInput) {
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
      view.dispError("No calender in use. Use 'use' command first");
      return;
    }

    Pattern pattern = Pattern.compile("edit event (\\S+) (?:(\"([^\"]+)\")|(\\S+)) from "
        + "(\\S+) to (\\S+) with (?:(\"([^\"]+)\")|(\\S+))", Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(textInput);

    if (!matcher.find()) {
      view.dispError("Invalid command to show status");
      return;
    }

    String property = matcher.group(1).toLowerCase();
    String subject = matcher.group(3) != null ? matcher.group(3) : matcher.group(4);
    LocalDateTime startTime = LocalDateTime.parse(matcher.group(5));
    LocalDateTime endTime = LocalDateTime.parse(matcher.group(6));
    String newProp = matcher.group(8) != null ? matcher.group(8).trim() : matcher.group(9).trim();

    try {
      Events events = calendar.findEvent(subject, startTime);
      if (events == null) {
        view.dispError("No event found for the subject " + subject);
        return;
      }

      if (createDuplicate(calendar, events, property, newProp, startTime)) {
        view.dispError("Duplicate event found for the subject " + subject);
        return;
      }

      Events oldEvent = new Events(events.getSubject(), events.getStartTime(), events.getEndTime());
      propertyChange(events, property, newProp);

      if (property.equals("subject") || property.equals("start") || property.equals("end")) {
        calendar.removeEvent(oldEvent);
        calendar.addEvent(events);
      }

      view.dispSuccess("Event has been edited. Subject: " + events.getSubject()
          + ". Status: " + events.getStatus());
    } catch (Exception e) {
      view.dispError("Error editing event: " + e.getMessage());
    }
  }

  /**
   * Check for duplicate events.
   *
   * @param calendar  instace of calendar object
   * @param event     instance of event
   * @param property  given property of event
   * @param newProp   new property mentioned
   * @param startTime new start time to assign
   * @return true if duplicate, else false
   */
  private boolean createDuplicate(Calendar calendar, Events event, String property,
                                  String newProp, LocalDateTime startTime) {
    String subject = property.equals("subject") ? newProp : event.getSubject();
    LocalDateTime newStartTime = property.equals("start")
        ? LocalDateTime.parse(newProp)
        : startTime;
    Events existing = calendar.findEvent(subject, newStartTime);
    return existing != null && !existing.equals(event);
  }

  /**
   * Function to apply a property change to the event.
   *
   * @param event       instance of the Event changes are being applied to
   * @param property    initial property field of the event
   * @param newProperty the property value to assign
   */
  private void propertyChange(Events event, String property, String newProperty) {
    switch (property) {
      case "subject":
        event.setSubject(newProperty);
        break;
      case "start":
        event.setStartTime(LocalDateTime.parse(newProperty));
        break;
      case "end":
        event.setEndTime(LocalDateTime.parse(newProperty));
        break;
      case "description":
        event.setDescription(newProperty);
        break;
      case "location":
        event.setLocation(newProperty);
        break;
      case "status":
        String status = newProperty.toLowerCase();
        if (status.equals("public") || status.equals("private")) {
          event.setStatus(status);
        } else {
          throw new IllegalArgumentException("Invalid status for event: " + newProperty);
        }
        break;
      default:
        throw new IllegalArgumentException("Invalid status: " + newProperty
            + ". Must be 'public' or 'private'");
    }
  }
}