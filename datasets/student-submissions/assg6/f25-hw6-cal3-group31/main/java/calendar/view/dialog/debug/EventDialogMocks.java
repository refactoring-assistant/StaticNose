package calendar.view.dialog.debug;

import calendar.view.dialog.EventDialogResult;
import java.util.HashSet;
import java.util.Set;

/**
 * Factory class for creating mock EventDialogResult objects for testing.
 * Provides builders for all types of event scenarios.
 */
public class EventDialogMocks {

  /**
   * Creates a simple non-recurring event with minimal fields.
   */
  public static EventDialogResult simpleEvent() {
    return new EventDialogResult(
        "Simple Event",
        2025, 11, 21,
        10, 0,
        2025, 11, 21,
        11, 0,
        null, null, null
    );
  }

  /**
   * Creates an event with all optional fields populated.
   */
  public static EventDialogResult eventWithAllFields() {
    return new EventDialogResult(
        "Complete Event",
        2025, 11, 21,
        10, 0,
        2025, 11, 21,
        12, 0,
        "This is a detailed description",
        "PHYSICAL",
        "PRIVATE"
    );
  }

  /**
   * Creates an event with online location.
   */
  public static EventDialogResult eventWithOnlineLocation() {
    return new EventDialogResult(
        "Online Meeting",
        2025, 11, 21,
        14, 30,
        2025, 11, 21,
        15, 30,
        "Virtual meeting via Zoom",
        "ONLINE",
        null
    );
  }

  /**
   * Creates an event with public status.
   */
  public static EventDialogResult eventWithPublicStatus() {
    return new EventDialogResult(
        "Public Event",
        2025, 11, 21,
        9, 0,
        2025, 11, 21,
        10, 0,
        null,
        null,
        "PUBLIC"
    );
  }

  /**
   * Creates a multi-day event.
   */
  public static EventDialogResult multiDayEvent() {
    return new EventDialogResult(
        "Conference",
        2025, 11, 21,
        9, 0,
        2025, 11, 23,
        17, 0,
        "Annual tech conference",
        "PHYSICAL",
        "PUBLIC"
    );
  }

  /**
   * Creates a recurring event with repeat until date.
   */
  public static EventDialogResult recurringEventWithUntilDate() {
    Set<String> repeatDays = new HashSet<>();
    repeatDays.add("MONDAY");
    repeatDays.add("WEDNESDAY");
    repeatDays.add("FRIDAY");

    return new EventDialogResult(
        "Weekly Meeting",
        2025, 11, 21,
        10, 0,
        2025, 11, 21,
        11, 0,
        "Team sync meeting",
        null,
        null,
        true,
        repeatDays,
        Integer.valueOf(2025), Integer.valueOf(12), Integer.valueOf(31),
        null
    );
  }

  /**
   * Creates a recurring event with occurrence count.
   */
  public static EventDialogResult recurringEventWithOccurrences() {
    Set<String> repeatDays = new HashSet<>();
    repeatDays.add("TUESDAY");
    repeatDays.add("THURSDAY");

    return new EventDialogResult(
        "Bi-weekly Standup",
        2025, 11, 21,
        15, 0,
        2025, 11, 21,
        15, 30,
        null,
        "ONLINE",
        "PRIVATE",
        true,
        repeatDays,
        null, null, null,
        10
    );
  }

  /**
   * Creates a recurring event on all weekdays.
   */
  public static EventDialogResult recurringWeekdayEvent() {
    Set<String> repeatDays = new HashSet<>();
    repeatDays.add("MONDAY");
    repeatDays.add("TUESDAY");
    repeatDays.add("WEDNESDAY");
    repeatDays.add("THURSDAY");
    repeatDays.add("FRIDAY");

    return new EventDialogResult(
        "Daily Standup",
        2025, 11, 21,
        9, 30,
        2025, 11, 21,
        10, 0,
        null,
        null,
        null,
        true,
        repeatDays,
        Integer.valueOf(2025), Integer.valueOf(12), Integer.valueOf(31),
        null
    );
  }

  /**
   * Creates a recurring event on weekends.
   */
  public static EventDialogResult recurringWeekendEvent() {
    Set<String> repeatDays = new HashSet<>();
    repeatDays.add("SATURDAY");
    repeatDays.add("SUNDAY");

    return new EventDialogResult(
        "Weekend Activity",
        2025, 11, 22,
        10, 0,
        2025, 11, 22,
        12, 0,
        "Fun weekend activity",
        "PHYSICAL",
        null,
        true,
        repeatDays,
        Integer.valueOf(2026), Integer.valueOf(3), Integer.valueOf(31),
        null
    );
  }

  /**
   * Creates an all-day event (represented as 00:00 to 23:59).
   */
  public static EventDialogResult allDayEvent() {
    return new EventDialogResult(
        "All Day Event",
        2025, 11, 21,
        0, 0,
        2025, 11, 21,
        23, 59,
        "Full day event",
        null,
        "PUBLIC"
    );
  }

  /**
   * Creates an event with very long description.
   */
  public static EventDialogResult eventWithLongDescription() {
    String longDesc = "This is a very long description that contains multiple lines "
        + "and detailed information about the event. It includes various details "
        + "such as agenda items, participants, and important notes.";

    return new EventDialogResult(
        "Detailed Event",
        2025, 11, 21,
        13, 0,
        2025, 11, 21,
        14, 30,
        longDesc,
        "PHYSICAL",
        "PRIVATE"
    );
  }

  /**
   * Creates an event with special characters in subject.
   */
  public static EventDialogResult eventWithSpecialCharacters() {
    return new EventDialogResult(
        "Event: \"Important\" & Urgent! (Q4 2025)",
        2025, 11, 21,
        11, 0,
        2025, 11, 21,
        12, 0,
        null,
        null,
        null
    );
  }
}

