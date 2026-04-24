package model;

/**
 * Enumeration defining the scope of edits for recurring event series.
 * This enum determines which events in a series are affected by an edit operation.
 */
public enum EditScope {
  /**
   * Edit only a single instance of the event.
   * If the event is part of a series, only the specified occurrence is modified.
   */
  SINGLE,
  /**
   * Edit the specified event and all future occurrences in the series.
   * This effectively splits the series at the specified point.
   */
  FROM_THIS,
  /**
   * Edit all events in the entire series.
   * All past, present, and future occurrences are modified.
   */
  ALL_IN_SERIES
}
