package calendar.controller.commands;

import calendar.controller.utils.CommandParserUtils;
import calendar.model.CalendarEvent;
import calendar.model.InterfaceCalendarModel;
import calendar.view.InterfaceCalendarView;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Command to handle printing events.
 */
public class PrintCommand implements CommandInterface {

  private final CommandParserUtils utils;
  private final ZoneId timeZone;

  /**
   * Sets up the print command with parser utilities and the calendar's time zone.
   *
   * @param utils the parser utilities to use
   */
  public PrintCommand(CommandParserUtils utils) {
    this.utils = utils;
    this.timeZone = ZoneId.of(InterfaceCalendarModel.TIME_ZONE_ID);
  }

  @Override
  public void execute(InterfaceCalendarModel model, InterfaceCalendarView view, List<String> args)
      throws Exception {
    if (args.size() < 3 || !args.get(0).equals("events")) {
      throw new Exception("Invalid 'print' command.");
    }

    String type = args.get(1).toLowerCase();
    List<CalendarEvent> events;

    if (type.equals("on")) {
      if (args.size() != 3) {
        throw new Exception("Usage: print events on YYYY-MM-DD");
      }
      LocalDate date = utils.parseDate(args.get(2));
      events = model.getEventsOnDay(date);
    } else if (type.equals("from")) {
      if (args.size() != 5 || !args.get(3).equals("to")) {
        throw new Exception("Usage: print events from YYYY-MM-DDTHH:MM to YYYY-MM-DDTHH:MM");
      }
      ZonedDateTime start = utils.parseDateTimeToZonedDateTime(args.get(2));
      ZonedDateTime end = utils.parseDateTimeToZonedDateTime(args.get(4));
      events = model.getEventsInRange(start, end);
    } else {
      throw new Exception("Invalid 'print' command. Must be 'on' or 'from'.");
    }

    if (events.isEmpty()) {
      view.displayMessage("No events found.");
    } else {
      List<String> eventDetails = new ArrayList<>();
      for (CalendarEvent event : events) {
        String eventString = String.format(
            "%s starting on %s at %s, ending on %s at %s",
            event.getSubject(),
            utils.formatDate(event.getStart()),
            utils.formatTime(event.getStart()),
            utils.formatDate(event.getEnd()),
            utils.formatTime(event.getEnd())
        );

        String locationStr = (event.getLocation() != null)
            ? "at " + event.getLocation()
            : "";
        if (!locationStr.isEmpty()) {
          eventString += " " + locationStr;
        }
        String descStr = (event.getDescription() != null && !event.getDescription().isEmpty())
            ? "Description: \"" + event.getDescription() + "\""
            : "";
        if (!descStr.isEmpty()) {
          eventString += " " + descStr;
        }
        eventDetails.add(eventString);
      }
      view.displayEvents(eventDetails);
    }
  }
}