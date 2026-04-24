package calendar.view;

/**
 * Utility class for managing timezone selections in the calendar application.
 * Provides a curated list of popular timezones for user-friendly selection.
 */
public class TimezoneUtils {

  /**
   * Gets a curated list of popular timezones covering major world cities.
   * Includes approximately 70 timezones across all continents.
   *
   * @return array of timezone IDs sorted alphabetically
   */
  public static String[] getPopularTimezones() {
    return new String[]{
        // North America - US
        "America/New_York",
        "America/Chicago",
        "America/Denver",
        "America/Phoenix",
        "America/Los_Angeles",
        "America/Anchorage",
        "Pacific/Honolulu",

        // North America - Canada
        "America/Toronto",
        "America/Winnipeg",
        "America/Edmonton",
        "America/Vancouver",
        "America/Halifax",

        // Latin America
        "America/Mexico_City",
        "America/Bogota",
        "America/Lima",
        "America/Santiago",
        "America/Sao_Paulo",
        "America/Buenos_Aires",

        // Europe - Western
        "Europe/London",
        "Europe/Dublin",
        "Europe/Lisbon",

        // Europe - Central
        "Europe/Paris",
        "Europe/Berlin",
        "Europe/Rome",
        "Europe/Madrid",
        "Europe/Amsterdam",
        "Europe/Brussels",
        "Europe/Vienna",
        "Europe/Zurich",
        "Europe/Stockholm",
        "Europe/Oslo",
        "Europe/Copenhagen",

        // Europe - Eastern
        "Europe/Athens",
        "Europe/Helsinki",
        "Europe/Warsaw",
        "Europe/Prague",
        "Europe/Budapest",
        "Europe/Bucharest",
        "Europe/Istanbul",
        "Europe/Moscow",

        // Middle East
        "Asia/Dubai",
        "Asia/Riyadh",
        "Asia/Jerusalem",
        "Asia/Tehran",

        // Asia - South
        "Asia/Kolkata",
        "Asia/Karachi",
        "Asia/Dhaka",

        // Asia - Southeast
        "Asia/Bangkok",
        "Asia/Jakarta",
        "Asia/Singapore",
        "Asia/Manila",
        "Asia/Kuala_Lumpur",
        "Asia/Ho_Chi_Minh",

        // Asia - East
        "Asia/Shanghai",
        "Asia/Hong_Kong",
        "Asia/Taipei",
        "Asia/Tokyo",
        "Asia/Seoul",

        // Pacific
        "Australia/Sydney",
        "Australia/Melbourne",
        "Australia/Brisbane",
        "Australia/Perth",
        "Pacific/Auckland",
        "Pacific/Fiji",

        // Africa
        "Africa/Cairo",
        "Africa/Johannesburg",
        "Africa/Lagos",
        "Africa/Nairobi",

        // UTC
        "UTC"
    };
  }
}