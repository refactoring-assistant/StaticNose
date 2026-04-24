package messaging;

import java.io.PrintStream;
import java.util.Objects;

/**
 * Default sink that prints info messages to stdout and errors to stderr so CLI modes behave as
 * before.
 */
public final class ConsoleMessageSink implements MessageSink {

  private final PrintStream info;
  private final PrintStream error;

  /**
   * Creates a sink using the given streams (mainly for tests).
   */
  public ConsoleMessageSink(PrintStream info, PrintStream error) {
    this.info = Objects.requireNonNull(info, "info");
    this.error = Objects.requireNonNull(error, "error");
  }

  /**
   * Uses {@link System#out} and {@link System#err}.
   */
  public ConsoleMessageSink() {
    this(System.out, System.out);
  }

  @Override
  public void accept(MessageLevel level, String message) {
    PrintStream target = (level == MessageLevel.ERROR) ? error : info;
    if (message == null) {
      target.println();
    } else {
      target.println(message);
    }
  }
}
