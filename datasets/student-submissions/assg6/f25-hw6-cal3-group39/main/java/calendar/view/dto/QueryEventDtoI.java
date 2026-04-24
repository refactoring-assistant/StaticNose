package calendar.view.dto;

/**
 * Represents a read-only contract for search or query requests in the calendar system.
 * Implementations of this interface provide raw input from the QueryDialog,
 * either for a single-date query or a range query.
 */
public interface QueryEventDtoI {

  /**
   * Indicates whether the query is a range query.
   *
   * @return {@code true} if querying a date range, {@code false} if querying a single date
   */
  boolean isRangeQuery();

  /**
   * Returns the single date being queried.
   * Relevant only if {@link #isRangeQuery()} returns {@code false}.
   *
   * @return the single date as a string, or {@code null} if not applicable
   */
  String singleDate();

  /**
   * Returns the start date of the range query.
   * Relevant only if {@link #isRangeQuery()} returns {@code true}.
   *
   * @return the start date as a string, or {@code null} if not applicable
   */
  String startDate();

  /**
   * Returns the start time of the range query.
   * Relevant only if {@link #isRangeQuery()} returns {@code true}.
   *
   * @return the start time as a string, or {@code null} if not applicable
   */
  String startTime();

  /**
   * Returns the end date of the range query.
   * Relevant only if {@link #isRangeQuery()} returns {@code true}.
   *
   * @return the end date as a string, or {@code null} if not applicable
   */
  String endDate();

  /**
   * Returns the end time of the range query.
   * Relevant only if {@link #isRangeQuery()} returns {@code true}.
   *
   * @return the end time as a string, or {@code null} if not applicable
   */
  String endTime();
}
