package calendar.controller.commands.utils;

import calendar.controller.commands.Icommands;
import calendar.model.CalendarInterface;
import calendar.view.Iview;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Command: print events on {@code date}.
 */
public class PrintOnCommand implements Icommands {
  private final Iview view;
  private final Matcher matcher;

  /**
   * Constructor for printing the events on a specific date.
   *
   * @param view interface for displaying the message.
   * @param matcher pattern for creating mather groups.
   */
  public PrintOnCommand(Iview view, Matcher matcher) {
    this.view = view;
    this.matcher = matcher;
  }

  @Override
  public void go(CalendarInterface model) throws IllegalArgumentException, IOException {
    LocalDate date = LocalDate.parse(matcher.group(1));

    try {
      List<String[]> events = model.printOn(date);

      if (events.isEmpty()) {
        view.displayMessage("No events on " + date);
        return;
      }
      List<String> formattedOutput = new ArrayList<>();
      formattedOutput.add("Events on " + date + ":");

      for (String[] eventData : events) {
        String subject = eventData[0];
        String startDateTime = eventData[1];
        String endDateTime = eventData[2];
        String location = eventData.length > 3 && !eventData[3].isEmpty()
            ? eventData[3] : "No location";

        formattedOutput.add(String.format("  %s | %s to %s | %s",
            subject, startDateTime, endDateTime, location));
      }

      view.displayOutput(formattedOutput);

    } catch (IllegalArgumentException e) {
      view.displayError(e.getMessage());
    }
  }
}

