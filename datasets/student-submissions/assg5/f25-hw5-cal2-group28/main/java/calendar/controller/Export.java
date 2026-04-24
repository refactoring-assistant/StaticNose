package calendar.controller;

import calendar.model.Calendar;
import calendar.model.CalenderManager;
import calendar.service.ExportCsv;
import calendar.service.ExportIcal;
import calendar.view.ViewConsole;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to control export to CSV/Ical.
 * */
public class Export implements CommandInterface {

  private final String textInput;

  /**
   * Constructor to initialise commands class.
   *
   * @param textInput the commands entered by user
   * */
  public Export(String textInput) {
    this.textInput = textInput;
  }

  /**
   * Function to execute commands.
   *
   * @param manager instance of calendar manager
   * @param view instance of view console
   * */
  @Override
  public void execute(CalenderManager manager, ViewConsole view) {
    Calendar calendar = manager.getCurrentCalender();
    if (calendar == null) {
      view.dispError("No calendar in use. Use 'use calendar' command first");
      return;
    }

    Pattern pattern = Pattern.compile("export cal (\\S+)");
    Matcher matcher = pattern.matcher(textInput);

    if (!matcher.find()) {
      view.dispError("Invalid command to edit calendar");
      return;
    }

    String fileName = matcher.group(1);
    String extension = fileName.substring(fileName.lastIndexOf('.') + 1);

    try {
      String exportPath;
      if (extension.equals("ical")) {
        ExportIcal expIcal = new ExportIcal();
        exportPath = expIcal.export(calendar.getAllEvents(), fileName);
        view.dispSuccess("Rendered as iCal. Path: " + exportPath);
      } else {
        ExportCsv expCsv = new ExportCsv();
        exportPath = expCsv.export(calendar.getAllEvents(), fileName);
        view.dispSuccess("Rendered as csv. Path: " + exportPath);
      }
    } catch (IOException e) {
      view.dispError("Error exporting to calendar" + e.getMessage());
    }
  }
}