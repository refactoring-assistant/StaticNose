package calendar.view.dialog.debug;

import calendar.view.dialog.CalendarDialogResult;

/**
 * Factory class for creating mock CalendarDialogResult objects for testing.
 * Provides builders for all types of calendar scenarios.
 */
public class CalendarDialogMocks {

  /**
   * Creates a calendar with default UTC timezone.
   */
  public static CalendarDialogResult calendarWithUtc() {
    return new CalendarDialogResult("My Calendar", "UTC");
  }

  /**
   * Creates a calendar with US Eastern timezone.
   */
  public static CalendarDialogResult calendarWithEasternTime() {
    return new CalendarDialogResult("Eastern Calendar", "America/New_York");
  }

  /**
   * Creates a calendar with US Pacific timezone.
   */
  public static CalendarDialogResult calendarWithPacificTime() {
    return new CalendarDialogResult("Pacific Calendar", "America/Los_Angeles");
  }

  /**
   * Creates a calendar with US Central timezone.
   */
  public static CalendarDialogResult calendarWithCentralTime() {
    return new CalendarDialogResult("Central Calendar", "America/Chicago");
  }

  /**
   * Creates a calendar with US Mountain timezone.
   */
  public static CalendarDialogResult calendarWithMountainTime() {
    return new CalendarDialogResult("Mountain Calendar", "America/Denver");
  }

  /**
   * Creates a calendar with European timezone (London).
   */
  public static CalendarDialogResult calendarWithLondonTime() {
    return new CalendarDialogResult("London Calendar", "Europe/London");
  }

  /**
   * Creates a calendar with European timezone (Paris).
   */
  public static CalendarDialogResult calendarWithParisTime() {
    return new CalendarDialogResult("Paris Calendar", "Europe/Paris");
  }

  /**
   * Creates a calendar with Asian timezone (Tokyo).
   */
  public static CalendarDialogResult calendarWithTokyoTime() {
    return new CalendarDialogResult("Tokyo Calendar", "Asia/Tokyo");
  }

  /**
   * Creates a calendar with Asian timezone (Shanghai).
   */
  public static CalendarDialogResult calendarWithShanghaiTime() {
    return new CalendarDialogResult("Shanghai Calendar", "Asia/Shanghai");
  }

  /**
   * Creates a calendar with Australian timezone (Sydney).
   */
  public static CalendarDialogResult calendarWithSydneyTime() {
    return new CalendarDialogResult("Sydney Calendar", "Australia/Sydney");
  }

  /**
   * Creates a calendar with a simple name.
   */
  public static CalendarDialogResult calendarWithSimpleName() {
    return new CalendarDialogResult("Work", "UTC");
  }

  /**
   * Creates a calendar with a complex name.
   */
  public static CalendarDialogResult calendarWithComplexName() {
    return new CalendarDialogResult("Q4 2025 - Project Alpha & Beta", "America/New_York");
  }

  /**
   * Creates a calendar with special characters in name.
   */
  public static CalendarDialogResult calendarWithSpecialCharacters() {
    return new CalendarDialogResult("Calendar: \"Important\" (2025)", "UTC");
  }

  /**
   * Creates a calendar with a long name.
   */
  public static CalendarDialogResult calendarWithLongName() {
    return new CalendarDialogResult(
        "Very Long Calendar Name For Testing Purposes With Multiple Words",
        "America/Los_Angeles"
    );
  }

  /**
   * Creates a calendar with numeric name.
   */
  public static CalendarDialogResult calendarWithNumericName() {
    return new CalendarDialogResult("2025-Q4-Calendar", "UTC");
  }

  /**
   * Creates a calendar with single character name.
   */
  public static CalendarDialogResult calendarWithSingleCharName() {
    return new CalendarDialogResult("A", "UTC");
  }

  /**
   * Creates a personal calendar.
   */
  public static CalendarDialogResult personalCalendar() {
    return new CalendarDialogResult("Personal", "America/New_York");
  }

  /**
   * Creates a work calendar.
   */
  public static CalendarDialogResult workCalendar() {
    return new CalendarDialogResult("Work", "America/Chicago");
  }

  /**
   * Creates a project calendar.
   */
  public static CalendarDialogResult projectCalendar() {
    return new CalendarDialogResult("Project Alpha", "UTC");
  }

  /**
   * Creates a team calendar.
   */
  public static CalendarDialogResult teamCalendar() {
    return new CalendarDialogResult("Team Meetings", "America/New_York");
  }

  /**
   * Creates a holiday calendar.
   */
  public static CalendarDialogResult holidayCalendar() {
    return new CalendarDialogResult("Holidays 2025", "UTC");
  }
}

