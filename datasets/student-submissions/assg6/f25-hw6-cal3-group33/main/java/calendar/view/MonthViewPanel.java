package calendar.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Panel displaying a month view calendar grid with day headers and 6×7 day cells.
 */
public class MonthViewPanel extends JPanel {

  /**
   * Day names displayed in header row.
   */
  private static final String[] DAY_NAMES = {
      "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
  };

  /**
   * Color for day name headers.
   */
  private static final Color HEADER_COLOR = new Color(40, 40, 40);

  /**
   * Background color for the entire grid.
   */
  private static final Color GRID_BACKGROUND = Color.WHITE;

  private JPanel headerPanel;
  private JPanel gridPanel;
  private DayPanel[][] dayPanels;

  private YearMonth currentMonth;
  private LocalDate selectedDate;
  private LocalDate today;

  /**
   * Constructs a MonthViewPanel displaying the current month with today selected.
   */
  public MonthViewPanel() {
    this.currentMonth = YearMonth.now();
    this.today = LocalDate.now();
    this.selectedDate = today;

    setLayout(new BorderLayout());
    setBackground(GRID_BACKGROUND);

    initializeHeader();
    initializeGrid();

    add(headerPanel, BorderLayout.NORTH);
    add(gridPanel, BorderLayout.CENTER);

    updateCalendarDisplay();
  }

  /**
   * Initializes the day name header row.
   */
  private void initializeHeader() {
    headerPanel = new JPanel(new GridLayout(1, 7));
    headerPanel.setBackground(GRID_BACKGROUND);
    headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.LIGHT_GRAY));
    headerPanel.setPreferredSize(new Dimension(0, 40));

    for (String dayName : DAY_NAMES) {
      JLabel dayLabel = new JLabel(dayName, SwingConstants.CENTER);
      dayLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
      dayLabel.setForeground(HEADER_COLOR);
      dayLabel.setBackground(GRID_BACKGROUND);
      dayLabel.setOpaque(true);
      headerPanel.add(dayLabel);
    }
  }

  /**
   * Initializes the 6×7 grid of empty day panels.
   */
  private void initializeGrid() {
    gridPanel = new JPanel(new GridLayout(6, 7, 0, 0));
    gridPanel.setBackground(GRID_BACKGROUND);

    dayPanels = new DayPanel[6][7];

    for (int row = 0; row < 6; row++) {
      for (int col = 0; col < 7; col++) {
        DayPanel dayPanel = new DayPanel();
        dayPanels[row][col] = dayPanel;
        gridPanel.add(dayPanel);
      }
    }
  }

  /**
   * Updates the calendar display for the current month.
   * Fills grid with dates, marks current month, today, and selected date.
   */
  public void updateCalendarDisplay() {
    LocalDate firstDayOfMonth = currentMonth.atDay(1);

    int firstDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7;

    LocalDate startDate = firstDayOfMonth.minusDays(firstDayOfWeek);

    int dayCounter = 0;
    for (int row = 0; row < 6; row++) {
      for (int col = 0; col < 7; col++) {
        LocalDate currentDate = startDate.plusDays(dayCounter);
        DayPanel dayPanel = dayPanels[row][col];

        boolean isCurrentMonth = currentDate.getMonth() == currentMonth.getMonth()
            && currentDate.getYear() == currentMonth.getYear();
        boolean isToday = currentDate.equals(today);
        boolean isSelected = currentDate.equals(selectedDate);

        dayPanel.setDate(currentDate, isCurrentMonth, isToday, isSelected);

        dayCounter++;
      }
    }
  }

  /**
   * Navigates to the next month.
   */
  public void nextMonth() {
    currentMonth = currentMonth.plusMonths(1);
    updateCalendarDisplay();
  }

  /**
   * Navigates to the previous month.
   */
  public void previousMonth() {
    currentMonth = currentMonth.minusMonths(1);
    updateCalendarDisplay();
  }

  /**
   * Sets the month to display.
   *
   * @param yearMonth the year and month to display
   */
  public void setMonth(YearMonth yearMonth) {
    this.currentMonth = yearMonth;
    updateCalendarDisplay();
  }

  /**
   * Sets the selected date and updates the display.
   *
   * @param date the date to select
   */
  public void setSelectedDate(LocalDate date) {
    this.selectedDate = date;
    updateCalendarDisplay();
  }

  /**
   * Gets the currently selected date.
   *
   * @return the selected LocalDate
   */
  public LocalDate getSelectedDate() {
    return selectedDate;
  }

  /**
   * Gets the current month being displayed.
   *
   * @return the current YearMonth
   */
  public YearMonth getCurrentMonth() {
    return currentMonth;
  }

  /**
   * Gets all day panels for external access.
   *
   * @return 2D array of DayPanel objects
   */
  public DayPanel[][] getDayPanels() {
    return dayPanels;
  }
}