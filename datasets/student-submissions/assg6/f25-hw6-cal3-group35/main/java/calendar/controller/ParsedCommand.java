package calendar.controller;

import java.util.Map;

/** Represents one parsed command from user input. */
public class ParsedCommand {

  /** The kind of command (e.g., create-single, print-on, export). */
  public final String kind;

  /** The key-value pairs of arguments. */
  public final Map<String, String> args;

  /**
   * Creates a ParsedCommand.
   *
   * @param kind command type
   * @param args arguments map
   */
  public ParsedCommand(String kind, Map<String, String> args) {
    this.kind = kind;
    this.args = args;
  }
}
