package calendar.view;

import java.io.IOException;
import java.util.Objects;

/**
 * Represents the view component for displaying output to the user.
 */
public class View implements IntView {
  private final Appendable out;

  /**
   * Constructs a View with the given output Appendable.
   *
   * @param out the output Appendable
   * @throws IllegalArgumentException if out is null
   */
  public View(Appendable out) {
    this.out = Objects.requireNonNull(out);
  }

  /**
   * Writes a message to the output.
   *
   * @param message the message to write
   * @throws IllegalStateException if writing fails
   */
  @Override
  public void write(String message) throws IllegalStateException {
    try {
      this.out.append(message);
    } catch (IOException e) {
      throw new IllegalStateException("Could not write to output Appendable");
    }
  }

  /**
   * Writes a message to the output with a new line at the end.
   *
   * @param message the message to write
   * @throws IllegalStateException if writing fails
   */
  @Override
  public void writeln(String message) throws IllegalStateException {
    write(message + System.lineSeparator());
  }
}

