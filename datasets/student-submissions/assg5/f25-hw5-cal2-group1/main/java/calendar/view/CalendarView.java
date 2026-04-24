package calendar.view;

import java.io.PrintStream;

/**
 * Text-based view for the calendar application.
 */
public class CalendarView implements IcalendarView {
  private final PrintStream out;

  /**
   * Creates a new calendar view.
   */
  public CalendarView(PrintStream out) {
    this.out = out;
  }

  @Override
  public void displayMessage(String message) {
    out.println(message);
  }

  @Override
  public void displayError(String error) {
    out.println("Error: " + error);
  }
}