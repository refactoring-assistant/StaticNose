package controller;

/**
 * Minimal command processor interface.
 */
public interface CommandProcessor {
  /**
   * this method interpret a String of command.
   *
   * @param command String
   */
  void interpret(String command);
}
