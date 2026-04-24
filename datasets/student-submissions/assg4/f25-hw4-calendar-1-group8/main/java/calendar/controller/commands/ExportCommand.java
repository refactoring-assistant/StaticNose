package calendar.controller.commands;

import calendar.controller.utils.CommandParserUtils;
import calendar.export.CalendarExporter;
import calendar.model.CalendarEvent;
import calendar.model.InterfaceCalendarModel;
import calendar.view.InterfaceCalendarView;
import java.nio.file.Path;
import java.util.List;

/**
 * Command to handle exporting the calendar.
 * This command co-ordinates getting data from the model and passing
 * it to the exporter utility.
 */
public class ExportCommand implements CommandInterface {

  private final CommandParserUtils utils;

  /**
   * Constructs an ExportCommand with the given utility parser.
   *
   * @param utils the command parser utilities used for date/time and argument handling
   */
  public ExportCommand(CommandParserUtils utils) {
    this.utils = utils;
  }

  @Override
  public void execute(InterfaceCalendarModel model, InterfaceCalendarView view, List<String> args)
      throws Exception {
    if (args.size() != 2 || !args.get(0).equals("cal")) {
      throw new Exception("Usage: export cal <fileName.csv>");
    }
    String fileName = args.get(1);

    List<CalendarEvent> allEvents = model.getAllEvents();
    Path absolutePath = CalendarExporter.exportTocsv(allEvents, fileName);
    view.displayExportConfirmation(absolutePath.toString());
  }
}