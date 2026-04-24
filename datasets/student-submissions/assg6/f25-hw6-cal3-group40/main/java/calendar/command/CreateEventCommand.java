package calendar.command;

import calendar.service.CalendarService;
import calendar.view.textbased.CalendarView;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * Command to create a calendar event.
 */
public class CreateEventCommand implements CalendarCommand {

  private final String subject;
  private final Map<String, String> params;

  /**
   * Constructs a CreateEventCommand.
   *
   * @param subject The subject of the event
   * @param params Rest of the parameters
   */
  public CreateEventCommand(String subject, Map<String, String> params) {
    this.subject = subject;
    this.params = params;
  }

  @Override
  public void execute(CalendarService service, CalendarView view) {
    try {
      String from = params.get("from");
      String to = params.get("to");
      String on = params.get("on");

      if (from != null && on != null) {
        throw new IllegalArgumentException("Cannot specify both 'on' and 'from'/'to'.");
      }
      if (from != null && to == null) {
        throw new IllegalArgumentException("Cannot specify 'from' without 'to'.");
      }
      if (from == null && on == null) {
        throw new IllegalArgumentException("Event must specify either 'from'"
            + " <datetime> 'to' <datetime> OR 'on' <date>.");
      }

      service.createEvent(
          subject,
          from,
          to,
          on,
          params.get("description"),
          params.get("at"),
          params.getOrDefault("status", "public").equalsIgnoreCase("private"),
          params.get("repeats"),
          parseInteger(params.get("for")),
          params.get("until")
      );

      view.showMessage("Event created successfully.");

    } catch (DateTimeParseException e) {
      throw new IllegalArgumentException(
          "Invalid date/time format. Use YYYY-MM-DD or YYYY-MM-DDTHH:MM:SS.", e);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid number for 'for' repetitions.", e);
    }
  }

  private Integer parseInteger(String value) {
    return (value != null) ? Integer.parseInt(value) : null;
  }
}
