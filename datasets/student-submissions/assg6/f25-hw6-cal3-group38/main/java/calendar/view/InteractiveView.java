package calendar.view;

import calendar.controller.CalendarController;
import calendar.controller.Command;
import calendar.controller.CommandParser;
import java.util.Scanner;

/**
 * Interactive view for user input/output.
 */
public class InteractiveView {
  private final CalendarController controller;
  public boolean running;
  public boolean welcomePrinted;

  // Added for testability - dependency injection
  private final InputProvider inputProvider;
  private final OutputHandler outputHandler;

  /**
   * InteractiveView.
   *
   * @param controller controller.
   */
  public InteractiveView(CalendarController controller) {
    this(controller, createSystemInputProvider(), createSystemOutputHandler());
  }

  /**
   * Test constructor with dependency injection.
   */
  public InteractiveView(CalendarController controller, InputProvider inputProvider,
                         OutputHandler outputHandler) {
    this.controller = controller;
    this.inputProvider = inputProvider;
    this.outputHandler = outputHandler;
    this.running = false;
    this.welcomePrinted = false;
  }

  /**
   * Start the interactive session.
   */
  public void start() {
    running = true;
    printWelcomeMessage();

    while (isRunning()) {
      processNextCommand();
    }
  }

  /**
   * Process a single command from user input.
   * This method is extracted for better testability.
   */
  public void processNextCommand() {
    printPrompt();
    String input = readInput();

    if (shouldSkipInput(input)) {
      return;
    }

    processCommand(input);
  }

  /**
   * Read input from the user.
   */
  public String readInput() {
    return inputProvider.readLine();
  }

  /**
   * Check if input should be skipped (empty lines).
   */
  public boolean shouldSkipInput(String input) {
    return input == null || input.trim().isEmpty();
  }

  /**
   * Execute a command through the controller.
   */
  public String executeCommand(Command command) {
    return command.execute(controller);
  }

  /**
   * Check if the input is an exit command.
   */
  public boolean isExitCommand(String input) {
    return input != null && input.trim().equalsIgnoreCase("exit");
  }

  /**
   * Stop the interactive session.
   */
  public void stop() {
    running = false;
  }

  /**
   * Check if the view is currently running.
   */
  public boolean isRunning() {
    return running;
  }

  /**
   * Print welcome message (extracted for testability).
   */
  public void printWelcomeMessage() {
    if (!welcomePrinted) {
      outputHandler.println("Welcome to Calendar Application!");
      outputHandler.println("Type 'exit' to quit, or enter commands below:");
      welcomePrinted = true;
    }
  }

  /**
   * Process a command string.
   */
  public void processCommand(String input) {
    try {
      Command command = CommandParser.parse(input);
      String result = executeCommand(command);
      printResult(result);

      if (isExitCommand(input)) {  // ← Checks for exit
        stop();  // ← Stops the loop, allowing natural program termination
      }
    } catch (Exception e) {
      printError(e.getMessage());
    }
  }

  /**
   * printPrompt.
   */
  public void printPrompt() {
    outputHandler.print("> ");
  }

  /**
   * printResult.
   *
   * @param result result.
   */
  public void printResult(String result) {
    outputHandler.println(result);
  }

  /**
   * printError.
   *
   * @param errorMessage error.
   */
  public void printError(String errorMessage) {
    outputHandler.println("Error: " + errorMessage);
  }

  /**
   * Interface for providing input in a testable way.
   */
  public interface InputProvider {

    /**
     * Reads a line of input.
     *
     * @return the input line, trimmed
     */
    String readLine();
  }

  /**
   * Interface for handling output in a testable way.
   */
  public interface OutputHandler {

    /**
     * Prints a message without newline.
     *
     * @param message the message to print
     */
    void print(String message);

    /**
     * Prints a message with newline.
     *
     * @param message the message to print
     */
    void println(String message);
  }

  /**
   * Default input provider using System.in.
   */
  public static class SystemInputProvider implements InputProvider {
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public String readLine() {
      return scanner.nextLine().trim();
    }
  }

  /**
   * Default output handler using System.out.
   */
  public static class SystemOutputHandler implements OutputHandler {
    @Override
    public void print(String message) {
      System.out.print(message);
    }

    @Override
    public void println(String message) {
      System.out.println(message);
    }
  }

  /**
   * Factory method to create SystemInputProvider.
   * This avoids the "cannot reference before superclass constructor" issue.
   */
  private static SystemInputProvider createSystemInputProvider() {
    return new SystemInputProvider();
  }

  /**
   * Factory method to create SystemOutputHandler.
   * This avoids the "cannot reference before superclass constructor" issue.
   */
  private static SystemOutputHandler createSystemOutputHandler() {
    return new SystemOutputHandler();
  }
}