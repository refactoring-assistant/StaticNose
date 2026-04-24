package calendar.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Month view panel component displaying the calendar grid.
 */
public class MonthViewPanel extends JPanel {
  private static final String[] DAYS_OF_WEEK = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
  private static final DateTimeFormatter MONTH_YEAR_FORMAT =
      DateTimeFormatter.ofPattern("MMMM yyyy");

  private final JPanel calendarPanel;
  private final Map<String, Color> calendarColors;
  private LocalDate currentMonth;
  private LocalDate selectedDate;
  private Map<LocalDate, List<ViewEvent>> currentMonthEvents;
  private Color currentCalendarColor;
  private Consumer<LocalDate> dateSelectionCallback;

  /**
   * Constructor for MonthViewPanel.
   */
  public MonthViewPanel() {
    super(new BorderLayout());
    this.calendarPanel = new JPanel(new GridLayout(0, 7));
    this.add(calendarPanel, BorderLayout.CENTER);
    this.currentMonthEvents = new HashMap<>();
    this.calendarColors = new HashMap<>();
    this.selectedDate = LocalDate.now();
    this.currentMonth = LocalDate.now();
  }

  /**
   * Sets the calendar color for the active calendar.
   *
   * @param color the calendar color
   */
  public void setCalendarColor(Color color) {
    this.currentCalendarColor = color;
  }

  /**
   * Updates the month view.
   *
   * @param month  the month to display
   * @param events the events for the month
   */
  public void updateMonthView(LocalDate month, Map<LocalDate, List<ViewEvent>> events) {
    this.currentMonth = month;
    this.currentMonthEvents = events;
    refreshMonthView();
  }

  /**
   * Updates the selected date (for highlighting).
   *
   * @param date the newly selected date
   */
  public void updateSelectedDate(LocalDate date) {
    this.selectedDate = date;
    refreshMonthView();
  }

  private void refreshMonthView() {

    this.calendarPanel.removeAll();

    for (String day : DAYS_OF_WEEK) {
      JLabel header = new JLabel(day, SwingConstants.CENTER);
      header.setBorder(BorderFactory.createLineBorder(Color.GRAY));
      header.setOpaque(true);
      header.setBackground(Color.LIGHT_GRAY);
      this.calendarPanel.add(header);
    }

    LocalDate firstDay = currentMonth.withDayOfMonth(1);
    int dayOfWeek = firstDay.getDayOfWeek().getValue() % 7;
    for (int i = 0; i < dayOfWeek; i++) {
      this.calendarPanel.add(new JPanel());
    }

    int noOfDaysInMonth = this.currentMonth.lengthOfMonth();
    Color calendarColor = currentCalendarColor;

    for (int day = 1; day <= noOfDaysInMonth; day++) {
      LocalDate date = currentMonth.withDayOfMonth(day);
      List<ViewEvent> eventList = currentMonthEvents.getOrDefault(date, new ArrayList<>());
      eventList.sort((e1, e2) -> e1.getStartDateTime()
          .compareTo(e2.getStartDateTime()));
      JPanel dayPanel = new DayPanel(date, eventList,
          date.equals(selectedDate), calendarColor, dateSelectionCallback);
      calendarPanel.add(dayPanel);
    }
    calendarPanel.revalidate();
    calendarPanel.repaint();
  }

  /**
   * Setter for date selection callback so container/view can supply a handler.
   */
  public void setDateSelectionCallback(Consumer<LocalDate> callback) {
    this.dateSelectionCallback = callback;
  }

  /**
   * Getter for the selected date shown in the month view.
   */
  public LocalDate getSelectedDate() {
    return this.selectedDate;
  }
}
