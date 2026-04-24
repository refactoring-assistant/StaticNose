package calendar.view.gui;

import calendar.controller.GuiController;
import calendar.model.Event;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Month view panel with 7x6 grid of day cells.
 */
public class MonthViewPanel extends JPanel {
  private GuiController controller;
  private DayCell[] dayCells;
  private static final String[] DAY_HEADERS = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

  /**.*/
  public MonthViewPanel(GuiController controller) {
    this.controller = controller;

    setLayout(new BorderLayout(0, 0));
    setBackground(new Color(248, 249, 250));
    setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));

    add(createDayHeaders(), BorderLayout.NORTH);
    add(createDayGrid(), BorderLayout.CENTER);

    refresh();
  }

  private JPanel createDayHeaders() {
    JPanel headerPanel = new JPanel(new GridLayout(1, 7, 3, 3));
    headerPanel.setBackground(new Color(248, 249, 250));
    headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

    for (String day : DAY_HEADERS) {
      JLabel label = new JLabel(day, SwingConstants.CENTER);
      label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
      label.setForeground(new Color(107, 114, 128));
      label.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
      headerPanel.add(label);
    }

    return headerPanel;
  }

  private JPanel createDayGrid() {
    JPanel grid = new JPanel(new GridLayout(6, 7, 3, 3));
    grid.setBackground(new Color(248, 249, 250));

    dayCells = new DayCell[42];

    for (int i = 0; i < 42; i++) {
      DayCell cell = new DayCell(controller);
      dayCells[i] = cell;
      grid.add(cell);
    }

    return grid;
  }

  /**.*/
  public void refresh() {
    LocalDate currentMonth = controller.getCurrentMonth();
    LocalDate today = LocalDate.now();
    LocalDate firstDay = currentMonth.withDayOfMonth(1);

    int firstDayOfWeek = firstDay.getDayOfWeek().getValue();
    if (firstDayOfWeek == 7) {
      firstDayOfWeek = 0;
    }

    LocalDate startDate = firstDay.minusDays(firstDayOfWeek);

    for (int i = 0; i < 42; i++) {
      LocalDate cellDate = startDate.plusDays(i);
      List<Event> events = controller.getEventsForDate(cellDate);
      boolean isCurrentMonth = cellDate.getMonth() == currentMonth.getMonth();
      boolean isToday = cellDate.equals(today);

      dayCells[i].setDate(cellDate, events, isCurrentMonth, isToday);
    }
  }
}