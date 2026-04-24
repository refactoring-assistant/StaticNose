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

/**
 * Executes commands from file without user interaction.
 * Validates file contains exit command before processing.
 */
public class HeadlessView implements InCalendarView {

  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMAT =
      DateTimeFormatter.ofPattern("HH:mm");
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
   *
   * @param controller the controller to execute commands with
   * @throws IOException if file reading fails
   */
  public void executeCommandsFromFile(InCalendarController controller) throws IOException {
    if (!Files.exists(commandFilePath)) {
      displayError("Command file not found: " + commandFilePath);
      return;
    }

    List<String> commands = readCommandsFromFile();

    if (!validateExitCommand(commands)) {
      displayError("Command file must end with 'exit' command");
      return;
    }

    for (String command : commands) {
      if (command.trim().isEmpty()) {
        continue;
      }

      out.println("Executing: " + command);

      try {
        controller.executeCommand(command);
      } catch (InvalidCommandException e) {
        continue;
      }

      if (command.trim().equalsIgnoreCase("exit")) {
        break;
      }
    }
  }

  private List<String> readCommandsFromFile() throws IOException {
    List<String> commands = new ArrayList<>();
    try (BufferedReader reader = Files.newBufferedReader(commandFilePath)) {
      String line;
      while ((line = reader.readLine()) != null) {
        commands.add(line);
        if (commands.size() > MAX_COMMANDS) {
          throw new IOException(
              "Command file exceeds maximum allowed commands: " + MAX_COMMANDS);
        }
      }
    }
    return commands;
  }

  private boolean validateExitCommand(List<String> commands) {
    if (commands.isEmpty()) {
      return false;
    }

    for (int i = commands.size() - 1; i >= 0; i--) {
      String cmd = commands.get(i).trim();
      if (!cmd.isEmpty()) {
        return cmd.equalsIgnoreCase("exit");
      }
    }

    return false;
  }
}