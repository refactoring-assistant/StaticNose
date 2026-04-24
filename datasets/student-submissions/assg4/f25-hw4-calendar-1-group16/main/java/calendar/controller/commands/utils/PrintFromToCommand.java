package calendar.controller.commands.utils;

import calendar.controller.commands.Icommands;
import calendar.model.CalendarInterface;
import calendar.view.Iview;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Command: print events from {@code start} to {@code end}.
 */
public class PrintFromToCommand implements Icommands {
  private final Iview view;
  private final Matcher matcher;

  /**
   * Constructor for printing events from a date to another.
   *
   * @param view interface for displaying the message.
   * @param matcher pattern for creating mather groups.
   */
  public PrintFromToCommand(Iview view, Matcher matcher) {
    this.view = view;
    this.matcher = matcher;
  }

  @Override
  public void go(CalendarInterface model) throws IllegalArgumentException, IOException {
    LocalDateTime start = LocalDateTime.parse(matcher.group(1));
    LocalDateTime end = LocalDateTime.parse(matcher.group(2));

    List<String[]> events = model.printFromTo(start, end);

    if (events.isEmpty()) {
      view.displayMessage("No events from " + start + " to " + end);
      return;
    }
    try {
      List<String> formattedOutput = new ArrayList<>();
      formattedOutput.add("Events from " + start + " to " + end + ":");

      for (String[] eventData : events) {
        String subject = eventData[0];
        String startDate = eventData[1];
        String startTime = eventData[2];
        String endDate = eventData[3];
        String endTime = eventData[4];
        String location = eventData.length > 5 && !eventData[5].isEmpty()
            ? eventData[5] : "No Location";

        formattedOutput.add(String.format("  %s | %s %s to %s %s | %s",
            subject, startDate, startTime, endDate, endTime, location));
      }

      view.displayOutput(formattedOutput);
    } catch (IllegalArgumentException e) {
      view.displayError(e.getMessage());
    }
  }
}

