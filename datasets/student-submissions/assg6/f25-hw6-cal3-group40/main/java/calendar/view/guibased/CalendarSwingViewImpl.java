package calendar.view.guibased;

import calendar.controller.UiFeatures;
import calendar.model.CalendarInterface;
import calendar.view.EventViewData;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;

/**
 * Implementation of the SwingView interface using Java Swing.
 */
public class CalendarSwingViewImpl extends JFrame implements SwingView {

  private final JComboBox<String> calendarSelector;
  private final JLabel currentCalendarLabel;
  private final MonthPanel monthPanel;
  private final JButton createEventButton;
  private final JButton createCalendarButton;
  private final JButton editCalendarButton;
  private final JButton prevMonthButton;
  private final JButton nextMonthButton;

  private final JPanel eventListPanel;
  private final JLabel selectedDateLabel;
  private UiFeatures features;
  private LocalDate currentMonth;
  private LocalDate selectedDate;

  private ZoneId currentZone = ZoneId.systemDefault();

  /**
   * Initializes the CalendarSwingViewImpl object.
   */
  public CalendarSwingViewImpl() {
    super("JaCoCoders Calendar");
    setSize(1200, 800);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout());

    currentMonth = LocalDate.now();
    selectedDate = LocalDate.now();

    calendarSelector = new JComboBox<>();
    createCalendarButton = new JButton("New Calendar");
    editCalendarButton = new JButton("Edit Calendar");
    prevMonthButton = new JButton("< Prev Month");
    nextMonthButton = new JButton("Next Month >");
    currentCalendarLabel = new JLabel("No Calendar Selected");
    monthPanel = new MonthPanel(this);
    createEventButton = new JButton("Create Event");
    selectedDateLabel = new JLabel("Select a date");
    eventListPanel = new JPanel();

    add(createTopPanel(), BorderLayout.NORTH);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(monthPanel),
        createRightPanel());
    splitPane.setResizeWeight(0.7);
    add(splitPane, BorderLayout.CENTER);
  }

  /**
   * Creates the top panel containing calendar selection and navigation.
   *
   * @return the top panel
   */
  private JPanel createTopPanel() {
    JPanel calendarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    calendarPanel.add(new JLabel("Select Calendar:"));
    calendarPanel.add(calendarSelector);
    calendarPanel.add(createCalendarButton);
    calendarPanel.add(editCalendarButton);

    JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    navPanel.add(prevMonthButton);
    navPanel.add(currentCalendarLabel);
    navPanel.add(nextMonthButton);

    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.add(calendarPanel, BorderLayout.NORTH);
    topPanel.add(navPanel, BorderLayout.SOUTH);

    return topPanel;
  }

  /**
   * Creates the right panel containing the event list.
   *
   * @return the right panel
   */
  private JPanel createRightPanel() {
    JPanel rightPanel = new JPanel(new BorderLayout());
    selectedDateLabel.setHorizontalAlignment(JLabel.CENTER);
    rightPanel.add(selectedDateLabel, BorderLayout.NORTH);

    eventListPanel.setLayout(new BoxLayout(eventListPanel, BoxLayout.Y_AXIS));
    rightPanel.add(new JScrollPane(eventListPanel), BorderLayout.CENTER);

    rightPanel.add(createEventButton, BorderLayout.SOUTH);

    return rightPanel;
  }

  @Override
  public void refresh() {
    if (features != null) {

      monthPanel.setMonth(currentMonth);
      monthPanel.setSelectedDate(selectedDate);

      LocalDate start = currentMonth.withDayOfMonth(1);
      LocalDate end = currentMonth.withDayOfMonth(currentMonth.lengthOfMonth());
      features.getEventsBetween(start.atStartOfDay(), end.atTime(23, 59));

      if (selectedDate != null) {
        features.getEventsOn(selectedDate);
      }

      String name = (String) calendarSelector.getSelectedItem();
      if (name != null) {
        currentCalendarLabel
            .setText(
                "Calendar: " + name + " (" + currentZone.getId() + ") | " + currentMonth.getMonth()
                    + " " + currentMonth.getYear());
      }
    }
    repaint();
  }

  @Override
  public void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.WARNING_MESSAGE);
  }

  @Override
  public void addFeatures(UiFeatures features) {
    this.features = features;

    calendarSelector.addActionListener(e -> {
      String selected = (String) calendarSelector.getSelectedItem();
      if (selected != null) {
        this.features.selectCalendar(selected);
      }
    });

    createCalendarButton.addActionListener(e -> showCreateCalendarDialog());
    editCalendarButton.addActionListener(e -> showEditCalendarDialog());

    prevMonthButton.addActionListener(e -> {
      currentMonth = currentMonth.minusMonths(1);
      refresh();
    });

    nextMonthButton.addActionListener(e -> {
      currentMonth = currentMonth.plusMonths(1);
      refresh();
    });

    createEventButton.addActionListener(e -> showCreateEventDialog(selectedDate));
  }

  @Override
  public void display() {
    setVisible(true);
    if (features != null) {
      features.listCalendars();
      features.getCurrentCalendarName();
    }
  }

  @Override
  public void updateCalendarList(List<CalendarInterface> calendars) {
    calendarSelector.removeAllItems();
    for (CalendarInterface cal : calendars) {
      calendarSelector.addItem(cal.getName());
    }
  }

  @Override
  public void showEventsForDay(LocalDate date, List<EventViewData> events) {
    monthPanel.setEventsForDay(date, events);
    if (date.equals(selectedDate)) {
      updateEventList(date, events);
    }
  }

  /**
   * Updates the view for event list for the specified date.
   *
   * @param date The date of which the events are being displayed.
   * @param events The events to be displayed to the user.
   */
  public void updateEventList(LocalDate date, List<EventViewData> events) {
    this.selectedDate = date;
    monthPanel.setSelectedDate(date);
    selectedDateLabel.setText("Events for: " + date.format(DateTimeFormatter.ISO_DATE));

    eventListPanel.removeAll();

    if (events != null && !events.isEmpty()) {
      for (EventViewData event : events) {
        eventListPanel.add(createEventRow(event));
        eventListPanel.add(new JSeparator());
      }

      eventListPanel.add(Box.createVerticalGlue());
    } else {
      eventListPanel.add(new JLabel("No events"));
    }

    eventListPanel.revalidate();
    eventListPanel.repaint();
  }

  /**
   * Creates a UI row for a single event.
   *
   * @param event the event data
   * @return the panel representing the event row
   */
  private JPanel createEventRow(EventViewData event) {
    JPanel eventRow = new JPanel(new BorderLayout());
    eventRow.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    eventRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

    LocalDateTime localStart = LocalDateTime.ofInstant(event.getStart(), currentZone);
    LocalDateTime localEnd = LocalDateTime.ofInstant(event.getEnd(), currentZone);

    String startStr = localStart.format(DateTimeFormatter.ofPattern("HH:mm"));
    String endStr = localEnd.format(DateTimeFormatter.ofPattern("HH:mm"));

    StringBuilder details = new StringBuilder();
    details.append("<html><b>").append(event.getSubject()).append("</b>");
    details.append("<br>Time: ").append(startStr).append(" - ").append(endStr);
    if (event.getLocation() != null && !event.getLocation().isEmpty()) {
      details.append("<br>Location: ").append(event.getLocation());
    }
    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      details.append("<br>Description: ").append(event.getDescription());
    }
    if (event.isPrivate()) {
      details.append("<br><i>Private</i>");
    }
    details.append("</html>");

    JLabel eventLabel = new JLabel(details.toString());
    eventLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));

    JButton editButton = new JButton("Edit");
    editButton.addActionListener(e -> showEditEventDialog(event));

    JPanel buttonPanel = new JPanel(new BorderLayout());
    buttonPanel.add(editButton, BorderLayout.NORTH);

    eventRow.add(eventLabel, BorderLayout.CENTER);
    eventRow.add(buttonPanel, BorderLayout.EAST);

    eventRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, eventRow.getPreferredSize().height));

    return eventRow;
  }

  @Override
  public void setCurrentCalendar(String name) {
    currentCalendarLabel
        .setText(
            "Calendar: " + name + " (" + currentZone.getId() + ") | " + currentMonth.getMonth()
                + " " + currentMonth.getYear());
    calendarSelector.setSelectedItem(name);
  }

  @Override
  public void setTimezone(ZoneId zone) {
    this.currentZone = zone;
  }

  private void showCreateCalendarDialog() {
    CreateCalendarDialog dialog = new CreateCalendarDialog(this, features);
    dialog.setVisible(true);
  }

  /**
   * Displays the create event dialog to the user.
   *
   * @param date The date on which the event needs to be created.
   */
  public void showCreateEventDialog(LocalDate date) {
    EventDialog dialog = new EventDialog(this, features, date, currentZone);
    dialog.setVisible(true);
  }

  /**
   * Displays the edit event dialog to the user.
   *
   * @param event The event that needs to be edited.
   */
  public void showEditEventDialog(EventViewData event) {
    EventDialog dialog = new EventDialog(this, features, event, currentZone);
    dialog.setVisible(true);
  }

  private void showEditCalendarDialog() {
    String currentName = (String) calendarSelector.getSelectedItem();
    if (currentName == null) {
      return;
    }

    EditCalendarDialog dialog = new EditCalendarDialog(this, features, currentName, currentZone);
    dialog.setVisible(true);
  }
}
