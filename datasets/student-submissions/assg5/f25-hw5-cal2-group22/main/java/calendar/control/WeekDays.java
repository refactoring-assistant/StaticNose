package calendar.control;

import java.time.DayOfWeek;

/**
 * ENUM to represent the week days.
 */
public enum WeekDays {
  M(DayOfWeek.MONDAY),
  T(DayOfWeek.TUESDAY),
  W(DayOfWeek.WEDNESDAY),
  R(DayOfWeek.THURSDAY),
  F(DayOfWeek.FRIDAY),
  S(DayOfWeek.SATURDAY),
  U(DayOfWeek.SUNDAY);

  private final DayOfWeek day;

  WeekDays(DayOfWeek day) {
    this.day = day;
  }

  public DayOfWeek getDay() {
    return day;
  }

}
