package calendar.controller.command;

import calendar.model.CalendarModel;
import calendar.model.MultiCalendarModel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command to handle checking busy status at a specific time.
 */
class StatusCommand implements Command {

  private final MultiCalendarModel multiModel;
  private static final DateTimeFormatter ISO_LOCAL_MIN =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

  /**
   * Creates a StatusCommand with the given multi-calendar model.
   *
   * @param multiModel the multi-calendar model to use
   */
  public StatusCommand(MultiCalendarModel multiModel) {
    this.multiModel = multiModel;
  }

  @Override
  public String execute(String command) {
    Pattern p = Pattern.compile(
        "^show\\s+status\\s+on\\s+([0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2})$",
        Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(command);
    if (!m.matches()) {
      throw new IllegalArgumentException("Invalid status command.");
    }
    CalendarModel model = multiModel.getCurrentModel();
    LocalDateTime t = LocalDateTime.parse(m.group(1), ISO_LOCAL_MIN);
    return model.isBusyAt(t) ? "busy" : "available";
  }
}

