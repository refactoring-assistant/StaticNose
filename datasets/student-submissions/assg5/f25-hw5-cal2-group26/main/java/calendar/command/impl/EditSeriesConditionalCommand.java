package calendar.command.impl;

import calendar.command.CommandInterface;
import calendar.controller.CalendarControllerInterface;
import calendar.model.EventInterface;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Command to edit a series with a date condition (only before/after a certain date).
 * This command works by filtering events and then using the controller's existing methods.
 */
public class EditSeriesConditionalCommand implements CommandInterface {
  private final CalendarControllerInterface controller;
  private final String subject;
  private final ZonedDateTime start;
  private final String property;
  private final Object newValue;
  private final ZonedDateTime conditionDate;
  private final boolean isBefore;

  /**
   * Creates a command to edit events in a series that match a date condition.
   *
   * @param subject       The name of the recurring event series.
   * @param start         The start time of an event in the series.
   * @param property      The property to change (e.g., subject, start, end).
   * @param newValue      The new value to set for the property.
   * @param conditionDate The date to compare against.
   * @param isBefore      True to edit only events before the date, false for after.
   */
  public EditSeriesConditionalCommand(CalendarControllerInterface controller, String subject,
                                      ZonedDateTime start,
                                      String property, Object newValue,
                                      ZonedDateTime conditionDate, boolean isBefore) {
    this.controller = controller;
    this.subject = subject;
    this.start = start;
    this.property = property;
    this.newValue = newValue;
    this.conditionDate = conditionDate;
    this.isBefore = isBefore;
  }

  @Override
  public String execute() {
    try {
      List<EventInterface> matchingEvents = controller.queryEvents(e ->
          e.getSubject().equals(subject)
      );

      if (matchingEvents.isEmpty()) {
        return "Error: No events found with subject: " + subject;
      }

      EventInterface targetEvent = matchingEvents.stream()
          .filter(e -> e.getStart().equals(start))
          .findFirst()
          .orElse(null);

      if (targetEvent == null) {
        return "Error: Event not found: " + subject + " at " + start;
      }

      List<EventInterface> eventsToEdit = matchingEvents.stream()
          .filter(e -> meetsCondition(e.getStart(), conditionDate, isBefore))
          .collect(Collectors.toList());

      if (eventsToEdit.isEmpty()) {
        return "Error: No events match the condition";
      }

      int editedCount = 0;
      for (EventInterface event : eventsToEdit) {
        controller.editSingleEvent(event.getSubject(), event.getStart(), event.getEnd(),
            property, newValue);
        editedCount++;
      }

      String condition = isBefore ? "before" : "after";
      return "Series edited conditionally: " + editedCount + " events updated (only "
          + condition + " " + conditionDate.toLocalDate() + ")";
    } catch (Exception e) {
      return "Error: " + e.getMessage();
    }
  }

  private boolean meetsCondition(ZonedDateTime eventStart, ZonedDateTime conditionDate,
                                 boolean isBefore) {
    if (isBefore) {
      return eventStart.toLocalDate().isBefore(conditionDate.toLocalDate());
    } else {
      return eventStart.toLocalDate().isAfter(conditionDate.toLocalDate());
    }
  }

  @Override
  public String getDescription() {
    String condition = isBefore ? "only before" : "only after";
    return "Edit series conditional: " + subject + " from " + start
        + " set " + property + " to " + newValue + " " + condition + " " + conditionDate;
  }
}