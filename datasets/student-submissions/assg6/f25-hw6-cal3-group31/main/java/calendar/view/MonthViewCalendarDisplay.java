package calendar.view;

import calendar.controller.IntViewRequestHandler;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 * A monthly view of the contents of a calendar.
 */
public class MonthViewCalendarDisplay implements IntCalendarDisplay, ActionListener {
  private static final int DAY_PANEL_COMPONENT_LIMIT = 4;
  private final IntGuiView view;
  private final JPanel calendarGrid = new JPanel();
  private final JLabel dateLabel = new JLabel();
  private final String[] daysOfTheWeek = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
  private final List<JPanel> dayPanels = new ArrayList<>();
  private final Map<Integer, ViewEvent> viewEvents = new HashMap<>();
  private IntViewRequestHandler requestHandler;

  private String calendarName;
  private Month month;
  private int year;

  /**
   * MonthVIewCalendarDisplay constructor.
   *
   * @param view the view to use
   */
  public MonthViewCalendarDisplay(IntGuiView view) {
    this.view = Objects.requireNonNull(view);
    month = LocalDate.now().getMonth();
    year = LocalDate.now().getYear();
  }

  /**
   * Sets the request handler for this display.
   *
   * @param requestHandler the request handler to use
   */
  public void setRequestHandler(IntViewRequestHandler requestHandler) {
    this.requestHandler = requestHandler;
  }

  @Override
  public void setup(Container pane, GridBagConstraints c) {
    setupChangeMonthLeft(pane, c);
    setupChangeMonthRight(pane, c);
    setupDateLabel(pane, c);
    setupCalendarGrid(pane, c);
  }

  @Override
  public void addEvent(ViewEvent event) {
    viewEvents.put(event.getStartDay(), event);

    // Add event to all days it spans across
    LocalDate eventStartDate = LocalDate.of(
        event.getStartYear(), event.getStartMonth(), event.getStartDay());
    LocalDate eventEndDate = LocalDate.of(
        event.getEndYear(), event.getEndMonth(), event.getEndDay());
    LocalDate currentDate = eventStartDate;

    while (!currentDate.isAfter(eventEndDate)) {
      int dayOfMonth = currentDate.getDayOfMonth();
      int monthOfYear = currentDate.getMonthValue();
      int yearOfEvent = currentDate.getYear();

      // Only add to days in the current displayed month
      if (monthOfYear == month.getValue() && yearOfEvent == year
          && dayOfMonth <= dayPanels.size()) {
        JPanel dayPanel = dayPanels.get(dayOfMonth - 1);

        if (dayPanel.getComponentCount() < DAY_PANEL_COMPONENT_LIMIT) {
          dayPanel.add(
              new CalendarDisplayEventPanel(
                  " " + String.format("%02d", event.getStartHour())
                      + ":" + String.format("%02d", event.getStartMinute())
                      + event.getSubject(),
                  event,
                  () -> {
                    if (requestHandler != null) {
                      requestHandler.handleViewEventDetailsRequest(event);
                    }
                  }));
          calendarGrid.revalidate();
          calendarGrid.repaint();
        } else if (dayPanel.getComponentCount() == DAY_PANEL_COMPONENT_LIMIT) {
          dayPanel.add(
              new CalendarDisplayEventPanel("More...", null, null));
          calendarGrid.revalidate();
          calendarGrid.repaint();
        }
      }

      currentDate = currentDate.plusDays(1);
    }
  }

  @Override
  public void setCalendarName(String calendarName) {
    this.calendarName = calendarName;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    switch (e.getActionCommand()) {
      case "Change Month Left":
        int newMonthValue = month.getValue() - 1;
        if (newMonthValue < 1) {
          newMonthValue = 12;
          year -= 1;
        }
        month = Month.of(newMonthValue);
        updateDateLabel(month, year);
        updateCalendarGridContents(calendarName, month, year);
        break;
      case "Change Month Right":
        int newMonthVal = month.getValue() + 1;
        if (newMonthVal > 12) {
          newMonthVal = 1;
          year += 1;
        }
        month = Month.of(newMonthVal);
        updateDateLabel(month, year);
        updateCalendarGridContents(calendarName, month, year);
        break;
      default:
        break;
    }
  }

  @Override
  public void refresh() {
    if (month != null) {
      updateCalendarGridContents(calendarName, month, year);
    }
  }

  @Override
  public void updateCalendarGridContents(String calendarName) {
    updateCalendarGridContents(calendarName, month, year);
  }

  // TODO dynamically size the grid instead of hardcoding 6*7 cells
  //  this will also require informing the eventPanels to allow for more slots
  //  before adding "+ x more"
  private void updateCalendarGridContents(String calendarName, Month month, int year) {
    calendarGrid.removeAll();
    dayPanels.clear();
    viewEvents.clear();

    Border lineBorder = BorderFactory.createLineBorder(Color.BLACK, 2);

    int firstDayInMonth = LocalDate.of(year, month, 1).getDayOfWeek().getValue() % 7;
    int numberOfDaysInMonth = LocalDate.of(year, month, 1).lengthOfMonth();

    List<ViewEvent> events = view.getEventsInRange(calendarName,
        year, month.getValue(), 1, year, month.getValue(), numberOfDaysInMonth);

    int dayCounter = 1;
    for (int i = 0; i < 6 * 7; i++) {
      JPanel dayPanel = new JPanel();
      dayPanel.setLayout(new BoxLayout(dayPanel, BoxLayout.Y_AXIS));
      dayPanel.setBorder(lineBorder);

      if (i < 7) {
        JLabel dayWordLabel = new JLabel(daysOfTheWeek[i]);
        dayWordLabel.setFont(dayWordLabel.getFont().deriveFont(16f));
        dayWordLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        dayPanel.add(dayWordLabel);
      }

      if (i >= firstDayInMonth && dayCounter <= numberOfDaysInMonth) {
        JLabel dayNumberLabel = new JLabel(String.valueOf(dayCounter));
        dayNumberLabel.setFont(dayNumberLabel.getFont().deriveFont(16f));
        dayNumberLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        dayPanel.add(dayNumberLabel);

        // Add events for this day
        int eventsAddedCount = 0;
        for (ViewEvent event : events) {
          // Check if event spans across this day (from start date to end date)
          boolean eventSpansThisDay = isEventOnDay(event, dayCounter, month.getValue(), year);

          if (eventSpansThisDay) {
            viewEvents.put(event.getStartDay(), event);

            if (eventsAddedCount < DAY_PANEL_COMPONENT_LIMIT) {
              dayPanel.add(
                  new CalendarDisplayEventPanel(
                      String.format("%02d", event.getStartHour())
                          + ":" + String.format("%02d", event.getStartMinute())
                          + " " + event.getSubject(),
                      event,
                      () -> {
                        if (requestHandler != null) {
                          requestHandler.handleViewEventDetailsRequest(event);
                        }
                      }));
              eventsAddedCount++;
            } else if (eventsAddedCount == DAY_PANEL_COMPONENT_LIMIT) {
              dayPanel.add(new CalendarDisplayEventPanel(
                  "More...", null, null));
              eventsAddedCount++;
            }
          }
        }

        dayPanels.add(dayPanel);
        dayCounter++;
      } else {
        dayPanel.setBackground(Color.LIGHT_GRAY);
      }

      calendarGrid.add(dayPanel);
    }
    calendarGrid.revalidate();
    calendarGrid.repaint();
  }

  private void setupDateLabel(Container pane, GridBagConstraints c) {
    month = LocalDate.now().getMonth();
    year = LocalDate.now().getYear();

    dateLabel.setText((month) + " " + (year));
    dateLabel.setFont(dateLabel.getFont().deriveFont(24f));

    c.gridx = 3;
    c.gridy = 0;
    pane.add(dateLabel, c);
  }

  private void setupChangeMonthLeft(Container pane, GridBagConstraints c) {
    JButton changeMonthLeftButton = new JButton("<");
    changeMonthLeftButton.setActionCommand("Change Month Left");
    changeMonthLeftButton.addActionListener(this);
    c.gridx = 1;
    c.gridy = 0;
    pane.add(changeMonthLeftButton, c);
  }

  private void setupChangeMonthRight(Container pane, GridBagConstraints c) {
    JButton changeMonthRightButton = new JButton(">");
    changeMonthRightButton.setActionCommand("Change Month Right");
    changeMonthRightButton.addActionListener(this);
    c.gridx = 2;
    c.gridy = 0;
    pane.add(changeMonthRightButton, c);
  }

  private void setupCalendarGrid(Container pane, GridBagConstraints c) {
    calendarGrid.setLayout(new GridLayout(6, 7));
    calendarGrid.setBorder(new EmptyBorder(10, 10, 10, 10));

    month = LocalDate.now().getMonth();
    year = LocalDate.now().getYear();

    updateCalendarGridContents(calendarName, month, year);

    c.insets = new Insets(0, 0, 0, 0);
    c.fill = GridBagConstraints.BOTH;
    c.weightx = 1.0;
    c.weighty = 1.0;
    c.gridx = 1;
    c.gridwidth = 7;
    c.gridheight = 5;
    c.gridy = 1;
    pane.add(calendarGrid, c);
  }

  private void updateDateLabel(Month month, int year) {
    dateLabel.setText((month) + " " + year);
  }

  /**
   * Checks if an event spans across a given day.
   * An event spans a day if the day falls between the event's start and end dates (inclusive).
   *
   * @param event the event to check
   * @param day the day of the month to check
   * @param month the month to check
   * @param year the year to check
   * @return true if the event spans this day, false otherwise
   */
  private boolean isEventOnDay(ViewEvent event, int day, int month, int year) {
    // Create LocalDate objects for comparison
    LocalDate eventStartDate = LocalDate.of(
        event.getStartYear(), event.getStartMonth(), event.getStartDay());
    LocalDate eventEndDate = LocalDate.of(
        event.getEndYear(), event.getEndMonth(), event.getEndDay());
    LocalDate currentDay = LocalDate.of(year, month, day);

    // Check if current day is between start and end dates (inclusive)
    return !currentDay.isBefore(eventStartDate) && !currentDay.isAfter(eventEndDate);
  }
}
