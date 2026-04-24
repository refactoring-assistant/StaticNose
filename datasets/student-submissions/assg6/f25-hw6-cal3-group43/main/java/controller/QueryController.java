package controller;

import interpreter.CommandInterpreter;
import interpreter.CommandInterpreter.CommandMatch;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import messaging.Messages;
import model.Calendar;
import model.Event;

/**
 * This class focuses on processing the export and read operations. It also includes basic
 * instructions on how to use the QueryController commands if used incorrectly.
 */
public class QueryController {

  public static final String DATE_FMT = "YYYY-MM-DD";
  public static final String DATETIME_FMT = "YYYY-MM-DDTHH:mm";
  public static final String DATE_ERROR = "All Date has to follow this format: " + DATE_FMT;
  public static final String DATETIME_ERROR =
      "All DateTime has to follow this format: " + DATETIME_FMT;
  public static final String PRINT_INSTRUCTION = String.join(System.lineSeparator(),
      "----------------- PRINT INSTRUCTION --------------",
      "You can print one day's events with:",
      "  print events on <date>",
      "where <date> must be: " + DATE_FMT + ".",
      "",
      "Or print a time range with:",
      "  print events from <dateTime> to <dateTime>",
      "where <dateTime> must be: " + DATETIME_FMT + "."
  );
  public static final String SHOW_INSTRUCTION = String.join(System.lineSeparator(),
      "----------------- SHOW INSTRUCTION --------------",
      "You can check the status at a specific time with:",
      "  show status on <dateTime>",
      "where <dateTime> must be: " + DATETIME_FMT + "."
  );
  public static final String EXPORT_INSTRUCTION = String.join(System.lineSeparator(),
      "----------------- EXPORT INSTRUCTION --------------",
      "You can export this calendar as a CSV with:",
      " export cal <name>",
      "where <name> must be a valid CSV file at a default or specified pathway.",
      "Or you can export this calendar as a Ical/ICS with:",
      " export cal <name>",
      "where <name> must be a valid Ical/ICS file at a default or specified pathway."
  );
  protected final CommandInterpreter interpreter;
  protected Calendar calendar;

  /**
   * Creates a QueryController using a fresh calendar.
   */
  public QueryController() {
    this(new Calendar());
  }

  /**
   * Creates a QueryController bound to the provided calendar.
   *
   * @param calendar active calendar instance
   */
  public QueryController(Calendar calendar) {
    this(calendar, new CommandInterpreter());
  }

  /**
   * Internal constructor used for tests to supply interpreter overrides.
   *
   * @param calendar    active calendar instance
   * @param interpreter interpreter used for parsing commands
   */
  protected QueryController(Calendar calendar, CommandInterpreter interpreter) {
    this.calendar = Objects.requireNonNull(calendar, "calendar");
    this.interpreter = Objects.requireNonNull(interpreter, "interpreter");
  }

  /**
   * Prints the print instructions.
   */
  protected static void printInstruction() {
    Messages.info(PRINT_INSTRUCTION);
  }

  /**
   * Prints the show instructions.
   */
  protected static void showInstruction() {
    Messages.info(SHOW_INSTRUCTION);
  }

  /**
   * Prints the export instructions.
   */
  protected static void exportInstruction() {
    Messages.info(EXPORT_INSTRUCTION);
  }

  /**
   * Sets a calendar to the QueryController object.
   *
   * @param calendar a calendar
   */
  public void setQueryController(Calendar calendar) {
    this.calendar = Objects.requireNonNull(calendar, "calendar");
  }

  /**
   * Handles {@code print events on <date>} using the regex match groups.
   *
   * @param match parsed command
   */
  protected void handlePrintOn(CommandMatch match) {
    try {
      LocalDate date = LocalDate.parse(match.matcher().group("date"));
      calendar.printEventsToday(date);
    } catch (DateTimeParseException e) {
      Messages.error(DATE_ERROR);
    }
  }

  /**
   * Handles {@code print events from <start> to <end>} using the captured dates.
   *
   * @param match parsed command
   */
  protected void handlePrintRange(CommandMatch match) {
    try {
      var matcher = match.matcher();
      LocalDateTime startTime = LocalDateTime.parse(matcher.group("start"));
      LocalDateTime endTime = LocalDateTime.parse(matcher.group("end"));
      calendar.printEventsSpan(startTime, endTime);
    } catch (DateTimeParseException e) {
      Messages.error(DATETIME_ERROR);
    }
  }

  /**
   * Handles {@code show status on <dateTime>}.
   *
   * @param match parsed command
   */
  protected void handleShow(CommandMatch match) {
    try {
      LocalDateTime date = LocalDateTime.parse(match.matcher().group("moment"));
      calendar.printAvailability(date);
    } catch (DateTimeParseException e) {
      Messages.error(DATETIME_ERROR);
    }
  }


  /**
   * Handles {@code export cal <file.csv>} and writes the CSV (.csv)
   * or iCal (.ical/.ics) to disk.
   *
   * @param match parsed command
   */
  protected void handleExport(CommandMatch match) {
    try {
      String fileOrDirect = match.matcher().group("file");
      Path enteredPath = Paths.get(fileOrDirect);
      Path filePath;

      if (enteredPath.isAbsolute()) {
        filePath = enteredPath;
      } else {
        Path currDirect = Paths.get(System.getProperty("user.dir"));
        filePath = currDirect.resolve(enteredPath);

        if (filePath.getParent() != null) {
          Files.createDirectories(filePath.getParent());
        }
      }

      String fileName = filePath.getFileName().toString().toLowerCase();

      if (fileName.endsWith(".csv")) {
        try (FileWriter writer = new FileWriter(filePath.toString())) {
          writer.append(
              "Subject,Start Date,Start Time,End Date,End Time,All Day Event,"
                  + "Description,Location,Private\n");

          for (Event event : calendar.getEvents()) {
            String csvEvent = event.convertCsv();
            writer.append(csvEvent);
            writer.append('\n');
          }
          writer.flush();

        }
      } else if (fileName.endsWith(".ical") || fileName.endsWith(".ics")) {
        try (FileWriter writer = new FileWriter(filePath.toString())) {
          writer.append("BEGIN:VCALENDAR\r\n");
          writer.append("VERSION:2.0\r\n");
          writer.append("PRODID:ICAL_EXPORT\r\n");

          for (Event event : calendar.getEvents()) {
            String icalEvent = event.convertIcal();
            writer.append(icalEvent);
            writer.append('\n');
          }
          writer.append("END:VCALENDAR\r\n");
          writer.flush();
        }
      } else {
        Messages.error("ERROR: wrong format input.");
        exportInstruction();
        return;
      }

      Messages.info("Exported to: "
          + filePath.toAbsolutePath().normalize());

    } catch (IOException e) {
      Messages.error("ERROR: wrong format input.");
      exportInstruction();
    }
  }


  /**
   * CommandController overrides this to handle create/edit commands.
   *
   * @param match parsed command match
   * @return true if handled
   */
  protected boolean handleSubclass(CommandMatch match) {
    return false;
  }

  /**
   * Umbrella help method that prints instructions on how to navigate commands.
   */
  protected void help() {
    printInstruction();
    showInstruction();
    exportInstruction();
  }


  /**
   * use this method to handle commands from the user.
   *
   * @param command as a String
   */
  public void interpret(String command) {
    CommandMatch match = interpreter.interpret(command);
    switch (match.type()) {
      case PRINT_ON:
        handlePrintOn(match);
        break;
      case PRINT_RANGE:
        handlePrintRange(match);
        break;
      case SHOW_STATUS:
        handleShow(match);
        break;
      case EXPORT_CAL:
        handleExport(match);
        break;
      case HELP:
        help();
        break;
      case EXIT:
        break;
      default:
        if (!handleSubclass(match)) {
          String normalized = match.normalized();
          String message = (normalized == null || normalized.isBlank())
              ? "ERROR: No command provided."
              : "ERROR: Could not understand command: " + normalized;
          Messages.error(message);
          help();
          return;
        }
        break;
    }
  }

  public String getSubject() {
    return calendar.getName();
  }

}
