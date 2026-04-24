package calendar.view;

import calendar.controller.GuiCalendarController;
import calendar.model.CalendarEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.YearMonth;
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
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Swing-based GUI implementation for the calendar application.
 * Displays month view with event management capabilities.
 * Dark minimalist theme with off-white accents.
 */
public class SwingCalendarView extends JFrame implements GuiCalendarView {
  private final GuiCalendarController controller;

  // Color scheme - Dark minimalist
  private static final Color BACKGROUND_DARK = new Color(20, 20, 20);
  private static final Color PANEL_DARK = new Color(30, 30, 30);
  private static final Color OFF_WHITE = new Color(240, 240, 235);
  private static final Color ACCENT_GRAY = new Color(60, 60, 60);
  private static final Color BUTTON_NORMAL = new Color(40, 40, 40);
  private static final Color BUTTON_HOVER = new Color(50, 50, 50);
  private static final Color SELECTED_DAY = new Color(70, 70, 80);
  private static final Color TODAY_HIGHLIGHT = new Color(50, 50, 45);
  private static final Color EVENT_INDICATOR = new Color(200, 200, 190);

  // Top panel components
  private JComboBox<String> calendarSelector;
  private JButton createCalendarButton;
  private JLabel currentMonthLabel;
  private JButton prevMonthButton;
  private JButton nextMonthButton;

  // Month view components
  private JPanel monthGridPanel;
  private JButton[][] dayButtons;

  // Side panel components
  private JLabel selectedDateLabel;
  private JTextArea eventsTextArea;
  private JButton createEventButton;
  private JButton editEventButton;

  // State
  private YearMonth currentMonth;
  private LocalDate selectedDate;
  private String currentCalendarName;

  /**
   * Constructs the Swing calendar view.
   *
   * @param controller the GUI controller
   */
  public SwingCalendarView(GuiCalendarController controller) {
    super("Calendar Application");
    this.controller = controller;
    this.currentMonth = YearMonth.now();
    this.selectedDate = LocalDate.now();

    // Set dark theme
    applyDarkTheme();

    setupUi();
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1200, 700);
    setLocationRelativeTo(null);
  }

  /**
   * Applies dark theme to UI components.
   */
  private void applyDarkTheme() {
    getContentPane().setBackground(BACKGROUND_DARK);

    // Set UI defaults for dialogs
    UIManager.put("OptionPane.background", PANEL_DARK);
    UIManager.put("Panel.background", PANEL_DARK);
    UIManager.put("OptionPane.messageForeground", OFF_WHITE);
  }

  /**
   * Sets up the UI components.
   */
  private void setupUi() {
    setLayout(new BorderLayout(10, 10));

    // Top panel
    add(createTopPanel(), BorderLayout.NORTH);

    // Center: Month view
    add(createMonthViewPanel(), BorderLayout.CENTER);

    // Right: Event details panel
    add(createEventPanel(), BorderLayout.EAST);
  }

  /**
   * Creates the top control panel.
   */
  private JPanel createTopPanel() {
    JPanel topPanel = new JPanel(new BorderLayout(10, 10));
    topPanel.setBackground(PANEL_DARK);
    topPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    // Left: Calendar selector
    JPanel calendarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
    calendarPanel.setBackground(PANEL_DARK);

    JLabel calLabel = new JLabel("Calendar:");
    calLabel.setForeground(OFF_WHITE);
    calLabel.setFont(new Font("Arial", Font.PLAIN, 14));
    calendarPanel.add(calLabel);

    calendarSelector = new JComboBox<>();
    calendarSelector.setPreferredSize(new Dimension(200, 30));
    calendarSelector.setBackground(BUTTON_NORMAL);
    calendarSelector.setForeground(OFF_WHITE);
    calendarSelector.addActionListener(e -> controller.onCalendarSelected(
        (String) calendarSelector.getSelectedItem()));
    calendarPanel.add(calendarSelector);

    createCalendarButton = createStyledButton("New Calendar");
    createCalendarButton.addActionListener(e -> controller.onCreateCalendar());
    calendarPanel.add(createCalendarButton);

    topPanel.add(calendarPanel, BorderLayout.WEST);

    // Center: Month navigation
    JPanel monthNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
    monthNavPanel.setBackground(PANEL_DARK);

    prevMonthButton = createStyledButton("◀ Previous");
    prevMonthButton.addActionListener(e -> controller.onPreviousMonth());
    monthNavPanel.add(prevMonthButton);

    currentMonthLabel = new JLabel();
    currentMonthLabel.setForeground(OFF_WHITE);
    currentMonthLabel.setFont(new Font("Arial", Font.BOLD, 20));
    currentMonthLabel.setHorizontalAlignment(SwingConstants.CENTER);
    currentMonthLabel.setPreferredSize(new Dimension(200, 30));
    updateMonthLabel();
    monthNavPanel.add(currentMonthLabel);

    nextMonthButton = createStyledButton("Next ▶");
    nextMonthButton.addActionListener(e -> controller.onNextMonth());
    monthNavPanel.add(nextMonthButton);

    topPanel.add(monthNavPanel, BorderLayout.CENTER);

    return topPanel;
  }

  /**
   * Creates the month view panel with day grid.
   */
  private JPanel createMonthViewPanel() {
    JPanel panel = new JPanel(new BorderLayout(5, 5));
    panel.setBackground(BACKGROUND_DARK);
    panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 10));

    // Day headers (Sun, Mon, Tue, etc.)
    JPanel headerPanel = new JPanel(new GridLayout(1, 7, 2, 2));
    headerPanel.setBackground(BACKGROUND_DARK);
    String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    for (String day : dayNames) {
      JLabel label = new JLabel(day, SwingConstants.CENTER);
      label.setFont(new Font("Arial", Font.BOLD, 13));
      label.setForeground(OFF_WHITE);
      label.setBackground(ACCENT_GRAY);
      label.setOpaque(true);
      label.setBorder(BorderFactory.createLineBorder(BACKGROUND_DARK, 1));
      label.setPreferredSize(new Dimension(120, 30));
      headerPanel.add(label);
    }
    panel.add(headerPanel, BorderLayout.NORTH);

    // Month grid (6 rows x 7 columns)
    monthGridPanel = new JPanel(new GridLayout(6, 7, 2, 2));
    monthGridPanel.setBackground(BACKGROUND_DARK);
    dayButtons = new JButton[6][7];

    for (int row = 0; row < 6; row++) {
      for (int col = 0; col < 7; col++) {
        JButton dayButton = new JButton();
        dayButton.setPreferredSize(new Dimension(120, 90));
        dayButton.setBackground(BUTTON_NORMAL);
        dayButton.setForeground(OFF_WHITE);
        dayButton.setFont(new Font("Arial", Font.PLAIN, 15));
        dayButton.setVerticalAlignment(SwingConstants.TOP);
        dayButton.setHorizontalAlignment(SwingConstants.LEFT);
        dayButton.setBorder(BorderFactory.createLineBorder(BACKGROUND_DARK, 1));
        dayButton.setFocusPainted(false);

        final int r = row;
        final int c = col;
        dayButton.addActionListener(e -> onDayClicked(r, c));

        dayButtons[row][col] = dayButton;
        monthGridPanel.add(dayButton);
      }
    }

    panel.add(monthGridPanel, BorderLayout.CENTER);
    return panel;
  }

  /**
   * Creates the right side event details panel.
   */
  private JPanel createEventPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setPreferredSize(new Dimension(350, 600));
    panel.setBackground(PANEL_DARK);
    panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    // Selected date label
    selectedDateLabel = new JLabel("Select a date");
    selectedDateLabel.setForeground(OFF_WHITE);
    selectedDateLabel.setFont(new Font("Arial", Font.BOLD, 16));
    selectedDateLabel.setAlignmentX(LEFT_ALIGNMENT);
    panel.add(selectedDateLabel);
    panel.add(Box.createRigidArea(new Dimension(0, 15)));

    // Events list
    eventsTextArea = new JTextArea(15, 25);
    eventsTextArea.setEditable(false);
    eventsTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
    eventsTextArea.setBackground(BUTTON_NORMAL);
    eventsTextArea.setForeground(OFF_WHITE);
    eventsTextArea.setCaretColor(OFF_WHITE);
    eventsTextArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    JScrollPane scrollPane = new JScrollPane(eventsTextArea);
    scrollPane.setBackground(BUTTON_NORMAL);
    scrollPane.setBorder(BorderFactory.createLineBorder(ACCENT_GRAY, 1));
    scrollPane.setAlignmentX(LEFT_ALIGNMENT);
    panel.add(scrollPane);
    panel.add(Box.createRigidArea(new Dimension(0, 15)));

    // Buttons
    createEventButton = createStyledButton("Create Event");
    createEventButton.setAlignmentX(LEFT_ALIGNMENT);
    createEventButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
    createEventButton.addActionListener(e -> controller.onCreateEvent(selectedDate));
    panel.add(createEventButton);
    panel.add(Box.createRigidArea(new Dimension(0, 8)));

    editEventButton = createStyledButton("Edit Events");
    editEventButton.setAlignmentX(LEFT_ALIGNMENT);
    editEventButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
    editEventButton.addActionListener(e -> controller.onEditEvents(selectedDate));
    panel.add(editEventButton);

    return panel;
  }

  /**
   * Creates a styled button with dark theme.
   */
  private JButton createStyledButton(String text) {
    JButton button = new JButton(text);
    button.setBackground(BUTTON_NORMAL);
    button.setForeground(OFF_WHITE);
    button.setFont(new Font("Arial", Font.PLAIN, 13));
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(ACCENT_GRAY, 1),
        BorderFactory.createEmptyBorder(5, 15, 5, 15)
    ));

    // Hover effect
    button.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseEntered(java.awt.event.MouseEvent evt) {
        button.setBackground(BUTTON_HOVER);
      }

      public void mouseExited(java.awt.event.MouseEvent evt) {
        button.setBackground(BUTTON_NORMAL);
      }
    });

    return button;
  }

  @Override
  public void display() {
    SwingUtilities.invokeLater(() -> setVisible(true));
  }

  @Override
  public void updateMonthView(YearMonth yearMonth, List<CalendarEvent> events,
                              String calendarName) {
    this.currentMonth = yearMonth;
    this.currentCalendarName = calendarName;
    updateMonthLabel();
    populateMonthGrid(events);
  }

  @Override
  public void updateDayEvents(LocalDate date, List<CalendarEvent> events) {
    this.selectedDate = date;
    selectedDateLabel.setText("Events on " + date.format(
        DateTimeFormatter.ofPattern("MMMM d, yyyy")));

    if (events.isEmpty()) {
      eventsTextArea.setText("No events scheduled.");
    } else {
      StringBuilder sb = new StringBuilder();
      for (CalendarEvent event : events) {
        sb.append("• ").append(event.getSubject()).append("\n");
        sb.append("  ").append(formatTime(event.getStartDateTime()))
            .append(" - ").append(formatTime(event.getEndDateTime())).append("\n");
        if (event.getLocation() != null && !event.getLocation().isEmpty()) {
          sb.append("  Location: ").append(event.getLocation()).append("\n");
        }
        sb.append("\n");
      }
      eventsTextArea.setText(sb.toString());
    }
  }

  @Override
  public void showErrorDialog(String title, String message) {
    SwingUtilities.invokeLater(() ->
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE));
  }

  @Override
  public void showInfoDialog(String title, String message) {
    SwingUtilities.invokeLater(() ->
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE));
  }

  @Override
  public void refresh() {
    controller.refreshView();
  }

  @Override
  public void displayMessage(String message) {
    // For GUI, messages go to console or can be logged
    System.out.println(message);
  }

  @Override
  public void displayError(String error) {
    showErrorDialog("Error", error);
  }

  @Override
  public void displayEvents(List<CalendarEvent> events, boolean showDateRange) {
    // Handled by updateDayEvents
  }

  @Override
  public void displayStatus(boolean isBusy) {
    showInfoDialog("Status", isBusy ? "Busy" : "Available");
  }

  @Override
  public void displayExportPath(String filePath) {
    showInfoDialog("Export Successful", "Calendar exported to:\n" + filePath);
  }

  /**
   * Updates the calendar selector dropdown.
   *
   * @param calendarNames list of calendar names
   * @param currentCalendar the currently selected calendar
   */
  public void updateCalendarList(List<String> calendarNames, String currentCalendar) {
    calendarSelector.removeAllItems();
    for (String name : calendarNames) {
      calendarSelector.addItem(name);
    }
    if (currentCalendar != null) {
      calendarSelector.setSelectedItem(currentCalendar);
    }
  }

  /**
   * Handles day button click.
   */
  private void onDayClicked(int row, int col) {
    JButton button = dayButtons[row][col];
    String text = button.getText();

    if (text.isEmpty() || !button.isEnabled()) {
      return; // Empty day cell
    }

    try {
      // Extract day number from HTML text
      String cleaned = text.replaceAll("<[^>]*>", "").trim();
      String dayStr = cleaned.split("\n")[0].split(" ")[0];
      int day = Integer.parseInt(dayStr);
      LocalDate clickedDate = currentMonth.atDay(day);
      controller.onDaySelected(clickedDate);
    } catch (Exception e) {
      // Ignore invalid clicks
    }
  }

  /**
   * Populates the month grid with days and event indicators.
   */
  private void populateMonthGrid(List<CalendarEvent> events) {
    LocalDate firstOfMonth = currentMonth.atDay(1);
    int firstDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Sun=0
    int daysInMonth = currentMonth.lengthOfMonth();

    LocalDate today = LocalDate.now();

    int day = 1;
    for (int row = 0; row < 6; row++) {
      for (int col = 0; col < 7; col++) {
        JButton button = dayButtons[row][col];

        if ((row == 0 && col < firstDayOfWeek) || day > daysInMonth) {
          button.setText("");
          button.setEnabled(false);
          button.setBackground(BACKGROUND_DARK);
          button.setBorder(BorderFactory.createEmptyBorder());
        } else {
          LocalDate date = currentMonth.atDay(day);

          // Count events on this day
          long eventCount = events.stream()
              .filter(e -> !e.getStartDateTime().toLocalDate().isAfter(date)
                  && !e.getEndDateTime().toLocalDate().isBefore(date))
              .count();

          String buttonText = "<html><div style='padding:5px;'>";
          buttonText += "<b>" + day + "</b>";
          if (eventCount > 0) {
            buttonText += "<br><span style='font-size:10px; color:#C8C8BE;'>";
            buttonText += eventCount + " event" + (eventCount > 1 ? "s" : "");
            buttonText += "</span>";
          }
          buttonText += "</div></html>";

          button.setText(buttonText);
          button.setEnabled(true);
          button.setFont(new Font("Arial", Font.PLAIN, 14));

          // Color coding
          if (date.equals(today)) {
            button.setBackground(TODAY_HIGHLIGHT);
            button.setBorder(BorderFactory.createLineBorder(EVENT_INDICATOR, 2));
          } else if (date.equals(selectedDate)) {
            button.setBackground(SELECTED_DAY);
            button.setBorder(BorderFactory.createLineBorder(OFF_WHITE, 2));
          } else {
            button.setBackground(BUTTON_NORMAL);
            button.setBorder(BorderFactory.createLineBorder(ACCENT_GRAY, 1));
          }

          day++;
        }
      }
    }
  }

  /**
   * Updates the month label.
   */
  private void updateMonthLabel() {
    currentMonthLabel.setText(currentMonth.format(
        DateTimeFormatter.ofPattern("MMMM yyyy")));
  }

  /**
   * Formats time for display.
   */
  private String formatTime(java.time.LocalDateTime dateTime) {
    return dateTime.format(DateTimeFormatter.ofPattern("h:mm a"));
  }

  /**
   * Gets the current month being displayed.
   *
   * @return the current month
   */
  public YearMonth getCurrentMonth() {
    return currentMonth;
  }

  /**
   * Gets the selected date.
   *
   * @return the selected date
   */
  public LocalDate getSelectedDate() {
    return selectedDate;
  }
}