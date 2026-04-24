package calendar.models;

/**
 * Scope of editing a series of events can be limited to single event, this event anf the following
 * events or all events in the series.
 */
public enum EditScope {
  THIS_EVENT_ONLY, THIS_AND_FOLLOWING, ALL_IN_SERIES
}
