package view;

import controller.CommandResult;
import java.io.IOException;

/**
 * Interface defining the contract for calendar application views.
 * This interface abstracts the user interface layer, allowing different
 * implementations (console, GUI, web, etc.) to be used interchangeably.
 */
public interface IcalendarView {

  /**
   * Displays a general informational message to the user.
   * This method is used for normal output such as welcome messages,
   * help text, and non-error command responses.
   *
   * @param message the message to display
   * @throws IOException if an I/O error occurs while displaying the message
   */
  void displayMessage(String message) throws IOException;

  /**
   * Displays an error message to the user.
   * Error messages should be visually distinguished from normal messages
   * (e.g., prefixed with "ERROR:", displayed in red, or shown in a separate area).
   *
   * @param error the error message to display
   * @throws IOException if an I/O error occurs while displaying the error
   */
  void displayError(String error) throws IOException;

  /**
   * Displays the result of a command execution.
   * This method examines the CommandResult and delegates to either
   * displayMessage() for successful results or displayError() for failures.
   *
   * @param result the command result containing success status and message
   * @throws IOException if an I/O error occurs while displaying the result
   */
  void displayResult(CommandResult result) throws IOException;

  /**
   * Retrieves input from the user.
   * This method should block until input is available or return an empty
   * string if no input is available (e.g., end of file reached).
   *
   * @return the user's input as a string, trimmed of leading/trailing whitespace,
   *      or an empty string if no input is available
   * @throws IOException if an I/O error occurs while reading input
   */
  String getInput() throws IOException;

  /**
   * Displays the command prompt to indicate the system is ready for input.
   * The prompt helps users understand when they can enter commands and
   * provides visual context for the application state.
   *
   * @throws IOException if an I/O error occurs while displaying the prompt
   */
  void displayPrompt() throws IOException;

  /**
   * Closes the view and releases any associated resources.
   * This method should be called when the view is no longer needed
   * to ensure proper cleanup of resources such as file handles,
   * network connections, or GUI components.
   *
   * <p>After calling this method, the view should not be used again.</p>
   *
   * @throws IOException if an I/O error occurs while closing resources
   */
  void close() throws IOException;
}