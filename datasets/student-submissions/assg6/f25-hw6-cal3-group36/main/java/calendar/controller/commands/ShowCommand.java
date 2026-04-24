package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.CalendarSystemModel;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Checks the status at the specified time.
 */
public class ShowCommand implements Command {

  private final String input;

  /**
   * Creating a command using the user input.
   * Input stored and parsed during execution.
   *
   * @param input command entered by the user.
   */
  public ShowCommand(String input) {
    this.input = input;
  }

  @Override
  public String execute(CalendarSystemModel model) {
    String timeStr = input.replaceAll("(?i)show status on", "").trim();
    LocalDateTime local = LocalDateTime.parse(timeStr);
    ZoneId zone = model.getActiveCalendar().getTimeZone();
    Instant instant = local.atZone(zone).toInstant();
    boolean busy = model.getActiveCalendar().isBusyAt(instant);
    return busy ? "busy" : "available";
  }
}