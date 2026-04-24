package calendarcontroller;

import calendarcontroller.commands.AppCommand;
import calendarcontroller.commands.CalendarCommand;
import calendarcontroller.commands.CopyEventCommand;
import calendarcontroller.commands.CopyEventsBetweenCommand;
import calendarcontroller.commands.CopyEventsOnCommand;
import calendarcontroller.commands.CreateCalendarCommand;
import calendarcontroller.commands.CreateSeriesFromForCommand;
import calendarcontroller.commands.CreateSeriesFromUntilCommand;
import calendarcontroller.commands.CreateSeriesOnForCommand;
import calendarcontroller.commands.CreateSeriesOnUntilCommand;
import calendarcontroller.commands.CreateSingleFromCommand;
import calendarcontroller.commands.CreateSingleOnCommand;
import calendarcontroller.commands.EditAllSeriesCommand;
import calendarcontroller.commands.EditCalendarCommand;
import calendarcontroller.commands.EditFutureEventsCommand;
import calendarcontroller.commands.EditSingleEventCommand;
import calendarcontroller.commands.ExportCsvCommand;
import calendarcontroller.commands.ExportIcalCommand;
import calendarcontroller.commands.PrintEventsFromToCommand;
import calendarcontroller.commands.PrintEventsOnCommand;
import calendarcontroller.commands.ShowStatusOnCommand;
import calendarcontroller.commands.UseCalendarCommand;
import calendarmodel.CalendarModel;
import calendarview.CalendarView;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import multicalendarmodel.CalendarNameException;
import multicalendarmodel.MultiCalendarModel;
import multicalendarmodel.ZonedCalendarModel;

/**
 * A text-based implementation of the {@link CalendarController}.
 *
 * <p>This controller reads commands from a {@link Readable} source (e.g., System.in),
 * parses them, executes them against the {@link CalendarModel},
 * and displays results using the {@link CalendarView}.</p>
 *
 * @see CalendarController
 */
public class TextCalendarController implements CalendarController {

  private final CalendarView view;
  private final Readable input;

  private final boolean isMultiCalendarMode;
  private final CalendarModel singleModel;
  private final MultiCalendarModel appModel;
  private ZonedCalendarModel activeCalendar;
  private String activeCalendarName;

  private final List<CalendarCommand> eventCommands;
  private final List<AppCommand> appCommands;

  /**
   * Constructs a new {@link TextCalendarController} for {@link CalendarModel}.
   *
   * @param model The data model to operate on.
   * @param view  The view to display output and errors.
   * @param input The source of user commands (e.g., System.in).
   * @throws IllegalArgumentException if model, view, or input are null.
   */
  public TextCalendarController(CalendarModel model, CalendarView view, Readable input) {
    if (model == null || view == null || input == null) {
      throw new IllegalArgumentException("Model, View, and Input is passed as null.");
    }
    this.isMultiCalendarMode = false;
    this.singleModel = model;
    this.appModel = null;
    this.activeCalendar = null;
    this.activeCalendarName = null;
    this.view = view;
    this.input = input;

    this.eventCommands = new ArrayList<>();
    registerLegacyCommands();
    this.appCommands = Collections.emptyList();
  }

  /**
   * Constructs a new {@link TextCalendarController} for {@link MultiCalendarModel}.
   *
   * @param appModel The data model to operate on.
   * @param view     The view to display output and errors.
   * @param input    The source of user commands (e.g., System.in).
   */
  public TextCalendarController(MultiCalendarModel appModel, CalendarView view, Readable input) {
    if (appModel == null || view == null || input == null) {
      throw new IllegalArgumentException("Model, View, and Input is passed as null.");
    }
    this.isMultiCalendarMode = true;
    this.appModel = appModel;
    this.singleModel = null;
    this.activeCalendar = null;
    this.activeCalendarName = null;
    this.view = view;
    this.input = input;

    this.eventCommands = new ArrayList<>();
    this.appCommands = new ArrayList<>();
    registerMultiCalendarCommands();
  }

  private void registerLegacyCommands() {
    eventCommands.add(new CreateSeriesFromForCommand());
    eventCommands.add(new CreateSeriesFromUntilCommand());
    eventCommands.add(new CreateSeriesOnForCommand());
    eventCommands.add(new CreateSeriesOnUntilCommand());
    eventCommands.add(new CreateSingleFromCommand());
    eventCommands.add(new CreateSingleOnCommand());
    eventCommands.add(new EditSingleEventCommand());
    eventCommands.add(new EditFutureEventsCommand());
    eventCommands.add(new EditAllSeriesCommand());
    eventCommands.add(new PrintEventsOnCommand());
    eventCommands.add(new PrintEventsFromToCommand());
    eventCommands.add(new ShowStatusOnCommand());
    eventCommands.add(new ExportCsvCommand());
    eventCommands.add(new ExportIcalCommand());
  }

  private void registerMultiCalendarCommands() {
    appCommands.add(new CreateCalendarCommand());
    appCommands.add(new EditCalendarCommand());
    appCommands.add(new UseCalendarCommand());
    appCommands.add(new CopyEventCommand());
    appCommands.add(new CopyEventsOnCommand());
    appCommands.add(new CopyEventsBetweenCommand());
    registerLegacyCommands();
  }

  /**
   * Public method to set currently active calendar.
   * This method will be called by AppCommands.
   *
   * @param name Calendar name to keep active
   * @throws CalendarNameException no name is found
   */
  public void setActiveCalendar(String name) throws CalendarNameException {
    if (!this.isMultiCalendarMode) {
      throw new IllegalArgumentException("Cannot set active calendar in legacy mode.");
    }
    this.activeCalendar = this.appModel.getCalendar(name);
    this.activeCalendarName = name;
    this.view.displayMessage("Now using calendar '" + name + "'.");
  }

  /**
   * Public method to retrieve current active calendar.
   *
   * @return Current active {@link ZonedCalendarModel} or null
   */
  public ZonedCalendarModel getActiveCalendar() {
    return this.activeCalendar;
  }

  /**
   * Gets the name of the currently active calendar.
   *
   * @return The active calendar's name, or null.
   */
  public String getActiveCalendarName() {
    return this.activeCalendarName;
  }

  /**
   * {@inheritDoc}
   *
   * <p>This implementation reads lines from the input source, attempts
   * to process them as commands, and continues until the "exit"
   * command is received or the input stream ends.</p>
   */
  @Override
  public void run() {
    Scanner scanner = new Scanner(this.input);
    boolean userExited = false;

    while (scanner.hasNextLine()) {
      String line = scanner.nextLine().trim();
      if (line.isEmpty()) {
        continue;
      }
      if (line.equalsIgnoreCase("exit")) {
        userExited = true;
        break;
      }
      try {
        processCommand(line);
      } catch (Exception e) {
        view.displayError("Exception while processing command: " + e.getMessage());
        e.printStackTrace();
      }
    }

    if (!userExited && !(this.input instanceof InputStreamReader)) {
      view.displayError("Input source ended without 'exit' command.");
    }
  }

  private void processCommand(String line) {
    if (this.isMultiCalendarMode) {
      processMultiCalendarCommand(line);
    } else {
      processLegacyCommand(line);
    }
  }

  private void processMultiCalendarCommand(String line) {
    for (AppCommand command : appCommands) {
      if (command.execute(line, this.appModel, this.view, this)) {
        return;
      }
    }
    if (this.activeCalendar == null) {
      for (CalendarCommand cmd : eventCommands) {
        if (cmd.execute(line, null, view)) {
          view.displayError("Error: No calendar is in use. "
              + "Please use 'use calendar --name <calName>'.");
          return;
        }
      }
    } else {
      for (CalendarCommand command : eventCommands) {
        if (command.execute(line, this.activeCalendar, this.view)) {
          return;
        }
      }
    }
    view.displayError("Invalid command format: " + line);
  }

  private void processLegacyCommand(String line) {
    for (CalendarCommand command : eventCommands) {
      if (command.execute(line, this.singleModel, this.view)) {
        return;
      }
    }
    view.displayError("Invalid command format: " + line);
  }
}
