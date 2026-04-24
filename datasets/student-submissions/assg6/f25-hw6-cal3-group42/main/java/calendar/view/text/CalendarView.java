package calendar.view.text;

import calendar.view.CalendarViewInterface;
import java.io.IOException;

/**
 * Calendar view class which displays the information as described by the view interface.
 */
public class CalendarView implements CalendarViewInterface {

  private final Appendable out;
  private final Appendable err;

  /**
   * Changes the output streams to any other required implementation other than System.
   *
   * @param out normal output stream object
   * @param err error output stream object
   */
  public CalendarView(Appendable out, Appendable err) {
    this.out = out;
    this.err = err;
  }

  /**
   * Default constructor for View which will set the output streams as System object.
   */
  public CalendarView() {
    this.out = System.out;
    this.err = System.err;
  }

  @Override
  public void display(String message) {
    try {
      this.out.append(message).append(System.lineSeparator());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void displayError(String message) {
    try {
      this.err.append(message).append(System.lineSeparator());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
