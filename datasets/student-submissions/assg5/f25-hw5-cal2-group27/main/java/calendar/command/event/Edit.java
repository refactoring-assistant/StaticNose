package calendar.command.event;

import calendar.command.EditProperty;
import calendar.command.EditScope;
import calendar.model.CalendarManager;
import calendar.model.Event;
import calendar.model.EventBuilder;
import calendar.model.EventStatus;
import calendar.model.MyCalendar;
import calendar.util.DateTimeParser;
import calendar.view.CalendarTextView;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Command to edit an existing event or event series.
 */
public class Edit extends AbstractEventCommand {

  private final EditScope scope;
  private final EditProperty property;
  private final String subject;
  private final String startStr;
  private final String endStr;
  private final String newValueStr;

  /**
   * Constructs a new Edit command.
   *
   * @param scope       The scope of the edit (single, this and future, or entire series).
   * @param property    The property to edit.
   * @param subject     The subject to identify the event.
   * @param startStr    The start time to identify the event.
   * @param endStr      The end time to identify the event (for single event edits).
   * @param newValueStr The new value for the property.
   */
  public Edit(EditScope scope, EditProperty property, String subject, String startStr,
              String endStr,
              String newValueStr) {
    this.scope = scope;
    this.property = property;
    this.subject = subject;
    this.startStr = startStr;
    this.endStr = endStr;
    this.newValueStr = newValueStr;
  }

  @Override
  protected void executeWithCalendar(CalendarManager manager, MyCalendar model,
                                     CalendarTextView view) {
    if (scope == null) {
      view.printMessage("Unknown edit scope.");
      return;
    }
    ZoneId zoneId = model.getZoneId();
    ZonedDateTime start = DateTimeParser.parseDateTime(startStr, zoneId);
    Object newValue = parseNewValue(property, newValueStr, zoneId);
    Consumer<EventBuilder> updater = createUpdater(property, newValue);

    try {
      switch (scope) {
        case EVENT:
          editSingleEvent(model, view, start, updater);
          break;
        case EVENTS:
          editThisAndFutureEvents(model, view, start, updater);
          break;
        case SERIES:
          editEntireSeries(model, view, start, updater);
          break;
        default:
          throw new IllegalArgumentException("Unknown edit scope: " + scope);
      }
    } catch (IllegalArgumentException e) {
      view.printMessage("Error: " + e.getMessage());
    }
  }

  private void editSingleEvent(MyCalendar model, CalendarTextView view, ZonedDateTime start,
                               Consumer<EventBuilder> updater) {
    ZonedDateTime end = DateTimeParser.parseDateTime(endStr, model.getZoneId());
    Optional<Event> optionalEvent = model.findEvent(subject, start, end);

    if (optionalEvent.isEmpty()) {
      view.printMessage("Error: Event not found.");
      return;
    }

    Event originalEvent = optionalEvent.get();
    EventBuilder builder = originalEvent.toBuilder();
    updater.accept(builder);

    Event newEvent = builder.build();
    model.replaceEvent(originalEvent.getId(), newEvent);
    view.printMessage("Event updated successfully.");
  }

  private void editThisAndFutureEvents(MyCalendar model, CalendarTextView view,
                                       ZonedDateTime start, Consumer<EventBuilder> updater) {
    List<Event> foundEvents = model.findEventsBySubjectAndStart(subject, start);
    Event pivotalEvent = getUniquePivotalEvent(foundEvents);
    model.updateEventsFrom(pivotalEvent.getSeriesId(), pivotalEvent.getStart(),
        property == EditProperty.START, updater);
    view.printMessage("Event series updated successfully (from this event onwards).");
  }

  private void editEntireSeries(MyCalendar model, CalendarTextView view, ZonedDateTime start,
                                    Consumer<EventBuilder> updater) {
    List<Event> foundEvents = model.findEventsBySubjectAndStart(subject, start);
    Event pivotalEvent = getUniquePivotalEvent(foundEvents);
    model.updateEntireSeries(pivotalEvent.getSeriesId(), pivotalEvent.getStart(),
        property == EditProperty.START, updater);
    view.printMessage("Entire event series updated successfully.");
  }

  private Event getUniquePivotalEvent(List<Event> foundEvents) {
    if (foundEvents.isEmpty()) {
      throw new IllegalArgumentException("Event not found.");
    }
    if (foundEvents.size() > 1) {
      throw new IllegalArgumentException(
          "Multiple events match, cannot edit series. Please specify more details.");
    }
    return foundEvents.get(0);
  }

  private Object parseNewValue(EditProperty property, String valueStr, ZoneId zoneId) {
    switch (property) {
      case START:
      case END:
        return DateTimeParser.parseDateTime(valueStr, zoneId);
      case STATUS:
        return EventStatus.fromString(valueStr);
      default:
        return valueStr;
    }
  }

  private Consumer<EventBuilder> createUpdater(EditProperty property, Object newValue) {
    switch (property) {
      case SUBJECT:
        String newSubject = (String) newValue;
        return eventBuilder -> eventBuilder.subject(newSubject);
      case START:
        ZonedDateTime newStart = (ZonedDateTime) newValue;
        return eventBuilder -> eventBuilder.start(newStart);
      case END:
        ZonedDateTime newEnd = (ZonedDateTime) newValue;
        return eventBuilder -> eventBuilder.end(newEnd);
      case DESCRIPTION:
        String newDescription = (String) newValue;
        return eventBuilder -> eventBuilder.description(newDescription);
      case LOCATION:
        String newLocation = (String) newValue;
        return eventBuilder -> eventBuilder.location(newLocation);
      case STATUS:
        EventStatus newStatus = (EventStatus) newValue;
        return eventBuilder -> eventBuilder.status(newStatus);
      default:
        throw new IllegalArgumentException("Unsupported property: " + property);
    }
  }
}
