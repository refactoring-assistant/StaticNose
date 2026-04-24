package calendar.view;

import calendar.controller.guicontroller.ViewListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Implementation of CalendarGuiView using Java Swing.
 * Provides a month view calendar with event management capabilities.
 * The view only handles UI and emits events through ViewListener interface.
 */
public class CalendarGuiViewImpl extends JFrame implements CalendarGuiView {

  private static final Color[] CALENDAR_COLORS = {
      new Color(173, 216, 230),
      new Color(255, 182, 193),
      new Color(144, 238, 144),
      new Color(255, 218, 185),
      new Color(221, 160, 221),
      new Color(176, 224, 230),
      new Color(255, 228, 181)
  };
  private final List<ViewListener> listeners;
  private final LocalDate currentMonth;
  private final LocalDate selectedDate;
  private final Map<String, Color> calendarColors;
  private int colorIndex;
  private Map<LocalDate, List<ViewEvent>> currentMonthEvents;
  private List<ViewEvent> currentDayEvents;
  private TopPanel topPanel;
  private MonthViewPanel monthViewPanel;
  private DayViewPanel dayViewPanel;
  private BottomPanel bottomPanel;


  /**
   * Constructor for CalendarGuiViewImpl.
   */
  public CalendarGuiViewImpl() {
    this.currentMonth = LocalDate.now();
    this.selectedDate = LocalDate.now();
    this.calendarColors = new HashMap<>();
    this.colorIndex = 0;
    this.listeners = new ArrayList<>();
    this.currentMonthEvents = new HashMap<>();
    this.currentDayEvents = new ArrayList<>();
    initializeGui();
  }

  private void initializeGui() {
    setTitle("Pseudo Calendar");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());

    topPanel = new TopPanel(this, listeners);
    add(topPanel, BorderLayout.NORTH);

    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

    monthViewPanel = new MonthViewPanel();
    monthViewPanel.setDateSelectionCallback(date -> {
      monthViewPanel.updateSelectedDate(date);
      bottomPanel.setSelectedDate(date);
      for (ViewListener listener : listeners) {
        listener.handleDateSelected(date);
      }
      for (ViewListener listener : listeners) {
        listener.handleRefresh();
      }
    });

    centerPanel.add(monthViewPanel, BorderLayout.CENTER);

    dayViewPanel = new DayViewPanel();
    centerPanel.add(dayViewPanel, BorderLayout.EAST);

    add(centerPanel, BorderLayout.CENTER);

    bottomPanel = new BottomPanel(this, listeners);
    add(bottomPanel, BorderLayout.SOUTH);

    setSize(1000, 700);
  }

  @Override
  public void display() {
    setVisible(true);
  }

  @Override
  public void setCalendars(List<String> calendarNames, String activeCalendarName) {

    for (String name : calendarNames) {
      if (!calendarColors.containsKey(name)) {
        calendarColors.put(name, CALENDAR_COLORS[colorIndex % CALENDAR_COLORS.length]);
        colorIndex++;
      }
    }

    topPanel.updateCalendarComboBox(calendarNames, activeCalendarName);

    if (activeCalendarName != null) {
      Color calendarColor = calendarColors.getOrDefault(activeCalendarName, Color.WHITE);
      monthViewPanel.setCalendarColor(calendarColor);
    }
  }

  @Override
  public void setMonthEvents(LocalDate month, Map<LocalDate, List<ViewEvent>> events) {
    LocalDate topMonth = topPanel.getCurrentMonth();
    if (topMonth != null && topMonth.getYear() == month.getYear()
        && topMonth.getMonth() == month.getMonth()) {
      this.currentMonthEvents = events;
      monthViewPanel.updateMonthView(month, events);
      monthViewPanel.updateSelectedDate(getSelectedDate());
      if (bottomPanel != null) {
        bottomPanel.setSelectedDate(getSelectedDate());
      }
    }
  }

  @Override
  public void setDayEvents(LocalDate date, List<ViewEvent> events) {
    if (date.equals(getSelectedDate())) {
      this.currentDayEvents = events;
      dayViewPanel.updateDayView(date, events);
      if (bottomPanel != null) {
        bottomPanel.setSelectedDate(date);
        bottomPanel.setDayEvents(events);
      }
    }
  }

  @Override
  public void addViewListener(ViewListener listener) {
    listeners.add(Objects.requireNonNull(listener));
  }

  @Override
  public LocalDate getCurrentMonth() {
    return topPanel.getCurrentMonth();
  }

  @Override
  public LocalDate getSelectedDate() {
    try {
      return monthViewPanel.getSelectedDate();
    } catch (Exception ex) {
      return this.selectedDate;
    }
  }

  @Override
  public void updateTimezone(String timezone) {
    topPanel.updateTimezone(timezone);
  }

  @Override
  public void updateCurrentMonth(LocalDate month) {
    topPanel.updateMonthYear(month);
  }

  @Override
  public void calendarRename(String oldName, String newName) {
    if (calendarColors.containsKey(oldName)) {
      Color oldColor = calendarColors.get(oldName);
      calendarColors.put(newName, oldColor);
      for (ViewListener listener : listeners) {
        listener.handleRefresh();
      }
    }
  }

  @Override
  public void render(String message) {
    JOptionPane.showMessageDialog(this, message, "Message",
        JOptionPane.INFORMATION_MESSAGE);
  }

  @Override
  public void renderError(String message) {
    JOptionPane.showMessageDialog(this, message, "Error",
        JOptionPane.ERROR_MESSAGE);
  }

}
