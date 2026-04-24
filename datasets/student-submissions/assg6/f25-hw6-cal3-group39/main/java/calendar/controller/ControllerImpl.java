package calendar.controller;

import calendar.commands.Command;
import calendar.commands.CopyCommand;
import calendar.commands.CreateCalendar;
import calendar.commands.CreateCommand;
import calendar.commands.EditCalendar;
import calendar.commands.EditCommand;
import calendar.commands.ExportCommand;
import calendar.commands.QueryCommand;
import calendar.commands.ShowCalCommand;
import calendar.commands.StatusCommand;
import calendar.model.Model;
import calendar.view.View;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Implements the calendar.Controller.Controller interface for the Calendar application.
 * Responsible for reading user input, dispatching commands, and handling errors.
 */
public class ControllerImpl implements Controller {

  private final Readable input;
  private final Model model;
  private final View view;
  private final Map<String, Command> commands = new HashMap<>();
  private final boolean interactive;
  private String calName = null;

  /**
   * Constructs a calendar.Controller.ControllerImpl.
   *
   * @param input       the readable source to read commands from
   * @param model       the model containing calendar data and logic
   * @param view        the view used to display messages and events
   * @param interactive true if running in interactive mode, false for headless mode
   */
  public ControllerImpl(Readable input, Model model, View view,
                        boolean interactive) {
    this.input = input;
    this.model = model;
    this.view = view;
    this.interactive = interactive;

    commands.put("createCalendar", new CreateCalendar());
    commands.put("editCalendar", new EditCalendar());
    commands.put("create", new CreateCommand());
    commands.put("edit", new EditCommand());
    commands.put("print", new QueryCommand());
    commands.put("show", new StatusCommand());
    commands.put("export", new ExportCommand());
    commands.put("copy", new CopyCommand());
    commands.put("showcal", new ShowCalCommand());
  }

  /**
   * Starts the controller loop, reading commands from the input stream,
   * executing them, and handling errors gracefully.
   */
  @Override
  public void go() {
    Scanner sc = new Scanner(input);

    while (true) {
      if (interactive) {
        view.promptCommand();
      }

      if (!sc.hasNextLine()) {
        view.showError("Error: Headless mode file must end with 'exit' command.");
        break;
      }

      String userIn = sc.nextLine().trim();
      String[] userCommand = userIn.split(" ");
      String cmd = userCommand[0].toLowerCase();

      if (cmd.equals("exit")) {
        break;
      }
      if (userIn.contains("calendar")) {
        if (cmd.equals("create")) {
          commands.get("createCalendar").execute(calName, userIn, model, view);
          continue;
        } else if (cmd.equals("use")) {
          if (userCommand.length == 4 && model.exists(userCommand[3])) {
            calName = userCommand[3];
            continue;
          } else {
            view.showError("Error: Calendar does not exist. " + userIn);
            continue;
          }
        } else if (cmd.equals("edit")) {
          commands.get("editCalendar").execute(calName, userIn, model, view);
          continue;
        }
      }

      if (calName == null) {
        view.showError("Please specify a calendar.");
        continue;
      }

      Command c = commands.get(cmd);

      if (c != null) {
        c.execute(calName, userIn, model, view);
      } else {
        view.showError("Invalid command: " + userIn);
      }
    }
  }

  /**
   * The method below parses a date in the form of a String to a LocalDate object.
   *
   * @param input DateTime user input String.
   * @return A localDate object.
   */
  public static LocalDate returnDate(String input) {
    return LocalDate.parse(input.substring(0, input.indexOf("T")));
  }

  /**
   * The method below parses time in the form of a String to a LocalTime object.
   *
   * @param input DateTime user input String.
   * @return A localTime object.
   */
  public static LocalTime returnTime(String input) {
    return LocalTime.parse(input.substring(input.indexOf("T") + 1));
  }

}