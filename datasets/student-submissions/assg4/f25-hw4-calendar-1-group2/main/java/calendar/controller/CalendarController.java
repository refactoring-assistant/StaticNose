package calendar.controller;

import calendar.model.CalendarInterface;
import calendar.view.CalendarView;
import java.io.IOException;
import java.util.Scanner;

/**
 * The implementation of CalendarControllerInterface that takes in input, appoints to the
 * specific command execution process accordingly, and transmits the result.
 */
public class CalendarController {
  private final CalendarInterface calendar;
  private final CalendarView view;
  private final Readable input;
  private final Appendable output;

  /**
   * The Constructor for CalendarController Class.
   *
   * @param calendar     the calendar object
   * @param calendarView the calendarView object
   * @param input        the abstracted input
   * @param output       the abstracted output
   */
  public CalendarController(CalendarInterface calendar, CalendarView calendarView,
                            Readable input, Appendable output) {
    this.calendar = calendar;
    this.view = calendarView;
    this.input = input;
    this.output = output;
  }

  /**
   * The executioner of taking in next line of command and processing.
   */
  public void go() throws IOException {
    Scanner scanner = new Scanner(this.input);
    String commandLine;
    boolean exitFlag = true;
    while (scanner.hasNextLine() && exitFlag) { // java cant judge int as bool
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
        CommandFactory exportEvent = new ExportCommand(commandLine, calendar, view);
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
