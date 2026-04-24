package calendar.view.gui;

import calendar.controller.gui.Features;
import calendar.model.event.CalendarEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Swing-based GUI implementation of the calendar view.
 * Displays a month view with day cells showing events.
 */
public class CalendarGuiViewImpl extends JFrame implements CalendarGuiView {

  private Features features;
  private YearMonth currentMonth;
  private JPanel calendarGridPanel;
  private JLabel monthYearLabel;
  private JLabel calendarNameLabel;
  private JLabel calendarTimezoneLabel;
  private JComboBox<String> calendarSelector;
  private final Map<String, Color> calendarColors;
  private int colorIndex = 0;
  private Color currentColor;

  private static final Color[] CALENDAR_COLOR_PALETTE = {
      new Color(59, 130, 246),
      new Color(34, 197, 94),
      new Color(249, 115, 22),
      new Color(168, 85, 247),
      new Color(236, 72, 153),
      new Color(20, 184, 166),
  };

  /**
   * Initialize the view implementation, set the current month and colors, and start UI build.
   */
  public CalendarGuiViewImpl() {
    this.currentMonth = YearMonth.now();
    this.calendarColors = new HashMap<>();
    initializeUi();
  }

  /**
   * Start UI build. Main, top, side and calendar panels created.
   */
  private void initializeUi() {
    setTitle("Calendar Application");
    setSize(1000, 700);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLayout(new BorderLayout(10, 10));

    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

    mainPanel.add(createTopPanel(), BorderLayout.NORTH);
    mainPanel.add(createCalendarPanel(), BorderLayout.CENTER);
    mainPanel.add(createSidebarPanel(), BorderLayout.EAST);

    add(mainPanel);
  }

  /**
   * Top panel showing main info and the info panel for calendar navigation created.
   *
   * @return the top panel to be added to the main panel
   */
  private JPanel createTopPanel() {
    JPanel calendarInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    calendarNameLabel = new JLabel("Default Calendar");
    calendarNameLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
    calendarInfoPanel.add(calendarNameLabel);
    calendarTimezoneLabel = new JLabel("(America/New_York)");
    calendarTimezoneLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
    calendarInfoPanel.add(calendarTimezoneLabel);

    calendarSelector = new JComboBox<>();
    calendarSelector.addActionListener(e -> {
      String selected = (String) calendarSelector.getSelectedItem();
      if (selected != null && features != null) {
        features.switchCalendar(selected);
        updateCalendarDisplay();
      }
    });
    calendarInfoPanel.add(new JLabel("  Switch to: "));
    calendarInfoPanel.add(calendarSelector);

    JButton prevButton = new JButton("< Previous");
    prevButton.addActionListener(e -> navigateMonth(-1));

    monthYearLabel = new JLabel();
    monthYearLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    updateMonthYearLabel();

    JButton nextButton = new JButton("Next >");
    nextButton.addActionListener(e -> navigateMonth(1));

    JButton todayButton = new JButton("Today");
    todayButton.addActionListener(e -> {
      currentMonth = YearMonth.now();
      refreshCalendarView();
    });

    JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    navPanel.add(prevButton);
    navPanel.add(monthYearLabel);
    navPanel.add(nextButton);
    navPanel.add(todayButton);

    JPanel topPanel = new JPanel(new BorderLayout(10, 10));
    topPanel.add(calendarInfoPanel, BorderLayout.WEST);
    topPanel.add(navPanel, BorderLayout.CENTER);

    currentColor = getCurrentColor("Default Calendar");

    return topPanel;
  }

  /**
   * Calendar panel created which includes the grid layout for calendar days.
   *
   * @return the calendar panel to be added to main panel
   */
  private JPanel createCalendarPanel() {
    JPanel headerPanel = new JPanel(new GridLayout(1, 7));
    String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    for (String day : dayNames) {
      JLabel label = new JLabel(day, SwingConstants.CENTER);
      label.setFont(new Font("SansSerif", Font.BOLD, 12));
      label.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Color.GRAY));
      headerPanel.add(label);
    }

    calendarGridPanel = new JPanel(new GridLayout(6, 7, 2, 2));
    calendarGridPanel.setBackground(currentColor);

    JPanel container = new JPanel(new BorderLayout());
    container.add(headerPanel, BorderLayout.NORTH);
    container.add(calendarGridPanel, BorderLayout.CENTER);

    return container;
  }

  /**
   * Sidebar containing all the calendar actions.
   *
   * @return the sidebar panel to be added to the main panel
   */
  private JPanel createSidebarPanel() {
    JPanel sidebar = new JPanel();
    sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
    sidebar.setBorder(new EmptyBorder(0, 10, 0, 0));
    sidebar.setPreferredSize(new Dimension(200, 0));

    JLabel actionsLabel = new JLabel("Actions");
    actionsLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
    actionsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    sidebar.add(actionsLabel);
    sidebar.add(Box.createRigidArea(new Dimension(0, 10)));

    modifyingButtons(sidebar);
    JButton editEventsBtn = createSidebarButton("Edit Events");
    editEventsBtn.addActionListener(e -> showEditEventsDialog());
    sidebar.add(editEventsBtn);
    sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
    viewingButtons(sidebar);
    miscButtons(sidebar);

    sidebar.add(Box.createVerticalGlue());

    return sidebar;
  }

  /**
   * Creating and editing buttons in the sidebar.
   *
   * @param sidebar the sidebar panel
   */
  private void modifyingButtons(JPanel sidebar) {
    JButton createCalBtn = createSidebarButton("New Calendar");
    createCalBtn.addActionListener(e -> showCreateCalendarDialog());
    sidebar.add(createCalBtn);
    sidebar.add(Box.createRigidArea(new Dimension(0, 5)));

    JButton createEventBtn = createSidebarButton("Create Event");
    createEventBtn.addActionListener(e -> showCreateEventDialog(null));
    sidebar.add(createEventBtn);
    sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
  }

  /**
   * Viewing and exporting buttons in the sidebar.
   *
   * @param sidebar the sidebar panel
   */
  private void viewingButtons(JPanel sidebar) {
    JButton viewAllBtn = createSidebarButton("View All Events");
    viewAllBtn.addActionListener(e -> showAllEventsDialog());
    sidebar.add(viewAllBtn);
    sidebar.add(Box.createRigidArea(new Dimension(0, 5)));

    JButton exportBtn = createSidebarButton("Export Calendar");
    exportBtn.addActionListener(e -> showExportDialog());
    sidebar.add(exportBtn);
    sidebar.add(Box.createRigidArea(new Dimension(0, 5)));
  }

  /**
   * Copy events between, edit calendar and exit buttons.
   *
   * @param sidebar the sidebar panel
   */
  private void miscButtons(JPanel sidebar) {
    JButton copyEventsBtn = createSidebarButton("Copy Events");
    copyEventsBtn.addActionListener(e -> showCopyEventsInRangeDialog());
    sidebar.add(copyEventsBtn);
    sidebar.add(Box.createRigidArea(new Dimension(0, 5)));

    JButton editCalBtn = createSidebarButton("Edit Calendar");
    editCalBtn.addActionListener(e -> showEditCalendarDialog());
    sidebar.add(editCalBtn);
    sidebar.add(Box.createRigidArea(new Dimension(0, 20)));

    JButton exitBtn = createSidebarButton("Exit");
    exitBtn.addActionListener(e -> {
      if (showConfirmation("Are you sure you want to exit?")) {
        System.exit(0);
      }
    });
    sidebar.add(exitBtn);
  }

  /**
   * Create button method to add a button to the sidebar with a specific dimension and alignment.
   *
   * @param text the text displayed on the button
   * @return the button once created
   */
  private JButton createSidebarButton(String text) {
    JButton button = new JButton(text);
    button.setAlignmentX(Component.LEFT_ALIGNMENT);
    button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
    return button;
  }

  /**
   * Add navigate functionality to the calendar to move between months.
   *
   * @param direction +1 for next month and -1 for previous month
   */
  private void navigateMonth(int direction) {
    currentMonth = currentMonth.plusMonths(direction);
    refreshCalendarView();
  }

  /**
   * Update the month and year display once calendar month is changed.
   */
  private void updateMonthYearLabel() {
    String monthName = currentMonth.getMonth().getDisplayName(
        TextStyle.FULL, Locale.getDefault());
    monthYearLabel.setText(monthName + " " + currentMonth.getYear());
  }

  private Color getCurrentColor(String calendarName) {
    return calendarColors.computeIfAbsent(calendarName,
        k -> CALENDAR_COLOR_PALETTE[colorIndex++ % CALENDAR_COLOR_PALETTE.length]);
  }

  /**
   * Update the display for the calendar, choose the current calendar to show along with the
   * color from the colors list.
   */
  private void updateCalendarDisplay() {
    if (features == null) {
      return;
    }

    String calendarName = features.getCurrentCalendarName();
    calendarNameLabel.setText(calendarName);
    currentColor = getCurrentColor(calendarName);
    calendarNameLabel.setForeground(currentColor);

    String calendarTimezone = features.getCurrentCalendarTimezone();
    calendarTimezoneLabel.setText("(" + calendarTimezone + ")");

    refreshCalendarView();
  }

  @Override
  public void setFeatures(Features features) {
    this.features = features;
    updateCalendarList();
    updateCalendarDisplay();
  }

  @Override
  public void displayGui() {
    setVisible(true);
  }

  @Override
  public void updateCalendarList() {
    if (features == null) {
      return;
    }

    calendarSelector.removeAllItems();
    List<String> calendarNames = features.getCalendarNames();
    for (String name : calendarNames) {
      calendarSelector.addItem(name);
    }

    String currentCalendar = features.getCurrentCalendarName();
    calendarSelector.setSelectedItem(currentCalendar);
  }

  @Override
  public void display(String message) {
    System.out.println(message);
  }

  @Override
  public void displayError(String message) {
    showError(message);
  }

  @Override
  public void showMessage(String message) {
    JOptionPane.showMessageDialog(this, message, "Information",
        JOptionPane.INFORMATION_MESSAGE);
  }

  @Override
  public void showError(String errorMessage) {
    JOptionPane.showMessageDialog(this, errorMessage, "Error",
        JOptionPane.ERROR_MESSAGE);
  }

  @Override
  public boolean showConfirmation(String message) {
    int result = JOptionPane.showConfirmDialog(this, message, "Confirm",
        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    return result == JOptionPane.YES_OPTION;
  }

  @Override
  public void refreshCalendarView() {
    if (features == null) {
      return;
    }

    calendarGridPanel.removeAll();
    updateMonthYearLabel();

    LocalDate firstOfMonth = currentMonth.atDay(1);
    int daysInMonth = currentMonth.lengthOfMonth();
    int startDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7;

    LocalDate monthStart = currentMonth.atDay(1);
    LocalDate monthEnd = currentMonth.atEndOfMonth();
    List<CalendarEvent> monthEvents = features.getEventsInRange(monthStart, monthEnd);

    Map<LocalDate, List<CalendarEvent>> eventsByDate = new HashMap<>();
    for (CalendarEvent event : monthEvents) {
      LocalDate eventDate = event.getStartDateTime().toLocalDate();
      eventsByDate.computeIfAbsent(eventDate, k -> new ArrayList<>()).add(event);
    }

    for (int i = 0; i < startDayOfWeek; i++) {
      calendarGridPanel.add(createEmptyDayCell());
    }

    for (int day = 1; day <= daysInMonth; day++) {
      LocalDate date = currentMonth.atDay(day);
      List<CalendarEvent> dayEvents = eventsByDate.getOrDefault(date, new ArrayList<>());
      calendarGridPanel.add(createDayCell(day, date, dayEvents));
    }

    int totalCells = startDayOfWeek + daysInMonth;
    int remainingCells = 42 - totalCells;
    for (int i = 0; i < remainingCells; i++) {
      calendarGridPanel.add(createEmptyDayCell());
    }

    calendarGridPanel.revalidate();
    calendarGridPanel.repaint();
    calendarGridPanel.setBackground(getCurrentColor(features.getCurrentCalendarName()));
  }

  /**
   * Create a cell which represents a day of the calendar month.
   *
   * @param dayNumber the day numeral of this cell of this month
   * @param date      the date assigned to this cell
   * @param events    all the events occurring on this cell
   * @return the day cell containing all info of this particular date
   */
  private JPanel createDayCell(int dayNumber, LocalDate date, List<CalendarEvent> events) {
    JPanel cell = new JPanel(new BorderLayout());
    cell.setBackground(Color.WHITE);
    cell.setBorder(BorderFactory.createLineBorder(Color.GRAY));

    if (date.equals(LocalDate.now())) {
      cell.setBackground(new Color(255, 250, 205));
    }

    JLabel dayLabel = new JLabel(String.valueOf(dayNumber));
    dayLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
    dayLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
    cell.add(dayLabel, BorderLayout.NORTH);

    JPanel eventsPanel = new JPanel();
    eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.Y_AXIS));
    eventsPanel.setOpaque(false);

    int maxDisplay = 3;
    for (int i = 0; i < Math.min(events.size(), maxDisplay); i++) {
      CalendarEvent event = events.get(i);
      JLabel eventLabel = new JLabel("- " + event.getSubject());
      eventLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
      eventLabel.setForeground(calendarColors.get(features.getCurrentCalendarName()));
      eventsPanel.add(eventLabel);
    }

    if (events.size() > maxDisplay) {
      JLabel moreLabel = new JLabel("  +" + (events.size() - maxDisplay) + " more");
      moreLabel.setFont(new Font("SansSerif", Font.ITALIC, 9));
      moreLabel.setForeground(Color.GRAY);
      eventsPanel.add(moreLabel);
    }

    cell.add(eventsPanel, BorderLayout.CENTER);

    cell.setCursor(new Cursor(Cursor.HAND_CURSOR));
    cell.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        showDayEventsDialog(date, events);
      }
    });

    return cell;
  }

  /**
   * Make an empty calendar cell to offset the first day of the month to the correct spot.
   *
   * @return the empty cell
   */
  private JPanel createEmptyDayCell() {
    JPanel cell = new JPanel();
    cell.setBackground(Color.LIGHT_GRAY);
    cell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    return cell;
  }

  /**
   * Show the dialogue box to see events of a particular date.
   *
   * @param date   the date on which this dialogue is run
   * @param events the events on this day
   */
  private void showDayEventsDialog(LocalDate date, List<CalendarEvent> events) {
    DayEventsDialog dialog = new DayEventsDialog(this, date, events, features);
    dialog.setVisible(true);

    if (dialog.wasModified()) {
      refreshCalendarView();
    }
  }

  /**
   * Show the dialogue to create a new calendar.
   */
  private void showCreateCalendarDialog() {
    CreateCalendarDialog dialog = new CreateCalendarDialog(this, features);
    dialog.setVisible(true);

    if (dialog.wasCreated()) {
      updateCalendarList();
    }
  }

  /**
   * Show the dialogue to create a new event.
   *
   * @param preselectedDate the date on which this dialogue is run
   */
  private void showCreateEventDialog(LocalDate preselectedDate) {
    CreateEventDialog dialog = new CreateEventDialog(this, features, preselectedDate);
    dialog.setVisible(true);

    if (dialog.wasCreated()) {
      refreshCalendarView();
    }
  }

  /**
   * Show the dialogue to see all events.
   */
  private void showAllEventsDialog() {
    if (features == null) {
      return;
    }

    List<CalendarEvent> allEvents = features.getAllEvents();
    AllEventsDialog dialog = new AllEventsDialog(this, allEvents, features);
    dialog.setVisible(true);

    if (dialog.wasModified()) {
      refreshCalendarView();
    }
  }

  /**
   * Show the dialogue to run the export command.
   */
  private void showExportDialog() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Export Calendar");

    int result = fileChooser.showSaveDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      String filename = fileChooser.getSelectedFile().getAbsolutePath();
      if (!filename.endsWith(".csv") && !filename.endsWith(".ics")) {
        filename += ".csv";
      }

      try {
        features.exportCalendar(filename);
        showMessage("Calendar exported successfully to " + filename);
      } catch (Exception e) {
        showError("Failed to export calendar: " + e.getMessage());
      }
    }
  }

  /**
   * Show the dialogue to run the edit calendar dialogue.
   */
  private void showEditCalendarDialog() {
    if (features == null) {
      return;
    }

    EditCalendarDialog dialog = new EditCalendarDialog(this, features);
    dialog.setVisible(true);

    if (dialog.wasModified()) {
      updateCalendarList();
      updateCalendarDisplay();
    }
  }

  /**
   * Show the dialogue to run the copy events between dialogue.
   */
  private void showCopyEventsInRangeDialog() {
    if (features == null) {
      return;
    }

    CopyEventsInRangeDialog dialog = new CopyEventsInRangeDialog(this, features);
    dialog.setVisible(true);

    if (dialog.wasCopied()) {
      refreshCalendarView();
    }
  }

  /**
   * Show the dialogue to run the edit events dialogue.
   */
  private void showEditEventsDialog() {
    if (features == null) {
      return;
    }

    EditEventsDialog dialog = new EditEventsDialog(this, features);
    dialog.setVisible(true);

    if (dialog.wasModified()) {
      refreshCalendarView();
    }
  }
}