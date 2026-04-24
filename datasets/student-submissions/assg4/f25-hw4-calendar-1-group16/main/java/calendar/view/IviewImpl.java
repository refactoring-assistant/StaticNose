package calendar.view;

import java.io.IOException;
import java.util.List;

/**
 * Console-based implementation of CalendarView.
 * Display output to {@code System.out} and errors to {@code System.err}.
 */

public class IviewImpl implements Iview {

  private final Appendable out;
  private final Appendable err;

  /**
   * Constructor for the View. It takes in an Appendable out, and an Appendable error.
   *
   * @param out the output that the View submits to
   * @param err potential errors when running application
   */

  public IviewImpl(Appendable out, Appendable err) {
    if (out == null || err == null) {
      throw new IllegalArgumentException("Output and error destinations cannot be null.");
    }
    this.out = out;
    this.err = err;
  }

  @Override
  public void displayMessage(String message) throws IOException {
    out.append(message).append("\n");
  }

  @Override
  public void displayError(String error) throws IOException {
    out.append("ERROR: ").append(error).append("\n");
  }

  @Override
  public void displayOutput(List<String> output) throws IOException {
    for (String line : output) {
      out.append(line).append("\n");
    }
  }
}
