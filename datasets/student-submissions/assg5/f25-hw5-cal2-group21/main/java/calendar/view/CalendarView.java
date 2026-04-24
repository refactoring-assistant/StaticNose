package calendar.view;

import java.util.Scanner;

/**
 * This class implements the view interface.
 */
public class CalendarView implements CalendarViewInterface {

  private final Scanner scanner;

  /**
   * Constructs calendar view.
   */
  public CalendarView() {
    this.scanner = new Scanner(System.in);
  }

  @Override
  public void showError(String errorMessage) {
    System.err.println(errorMessage);
  }

  @Override
  public void showMessage(String message) {
    System.out.println(message);
  }

  @Override
  public String getCommand() {
    System.out.print("Enter command: ");
    return scanner.nextLine();
  }

}
