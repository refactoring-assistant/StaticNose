package calendar.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.zone.ZoneRulesException;
import java.util.Set;

/**
 * Utility class for timezone operations and conversions.
 * Provides methods to convert times between timezones and validate timezone strings.
 */
public class TimezoneConverter {

  // Private constructor to prevent instantiation
  private TimezoneConverter() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Converts a LocalDateTime from one timezone to another.
   *
   * <p>Example: 2pm EST → 11am PST
   *
   * @param dateTime   The date/time in the source timezone
   * @param sourceZone Source timezone
   * @param targetZone Target timezone
   * @return LocalDateTime in the target timezone
   * @throws IllegalArgumentException if any parameter is null
   */
  public static LocalDateTime convertBetweenTimezones(LocalDateTime dateTime,
                                                      ZoneId sourceZone,
                                                      ZoneId targetZone) {
    if (dateTime == null) {
      throw new IllegalArgumentException("DateTime cannot be null");
    }
    if (sourceZone == null) {
      throw new IllegalArgumentException("Source timezone cannot be null");
    }
    if (targetZone == null) {
      throw new IllegalArgumentException("Target timezone cannot be null");
    }

    // Convert to ZonedDateTime in source timezone
    ZonedDateTime sourceZoned = dateTime.atZone(sourceZone);

    // Convert to target timezone
    ZonedDateTime targetZoned = sourceZoned.withZoneSameInstant(targetZone);

    // Extract LocalDateTime (without zone info)
    return targetZoned.toLocalDateTime();
  }

  /**
   * Validates if a timezone string is a valid IANA timezone.
   *
   * @param timezoneStr Timezone string (e.g., "America/New_York")
   * @return true if valid IANA timezone
   */
  public static boolean isValidTimezone(String timezoneStr) {
    if (timezoneStr == null || timezoneStr.trim().isEmpty()) {
      return false;
    }

    try {
      ZoneId.of(timezoneStr);
      return true;
    } catch (ZoneRulesException e) {
      return false;
    }
  }

  /**
   * Parses a timezone string to ZoneId.
   *
   * @param timezoneStr Timezone string in IANA format
   * @return ZoneId object
   * @throws IllegalArgumentException if timezone string is invalid
   */
  public static ZoneId parseTimezone(String timezoneStr) {
    if (timezoneStr == null || timezoneStr.trim().isEmpty()) {
      throw new IllegalArgumentException("Timezone string cannot be null or empty");
    }

    try {
      return ZoneId.of(timezoneStr);
    } catch (ZoneRulesException e) {
      throw new IllegalArgumentException("Invalid timezone: " + timezoneStr
          + ". Expected IANA format (e.g., 'America/New_York')");
    }
  }

  /**
   * Gets all available timezone IDs.
   *
   * @return Set of all available IANA timezone IDs
   */
  public static Set<String> getAvailableTimezones() {
    return ZoneId.getAvailableZoneIds();
  }

  /**
   * Calculates the time difference in hours between two timezones at a specific date.
   * Note: This accounts for DST changes.
   *
   * @param date       Date to check (DST may vary by date)
   * @param sourceZone Source timezone
   * @param targetZone Target timezone
   * @return Offset in hours (positive if target is ahead, negative if behind)
   */
  public static double getTimezoneOffsetHours(LocalDate date,
                                              ZoneId sourceZone,
                                              ZoneId targetZone) {
    if (date == null || sourceZone == null || targetZone == null) {
      throw new IllegalArgumentException("Parameters cannot be null");
    }

    // Use noon to avoid edge cases around midnight
    LocalDateTime noon = date.atTime(12, 0);

    ZonedDateTime sourceZoned = noon.atZone(sourceZone);
    ZonedDateTime targetZoned = sourceZoned.withZoneSameInstant(targetZone);

    long sourceOffsetSeconds = sourceZoned.getOffset().getTotalSeconds();
    long targetOffsetSeconds = targetZoned.getOffset().getTotalSeconds();

    return (targetOffsetSeconds - sourceOffsetSeconds) / 3600.0;
  }

  /**
   * Checks if two timezones are equivalent at a given date.
   * (e.g., EST and America/New_York)
   *
   * @param date  Date to check
   * @param zone1 First timezone
   * @param zone2 Second timezone
   * @return true if timezones have same offset at given date
   */
  public static boolean areTimezonesEquivalent(LocalDate date, ZoneId zone1, ZoneId zone2) {
    if (date == null || zone1 == null || zone2 == null) {
      return false;
    }

    LocalDateTime noon = date.atTime(12, 0);
    ZonedDateTime zoned1 = noon.atZone(zone1);
    ZonedDateTime zoned2 = noon.atZone(zone2);

    return zoned1.getOffset().equals(zoned2.getOffset());
  }

  /**
   * Formats a ZoneId for display purposes.
   *
   * @param zoneId ZoneId to format
   * @return User-friendly string (e.g., "America/New_York (EST)")
   */
  public static String formatTimezoneForDisplay(ZoneId zoneId) {
    if (zoneId == null) {
      return "Unknown";
    }

    ZonedDateTime now = ZonedDateTime.now(zoneId);
    String shortName = now.getZone().getDisplayName(
        java.time.format.TextStyle.SHORT,
        java.util.Locale.ENGLISH);

    return zoneId.getId() + " (" + shortName + ")";
  }
}