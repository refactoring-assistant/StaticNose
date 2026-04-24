package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A panel that makes the month view of the selected calendar. It displays the
 * month and year that is currently being viewed, in addition to the correct
 * placement of dates in correspondance to days.
 */
public class CalendarPanel extends JPanel {
  private final Consumer<LocalDate> dayClickHandler;
  JLabel monthLabel;
  JPanel monthPanel;
  private YearMonth currentMonth;

  /**
   * Constructs a CalendarPanel object and initializes the appropriate values to it.
   *
   * @param initialMonth    the month that is displayed (current)
   * @param dayClickHandler a handler to keep track of what day was selected
   */
  public CalendarPanel(YearMonth initialMonth, Consumer<LocalDate> dayClickHandler) {
    this.currentMonth = initialMonth;
    this.dayClickHandler = dayClickHandler;

    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JPanel naviBar = new JPanel(new BorderLayout());

    monthLabel = new JLabel(buildMonthYearHeader(this.currentMonth), JLabel.CENTER);

    JButton prevButton = new JButton("<");
    JButton nextButton = new JButton(">");

    naviBar.add(prevButton, BorderLayout.WEST);
    naviBar.add(monthLabel, BorderLayout.CENTER);
    naviBar.add(nextButton, BorderLayout.EAST);

    monthPanel = new JPanel(new BorderLayout());
    buildSelectMonthGrid();

    prevButton.addActionListener(e -> {
      currentMonth = currentMonth.minusMonths(1);
      monthLabel.setText(buildMonthYearHeader(currentMonth));
      buildSelectMonthGrid();
    });

    nextButton.addActionListener(e -> {
      currentMonth = currentMonth.plusMonths(1);
      monthLabel.setText(buildMonthYearHeader(currentMonth));
      buildSelectMonthGrid();
    });

    add(naviBar, BorderLayout.NORTH);
    add(monthPanel, BorderLayout.CENTER);
  }


  private String buildMonthYearHeader(YearMonth date) {
    return date.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault())
        + " " + date.getYear();
  }

  private void buildSelectMonthGrid() {
    monthPanel.removeAll();

    JPanel grid = new JPanel(new GridLayout(0, 7, 4, 4));

    DayOfWeek[] order = DayOfWeek.values();
    for (DayOfWeek dow : order) {
      JLabel cell = new JLabel(
          dow.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
          JLabel.CENTER
      );
      cell.setOpaque(true);
      cell.setBackground(new Color(230, 230, 230));
      grid.add(cell);
    }

    LocalDate firstDay = currentMonth.atDay(1);
    int leadingEmpties = firstDay.getDayOfWeek().getValue() - 1;
    for (int i = 0; i < leadingEmpties; i++) {
      grid.add(makeDayCell(""));
    }

    for (int day = 1; day <= currentMonth.lengthOfMonth(); day++) {
      grid.add(makeDayCell(String.valueOf(day)));
    }

    monthPanel.add(grid, BorderLayout.CENTER);

    monthPanel.revalidate();
    monthPanel.repaint();
  }

  private JPanel makeDayCell(String text) {
    JPanel cell = new JPanel(new BorderLayout());
    JLabel label = new JLabel(text, JLabel.CENTER);
    cell.setPreferredSize(new Dimension(64, 48));
    cell.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 210)));
    cell.add(label, BorderLayout.CENTER);
    cell.setBackground(Color.WHITE);
    cell.setOpaque(true);

    if (text == null || text.isEmpty()) {
      cell.setBackground(new Color(240, 240, 240));
      cell.setEnabled(false);
      return cell;
    }

    cell.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        try {
          int dayNumber = Integer.parseInt(text);
          LocalDate selected = currentMonth.atDay(dayNumber);
          if (dayClickHandler != null) {
            dayClickHandler.accept(selected);
          }
        } catch (NumberFormatException ignored) {
          // non-day cells should do nothing
        }
      }

      @Override
      public void mouseEntered(java.awt.event.MouseEvent e) {
        cell.setBackground(new Color(245, 245, 245));
      }

      @Override
      public void mouseExited(java.awt.event.MouseEvent e) {
        cell.setBackground(Color.WHITE);
      }
    });

    return cell;
  }
}
