package calendar.controller.commands.utils;

import calendar.controller.commands.Icommands;
import calendar.model.CalendarInterface;
import calendar.view.Iview;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Matcher;

/**
 * Method for exporting the file.
 */
public class ExportCommand implements Icommands {
  private final Iview view;
  private final Matcher matcher;

  /**
   * Constructor for exporting the events into {@code .csv}.
   *
   * @param view interface for displaying the message.
   * @param matcher pattern for creating mather groups.
   */
  public ExportCommand(Iview view, Matcher matcher) {
    this.view = view;
    this.matcher = matcher;
  }

  @Override
  public void go(CalendarInterface model) throws IllegalArgumentException, IOException {
    String filename = matcher.group(1);

    try {
      List<String[]> csvData = model.exportCalendar();

      PrintWriter writer = new PrintWriter(new FileWriter(filename));

      for (String[] row : csvData) {
        writer.println(String.join(",", row));
      }

      writer.close();

      view.displayMessage("Calendar exported to: " + filename);
    } catch (IOException e) {
      throw new IllegalArgumentException("Failed to export: " + e.getMessage());
    }
  }
}
