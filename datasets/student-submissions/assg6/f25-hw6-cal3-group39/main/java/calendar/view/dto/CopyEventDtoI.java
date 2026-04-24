package calendar.view.dto;

import calendar.model.InterfaceEvent;

/**
 * Represents a read-only contract for a request to copy events within
 * a calendar system. Implementations of this interface encapsulate
 * all information needed to perform event-copy operations such as
 * copying a single event, all events on a specific date, or events
 * within a date range.
 */
public interface CopyEventDtoI {

  /**
   * Describes the different modes of copying events.
   *
   * <ul>
   *   <li>{@code SELECTED_EVENT}: Copy a single, explicitly provided event.</li>
   *   <li>{@code ALL_ON_DATE}: Copy all events that occur on a specified date.</li>
   *   <li>{@code DATE_RANGE}: Copy all events within a defined date range.</li>
   * </ul>
   */
  enum CopyMode {
    SELECTED_EVENT,
    ALL_ON_DATE,
    DATE_RANGE
  }

  /**
   * Returns the copy mode describing how events should be duplicated.
   *
   * @return the {@link CopyMode} representing the copy strategy
   */
  CopyMode getMode();

  /**
   * Returns the name of the target calendar where events will be pasted.
   *
   * @return the target calendar name
   */
  String getTargetCalendarName();

  /**
   * Returns the date on which events will be pasted.
   *
   * @return the target date as a string
   */
  String getTargetDate();

  /**
   * Returns the specific time for pasting a single copied event, if applicable.
   *
   * @return the target time, or {@code null} if not applicable
   */
  String getTargetTime();

  /**
   * Returns the event being copied when the mode is {@code SELECTED_EVENT}.
   *
   * @return the selected event, or {@code null} if not applicable
   */
  InterfaceEvent getSelectedEvent();

  /**
   * Returns the source date used when the mode is {@code ALL_ON_DATE}.
   *
   * @return the source date, or {@code null} if not applicable
   */
  String getSourceDate();

  /**
   * Returns the start date of the date range used when the mode
   * is {@code DATE_RANGE}.
   *
   * @return the range start date, or {@code null} if not applicable
   */
  String getRangeStart();

  /**
   * Returns the end date of the date range used when the mode
   * is {@code DATE_RANGE}.
   *
   * @return the range end date, or {@code null} if not applicable
   */
  String getRangeEnd();
}
