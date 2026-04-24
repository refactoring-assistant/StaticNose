package calendar.controller.command;

import calendar.io.CsvExporter;
import calendar.model.calendar.Icalendar;
import calendar.view.IcalendarView;
import java.io.File;

/**
 * Executes the command for the Calendar.
 */
public class ExportCommand implements Icommand {

  private final String fileName;

  /**
   * The file where to execute the command.
   *
   * @param fileName the file to exceute commands.
   */
  public ExportCommand(String fileName) {
    this.fileName = fileName;
  }

  /**
   * Execute this command on the given calendar & calendar.view.
   *
   * @param calendar the calendar object to operate on
   * @param view     the calendar.view object to operate on
   * @throws Exception if command fails to execute
   */
  @Override
  public void execute(Icalendar calendar, IcalendarView view) throws Exception {
    // Export CSV
    CsvExporter.exportCalendar(calendar, fileName);

    // get abs path
    File file = new File(fileName);
    String absolutePath = file.getAbsolutePath();

    //Display path
    view.displayExportPath(absolutePath);
  }
}
