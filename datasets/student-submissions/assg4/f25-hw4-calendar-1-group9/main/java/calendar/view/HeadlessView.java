package calendar.view;

import calendar.controller.InCalendarController;
import calendar.exception.InvalidCommandException;
import calendar.model.InEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Executes commands from file without user interaction.
 * Validates file contains exit command before processing.
 * All date/times displayed are in EST (America/New_York) timezone.
 */
public class HeadlessView implements InCalendarView {

  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);

  private static final DateTimeFormatter TIME_FORMAT =
      DateTimeFormatter.ofPattern("HH:mm", Locale.US);

  private static final int MAX_COMMANDS = 10000;

  private final Path commandFilePath;
  private final PrintStream out;

  /**
   * Constructs a HeadlessView with a command file path.
   *
   * @param commandFilePath path to the commands file
   */
  public HeadlessView(Path commandFilePath) {
    this(commandFilePath, System.out);
  }

  /**
   * Constructs a HeadlessView with custom output stream.
   *
   * @param commandFilePath path to the commands file
   * @param output          the output stream
   */
  public HeadlessView(Path commandFilePath, PrintStream output) {
    this.commandFilePath = commandFilePath;
    this.out = output;
  }

  @Override
  public void displayMessage(String message) {
    out.println(message);
  }

  @Override
  public void displayError(String error) {
    out.println("ERROR: " + error);
  }

  @Override
  public void displaySuccess(String message) {
    out.println("SUCCESS: " + message);
  }

  @Override
  public void displayEvents(List<InEvent> events) {
    if (events == null || events.isEmpty()) {
      out.println("No events found.");
      return;
    }

    for (InEvent event : events) {
      StringBuilder sb = new StringBuilder();
      sb.append("- ").append(event.getSubject());
      sb.append(" starting on ");
      sb.append(event.getStartDateTime().format(DATE_FORMAT));
      sb.append(" at ");
      sb.append(event.getStartDateTime().format(TIME_FORMAT));
      sb.append(", ending on ");
      sb.append(event.getEndDateTime().format(DATE_FORMAT));
      sb.append(" at ");
      sb.append(event.getEndDateTime().format(TIME_FORMAT));

      event.getLocation().ifPresent(loc -> sb.append(", Location: ").append(loc));

      out.println(sb.toString());
    }
  }

  /**
   * Executes commands from the file.
   * Validates that file ends with exit command.
   * Handles platform-independent line endings (\n, \r\n, \r).
   *
   * @param controller the controller to execute commands
   * @throws IOException if file reading fails
   */
  public void executeCommandsFromFile(InCalendarController controller) throws IOException {
    if (commandFilePath == null) {
      throw new IllegalArgumentException("Command file path cannot be null");
    }

    if (!Files.exists(commandFilePath)) {
      throw new IOException("Command file not found: " + commandFilePath);
    }

    List<String> commands = readCommands();

    if (commands.isEmpty() || !commands.get(commands.size() - 1).equalsIgnoreCase("exit")) {
      displayError("Command file must end with 'exit' command");
      return;
    }

    for (String command : commands) {
      if (command.isEmpty()) {
        continue;
      }

      displayMessage("Executing: " + command);

      try {
        controller.executeCommand(command);
      } catch (InvalidCommandException e) {
        System.out.println("do nothing");
      } catch (Exception e) {
        displayError("Unexpected error: " + e.getMessage());
      }
    }
  }

  /**
   * Reads and validates commands from file.
   * Handles various line ending formats (LF, CRLF, CR).
   *
   * @return list of command strings
   * @throws IOException if reading fails or limit exceeded
   */
  private List<String> readCommands() throws IOException {
    List<String> commands = new ArrayList<>();

    try (BufferedReader reader = Files.newBufferedReader(commandFilePath)) {
      String line;
      int lineCount = 0;

      while ((line = reader.readLine()) != null) {
        lineCount++;

        if (lineCount > MAX_COMMANDS) {
          throw new IOException(
              "Command file exceeds maximum allowed commands: " + MAX_COMMANDS);
        }

        String trimmedLine = line.trim();

        if (!trimmedLine.isEmpty()) {
          commands.add(trimmedLine);
        }
      }
    }

    return commands;
  }
}