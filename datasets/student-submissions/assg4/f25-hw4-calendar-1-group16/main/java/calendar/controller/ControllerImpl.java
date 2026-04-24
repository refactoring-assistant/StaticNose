package calendar.controller;

import calendar.controller.commands.CommandFactory;
import calendar.controller.commands.Icommands;
import calendar.model.CalendarInterface;
import calendar.view.Iview;
import java.io.IOException;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * This is the calendar controller implementation.
 * Responsibilities include:
 * <ul>
 * <li> Read user input
 * <li> Parse commands using regex
 * <li> Create command objects
 * <li> Execute commands on Model
 * <li> Display the result in the view
 * </ul>
 */
public class ControllerImpl implements Icontroller {

  private final CalendarInterface model;
  private final CommandFactory commandFactory;
  private final Iview view;

  /**
   * Create Controller with model & view.
   *
   * @param model the calendar model
   * @param view the view for displaying output
   */
  public ControllerImpl(CalendarInterface model, Iview view) {
    if (model == null || view == null) {
      throw new IllegalArgumentException("Model or View is null");
    }

    this.model = model;
    this.commandFactory = new CommandFactory(view);
    this.view = view;
  }

  @Override
  public void run(Readable input) throws IOException {
    view.displayMessage("Calendar Application!");
    view.displayMessage("");
    List<String> help = Arrays.asList(
        "EXAMPLES:",
        "  create event \"Team Meeting\" from 2025-01-15T14:00 to 2025-01-15T15:00",
        "  create event Standup from 2025-01-15T09:00 to 2025-01-15T09:15 repeats MTWRF for 5",
        "  edit event subject Meeting from 2025-01-15T14:00 to 2025-01-15T15:00 with "
            + "\"Important Meeting\"",
        "  print events on 2025-01-15",
        "  show status at 2025-01-15T14:30",
        "  export calendar.csv",
        "-----",
        " "
    );

    boolean exitCommandFound = false;
    view.displayMessage(String.join("\n", help));
    Scanner scanner = new Scanner(input);
    while (scanner.hasNextLine()) {
      try {
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) {
          continue;
        }

        if (line.equalsIgnoreCase("exit")) {
          exitCommandFound = true;
          view.displayMessage("Bye!");
          break;
        }
        executeCommand(line);
      } catch (IOException e) {
        throw e;
      } catch (Exception e) {
        view.displayError("Unexpected Error: " + e.getMessage());
      }
    }

    if (!exitCommandFound) {
      view.displayError("Input ended without an 'exit' command.");
    }
  }

  @Override
  public boolean executeCommand(String input) throws IOException {
    try {
      Icommands command = commandFactory.createCommand(input);
      if (command == null) {
        view.displayError("Unknown Command!");
        return false;
      }
      command.go(model);
      return true;

    } catch (IllegalArgumentException e) {
      view.displayError(e.getMessage());
      return false;

    } catch (DateTimeParseException e) {
      view.displayError("Invalid Date/Time format: " + e.getMessage());
      return false;
    } catch (Exception e) {
      view.displayError("Unexpected Error: " + e.getMessage());
      return false;
    }
  }
}