package calendar.model;

/**
 * Edit scope as specified:
 * - SINGLE: only the identified instance.
 * - FROM_THIS: the identified instance and all future instances in its series.
 * - ENTIRE_SERIES: all instances of the series (past and future).
 */
public enum EditScope {
  SINGLE,
  FROM_THIS,
  ENTIRE_SERIES
}

