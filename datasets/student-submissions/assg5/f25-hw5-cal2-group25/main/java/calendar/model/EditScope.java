package calendar.model;

/**
 * An enum to track the scope of edit
 * Instance - For single event edit.
 * From_Here - For series edit from a certain event.
 * Whole_Series- For editing all events in a series.
 */
public enum EditScope { INSTANCE, FROM_HERE, WHOLE_SERIES }