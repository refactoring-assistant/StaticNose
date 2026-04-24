package messaging;

/**
 * Destination for user-facing messages. Implementations decide where texts go (console, buffer,
 * GUI dialog, etc.).
 */
public interface MessageSink {

  /**
   * Accept a message at the given level.
   *
   * @param level   severity of the message
   * @param message user-facing text
   */
  void accept(MessageLevel level, String message);
}
