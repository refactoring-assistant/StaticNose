package calendar.controller;

import calendar.model.CalendarModel;
import calendar.view.CalendarView;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Function;

/**
 * Represents the abstract controller with getting the current model to run and running controller.
 */
public abstract class AbstractController implements CalendarController {
  protected final CalendarView view;
  protected final Readable inputStream;
  protected final Map<String, Function<CalendarModel, Command>> commands;

  /**
   * Constructs an abstract controller given view, the readable input stream, and empty map of
   * commands.
   *
   * @param view        the calendar view.
   * @param inputStream the readable.
   * @throws NullPointerException if view/input stream is null.
   */
  public AbstractController(CalendarView view, Readable inputStream) {
    this.view = Objects.requireNonNull(view);
    this.inputStream = Objects.requireNonNull(inputStream);
    this.commands = new HashMap<>();
  }

  /**
   * Gets the current model to run in controller.
   *
   * @return the current calendar model.
   */
  protected abstract CalendarModel getModelToRun();

  @Override
  public void run() {
    Scanner scanner = new Scanner(this.inputStream);
    int lineCount = 0;

    while (scanner.hasNext()) {
      try {
        lineCount++;
        String command = scanner.next();

        if (command.equalsIgnoreCase("exit")) {
          view.renderMessage("Exiting...");
          return;
        }

        Function<CalendarModel, Command> commandFunction =
            this.commands.getOrDefault(command, null);

        if (commandFunction == null) {
          if (scanner.hasNextLine()) {
            scanner.nextLine();
          }
          throw new IllegalStateException("Command " + command + " not found");
        }

        Command commandToRun = commandFunction.apply(this.getModelToRun());
        commandToRun.execute(scanner);

        if (!scanner.hasNextLine()) {
          this.view.renderMessage("No exit command at end of file. Exiting...");
          return;
        }
      } catch (IllegalStateException | IllegalArgumentException e) {
        this.view.renderMessage(e.getMessage() + " on line " + lineCount);
      }
    }
  }
}
