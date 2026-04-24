package calendar.model;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * This interface represents all the methods required to represent a calendar event.
 */
public interface Event {
  /**
   * A method returning the id of the series the event belongs to.
   *
   * @return A UUID of the belonging series.
   */
  UUID seriesId();

  /**
   * A method returning the subject of the event.
   *
   * @return String subject.
   */
  String subject();

  /**
   * A method returning the description of the event.
   *
   * @return String description.
   */
  String description();

  /**
   * A method returning the start timestamp of the event.
   *
   * @return A LocalDateTime start timestamp.
   */
  LocalDateTime startsAt();

  /**
   * A method returning the end timestamp of the event.
   *
   * @return A LocalDateTime end timestamp.
   */
  LocalDateTime endsAt();

  /**
   * A method returning the location of the event.
   *
   * @return A Location.
   */
  Location location();

  /**
   * A method returning the status of the event.
   *
   * @return A Status.
   */
  Status status();


  /**
   * A method help to quickly compare if a set of subject, start and end timestamps match
   * the event.
   *
   * @param subject The String subject to be matched.
   * @param startsAt The LocalDateTime timestamp to be matched against the event start timestamp.
   * @param endsAt The LocalDateTime timestamp to be matched against the event end timestamp.
   * @return True if matches perfectly, false otherwise.
   */
  boolean matches(String subject, LocalDateTime startsAt, LocalDateTime endsAt);
}
