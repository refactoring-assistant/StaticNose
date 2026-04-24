package calendar.util;

import calendar.model.Event;
import calendar.model.EventBuilder;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Utility class for working with {@link Event} objects.
 */
public final class EventUtil {

  /**
   * Private constructor to prevent instantiation.
   */
  private EventUtil() {
  }

  /**
   * Builds a copy of an existing event with a new start time and optionally a new series ID.
   *
   * @param template   the original event to copy
   * @param newStart   the new start time for the copied event
   * @param newSeriesId the new series ID, or {@code null} if not part of a series
   * @return a new {@link Event} instance with updated start, end, and series ID
   */
  public static Event buildCopiedEvent(
          Event template, ZonedDateTime newStart, UUID newSeriesId) {

    EventBuilder builder = template.toBuilder()
            .start(newStart)
            .end(newStart.plus(Duration.between(template.getStart(), template.getEnd())));
    builder.seriesId(newSeriesId);
    return builder.build();
  }
}