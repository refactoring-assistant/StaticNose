package calendar.view;

import java.io.IOException;

/**
 * Implementation of Calendar View.
 * Used to display the output to a command line.
 */
public class CalendarViewImpl implements CalendarView {

  private final Appendable out;


  /**
   * Constructor CLI view. Passing an Appendable as input.
   *
   * @param out contains the value to be displayed or parsed to the view.
   */
  public CalendarViewImpl(Appendable out) {
    if (out == null) {
      throw new IllegalArgumentException(
          "Error: view cannot be null or print stream cannot be null.");
    }
    this.out = out;
  }

  /**
   * View constructor without the enforced input.
   */
  public CalendarViewImpl() {
    this.out = System.out;
  }

  private void write(String message) {
    try {
      out.append(message).append(System.lineSeparator());
    } catch (IOException e) {
      throw new IllegalStateException("Error: Cannot write to output stream.", e);
    }
  }

  @Override
  public void render(String message) {
    write(message);
  }

  @Override
  public void renderError(String message) {
    write("!!***" + message + "***!!");
  }
}
