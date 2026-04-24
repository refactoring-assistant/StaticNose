package calendar.controller.command;

import calendar.exceptions.EventNotFoundException;
import calendar.model.calendar.Icalendar;
import calendar.model.event.EventSeries;
import calendar.model.event.EventStatus;
import calendar.model.event.Ievent;
import calendar.view.IcalendarView;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Command to edit events in the calendar.
 * Supports three modes: editing a single instance, editing from this event forward,
 * or editing all events in the series.
 */
public class EditEventCommand implements Icommand {

  /**
   * Enumeration of edit modes for event modification.
   */
  public enum EditMode {
    /** Edit single instance only. */
    SINGLE,
    /** Edit this event and all future events in series. */
    FROM_THIS,
    /** Edit all events in the series. */
    ALL_SERIES
  }

  private final EditMode mode;
  private final String property;
  private final String eventSubject;
  private final LocalDateTime eventStart;
  private final String newValue;

  /**
   * Constructor for edit command.
   *
   * @param mode the edit mode (SINGLE, FROM_THIS, or ALL_SERIES)
   * @param property the property to edit (subject, start, end, description, location, status)
   * @param eventSubject the subject of the event to find
   * @param eventStart the start time of the event to find
   * @param newValue the new value for the property
   */
  public EditEventCommand(EditMode mode, String property, String eventSubject,
                          LocalDateTime eventStart, String newValue) {
    this.mode = mode;
    this.property = property;
    this.eventSubject = eventSubject;
    this.eventStart = eventStart;
    this.newValue = newValue;
  }

  /**
   * Executes the edit event command.
   *
   * @param calendar the calendar containing the event to edit
   * @param view the view to display messages to
   * @throws Exception if the event cannot be found or edited
   */
  @Override
  public void execute(Icalendar calendar, IcalendarView view) throws Exception {
    try {
      Optional<Ievent> eventOpt = calendar.findEvent(eventSubject, eventStart);

      if (eventOpt.isEmpty()) {
        throw new EventNotFoundException(
            "No event found with subject '" + eventSubject + "' starting at " + eventStart
        );
      }

      Ievent originalEvent = eventOpt.get();

      Ievent modifiedEvent = applyPropertyChange(originalEvent, property, newValue);

      EventSeries.ModificationType modificationType;
      switch (mode) {
        case SINGLE:
          modificationType = EventSeries.ModificationType.SINGLE;
          break;
        case FROM_THIS:
          modificationType = EventSeries.ModificationType.FROM_THIS;
          break;
        case ALL_SERIES:
          modificationType = EventSeries.ModificationType.ALL;
          break;
        default:
          throw new IllegalArgumentException("Unknown edit mode: " + mode);
      }

      calendar.updateEventInSeries(originalEvent, modifiedEvent, modificationType);

      String modeMessage;
      switch (mode) {
        case SINGLE:
          modeMessage = "Event edited successfully.";
          break;
        case FROM_THIS:
          modeMessage = "Events edited successfully (from this event forward).";
          break;
        case ALL_SERIES:
          modeMessage = "All events in series edited successfully.";
          break;
        default:
          modeMessage = "Event(s) edited successfully.";
      }
      view.displayMessage(modeMessage);

    } catch (EventNotFoundException e) {
      view.displayError(e.getMessage());
      throw e;
    } catch (IllegalStateException e) {
      view.displayError("Cannot edit event: " + e.getMessage());
      throw e;
    } catch (IllegalArgumentException e) {
      view.displayError("Cannot edit event: " + e.getMessage());
      throw e;
    }
  }

  /**
   * Apply a property change to an event using immutable pattern.
   *
   * @param event the original event
   * @param property the property to change
   * @param newValue the new value
   * @return a new event with the property changed
   * @throws IllegalArgumentException if the property is unknown or newValue is invalid
   */
  private Ievent applyPropertyChange(Ievent event, String property, String newValue)
      throws IllegalArgumentException {

    switch (property.toLowerCase()) {
      case "subject":
        return event.withSubject(newValue);

      case "start":
        LocalDateTime newStart = LocalDateTime.parse(newValue);
        return event.withStartDateTime(newStart);

      case "end":
        LocalDateTime newEnd = LocalDateTime.parse(newValue);
        return event.withEndDateTime(newEnd);

      case "description":
        return event.withDescription(newValue);

      case "location":
        return event.withLocation(newValue);

      case "status":
        EventStatus newStatus = parseStatus(newValue);
        return event.withStatus(newStatus);

      default:
        throw new IllegalArgumentException("Unknown property: " + property);
    }
  }

  /**
   * Parse status string to EventStatus enum.
   *
   * @param status the status string (public or private)
   * @return the EventStatus enum
   * @throws IllegalArgumentException if the status is not public or private
   */
  private EventStatus parseStatus(String status) throws IllegalArgumentException {
    if (status.equalsIgnoreCase("public")) {
      return EventStatus.PUBLIC;
    } else if (status.equalsIgnoreCase("private")) {
      return EventStatus.PRIVATE;
    } else {
      throw new IllegalArgumentException("Status must be 'public' or 'private'");
    }
  }
}