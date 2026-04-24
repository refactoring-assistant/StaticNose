package calendar.view;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Provides an interactive console-based implementation of CalendarView.
 * Uses standard input and output streams.
 */
public class InteractiveView implements CalendarView {

  private final BufferedReader reader;

  /**
   * Constructs a new interactive view using standard IO.
   */
  public InteractiveView() {
    this.reader = new BufferedReader(new InputStreamReader(System.in));
  }

  @Override
  public void renderMessage(String message) throws IOException {
    System.out.println(message);
  }

  @Override
  public String readInput() throws IOException {
    return reader.readLine();
  }
}
