package calendar.controller;

/**
 * Enum representing the scope of an edit operation.
 */
public enum EditType {
  /** Edit only a single event instance. */
  SINGLE,

  /** Edit this event and all future events in the series. */
  FUTURE,

  /** Edit all events in the series. */
  ALL
}