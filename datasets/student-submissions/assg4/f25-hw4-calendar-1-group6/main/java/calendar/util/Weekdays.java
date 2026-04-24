package calendar.util;

import java.time.DayOfWeek;
import java.util.EnumSet;

/**
 * Converts "MRU" style tokens to Day Of Week set.
 * M=Mon, T=Tue, W=Wed, R=Thu, F=Fri, S=Sat, U=Sun
 */
public final class Weekdays {
  private Weekdays() {
  }

  /**
   * Parses a weekday token into an EnumSet of days.
   *
   * @param token string containing weekday letters
   * @return set of corresponding DayOfWeek values
   * @throws IllegalArgumentException if the token contains an invalid letter
   */
  public static EnumSet<DayOfWeek> parse(String token) {
    EnumSet<DayOfWeek> set = EnumSet.noneOf(DayOfWeek.class);
    String s = token.trim().toUpperCase();
    for (char c : s.toCharArray()) {
      switch (c) {
        case 'M':
          set.add(DayOfWeek.MONDAY);
          break;
        case 'T':
          set.add(DayOfWeek.TUESDAY);
          break;
        case 'W':
          set.add(DayOfWeek.WEDNESDAY);
          break;
        case 'R':
          set.add(DayOfWeek.THURSDAY);
          break;
        case 'F':
          set.add(DayOfWeek.FRIDAY);
          break;
        case 'S':
          set.add(DayOfWeek.SATURDAY);
          break;
        case 'U':
          set.add(DayOfWeek.SUNDAY);
          break;
        default:
          throw new IllegalArgumentException("Invalid weekday letter: " + c);
      }
    }
    return set;
  }
}
