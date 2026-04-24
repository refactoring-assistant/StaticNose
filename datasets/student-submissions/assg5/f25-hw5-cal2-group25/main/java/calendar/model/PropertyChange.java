package calendar.model;

import java.time.LocalDateTime;

/**
 * Describes a single property change for an event edit operation.
 * A property change indicates which field of an event is being updated and what the new value is.
 */
public final class PropertyChange {

  /**
   * The type of property being changed.
   */
  public enum Kind { SUBJECT, START, END, DESCRIPTION, LOCATION, STATUS }

  private final Kind kind;
  private final String stringValue;
  private final LocalDateTime dateTimeValue;

  /**
   * Constructor to create a Property Change Object storing Kind of property.
   *
   * @param k  Kind of property being changed.
   * @param s  String value of the property.
   * @param dt Date time value if the property is Start and End time.
   */
  private PropertyChange(Kind k, String s, LocalDateTime dt) {
    kind = k;
    stringValue = s;
    dateTimeValue = dt;
  }

  /**
   * Creates a change for the event subject.
   *
   * @param v the new subject value
   * @return a new PropertyChange for the subject
   */
  public static PropertyChange subject(String v) {
    return new PropertyChange(Kind.SUBJECT, v, null);
  }

  /**
   * Creates a change for the event description.
   *
   * @param v the new description value
   * @return a new PropertyChange for the description
   */
  public static PropertyChange description(String v) {
    return new PropertyChange(Kind.DESCRIPTION, v, null);
  }

  /**
   * Creates a change for the event location.
   *
   * @param v the new location value
   * @return a new PropertyChange for the location
   */
  public static PropertyChange location(String v) {
    return new PropertyChange(Kind.LOCATION, v, null);
  }

  /**
   * Creates a change for the event status.
   *
   * @param v the new status value
   * @return a new PropertyChange for the status
   */
  public static PropertyChange status(String v) {
    return new PropertyChange(Kind.STATUS, v, null);
  }

  /**
   * Creates a change for the event start time.
   *
   * @param v the new start time
   * @return a new PropertyChange for the start time
   */
  public static PropertyChange start(LocalDateTime v) {
    return new PropertyChange(Kind.START, null, v);
  }

  /**
   * Creates a change for the event end time.
   *
   * @param v the new end time
   * @return a new PropertyChange for the end time
   */
  public static PropertyChange end(LocalDateTime v) {
    return new PropertyChange(Kind.END, null, v);
  }

  /**
   * Gets the kind of property being changed.
   *
   * @return the property kind
   */
  public Kind kind() {
    return kind;
  }

  /**
   * Gets the string value for this change.
   *
   * @return the new string value.
   */
  public String stringValue() {
    return stringValue;
  }

  /**
   * Gets the date-time value for this change.
   *
   * @return the new date-time value.
   */
  public LocalDateTime dateTimeValue() {
    return dateTimeValue;
  }
}
