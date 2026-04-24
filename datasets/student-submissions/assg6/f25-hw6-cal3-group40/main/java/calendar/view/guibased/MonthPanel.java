package calendar.view.guibased;

import calendar.view.EventViewData;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * This class represents the Month Panel.
 */
public class MonthPanel extends JPanel {

  private final CalendarSwingViewImpl parentView;
  private final Map<LocalDate, List<EventViewData>> eventsMap;
  private LocalDate currentMonth;
  private LocalDate selectedDate;

  /**
   * Initializes the MonthPanel object.
   *
   * @param parentView The parent view
   */
  public MonthPanel(CalendarSwingViewImpl parentView) {
    this.parentView = parentView;
    this.currentMonth = LocalDate.now();
    this.eventsMap = new HashMap<>();
    this.selectedDate = LocalDate.now();

    setLayout(new GridLayout(0, 7));
  }

  /**
   *  Sets the month.
   *
   * @param month The month value.
   */
  public void setMonth(LocalDate month) {
    this.currentMonth = month;
    this.eventsMap.clear();
    removeAll();
    buildCalendar();
    revalidate();
    repaint();
  }

  /**
   * Sets event fot the given date.
   *
   * @param date The given date.
   * @param events List of events.
   */
  public void setEventsForDay(LocalDate date, List<EventViewData> events) {
    eventsMap.put(date, events);
    removeAll();
    buildCalendar();
    revalidate();
    repaint();
  }

  /**
   * Sets the selected date.
   *
   * @param date The date set the value.
   */
  public void setSelectedDate(LocalDate date) {
    this.selectedDate = date;
    buildCalendar();
  }

  private void buildCalendar() {
    removeAll();

    addHeaders();

    if (currentMonth == null) {
      return;
    }

    YearMonth yearMonth = YearMonth.from(currentMonth);
    LocalDate firstOfMonth = currentMonth.withDayOfMonth(1);
    int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();
    int startDay = (dayOfWeek == 7) ? 0 : dayOfWeek;

    int daysInMonth = yearMonth.lengthOfMonth();

    addEmptyCells(startDay);
    addDayCells(daysInMonth);

    revalidate();
    repaint();
  }

  /**
   * Adds the day of week headers to the panel.
   */
  private void addHeaders() {
    String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    for (String day : days) {
      JLabel header = new JLabel(day, JLabel.CENTER);
      header.setBorder(BorderFactory.createEtchedBorder());
      add(header);
    }
  }

  /**
   * Adds empty cells for days before the first of the month.
   *
   * @param startDay the number of empty cells to add
   */
  private void addEmptyCells(int startDay) {
    for (int i = 0; i < startDay; i++) {
      add(new JPanel());
    }
  }

  /**
   * Adds cells for each day of the month.
   *
   * @param daysInMonth the number of days in the month
   */
  private void addDayCells(int daysInMonth) {
    for (int day = 1; day <= daysInMonth; day++) {
      LocalDate date = currentMonth.withDayOfMonth(day);
      DayCell cell = new DayCell(date);
      if (date.equals(selectedDate)) {
        cell.setBackground(new Color(220, 240, 255));
      }
      add(cell);
    }
  }

  private class DayCell extends JPanel {
    private final LocalDate date;

    public DayCell(LocalDate date) {
      this.date = date;
      this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
      this.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
      this.setBackground(Color.WHITE);

      JLabel dayLabel = new JLabel(String.valueOf(date.getDayOfMonth()));
      this.add(dayLabel);

      List<EventViewData> events = eventsMap.get(date);
      if (events != null) {
        for (EventViewData event : events) {
          JLabel eventLabel = new JLabel(event.getSubject());
          eventLabel.setOpaque(true);
          eventLabel.setBackground(Color.CYAN);
          eventLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
          this.add(eventLabel);
        }
      }

      this.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          parentView.updateEventList(date, eventsMap.get(date));
        }
      });
    }
  }
}
