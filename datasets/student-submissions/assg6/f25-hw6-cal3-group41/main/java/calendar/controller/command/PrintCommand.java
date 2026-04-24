package calendar.controller.command;

import calendar.model.CalendarModel;
import calendar.model.Event;
import calendar.model.MultiCalendarModel;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command to handle printing/querying events.
 */
class PrintCommand implements Command {

  private final MultiCalendarModel multiModel;
  private static final DateTimeFormatter ISO_LOCAL_MIN =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  /**
   * Creates a PrintCommand with the given multi-calendar model.
   *
   * @param multiModel the multi-calendar model to use
   */
  public PrintCommand(MultiCalendarModel multiModel) {
    this.multiModel = multiModel;
  }

  @Override
  public String execute(String command) {
    Pattern pon = Pattern.compile(
        "^print\\s+events\\s+on\\s+([0-9]{4}-[0-9]{2}-[0-9]{2})$",
        Pattern.CASE_INSENSITIVE);
    Pattern prange = Pattern.compile(
        "^print\\s+events\\s+from\\s+"
            + "([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2})\\s+"
            + "to\\s+([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2})$",
        Pattern.CASE_INSENSITIVE);

    CalendarModel model = multiModel.getCurrentModel();
    
    Matcher mon = pon.matcher(command);
    Matcher mrange = prange.matcher(command);
    List<Event> toPrint;

    if (mon.matches()) {
      LocalDate d = LocalDate.parse(mon.group(1));
      toPrint = model.getEventsOn(d);
    } else if (mrange.matches()) {
      LocalDateTime start = LocalDateTime.parse(mrange.group(1), ISO_LOCAL_MIN);
      LocalDateTime end = LocalDateTime.parse(mrange.group(2), ISO_LOCAL_MIN);
      if (start.isAfter(end)) {
        throw new IllegalArgumentException("Start date must be before or equal to end date.");
      }
      toPrint = model.getEventsBetween(start, end);
    } else {
      throw new IllegalArgumentException("Invalid print command.");
    }

    if (toPrint.isEmpty()) {
      return "No events found.";
    }
    StringBuilder sb = new StringBuilder();
    for (Event e : toPrint) {
      sb.append(String.format("%s starting on %s at %s, ending on %s at %s%s%n",
          e.subject(),
          e.startDate().toLocalDate(), e.startDate().toLocalTime(),
          e.endDate().toLocalDate(), e.endDate().toLocalTime(),
          (e.location() == null || e.location().isEmpty() ? "" : " @" + e.location())));
    }
    return sb.toString().trim();
  }
}

