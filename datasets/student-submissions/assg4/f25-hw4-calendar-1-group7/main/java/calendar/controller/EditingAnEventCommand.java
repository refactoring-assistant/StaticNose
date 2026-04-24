package calendar.controller;

import calendar.model.Calendar;
import calendar.model.Event;
import calendar.view.MyCalendarView;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * Command to edit event properties.
 * Supports editing single events, future events in a series, or all events in a series.
 */
public class EditingAnEventCommand implements Command {
  private final EditType editType;
  private final String property;
  private final String subject;
  private final LocalDateTime start;
  private final String newValue;

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  /**
   * Creates a new EditEventCommand.
   *
   * @param editType the scope of editing (SINGLE, FUTURE, ALL)
   * @param property the property to edit
   * @param subject the event subject
   * @param start the start time of the event
   * @param newValue the new value for the property
   */
  public EditingAnEventCommand(EditType editType, String property, String subject,
                               LocalDateTime start, String newValue) {
    this.editType = editType;
    this.property = property.toLowerCase();
    this.subject = subject;
    this.start = start;
    this.newValue = newValue;
  }

  @Override
  public void execute(Calendar calendar, MyCalendarView view) {
    try {
      List<Event> matchingEvents = calendar.findEvents(subject, start);

      if (matchingEvents.isEmpty()) {
        view.displayError("No event found matching: " + subject + " at " + start);
        return;
      }

      if (matchingEvents.size() > 1 && editType == EditType.SINGLE) {
        view.displayError("Multiple events found. Cannot edit - specify more details.");
        return;
      }

      Event targetEvent = matchingEvents.get(0);
      String seriesId = targetEvent.getSeriesId();

      switch (editType) {
        case SINGLE:
          editSingleEvent(calendar, targetEvent, view);
          break;
        case FUTURE:
          editFutureEvents(calendar, targetEvent, seriesId, view);
          break;
        case ALL:
          editAllEvents(calendar, targetEvent, seriesId, view);
          break;
        default:
          view.displayError("Unknown edit type");
          break;
      }
    } catch (Exception e) {
      view.displayError("Error editing event: " + e.getMessage());
    }
  }

  private void editSingleEvent(Calendar calendar, Event event, MyCalendarView view) {
    applyEdit(event);

    view.displayMessage("Event edited successfully");
  }

  private void editFutureEvents(Calendar calendar, Event startEvent, String seriesId,
                                MyCalendarView view) {
    if (seriesId == null) {
      editSingleEvent(calendar, startEvent, view);
      return;
    }

    List<Event> seriesEvents = calendar.getEventsBySeries(seriesId);
    int editedCount = 0;
    String newSeriesId = null;

    // If editing start time, create new series for future events
    if (property.equals("start")) {
      newSeriesId = UUID.randomUUID().toString();
    }

    for (Event event : seriesEvents) {
      if (!event.getStart().isBefore(startEvent.getStart())) {
        applyEdit(event);

        if (newSeriesId != null) {
          event.setSeriesId(newSeriesId);
        }

        editedCount++;
      }
    }

    view.displayMessage("Edited " + editedCount + " future event(s)");
  }

  private void editAllEvents(Calendar calendar, Event targetEvent, String seriesId,
                             MyCalendarView view) {
    List<Event> events;

    if (seriesId != null) {
      events = calendar.getEventsBySeries(seriesId);
    } else {
      events = calendar.findEvents(subject, start);
    }

    if (events.isEmpty()) {
      view.displayError("No events found to edit");
      return;
    }

    for (Event event : events) {
      applyEdit(event);
    }

    view.displayMessage("Edited " + events.size() + " event(s)");
  }

  private void applyEdit(Event event) {
    switch (property) {
      case "subject":
        event.setSubject(newValue);
        break;
      case "start":
        LocalDateTime newStart = LocalDateTime.parse(newValue, DATE_TIME_FORMATTER);
        event.setStart(newStart);
        break;
      case "end":
        LocalDateTime newEnd = LocalDateTime.parse(newValue, DATE_TIME_FORMATTER);
        event.setEnd(newEnd);
        break;
      case "location":
        event.setLocation(newValue);
        break;
      case "description":
        event.setDescription(newValue);
        break;
      case "status":
        event.setStatus(newValue);
        break;
      default:
        throw new IllegalArgumentException("Invalid property: " + property);
    }
  }

  @Override
  public boolean validate() {
    if (property == null || subject == null || start == null || newValue == null) {
      return false;
    }

    switch (property) {
      case "subject":
      case "start":
      case "end":
      case "location":
      case "description":
      case "status":
        return true;
      default:
        return false;
    }
  }
}