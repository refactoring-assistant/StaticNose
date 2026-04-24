package calendar.controller;

import calendar.controller.commands.CommandInterface;
import calendar.controller.commands.CreateCommand;
import calendar.controller.commands.EditCommand;
import calendar.controller.commands.ExportCommand;
import calendar.controller.commands.PrintCommand;
import calendar.controller.commands.ShowCommand;
import calendar.controller.utils.CommandParserUtils;
import calendar.model.InterfaceCalendarModel;
import calendar.view.InterfaceCalendarView;
import java.io.IOException;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main Controller for the Calendar application.
 * It connects the Model and View and drives the application loop.
 * It uses the Command Pattern to delegate work for each command.
 */
public class CalendarController {

  private final InterfaceCalendarModel model;
  private final InterfaceCalendarView view;
  private final InputSource input;
  private final Map<String, CommandInterface> commands;

  private static final ZoneId TIME_ZONE = ZoneId.of(InterfaceCalendarModel.TIME_ZONE_ID);

  /**
   * Creates a new CalendarController.
   *
   * @param model The business logic (Model)
   * @param view  The output destination (View)
   * @param input The command source (Input)
   */
  public CalendarController(InterfaceCalendarModel model, InterfaceCalendarView view,
                            InputSource input) {
    this.model = model;
    this.view = view;
    this.input = input;

    CommandParserUtils utils = new CommandParserUtils(TIME_ZONE);

    this.commands = new HashMap<>();
    this.commands.put("create", new CreateCommand(utils));
    this.commands.put("edit", new EditCommand(utils));
    this.commands.put("print", new PrintCommand(utils));
    this.commands.put("show", new ShowCommand(utils));
    this.commands.put("export", new ExportCommand(utils));
  }

  /**
   * Starts the main application loop.
   * Reads commands one by one and processes them until 'exit' or End of file.
   */
  public void run() {
    boolean running = true;

    try (input) {
      while (running && input.hasNextLine()) {
        String commandString = input.readLine();

        if (commandString == null) {
          break;
        }

        commandString = commandString.trim();
        if (commandString.isEmpty()) {
          continue;
        }

        if (commandString.equalsIgnoreCase("exit")) {
          running = false;
          continue;
        }

        try {
          processCommand(commandString);
        } catch (Exception e) {
          view.displayError(e.getMessage());
        }
      }

      if (running && input instanceof FileInputSource) {
        view.displayError("Input file ended without an 'exit' command.");
      }

    } catch (IOException e) {
      view.displayError("An I/O error occurred: " + e.getMessage());
    }
  }

  /**
   * Parses and executes a single command string.
   */
  private void processCommand(String commandString) throws Exception {
    List<String> tokens = CommandParser.parse(commandString);
    if (tokens.isEmpty()) {
      return;
    }

    String commandKey = tokens.get(0).toLowerCase();
    CommandInterface command = commands.get(commandKey);

    if (command == null) {
      throw new Exception("Unknown command: " + commandKey);
    }

    command.execute(model, view, tokens.subList(1, tokens.size()));
  }
}