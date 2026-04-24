package calendar.controller;

import calendar.calendarentity.CalendarEntityInterface;
import calendar.model.CalendarInterface;
import calendar.model.MultiCalendarManagerInterface;
import calendar.view.CalendarView;
import java.io.IOException;
import java.util.Scanner;

/**
 * The implementation of CalendarControllerInterface that takes in input, appoints to the
 * specific command execution process accordingly, and transmits the result.
 */
public class CalendarController {
  private CalendarInterface calendar;
  private final CalendarView view;
  private final Readable input;
  private final Appendable output;
  private final MultiCalendarManagerInterface manager;

  /**
   * The Constructor for CalendarController Class.
   *
   * @param manager      the calendar manager
   * @param calendarView the calendarView object
   * @param input        the abstracted input
   * @param output       the abstracted output
   */
  public CalendarController(MultiCalendarManagerInterface manager, CalendarView calendarView,
                            Readable input, Appendable output) {
    this.calendar = null;
    this.view = calendarView;
    this.input = input;
    this.output = output;
    this.manager = manager;
  }

  /**
   * The executioner of taking in next line of command and processing.
   */
  public void go() throws IOException {
    Scanner scanner = new Scanner(this.input);
    String commandLine;
    boolean exitFlag = true;
    while (scanner.hasNextLine() && exitFlag) {
      commandLine = scanner.nextLine();
      if (commandLine.trim().isEmpty()) {
        continue;
      }
      if (commandLine.equalsIgnoreCase("exit")) {
        exitFlag = false;
        break;
      }
      processCommand(commandLine.trim());
    }
  }

  private void processCommand(String commandLine) throws IOException {
    String commandType = "";
    int indexFirstSpace = commandLine.indexOf(' ');
    if (indexFirstSpace != -1) {
      commandType = commandLine.substring(0, indexFirstSpace);
    } else {
      commandType = commandLine;
    }

    if (commandLine.startsWith("create calendar")) {
      CommandFactory createCalendar = new CreateCalendarCommand(commandLine, manager, view);
      createCalendar.execute();
      return;
    }

    if (commandLine.startsWith("edit calendar")) {
      CommandFactory editCalendar = new EditCalendarCommand(commandLine, manager, view);
      editCalendar.execute();
      return;
    }

    if (commandLine.startsWith("use calendar")) {
      CommandFactory useCalendar = new UseCalendarCommand(commandLine, manager, view);
      useCalendar.execute();
      return;
    }

    if (commandLine.startsWith("copy")) {
      CommandFactory copyEvent = new CopyCommand(commandLine, manager, view);
      copyEvent.execute();
      return;
    }

    CalendarEntityInterface entity = manager.getCurrentCalendarEntity();
    if (entity == null) {
      output.append("No current calendar. Please specify which calendar you want to use\n");
      return;
    }
    this.calendar = entity.getCalendar();
    if (this.calendar == null) {
      output.append("No current calendar. Please specify which calendar you want to use\n");
      return;
    }

    switch (commandType) {
      case "create":
        CommandFactory createEvent = new CreateCommand(commandLine, calendar);
        try {
          createEvent.execute();
        } catch (IllegalArgumentException e) {
          this.output.append(e.getMessage()).append("\n");
        }
        break;
      case "edit":
        CommandFactory editEvent = new EditCommand(commandLine, calendar);
        try {
          editEvent.execute();
        } catch (IllegalArgumentException e) {
          this.output.append(e.getMessage()).append("\n");
        }
        break;
      case "print":
        CommandFactory printEvent = new PrintCommand(commandLine, calendar, view);
        printEvent.execute();
        break;
      case "export":
        CommandFactory exportEvent = new ExportCommand(commandLine, entity, view);
        exportEvent.execute();
        break;
      case "show":
        CommandFactory showEvent = new ShowCommand(commandLine, calendar, view);
        showEvent.execute();
        break;
      case "exit":
        break;
      default:
        System.out.println("Invalid command line");
    }
  }
}
