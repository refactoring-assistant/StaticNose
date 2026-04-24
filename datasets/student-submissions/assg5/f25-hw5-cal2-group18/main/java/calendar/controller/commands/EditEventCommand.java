package calendar.controller.commands;

import calendar.model.Calendar;
import calendar.model.Event;
import java.time.LocalDateTime;

/**
 * Command to edit a single event's property.
 */
public class EditEventCommand implements CalendarCommand {

  private final String property;
  private final String subject;
  private final LocalDateTime start;
  private final LocalDateTime end;
  private final String newValue;

  /**
   * Constructs an EditEventCommand with start and end times.
   *
   * @param property the event property to edit (e.g., subject, description, location)
   * @param subject  the subject of the event to edit
   * @param start    the start time of the event
   * @param end      the end time of the event (can be null)
   * @param newValue the new value to set for the property
   */
  public EditEventCommand(String property, String subject,
                          LocalDateTime start, LocalDateTime end, String newValue) {
    this.property = property;
    this.subject = subject;
    this.start = start;
    this.end = end;
    this.newValue = newValue;
  }

  /**
   * Constructs an EditEventCommand with only start time.
   *
   * @param property the event property to edit
   * @param subject  the subject of the event to edit
   * @param start    the start time of the event
   * @param newValue the new value to set for the property
   */
  public EditEventCommand(String property, String subject,
                          LocalDateTime start, String newValue) {
    this(property, subject, start, null, newValue);
  }

  @Override
  public String execute(Calendar model) {
    try {
      Event event;

      if (end != null) {
        event = model.findEventByTimes(subject, start, end);
        if (event == null) {
          return "Event not found with subject '" + subject + "' from " + start + " to " + end;
        }
      } else {
        event = model.findEvent(subject, start);
        if (event == null) {
          return "Event not found with subject '" + subject + "' starting at " + start;
        }
      }

      model.editEvent(subject, start, property, newValue);
      return "Event updated";
    } catch (IllegalStateException e) {
      return "Error: " + e.getMessage();
    } catch (IllegalArgumentException e) {
      return "Error: " + e.getMessage();
    }
  }
}