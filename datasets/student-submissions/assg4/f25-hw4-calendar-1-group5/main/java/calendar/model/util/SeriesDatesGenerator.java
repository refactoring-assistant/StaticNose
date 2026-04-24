package calendar.model.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * This class helps to generate all the dates of an event series.
 */
public class SeriesDatesGenerator {
  private static DayOfWeek getClosestFutureDayOfWeek(DayOfWeek weekday,
                                                     List<DayOfWeek> sortedWeekDays) {
    DayOfWeek result = null;
    for (DayOfWeek sortedWeekDay : sortedWeekDays) {
      if (sortedWeekDay.getValue() >= weekday.getValue()) {
        result = sortedWeekDay;
        break;
      }
    }

    if (result == null) {
      result = sortedWeekDays.get(0);
    }

    return result;
  }

  private static int getDaysToAdvance(DayOfWeek from, DayOfWeek to) {
    if (to.getValue() <= from.getValue()) {
      return 7 - (from.getValue() - to.getValue());
    } else {
      return to.getValue() - from.getValue();
    }
  }

  /**
   * A method to generate all dates for an event that repeats on certain weekdays for a certain
   * number of times.
   *
   * <p>If the event spans more than a day, then throws an error.
   *
   * @param from       The start timestamp of the event.
   * @param to         The end timestamp of the event.
   * @param weekdays   A non-empty set of weekdays the event could repeat on.
   * @param repetition A positive integer count of the number of times the event must repeat.
   * @return A List of tuples of all the timestamps the event could repeat on.
   */
  public static List<List<LocalDateTime>> generate(LocalDateTime from, LocalDateTime to,
                                                   HashSet<DayOfWeek> weekdays, int repetition) {
    Objects.requireNonNull(from);
    Objects.requireNonNull(to);
    Objects.requireNonNull(weekdays);

    if (weekdays.isEmpty() || repetition <= 0) {
      throw new IllegalArgumentException("Invalid weekdays or repetition");
    }

    List<DayOfWeek> sortedWeekDays = new ArrayList<>(weekdays);
    sortedWeekDays.sort(Comparator.comparingInt(DayOfWeek::getValue));

    DayOfWeek closestFutureDayOfWeek =
        getClosestFutureDayOfWeek(from.getDayOfWeek(), sortedWeekDays);
    int daysToAdvance = from.getDayOfWeek().equals(closestFutureDayOfWeek) ? 0 :
        getDaysToAdvance(from.getDayOfWeek(), closestFutureDayOfWeek);

    List<List<LocalDateTime>> result = new ArrayList<>();

    int marker;
    int count;
    LocalDateTime startsAt;
    LocalDateTime endsAt;

    for (marker = sortedWeekDays.indexOf(closestFutureDayOfWeek), count = 0, startsAt =
        from.plusDays(daysToAdvance), endsAt = to.plusDays(daysToAdvance); count < repetition;
         count++, daysToAdvance = getDaysToAdvance(sortedWeekDays.get(marker),
             sortedWeekDays.get((marker + 1) % sortedWeekDays.size())), marker =
             (marker + 1) % sortedWeekDays.size(), startsAt = startsAt.plusDays(daysToAdvance),
             endsAt = endsAt.plusDays(daysToAdvance)) {
      result.add(List.of(startsAt, endsAt));
    }

    return result;
  }

  /**
   * A method to generate all dates for an event that repeats on certain weekdays until a certain
   * timestamp.
   *
   * <p>If the event spans more than a day, then throws an error.
   *
   * <p>If the repeat until timestamp lies before the event ends, then throws an error.
   *
   * @param from        The start timestamp of the event.
   * @param to          The end timestamp of the event.
   * @param weekdays    A non-empty set of weekdays the event could repeat on.
   * @param repeatUntil A LocalDateTime timestamp until which the event could repeat.
   * @return A List of tuples of all the timestamps the event could repeat on.
   */
  public static List<List<LocalDateTime>> generate(LocalDateTime from, LocalDateTime to,
                                                   HashSet<DayOfWeek> weekdays,
                                                   LocalDate repeatUntil) {
    Objects.requireNonNull(from);
    Objects.requireNonNull(to);
    Objects.requireNonNull(weekdays);
    Objects.requireNonNull(repeatUntil);

    if (weekdays.isEmpty() || repeatUntil.isBefore(to.toLocalDate())) {
      throw new IllegalArgumentException("Invalid weekdays or repeat until");
    }

    List<DayOfWeek> sortedWeekDays = new ArrayList<>(weekdays);
    sortedWeekDays.sort(Comparator.comparingInt(DayOfWeek::getValue));

    DayOfWeek closestFutureDayOfWeek =
        getClosestFutureDayOfWeek(from.getDayOfWeek(), sortedWeekDays);
    int daysToAdvance = from.getDayOfWeek().equals(closestFutureDayOfWeek) ? 0 :
        getDaysToAdvance(from.getDayOfWeek(), closestFutureDayOfWeek);

    List<List<LocalDateTime>> result = new ArrayList<>();

    int marker;
    LocalDateTime startsAt;
    LocalDateTime endsAt;

    for (marker = sortedWeekDays.indexOf(closestFutureDayOfWeek), startsAt =
        from.plusDays(daysToAdvance), endsAt = to.plusDays(daysToAdvance);
         repeatUntil.isEqual(endsAt.toLocalDate()) || repeatUntil.isAfter(endsAt.toLocalDate());
         daysToAdvance = getDaysToAdvance(sortedWeekDays.get(marker),
             sortedWeekDays.get((marker + 1) % sortedWeekDays.size())), marker =
             (marker + 1) % sortedWeekDays.size(), startsAt = startsAt.plusDays(daysToAdvance),
             endsAt = endsAt.plusDays(daysToAdvance)) {
      result.add(List.of(startsAt, endsAt));
    }

    return result;
  }
}
