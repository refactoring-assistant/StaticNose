package calendar.view;

import calendar.model.Icalendar;

/**
 * Implements IcalendarView to display messages on the console.
 * Responsible for presenting all user-facing outputs from the controller.
 */
public class CalendarViewImpl implements IcalendarView {

  private Appendable out;

  /**
   * Constructs a CalendarViewImpl that writes output to the given destination.
   *
   * @param out the output destination
   */
  public CalendarViewImpl(Appendable out) {
    this.out = out;
  }

  /**
   * Appends the provided output message to the console or configured output stream.
   *
   * @param output the message or result to display
   * @throws IllegalArgumentException if the output cannot be written
   */
  @Override
  public void showOutput(String output) {
    try {
      this.out.append(output);
    } catch (Exception e) {
      throw new IllegalArgumentException("Something went wrong");
    }
  }
}
