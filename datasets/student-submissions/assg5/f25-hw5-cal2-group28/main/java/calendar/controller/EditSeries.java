package calendar.controller;

import calendar.model.Calendar;
import calendar.model.CalenderManager;
import calendar.model.Events;
import calendar.view.ViewConsole;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to control editing all events in a series.
 */
public class EditSeries implements CommandInterface {

  private final String inputText;

  /**
   * Constructor to initialise commands class.
   *
   * @param textInput the commands entered by user
   */
  public EditSeries(String textInput) {
    this.inputText = textInput;
  }

  /**
   * Function to execute the command.
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

    Pattern pattern = Pattern.compile("edit series (\\S+) (?:(\"([^\"]+)\")|(\\S+)) "
        + "from (\\S+) with (?:(\"([^\"]+)\")|(\\S+))", Pattern.CASE_INSENSITIVE);
    Matcher matcher = pattern.matcher(inputText);

    if (!matcher.find()) {
      view.dispError("Invalid command to edit series");
      return;
    }

    String property = matcher.group(1);
    String subject = matcher.group(3) != null ? matcher.group(3).trim() : matcher.group(4);
    LocalDateTime startTime = LocalDateTime.parse(matcher.group(5));
    String newProp = matcher.group(7) != null ? matcher.group(7).trim() : matcher.group(8).trim();

    try {
      List<Events> matchEvents = calendar.findEvents(subject, startTime);
      if (matchEvents.isEmpty()) {
        view.dispError("No matching events found for:  " + subject + " starting at: " + startTime);
        return;
      }

      Events event = matchEvents.get(0);
      String seriesId = event.getIdSeries();

      if (seriesId == null) {
        view.dispError("Event is not part of series");
        return;
      }

      List<Events> editEvents = calendar.getEventSeries(seriesId, event.getInitStart());
      if (editEvents.isEmpty()) {
        view.dispError("No events found in series with ID: " + seriesId);
        return;
      }

      for (Events editEvent : editEvents) {
        if (createDuplicate(calendar, editEvent, property, newProp, editEvent.getStartTime())) {
          view.dispError("Duplicate events found in series beginning at: " + startTime);
          return;
        }
      }

      String newSeriesId = UUID.randomUUID().toString();

      for (Events editEvent : editEvents) {
        Events oldEvent = new Events(editEvent.getSubject(), editEvent.getStartTime(),
            editEvent.getEndTime());

        propertyChange(editEvent, property, newProp);

        if (property.equals("subject") || property.equals("start") || property.equals("end")) {
          calendar.removeEvent(oldEvent);
          calendar.addEvent(editEvent);
        }

        if (property.equals("start")) {
          editEvent.setIdSeries(newSeriesId);
          editEvent.setInitStart(LocalDateTime.parse(
              newProp.trim().replace("'", ""))); // Trim quotes
        }

      }
      view.dispSuccess("Successfully edited events"); // Only print once
    } catch (Exception e) {
      view.dispError("Error editing events: " + e.getMessage());
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
        throw new IllegalArgumentException("Invalid property: " + property);
    }
  }
}