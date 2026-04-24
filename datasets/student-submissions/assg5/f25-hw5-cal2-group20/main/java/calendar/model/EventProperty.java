package calendar.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Represents an enumeration of event property. Currently consists of subject , start , end ,
 * description , location , and status keywords.
 */
public enum EventProperty {
  SUBJECT("subject") {
    @Override
    public Event.EventBuilder editProp(Event.EventBuilder builder, String val) {
      return builder.subject(val);
    }
  }, START("start") {
    @Override
    public Event.EventBuilder editProp(Event.EventBuilder builder, String val) {
      try {
        return builder.startDateTime(LocalDateTime.parse(val));
      } catch (DateTimeParseException e) {
        throw new IllegalArgumentException("Invalid start date: " + val);
      }

    }
  }, END("end") {
    @Override
    public Event.EventBuilder editProp(Event.EventBuilder builder, String val) {
      try {
        return builder.endDateTime(LocalDateTime.parse(val));
      } catch (DateTimeParseException e) {
        throw new IllegalArgumentException("Invalid end date: " + val);
      }
    }
  }, DESCRIPTION("description") {
    @Override
    public Event.EventBuilder editProp(Event.EventBuilder builder, String val) {
      return builder.description(val);
    }
  }, LOCATION("location") {
    @Override
    public Event.EventBuilder editProp(Event.EventBuilder builder, String val) {
      try {
        return builder.location(EventLocation.valueOf(val.toUpperCase()));
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid event location: " + val);
      }

    }
  },
  STATUS("status") {
    @Override
    public Event.EventBuilder editProp(Event.EventBuilder builder, String val) {
      try {
        return builder.status(EventStatus.valueOf(val.toUpperCase()));
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Invalid event status: " + val);
      }
    }
  };

  private final String prop;

  /**
   * Edits the property of event, given the event builder and the new string value of property.
   *
   * @param builder the event builder.
   * @param val the new value of the property in string.
   * @return the edited event in event builder form.
   */
  public abstract Event.EventBuilder editProp(Event.EventBuilder builder, String val);

  /**
   * Constructs an enum event property given property name in String.
   *
   * @param prop name of an event property.
   */
  private EventProperty(String prop) {
    this.prop = prop;
  }
}
