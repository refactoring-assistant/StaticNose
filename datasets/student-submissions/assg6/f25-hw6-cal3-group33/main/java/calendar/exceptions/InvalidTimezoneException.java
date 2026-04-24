package calendar.exceptions;

/**
 * Exception thrown when an invalid timezone identifier is provided.
 */
public class InvalidTimezoneException extends Exception {

  /**
   * Constructs an InvalidTimezoneException with a default message.
   *
   * @param timezone the invalid timezone string
   */
  public InvalidTimezoneException(String timezone) {
    super("Invalid timezone: '" + timezone + "'. Must be a valid IANA timezone "
        + "(e.g., 'America/New_York')");
  }

  /**
   * Constructs an InvalidTimezoneException with a custom message.
   *
   * @param timezone the invalid timezone string
   * @param message custom error message
   */
  public InvalidTimezoneException(String timezone, String message) {
    super(message);
  }
}