package calendar.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Panel representing a single day cell in the calendar grid.
 * Displays day number and event indicators (dots + count).
 */
public class DayPanel extends JPanel {

  /**
   * Text color for days in the current month.
   */
  private static final Color CURRENT_MONTH_COLOR = Color.BLACK;

  /**
   * Text color for days outside current month (grayed out).
   */
  private static final Color OTHER_MONTH_COLOR = new Color(180, 180, 180);

  /**
   * Background color for today's date.
   */
  private static final Color TODAY_BACKGROUND = new Color(225, 245, 254);

  /**
   * Border color for selected date.
   */
  private static final Color SELECTED_BORDER_COLOR = new Color(66, 133, 244);

  /**
   * Normal background color.
   */
  private static final Color NORMAL_BACKGROUND = Color.WHITE;

  /**
   * Normal border color.
   */
  private static final Color BORDER_COLOR = new Color(230, 230, 230);

  private JLabel dayNumberLabel;
  private JPanel eventIndicatorPanel;

  private LocalDate date;
  private boolean isCurrentMonth;
  private boolean isToday;
  private boolean isSelected;
  private List<Color> eventColors;

  /**
   * Constructs an empty DayPanel.
   */
  public DayPanel() {
    setLayout(new BorderLayout(5, 5));
    setBackground(NORMAL_BACKGROUND);
    setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
    setCursor(new Cursor(Cursor.HAND_CURSOR));

    this.eventColors = new ArrayList<>();

    initializeDayNumberLabel();
    initializeEventIndicatorPanel();

    add(dayNumberLabel, BorderLayout.NORTH);
    add(eventIndicatorPanel, BorderLayout.SOUTH);
  }

  /**
   * Initializes the day number label (top-left corner).
   */
  private void initializeDayNumberLabel() {
    dayNumberLabel = new JLabel();
    dayNumberLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
    dayNumberLabel.setBorder(BorderFactory.createEmptyBorder(5, 8, 0, 0));
    dayNumberLabel.setVerticalAlignment(SwingConstants.TOP);
  }

  /**
   * Initializes the event indicator panel (bottom area).
   */
  private void initializeEventIndicatorPanel() {
    eventIndicatorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
    eventIndicatorPanel.setOpaque(false);
  }

  /**
   * Sets the date and display properties for this day panel.
   *
   * @param date           the date this panel represents
   * @param isCurrentMonth true if date is in the currently displayed month
   * @param isToday        true if this date is today
   * @param isSelected     true if this date is selected by user
   */
  public void setDate(LocalDate date, boolean isCurrentMonth, boolean isToday, boolean isSelected) {
    this.date = date;
    this.isCurrentMonth = isCurrentMonth;
    this.isToday = isToday;
    this.isSelected = isSelected;

    updateDisplay();
  }

  /**
   * Sets the events for this day.
   * Each color represents one event (typically the calendar color).
   *
   * @param eventColors list of colors representing events
   */
  public void setEvents(List<Color> eventColors) {
    this.eventColors = new ArrayList<>(eventColors);
    updateEventIndicators();
  }

  /**
   * Updates the visual display based on current state.
   * Applies: day number, text color, background color, border.
   */
  private void updateDisplay() {
    if (date == null) {
      dayNumberLabel.setText("");
      return;
    }

    dayNumberLabel.setText(String.valueOf(date.getDayOfMonth()));

    if (isCurrentMonth) {
      dayNumberLabel.setForeground(CURRENT_MONTH_COLOR);
    } else {
      dayNumberLabel.setForeground(OTHER_MONTH_COLOR);
    }

    if (isToday) {
      setBackground(TODAY_BACKGROUND);
      eventIndicatorPanel.setOpaque(true);
      eventIndicatorPanel.setBackground(TODAY_BACKGROUND);
    } else {
      setBackground(NORMAL_BACKGROUND);
      eventIndicatorPanel.setOpaque(false);
    }

    if (isSelected) {
      setBorder(BorderFactory.createLineBorder(SELECTED_BORDER_COLOR, 3));
    } else {
      setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
    }
  }

  /**
   * Updates the event indicator display.
   * Shows up to 3 colored dots, then "+N" for additional events.
   */
  private void updateEventIndicators() {
    eventIndicatorPanel.removeAll();

    if (eventColors.isEmpty()) {
      eventIndicatorPanel.revalidate();
      eventIndicatorPanel.repaint();
      return;
    }

    int dotsToShow = Math.min(3, eventColors.size());
    for (int i = 0; i < dotsToShow; i++) {
      JLabel dot = new JLabel("●");
      dot.setForeground(eventColors.get(i));
      dot.setFont(new Font("SansSerif", Font.PLAIN, 12));
      eventIndicatorPanel.add(dot);
    }

    if (eventColors.size() > 3) {
      JLabel countLabel = new JLabel("+" + (eventColors.size() - 3));
      countLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
      countLabel.setForeground(new Color(100, 100, 100));
      eventIndicatorPanel.add(countLabel);
    }

    eventIndicatorPanel.revalidate();
    eventIndicatorPanel.repaint();
  }

  /**
   * Gets the date this panel represents.
   *
   * @return the LocalDate, or null if empty cell
   */
  public LocalDate getDate() {
    return date;
  }

  /**
   * Checks if this panel represents a day in the current month.
   *
   * @return true if in current month
   */
  public boolean isCurrentMonth() {
    return isCurrentMonth;
  }
}