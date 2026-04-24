package calendar.controllers;

import calendar.models.Calendar;
import calendar.views.ObservableView;
import java.io.InputStream;
import java.util.Scanner;

/**
 * The controller that orchestrates the communication between the model and the view.
 */
public class ControllerImpl implements Controller {

  private final Calendar model;
  private final ObservableView view;
  private final InputStream in;
  private final CommandParser parser;

  /**
   * Initialize the controller with appropriate model and view.
   *
   * @param model the model containing the business logic
   * @param view  the view that displays info to the user
   * @param in    the input from which user commands can be read from
   */
  public ControllerImpl(Calendar model, ObservableView view, InputStream in, CommandParser parser) {
    this.model = model;
    this.view = view;
    this.in = in;
    this.parser = parser;
  }

  @Override
  public void go() {

    boolean exitCommandEncountered = false;

    try (Scanner scanner = new Scanner(this.in)) {
      while (scanner.hasNextLine()) {
        String command = scanner.nextLine();
        if (command.trim().equalsIgnoreCase("exit")) {
          exitCommandEncountered = true;
          break;
        }
        Command executor;
        try {
          executor = parser.parse(command);
          executor.execute();
        } catch (CommandParseException e) {
          view.displayError("Error parsing command: " + e.getMessage());
        }
      }
    }

    if (!exitCommandEncountered) {
      view.displayError("Input ended without an 'exit' command. Terminating program...");
    }
  }
}
