package calendar.controller;

import calendar.controller.commands.Command;
import calendar.controller.commands.CommandTokenizer;
import calendar.controller.commands.CommandTokenizerImpl;
import calendar.controller.commands.CreateCommand;
import calendar.controller.commands.EditEventCommand;
import calendar.controller.commands.EditMultipleEventsCommand;
import calendar.controller.commands.EditSeriesCommand;
import calendar.controller.commands.ExportCommand;
import calendar.controller.commands.PrintCommand;
import calendar.controller.commands.UserStatusCommand;
import calendar.model.AdvancedCalendarImpl;
import calendar.model.CalendarContainerImpl;
import calendar.model.interfaces.CalendarContainer;
import calendar.model.interfaces.CalendarEditable;
import calendar.view.CalendarView;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Function;

/**
 * Implementation of the Calendar controller interface.
 * This class handles the user commands from Readable and executes them
 * via the model Commands and updates the view.
 */
public class CalendarControllerImpl implements CalendarController {

  protected final CalendarContainer calendarContainer;
  protected final CalendarEditable calendar;
  protected final Readable inputStream;
  protected final CalendarView calendarView;
  protected Map<String, Function<CalendarContainer, Command>> commands;

  /**
   * Backward-compatible constructor (keeps old behavior).
   * Delegates to the new constructor by creating a fresh CalendarContainerImpl.
   *
   * @param calendar     Calendar Editable Model.
   * @param inputStream  used to read the user input.
   * @param calendarView Output to the user view.
   */
  public CalendarControllerImpl(CalendarEditable calendar, Readable inputStream,
                                CalendarView calendarView) {
    this(new CalendarContainerImpl(), calendar, inputStream, calendarView);
  }

  /**
   * New primary constructor: accepts an externally-provided CalendarContainer.
   * This ensures the controller uses the *exact same* container instance that callers pass in.
   *
   * @param calendarContainer an existing CalendarContainer to use
   * @param calendar          Calendar Editable Model (used for default calendar if needed)
   * @param inputStream       used to read the user input
   * @param calendarView      Output to the user view
   */
  public CalendarControllerImpl(CalendarContainer calendarContainer,
                                CalendarEditable calendar,
                                Readable inputStream,
                                CalendarView calendarView) {
    this.calendar = Objects.requireNonNull(calendar);
    this.inputStream = inputStream;
    this.calendarView = Objects.requireNonNull(calendarView);
    this.commands = new HashMap<>();

    this.calendarContainer = Objects.requireNonNull(calendarContainer);
    try {
      if (!calendarContainer.getCalendars().containsKey("default")) {
        this.calendarContainer.addCalendar("default",
            new AdvancedCalendarImpl
                .AdvancedCalendarBuilder("default", ZoneId.systemDefault())
                .setCalendar(calendar)
                .build());
      }
      if (calendarContainer.getActiveCalendar() == null
          || calendarContainer.getCalendars().containsKey("default")) {
        this.calendarContainer.setActiveCalendar("default");
      }
    } catch (Exception e) {
      calendarView.renderError(e.getMessage());
    }

    this.commands.put("create event",
        (container) -> new CreateCommand(container.getActiveCalendar()));
    this.commands.put("edit event",
        (container) -> new EditEventCommand(container.getActiveCalendar()));
    this.commands.put("edit events",
        (container) -> new EditMultipleEventsCommand(container.getActiveCalendar()));
    this.commands.put("edit series",
        (container) -> new EditSeriesCommand(container.getActiveCalendar()));
    this.commands.put("print events",
        (container) -> new PrintCommand(container.getActiveCalendar()));
    this.commands.put("export cal",
        (container) -> new ExportCommand(container.getActiveCalendar()));
    this.commands.put("show status",
        (container) -> new UserStatusCommand(container.getActiveCalendar()));
  }

  @Override
  public void run() {
    this.welcomeMessage();
    Scanner scanner = new Scanner(inputStream);
    CommandTokenizer tokenizer = new CommandTokenizerImpl();
    while (scanner.hasNext()) {
      String command = scanner.nextLine();
      List<String> tokens = tokenizer.parser(command);
      if (tokens.isEmpty()) {
        continue;
      }
      if (tokens.size() == 1 && tokens.get(0).equals("exit")) {
        calendarView.render("Exiting!!");
        this.farewellMessage();
        return;
      }
      if (tokens.size() <= 2) {
        calendarView.renderError("Error: Invalid command: " + command);
        continue;
      }
      String commandKey = tokens.get(0) + " " + tokens.get(1);
      Function<CalendarContainer, Command> commandFunction = commands.get(commandKey);

      if (commandFunction == null) {
        calendarView.renderError("Error: Invalid command: " + command);
        continue;
      }
      try {
        calendarView.render(commandFunction.apply(calendarContainer).execute(tokens));
      } catch (Exception e) {
        calendarView.renderError(e.getMessage());
      }
    }
  }

  /**
   * Used print a Farewell message.
   */
  protected void farewellMessage() {
    calendarView.render("Thank you for using this program!");
  }

  private void welcomeMessage() {

    String sb = headerSection()
        + calendarCommandsSection()
        + copyCommandsSection()
        + individualCalendarSection()
        + exitSection();

    calendarView.render(sb);
  }

  private String headerSection() {
    return "Welcome to the calendar!" + System.lineSeparator()
        + "Use below mentioned commands to interact with the application..."
        + System.lineSeparator();
  }

  private String calendarCommandsSection() {
    return "To create a new calendar" + System.lineSeparator()
        + "create calendar --name <calName> --timezone area/location"
        + System.lineSeparator()
        + "To edit an existing calendar" + System.lineSeparator()
        + "edit calendar --name <name-of-calendar> --property <property-name> <new-property-value>"
        + System.lineSeparator()
        + "To use a calendar that is created:" + System.lineSeparator()
        + "use calendar --name <name-of-calendar>" + System.lineSeparator();
  }

  private String copyCommandsSection() {
    return "To copy events from one calendar to another:" + System.lineSeparator()
        + "copy event <eventName> on <dateStringTtimeString> "
        + "--target <calendarName> to <dateStringTtimeString>" + System.lineSeparator()
        + "copy events on <dateString> --target <calendarName> to <dateString>"
        + System.lineSeparator()
        + "copy events between <dateString> and <dateString> "
        + "--target <calendarName> to <dateString>" + System.lineSeparator()
        + System.lineSeparator();
  }

  /**
   * Returns a formatted string listing all commands supported
   * for managing individual calendars and events.
   *
   * @return a string containing all supported individual calendar commands
   */
  private String individualCalendarSection() {
    return "Commands Supported on individual calendars:" + System.lineSeparator()
        + "create event <eventSubject> from <dateStringTtimeString> to "
        + "<dateStringTtimeString>" + System.lineSeparator()
        + "create event <eventSubject> from <dateStringTtimeString> to "
        + "<dateStringTtimeString> repeats <weekdays> for <N> times"
        + System.lineSeparator()
        + "create event <eventSubject> from <dateStringTtimeString> to "
        + "<dateStringTtimeString> repeats <weekdays> until <dateString>"
        + System.lineSeparator()
        + "create event <eventSubject> on <dateString>" + System.lineSeparator()
        + "create event <eventSubject> on <dateString> repeats <weekdays> for <N> times"
        + System.lineSeparator()
        + "create event <eventSubject> on <dateString> repeats <weekdays> "
        + "until <dateString>" + System.lineSeparator()
        + "edit event <property> <eventSubject> from <dateStringTtimeString> to "
        + "<dateStringTtimeString> with <NewPropertyValue>" + System.lineSeparator()
        + "edit events <property> <eventSubject> from <dateStringTtimeString> "
        + "with <NewPropertyValue>" + System.lineSeparator()
        + "edit series <property> <eventSubject> from <dateStringTtimeString> "
        + "with <NewPropertyValue>" + System.lineSeparator()
        + "print events on <dateString>" + System.lineSeparator()
        + "print events from <dateStringTtimeString> to <dateStringTtimeString>"
        + System.lineSeparator()
        + "export cal fileName.csv or export cal fileName.ical"
        + System.lineSeparator()
        + "show status on <dateStringTtimeString>" + System.lineSeparator();
  }


  private String exitSection() {
    return "To exit the application:" + System.lineSeparator()
        + "exit" + System.lineSeparator();
  }

}

