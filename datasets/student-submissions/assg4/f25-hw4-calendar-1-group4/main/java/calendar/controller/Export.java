package calendar.controller;

import calendar.model.Icalendar;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Handles the export command for saving calendar data to a CSV file.
 * The generated file can be imported into external calendar applications
 * such as Google Calendar for verification or sharing.
 */
public class Export implements Command {

  private final String[] args;
  private final Icalendar model;

  /**
   * Constructs a new Export command with the specified arguments and model.
   *
   * @param args the full array of user command tokens
   * @param model the calendar model instance
   */
  public Export(String[] args, Icalendar model) {
    this.args = args;
    this.model = model;
  }

  /**
   * Executes the export command.
   * Validates the command syntax and delegates the export operation to the model.
   * Upon success, prints the absolute path of the generated CSV file.
   * Expected format: export cal fileName.csv
   * Example: export cal myCalendar.csv
   */
  @Override
  public String execute() {
    if (args.length != 3 || !args[0].equalsIgnoreCase("export")
        || !args[1].equalsIgnoreCase("cal")) {
      return "Error: Invalid export command. Usage: export cal fileName.csv\n";
    }

    String filename = args[2];
    String[] headers = {
        "Subject", "Start Date", "Start Time", "End Date", "End Time",
        "All Day Event", "Location", "Description", "Private"
    };

    Path outputPath = Paths.get(filename).toAbsolutePath();

    try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
      writer.write(String.join(",", headers));
      writer.newLine();

      for (String[] row : model.exportCalendar(filename)) {
        writer.write(String.join(",", row));
        writer.newLine();
      }
      return "Calendar exported successfully to: " + outputPath.toString() + "\n";
    } catch (IOException e) {
      return "Error exporting calendar: " + e.getMessage() + "\n";
    }
  }

}
