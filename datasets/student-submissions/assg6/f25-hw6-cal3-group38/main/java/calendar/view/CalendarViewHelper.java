package calendar.view;

import calendar.model.Event;
import calendar.model.EventStatus;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Helper class containing testable business logic for CalendarGuiView.
 */
public class CalendarViewHelper {

  /**
   * Formats events for a specific date into a user-friendly string.
   */
  public static String formatEventsForDate(List<Event> events, LocalDate date) {
    StringBuilder sb = new StringBuilder();
    sb.append("📅 Events for ")
        .append(date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")))
        .append("\n");
    sb.append("─────────────────────────────────────\n\n");

    if (events.isEmpty()) {
      sb.append("No events scheduled for this day.\n\n");
      sb.append("💡 Click 'Create Event' to add an event!");
    } else {
      sb.append("Found ").append(events.size()).append(" event(s):\n\n");
      for (int i = 0; i < events.size(); i++) {
        Event event = events.get(i);
        sb.append(i + 1).append(". ").append(formatEventForDisplay(event)).append("\n\n");
      }
      sb.append("💡 Click on an event to select it for editing or deletion.");
    }

    return sb.toString();
  }

  /**
   * Formats a single event for display with emojis and structured layout.
   */
  public static String formatEventForDisplay(Event event) {
    StringBuilder sb = new StringBuilder();
    sb.append("📝 ").append(event.getSubject()).append("\n");

    if (event.isAllDayEvent()) {
      sb.append("   🕒 All Day\n");
    } else {
      sb.append("   🕒 ")
          .append(event.getStartDateTime().format(DateTimeFormatter.ofPattern("HH:mm")))
          .append(" - ")
          .append(event.getEndDateTime().format(DateTimeFormatter.ofPattern("HH:mm")))
          .append("\n");
    }

    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      sb.append("   📍 ").append(event.getLocation()).append("\n");
    }

    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      sb.append("   📋 ").append(event.getDescription()).append("\n");
    }

    sb.append("   🔒 ").append(event.getStatus()).append("\n");

    if (event.getSeriesId() != null) {
      sb.append("   🔄 Recurring Event\n");
    }

    return sb.toString();
  }

  /**
   * Formats detailed event information for the selected event view.
   */
  public static String formatEventDetails(Event event) {
    StringBuilder sb = new StringBuilder();
    sb.append("⭐ SELECTED EVENT\n");
    sb.append("─────────────────────────────────────\n\n");
    sb.append("📝 Subject: ").append(event.getSubject()).append("\n\n");

    if (event.isAllDayEvent()) {
      sb.append("🕒 Time: All Day\n");
    } else {
      sb.append("🕒 Start: ")
          .append(event.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
          .append("\n");
      sb.append("🕒 End: ")
          .append(event.getEndDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
          .append("\n");
    }

    sb.append("\n");

    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      sb.append("📍 Location: ").append(event.getLocation()).append("\n");
    }

    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      sb.append("📋 Description: ").append(event.getDescription()).append("\n");
    }

    sb.append("🔒 Status: ").append(event.getStatus()).append("\n");

    if (event.getSeriesId() != null) {
      sb.append("🔄 Type: Recurring Event Series\n");
    } else {
      sb.append("🔄 Type: Single Event\n");
    }

    sb.append("\n💡 Use the buttons below to edit or delete this event.");

    return sb.toString();
  }

  /**
   * Formats events as compact text for calendar day display.
   */
  public static String formatEventsText(List<Event> events) {
    StringBuilder eventsText = new StringBuilder();
    for (int i = 0; i < events.size(); i++) {
      Event event = events.get(i);
      if (i > 0) {
        eventsText.append("\n");
      }

      if (event.isAllDayEvent()) {
        eventsText.append("✓ ");
      } else {
        eventsText.append(event.getStartDateTime().format(DateTimeFormatter.ofPattern("HH:mm")))
            .append(" ");
      }
      eventsText.append(event.getSubject());
    }
    return eventsText.toString();
  }

  /**
   * Formats all events for a month into a comprehensive overview.
   */
  public static String formatMonthEvents(List<Event> allEvents, String monthName) {
    StringBuilder sb = new StringBuilder();
    sb.append("📊 ALL EVENTS FOR ")
        .append(monthName.toUpperCase())
        .append("\n");
    sb.append("=========================================\n\n");

    if (allEvents.isEmpty()) {
      sb.append("No events scheduled for this month.\n");
      sb.append("Click 'Create Event' to add your first event!");
    } else {
      for (Event event : allEvents) {
        sb.append("📅 ")
            .append(event.getStartDateTime().toLocalDate()
                .format(DateTimeFormatter.ofPattern("EEE, MMM d")))
            .append(":\n");
        sb.append("   • ").append(event.getSubject());

        if (!event.isAllDayEvent()) {
          sb.append(" (")
              .append(event.getStartDateTime().format(DateTimeFormatter.ofPattern("HH:mm")))
              .append(")");
        } else {
          sb.append(" (All Day)");
        }

        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
          sb.append(" 📍").append(event.getLocation());
        }
        sb.append("\n");
      }
    }

    return sb.toString();
  }

  /**
   * Parses a string of day characters into a set of DayOfWeek enums.
   */
  public static Set<DayOfWeek> parseRepeatDays(String daysStr) {
    Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);

    if (daysStr == null || daysStr.isEmpty()) {
      return EnumSet.of(DayOfWeek.MONDAY);
    }

    for (char c : daysStr.toUpperCase().toCharArray()) {
      switch (c) {
        case 'M':
          days.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          days.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          days.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          days.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          days.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          days.add(DayOfWeek.SATURDAY);
          break;
        case 'U':
          days.add(DayOfWeek.SUNDAY);
          break;
        default:
          break;
      }
    }

    return days.isEmpty() ? EnumSet.of(DayOfWeek.MONDAY) : days;
  }

  /**
   * Formats repeat days set into a compact string representation.
   */
  public static String formatRepeatDays(Set<DayOfWeek> repeatDays) {
    StringBuilder sb = new StringBuilder();
    if (repeatDays.contains(DayOfWeek.MONDAY)) {
      sb.append('M');
    }
    if (repeatDays.contains(DayOfWeek.TUESDAY)) {
      sb.append('T');
    }
    if (repeatDays.contains(DayOfWeek.WEDNESDAY)) {
      sb.append('W');
    }
    if (repeatDays.contains(DayOfWeek.THURSDAY)) {
      sb.append('R');
    }
    if (repeatDays.contains(DayOfWeek.FRIDAY)) {
      sb.append('F');
    }
    if (repeatDays.contains(DayOfWeek.SATURDAY)) {
      sb.append('S');
    }
    if (repeatDays.contains(DayOfWeek.SUNDAY)) {
      sb.append('U');
    }
    return sb.toString();
  }

  /**
   * Validates if a calendar name is acceptable.
   */
  public static boolean isValidCalendarName(String name) {
    return name != null && !name.trim().isEmpty();
  }

  /**
   * Validates if an event subject is acceptable.
   */
  public static boolean isValidEventSubject(String subject) {
    return subject != null && !subject.trim().isEmpty();
  }

  /**
   * Gets a default date, using selected date if available or current date otherwise.
   */
  public static LocalDate getDefaultDate(LocalDate selectedDate) {
    return selectedDate != null ? selectedDate : LocalDate.now();
  }
}