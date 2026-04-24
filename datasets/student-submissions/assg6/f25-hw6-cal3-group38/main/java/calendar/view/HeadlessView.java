package calendar.view;

import calendar.controller.CalendarController;
import calendar.controller.Command;
import calendar.controller.CommandParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Headless view for processing command files.
 */
public class HeadlessView {
  private final CalendarController controller;

  /**
   * Headless view.
   *
   * @param controller controller.
   */
  public HeadlessView(CalendarController controller) {
    this.controller = controller;
  }

  /**
   * Process a command file.
   *
   * @param filePath filepath.
   */
  public void processFile(String filePath) {
    try {
      List<String> commands = Files.readAllLines(Path.of(filePath));
      processCommands(commands);
    } catch (IOException e) {
      handleFileError(e);
    }
  }

  /**
   * Process a list of commands.
   * Extracted for better testability.
   */
  public void processCommands(List<String> commands) {
    boolean exitFound = false;

    for (String commandLine : commands) {
      if (isBlankLine(commandLine)) {
        continue;
      }

      CommandResult result = processSingleCommand(commandLine);
      printCommandResult(commandLine, result);

      if (result.isExitCommand()) {
        exitFound = true;
        break;
      }
    }

    if (!exitFound) {
      printNoExitWarning();
    }
  }

  /**
   * Process a single command line.
   */
  public CommandResult processSingleCommand(String commandLine) {
    try {
      Command command = CommandParser.parse(commandLine.trim());
      String result = command.execute(controller);
      return CommandResult.success(result, isExitCommand(commandLine));
    } catch (Exception e) {
      return CommandResult.error(e.getMessage(), isExitCommand(commandLine));
    }
  }

  /**
   * Check if a line is blank (empty or whitespace only).
   */
  public boolean isBlankLine(String line) {
    return line.trim().isEmpty();
  }

  /**
   * Check if a command line is an exit command.
   */
  public boolean isExitCommand(String commandLine) {
    return commandLine.trim().equalsIgnoreCase("exit");
  }

  /**
   * Print the result of a command execution.
   */
  public void printCommandResult(String commandLine, CommandResult result) {
    System.out.println("Command: " + commandLine);

    if (result.isSuccess()) {
      System.out.println("Result: " + result.getResult());
    } else {
      System.out.println("Error: " + result.getErrorMessage());
    }

    System.out.println();
  }

  /**
   * Print warning when no exit command is found.
   */
  public void printNoExitWarning() {
    System.out.println("Warning: No exit command found in file");
  }

  /**
   * Handle file reading errors.
   */
  public void handleFileError(IOException e) {
    System.out.println("Error reading file: " + e.getMessage());
  }

  /**
   * Helper class to represent command execution results.
   */
  public static class CommandResult {
    private final boolean success;
    private final String result;
    private final String errorMessage;
    private final boolean exitCommand;

    /**
     * CommandResult.
     *
     * @param success success
     * @param result result
     * @param errorMessage errorMessage
     * @param exitCommand exitCommand
     */
    public CommandResult(boolean success, String result,
                         String errorMessage, boolean exitCommand) {
      this.success = success;
      this.result = result;
      this.errorMessage = errorMessage;
      this.exitCommand = exitCommand;
    }

    /**
     * Create a successful command result.
     *
     * @param result the result string
     * @param exitCommand whether this is an exit command
     * @return CommandResult instance
     */
    public static CommandResult success(String result, boolean exitCommand) {
      return new CommandResult(true, result, null, exitCommand);
    }

    /**
     * Create an error command result.
     *
     * @param errorMessage the error message
     * @param exitCommand whether this is an exit command
     * @return CommandResult instance
     */
    public static CommandResult error(String errorMessage, boolean exitCommand) {
      return new CommandResult(false, null, errorMessage, exitCommand);
    }

    /**
     * Check if the command was successful.
     *
     * @return true if successful
     */
    public boolean isSuccess() {
      return success;
    }

    /**
     * Get the result string.
     *
     * @return the result
     */
    public String getResult() {
      return result;
    }

    /**
     * Get the error message.
     *
     * @return the error message
     */
    public String getErrorMessage() {
      return errorMessage;
    }

    /**
     * Check if this is an exit command.
     *
     * @return true if exit command
     */
    public boolean isExitCommand() {
      return exitCommand;
    }
  }
}