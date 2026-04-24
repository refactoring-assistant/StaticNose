package messaging;

import java.util.Objects;

/**
 * Static facade for user-facing messages. Controllers/models route their text through this so the
 * active view can decide what to do with it (print, display dialog, store, etc.).
 */
public final class Messages {

  private static volatile MessageSink sink = new ConsoleMessageSink();

  /**
   * empty constructor.
   */
  private Messages() {
  }

  /**
   * getter for Sink.
   *
   * @return the active sink (mainly for tests)
   */
  public static MessageSink getSink() {
    return sink;
  }

  /**
   * Overrides the current sink. GUI mode can provide a custom sink that captures errors instead of
   * printing them.
   *
   * @param newSink destination for future messages
   */
  public static void setSink(MessageSink newSink) {
    sink = Objects.requireNonNull(newSink, "newSink");
  }

  /**
   * Sends an informational message.
   *
   * @param message text (null prints a blank line)
   */
  public static void info(String message) {
    sink.accept(MessageLevel.INFO, message);
  }

  /**
   * Sends an error message.
   *
   * @param message text (null prints a blank line)
   */
  public static void error(String message) {
    sink.accept(MessageLevel.ERROR, message);
  }
}
