package calendar.controller;

import calendar.CsvExporter;
import calendar.Exporter;
import calendar.IcalExporter;
import calendar.model.CalendarModel;
import calendar.view.CalendarView;
import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents an Export command that is a part of controller command design patter. Executes
 * the CSV and ICS export method when asked by user in prompt.
 */
public class ExportCommand implements Command {
  private final CalendarView view;
  private Exporter exporter;
  private final CalendarModel model;

  /**
   * Constructs an Export Calendar command, given the view.
   *
   * @param view the calendar view.
   * @param model the current calendar model
   * @throws NullPointerException if any of arguments is null.
   */
  public ExportCommand(CalendarModel model, CalendarView view) {
    if (model == null) {
      throw new IllegalStateException("No Calendar in use to execute export");
    } else {
      this.model = model;
    }
    this.view = Objects.requireNonNull(view);
    this.exporter = null;
  }

  // for package private internal testing, not available for public
  ExportCommand(CalendarModel model, CalendarView view, Exporter exporter) {
    this.model = Objects.requireNonNull(model);
    this.view = Objects.requireNonNull(view);
    this.exporter = Objects.requireNonNull(exporter);
  }

  @Override
  public void execute(Scanner scanner) {
    String command = scanner.nextLine().trim();
    if (this.exporter == null) {
      this.exporter = this.factoryExport(command);
    }
    try {
      String absPath = this.exporter.export(model);
      view.renderMessage("Successfully exported to " + absPath);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot create a file");
    }
  }

  // decides what exporter is selected for export command
  private Exporter factoryExport(String command) {
    Pattern csvPattern = Pattern.compile("^cal\\s+([\\w\\-/\\\\]+\\.csv)$",
        Pattern.CASE_INSENSITIVE);
    Pattern icalPattern = Pattern.compile("^cal\\s+([\\w\\-/\\\\]+\\.ical)$",
        Pattern.CASE_INSENSITIVE);
    Matcher matchCsv = csvPattern.matcher(command);
    Matcher matchIcal = icalPattern.matcher(command);
    if (matchCsv.matches()) {
      String filename = matchCsv.group(1); // getting the filename
      return new CsvExporter(filename);
    } else if (matchIcal.matches()) {
      String filename = matchIcal.group(1);
      return new IcalExporter(filename);
    } else {
      throw new IllegalStateException(
        "Invalid command. Should be: export cal filename.csv/filename.ical");
    }
  }
}
