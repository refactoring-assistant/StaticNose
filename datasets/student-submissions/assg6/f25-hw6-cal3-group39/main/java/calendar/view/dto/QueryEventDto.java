package calendar.view.dto;

/**
 * Below is the query event data transfer object.
 */
public class QueryEventDto implements QueryEventDtoI {

  boolean isRangeQuery;
  String singleDate;
  String startDate;
  String startTime;
  String endDate;
  String endTime;

  /**
   * Below is the public constructor.
   *
   * @param isRangeQuery if it is range query true or false.
   * @param singleDate   if query is for a single day.
   * @param startDate    start date of query
   * @param startTime    end time of query
   * @param endDate      end date of query
   * @param endTime      end time of query
   */
  public QueryEventDto(boolean isRangeQuery, String singleDate, String startDate,
                       String startTime, String endDate, String endTime) {
    this.isRangeQuery = isRangeQuery;
    this.singleDate = singleDate;
    this.startDate = startDate;
    this.startTime = startTime;
    this.endDate = endDate;
    this.endTime = endTime;
  }

  /**
   * Indicates whether the query is a range query.
   *
   * @return {@code true} if querying a date range, {@code false} if querying a single date
   */
  @Override
  public boolean isRangeQuery() {
    return this.isRangeQuery;
  }

  /**
   * Returns the single date being queried.
   * Relevant only if {@link #isRangeQuery()} returns {@code false}.
   *
   * @return the single date as a string, or {@code null} if not applicable
   */
  @Override
  public String singleDate() {
    return this.singleDate;
  }

  /**
   * Returns the start date of the range query.
   * Relevant only if {@link #isRangeQuery()} returns {@code true}.
   *
   * @return the start date as a string, or {@code null} if not applicable
   */
  @Override
  public String startDate() {
    return this.startDate;
  }

  /**
   * Returns the start time of the range query.
   * Relevant only if {@link #isRangeQuery()} returns {@code true}.
   *
   * @return the start time as a string, or {@code null} if not applicable
   */
  public String startTime() {
    return this.startTime;
  }


  /**
   * Returns the end date of the range query.
   * Relevant only if {@link #isRangeQuery()} returns {@code true}.
   *
   * @return the end date as a string, or {@code null} if not applicable
   */
  @Override
  public String endDate() {
    return this.endDate;
  }

  /**
   * Returns the end time of the range query.
   * Relevant only if {@link #isRangeQuery()} returns {@code true}.
   *
   * @return the end time as a string, or {@code null} if not applicable
   */
  @Override
  public String endTime() {
    return this.endTime;
  }

}