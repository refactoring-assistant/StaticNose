package calendar.view;

import calendar.controller.IntViewRequestHandler;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * A GUI implementation of the IntView interface for a calendar application.
 * This class sets up the main window and its components using Java Swing.
 * Does not directly access model classes to maintain MVC separation.
 */
public class GuiView extends JFrame implements IntGuiView {
  private DefaultListModel<String> calendarsListModel;
  private IntViewRequestHandler requestHandler;
  private final MyCalendarsScrollPane myCalendarsScrollPane;
  private IntCalendarDisplay calendarDisplay;
  private String calendarName;

  /**
   * Constructs a new GuiView instance, initializing the main window and its components.
   */
  public GuiView() {
    super("Calendar Application");
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setExtendedState(JFrame.MAXIMIZED_BOTH);
    this.myCalendarsScrollPane = new MyCalendarsScrollPane(this);
    this.calendarDisplay = new MonthViewCalendarDisplay(this);

  }

  @Override
  public void go() {
    Container pane = this.getContentPane();
    pane.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(10, 10, 10, 10);

    setupCreateEventButton(pane, c);
    setupCreateCalendarButton(pane, c);
    calendarDisplay.setup(pane, c);
    setupMyCalendarLabel(pane, c);
    setupMyCalendarsScrollPane(pane, c);

    this.pack();
    this.setVisible(true);
  }

  @Override
  public void createMessagePopup(Object message, String title, int messageType) {
    JOptionPane.showMessageDialog(
        null, message, title, messageType);
  }

  @Override
  public void addCalendar(String calendarName) {
    myCalendarsScrollPane.addCalendar(calendarName);
    if (this.calendarName == null) {
      changeCalendar(calendarName);
    }
  }

  @Override
  public void changeCalendar(String calendarName) {
    this.calendarName = calendarName;
    calendarDisplay.setCalendarName(calendarName);
    calendarDisplay.updateCalendarGridContents(calendarName);
  }

  @Override
  public void refreshCalendarDisplay() {
    if (calendarDisplay != null) {
      calendarDisplay.refresh();
    }
  }

  private void setupMyCalendarsScrollPane(Container pane, GridBagConstraints c) {
    c.insets = new Insets(0, 10, 10, 10);
    c.weightx = 0.0;
    c.fill = GridBagConstraints.BOTH;
    c.gridx = 0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.gridy = 3;
    pane.add(myCalendarsScrollPane, c);
  }

  private void setupCreateCalendarButton(Container pane, GridBagConstraints c) {
    JButton createCalendarButton = new JButton("Create Calendar");
    createCalendarButton.addActionListener(e -> handleCreateCalendar());
    c.gridx = 0;
    c.gridy = 1;
    pane.add(createCalendarButton, c);
  }

  private void setupCreateEventButton(Container pane, GridBagConstraints c) {
    JButton createEventButton = new JButton("Create Event");
    createEventButton.addActionListener(e -> handleCreateEvent());

    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridx = 0;
    c.gridy = 0;
    pane.add(createEventButton, c);
  }

  private void setupMyCalendarLabel(Container pane, GridBagConstraints c) {
    JLabel myCalendarsLabel = new JLabel("My Calendars");
    myCalendarsLabel.setFont(myCalendarsLabel.getFont().deriveFont(16f));

    c.insets = new Insets(10, 10, 10, 10);
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 0.0;
    c.weighty = 0.0;
    c.gridwidth = 1;
    c.gridheight = 1;
    c.gridx = 0;
    c.gridy = 2;
    pane.add(myCalendarsLabel, c);
  }

  @Override
  public List<ViewEvent> getEventsInRange(String calendarName,
                                          int startYear, int startMonth, int startDay,
                                          int endYear, int endMonth, int endDay) {

    return requestHandler.handleGetViewEventsInRange(calendarName,
        startYear, startMonth, startDay,
        endYear, endMonth, endDay);
  }

  @Override
  public void setRequestHandler(IntViewRequestHandler requestHandler) {
    this.requestHandler = requestHandler;
    // Pass the request handler to the calendar display if it supports it
    if (calendarDisplay instanceof MonthViewCalendarDisplay) {
      ((MonthViewCalendarDisplay) calendarDisplay).setRequestHandler(requestHandler);
    }
  }

  @Override
  public IntViewRequestHandler getRequestHandler() {
    return requestHandler;
  }

  /**
   * Handles the create calendar button click.
   * Delegates to the controller to show dialog and create calendar.
   */
  private void handleCreateCalendar() {
    requestHandler.handleCreateCalendarRequest();
  }

  /**
   * Handles the create event button click.
   * Delegates to the controller to show dialog and create event.
   */
  private void handleCreateEvent() {
    // Use today's date as default
    java.time.LocalDate today = java.time.LocalDate.now();
    requestHandler.handleCreateEventRequest(
        today.getYear(), today.getMonthValue(), today.getDayOfMonth());
  }

}