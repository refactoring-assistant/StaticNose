package calendar.view;

import java.awt.BorderLayout;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Panel that displays the events for a selected day.
 */
public class DayViewPanel extends JPanel {

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("h:mm a");
  private JLabel dayLabel;
  private JTextArea dayEventsArea;
  private LocalDate selectedDate;
  private List<ViewEvent> currentDayEvents;

  /**
   * Creates the day view panel and initializes its components.
   */
  public DayViewPanel() {
    super(new BorderLayout());
    initializeComponents();
    this.currentDayEvents = new ArrayList<>();
  }

  private void initializeComponents() {
    dayLabel = new JLabel("Selected Date: " + LocalDate.now().format(DATE_FORMAT));

    dayEventsArea = new JTextArea(15, 25);
    dayEventsArea.setEditable(false);
    dayEventsArea.setLineWrap(true);
    dayEventsArea.setWrapStyleWord(true);

    add(dayLabel, BorderLayout.NORTH);
    add(new JScrollPane(dayEventsArea), BorderLayout.CENTER);
  }

  /**
   * Updates the view with the given date and its events.
   *
   * @param date   the date to display
   * @param events the events occurring on that date
   */
  public void updateDayView(LocalDate date, List<ViewEvent> events) {
    this.selectedDate = date;
    this.currentDayEvents = events;
    refreshDayView();
  }

  private void refreshDayView() {
    if (dayEventsArea == null) {
      return;
    }

    StringBuilder sb = new StringBuilder()
        .append("Events on ").append(selectedDate.format(DATE_FORMAT))
        .append(System.lineSeparator()).append(System.lineSeparator());

    if (currentDayEvents.isEmpty()) {
      sb.append("No events scheduled.");
    } else {
      currentDayEvents.sort(Comparator.comparing(ViewEvent::getStartDateTime));
      currentDayEvents.forEach(event -> sb.append(formatEvent(event)));
    }

    dayEventsArea.setText(sb.toString());
    dayLabel.setText("Selected Date: " + selectedDate.format(DATE_FORMAT));
  }

  private String formatEvent(ViewEvent event) {
    StringBuilder sb = new StringBuilder();

    sb.append("• ").append("Subject: ").append(event.getSubject()).append(System.lineSeparator());

    if (event.isAllDay()) {
      sb.append("  All Day Event").append(System.lineSeparator());
    } else {
      sb.append("  Time: ")
          .append(formatTime(event.getStartDateTime().toLocalTime()))
          .append(" - ")
          .append(formatTime(event.getEndDateTime().toLocalTime()))
          .append(System.lineSeparator());
    }

    if (event.isSeries()) {
      sb.append("  Event Type: Series").append(System.lineSeparator());
    } else {
      sb.append("  Event Type: Single").append(System.lineSeparator());
    }

    if (!"UNKNOWN".equals(event.getLocation())) {
      sb.append("  Location: ").append(event.getLocation()).append(System.lineSeparator());
    }

    if (!"No description given".equals(event.getDescription())) {
      sb.append("  Description: ").append(event.getDescription()).append(System.lineSeparator());
    }

    return sb.append(System.lineSeparator()).toString();
  }

  /**
   * Helper method to format time in 12-hour format.
   * * @param time The LocalTime object to format.
   *
   * @return A string representation of the time (e.g., "2:30 PM").
   */
  private String formatTime(LocalTime time) {
    return time.format(TIME_FORMAT);
  }
}