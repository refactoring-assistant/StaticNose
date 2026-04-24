package calendarview;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * A console-based implementation of the {@link CalendarView} interface.
 *
 * <p>This class prints all output directly to {@code System.out}
 * and {@code System.err}.</p>
 */
public class ConsoleView implements CalendarView {

  private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  /**
   * {@inheritDoc}
   *
   * <p>Prints the message to {@code System.out}.</p>
   */
  @Override
  public void displayMessage(String message) {
    System.out.println(message);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Prints the error message to {@code System.err}.</p>
   */
  @Override
  public void displayError(String errorMessage) {
    System.err.println(errorMessage);
  }

  /**
   * {@inheritDoc}
   *
   * <p>Prints a header with the date, followed by each formatted event string,
   * or a "no events" message.</p>
   */
  @Override
  public void displayEventsOn(LocalDate date, List<String> formattedEvents) {
    System.out.println("Events on " + date.format(dateFormatter) + " : ");
    if (formattedEvents.isEmpty()) {
      System.out.println("No events found or scheduled.");
    } else {
      for (String eventString : formattedEvents) {
        System.out.println(eventString);
      }
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Prints a summary of events found in the time frame, followed
   * by each formatted event string, or a "no events" message.</p>
   */
  @Override
  public void displayEventsFromTo(List<String> formattedEvents) {
    if (formattedEvents.isEmpty()) {
      System.out.println("No events found for the given time frame.");
    } else {
      System.out.println("Event(s): " + formattedEvents.size()
          + " are found for the given time frame.");
      for (String eventString : formattedEvents) {
        System.out.println(eventString);
      }
    }

  }

  /**
   * {@inheritDoc}
   *
   * <p>Prints either "Status is Busy" or "Status is Available".</p>
   */
  @Override
  public void displayBusyStatus(boolean isBusy) {
    if (isBusy) {
      System.out.println("Status is Busy");
    } else {
      System.out.println("Status is Available");
    }
  }

  /**
   * {@inheritDoc}
   *
   * <p>Prints a success message followed by the absolute path to the exported file.</p>
   */
  @Override
  public void displayExportSuccess(String absolutePath) {
    System.out.println("Calendar Exported Successfully to: ");
    System.out.println(absolutePath);
  }
}
