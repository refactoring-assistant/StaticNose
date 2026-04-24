package calendar.view;

/**
 * This is implementation of Calendar View.
 */
public class ConsoleView implements CalendarView {
  @Override
  public void println(String s) {
    System.out.println(s);
  }

  @Override
  public void printPrompt(String prompt) {
    System.out.print(prompt);
  }
}
