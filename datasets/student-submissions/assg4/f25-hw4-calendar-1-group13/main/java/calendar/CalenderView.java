package calendar;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * The Calendar View.
 */
public class CalenderView implements IcalenderView {
  private final PrintStream pout;
  private final PrintStream pout2;

  /**
   * This is a Calendar view Constructor.
   *
   * @param out the stream that needs to be outputted.
   */
  public CalenderView(PrintStream out, PrintStream out1) {
    pout = out;
    pout2 = out1;
  }

  /**
   * Edition of series,event success.
   */
  public void editSucess() {
    pout.println("Edit success\n");
  }

  /**
   * Shows the options to the user.
   */
  public void showOptions() {
    pout2.println("Select an option:");
    pout2.println(
        "1. Add Event \"create event meeting from 2025-10-2412:00 to 2025-10-2512:00\"");
    pout2.println("2. Display event \"print events on 2025-10-23\"");
    pout2.println(
        "3. Edit event \"edit event subject \"Meeting College\" "
            + "from 2025-10-2909:00 to 2025-10-3019:00 with \"Meeting College Modifications\"");
    pout2.println("4. Show status \"show status on 2025-10-3009:00\"");
    pout2.println("5. Export cal \"export csv src/main/java/calendar/validCommands2.csv\"");
    pout2.println("6. exit");
  }


  /**
   * Displays the events to the user.
   *
   * @param dayEvents All the possible events.
   */
  public void showEvents(List<Event> dayEvents) {
    for (Event event : dayEvents) {
      pout.println(event.toBulletFormat());
    }
  }

  /**
   * Shows the format error.
   */
  public void showError() {
    pout.println("Wrong Format");
  }

  /**
   * Shows that event already exists.
   */
  public void eventExistsError() {
    pout.println("Event Already Exists");
  }

  /**
   * The status of the person on a date.
   *
   * @param a true or false from the controller.
   */
  public void isBusy(boolean a) {
    if (a) {
      pout.println("Busy");
    } else {
      pout.println("Not Busy");
    }
  }

  /**
   * Displays the path of the exported csv file.
   *
   * @param s the absolute path to display.
   */
  @Override
  public void displayAbsolutePath(String s) {
    pout.println(s);
  }

  @Override
  public void addEventSucess() {
    pout.println("creation success\n");
  }
}
