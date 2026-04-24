package calendar.controller;

import calendar.model.CalenderManager;
import calendar.view.ViewConsole;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Controller class with Command Design Pattern.
 * Coordinates between model and view by delegating to command objets.
 */
public class Controller {
  private final CalenderManager calendarManager;
  private final ViewConsole view;
  private final Map<String, Function<String, CommandInterface>> commandFactory;

  /**
   * Constructor to initialise controller.
   *
   * @param calendarManager instance of calendar manager
   * @param view            instance of interactive view
   *
   */
  public Controller(CalenderManager calendarManager, ViewConsole view) {
    this.calendarManager = calendarManager;
    this.view = view;
    this.commandFactory = new HashMap<>();
    initCommands();
  }

  /**
   * Function to initialise the command factory with all available commands.
   * */
  private void initCommands() {
    commandFactory.put("create calendar", CreateCalendar::new);
    commandFactory.put("use calendar", UseCalendar::new);
    commandFactory.put("edit calendar", EditCalendar::new);
    commandFactory.put("print", QueryEvents::new);
    commandFactory.put("export", Export::new);
    commandFactory.put("create event", input -> {
      if (input.contains("repeats")) {
        return new CreateRecurringEvent(input);
      } else {
        return new CreateEvent(input);
      }
    });
    commandFactory.put("copy", CopyEvent::new);
    commandFactory.put("show", ShowStatus::new);
    commandFactory.put("edit event", EditEvent::new);
    commandFactory.put("edit events", EditEvents::new);
    commandFactory.put("edit series", EditSeries::new);
  }

  /**
   * Function to process input string and accordingly create calendar events.
   *
   * @param inputStr the input string from command line
   * @throws IllegalArgumentException when input command is invalid
   *
   */
  public void processInput(String inputStr) throws IllegalArgumentException {
    try {

      inputStr = inputStr.trim();
      if (inputStr.isEmpty()) {
        return;
      }

      if (inputStr.equalsIgnoreCase("exit")) {
        view.dispEnd();
        return;
      }

      CommandInterface commandInterface = parseCommand(inputStr);
      if (commandInterface == null) {
        view.dispError("Unknown command: " + inputStr);
      } else {
        commandInterface.execute(calendarManager, view);
      }
    } catch (Exception e) {
      view.dispError("Error processing command: " + e.getMessage());
    }
  }

  /**
   * Function to parse input and create appropriate command object.
   *
   * @param inputStr user input
   * @return the command object or null if not found
   * */
  private CommandInterface parseCommand(String inputStr) {
    for (Map.Entry<String, Function<String, CommandInterface>> entry : commandFactory.entrySet()) {
      if (inputStr.startsWith(entry.getKey())) {
        return entry.getValue().apply(inputStr);
      }
    }
    return null;
  }
}