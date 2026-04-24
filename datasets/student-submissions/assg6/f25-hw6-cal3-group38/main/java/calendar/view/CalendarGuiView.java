package calendar.view;

import calendar.controller.CalendarController;
import calendar.model.Event;
import calendar.model.EventStatus;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

/**
 * Main GUI view for the calendar application.
 */
public class CalendarGuiView extends JFrame {
  private final CalendarController controller;
  private JComboBox<String> calendarSelector;
  private JLabel currentMonthLabel;
  private YearMonth currentYearMonth;
  private JPanel calendarGrid;
  private JTextArea eventDetailsArea;
  private final List<CalendarDayPanel> dayPanels;
  private CalendarDayPanel selectedDayPanel;
  private Event selectedEvent;

  // Color scheme
  private final Color selectedDayColor = new Color(220, 240, 255);
  private final Color todayColor = new Color(255, 255, 200);
  private final Color otherMonthColor = new Color(240, 240, 240);

  /**
   * Constructs the main GUI view.
   *
   * @param controller the calendar controller to handle business logic
   */
  public CalendarGuiView(CalendarController controller) {
    this.controller = controller;
    this.currentYearMonth = YearMonth.now();
    this.dayPanels = new ArrayList<>();
    this.selectedDayPanel = null;
    this.selectedEvent = null;

    initializeGui();
    refreshView();
  }

  private void initializeGui() {
    setTitle("Calendar Application - Professional Edition");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setPreferredSize(new Dimension(1400, 900));

    // Main container with modern layout
    JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
    mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
    mainPanel.setBackground(Color.WHITE);

    // Top panel - Calendar selection and navigation
    mainPanel.add(createTopPanel(), BorderLayout.NORTH);

    // Center panel - Calendar grid
    mainPanel.add(createCalendarPanel(), BorderLayout.CENTER);

    // Right panel - Event details and controls
    mainPanel.add(createSidePanel(), BorderLayout.EAST);

    setContentPane(mainPanel);
    pack();
    setLocationRelativeTo(null);
  }

  private JPanel createTopPanel() {
    JPanel topPanel = new JPanel(new BorderLayout(10, 0));
    topPanel.setBackground(Color.WHITE);
    topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

    // Left: Calendar selection
    JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    leftPanel.setBackground(Color.WHITE);

    leftPanel.add(new JLabel("📅 Calendar:"));
    calendarSelector = new JComboBox<>();
    calendarSelector.setPreferredSize(new Dimension(150, 30));
    calendarSelector.addActionListener(e -> onCalendarSelected());
    leftPanel.add(calendarSelector);

    JButton newCalendarBtn = new JButton("➕ New Calendar");
    newCalendarBtn.addActionListener(e -> createNewCalendar());
    leftPanel.add(newCalendarBtn);

    topPanel.add(leftPanel, BorderLayout.WEST);

    // Center: Month navigation
    JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
    centerPanel.setBackground(Color.WHITE);

    JButton prevYearBtn = createStyledButton("⏪", "Previous Year");
    prevYearBtn.addActionListener(e -> navigateYear(-1));
    centerPanel.add(prevYearBtn);

    JButton prevMonthBtn = createStyledButton("◀", "Previous Month");
    prevMonthBtn.addActionListener(e -> navigateMonth(-1));
    centerPanel.add(prevMonthBtn);

    currentMonthLabel = new JLabel("", JLabel.CENTER);
    currentMonthLabel.setFont(new Font("Arial", Font.BOLD, 16));
    currentMonthLabel.setPreferredSize(new Dimension(200, 30));
    updateMonthLabel();
    centerPanel.add(currentMonthLabel);

    JButton nextMonthBtn = createStyledButton("▶", "Next Month");
    nextMonthBtn.addActionListener(e -> navigateMonth(1));
    centerPanel.add(nextMonthBtn);

    JButton nextYearBtn = createStyledButton("⏩", "Next Year");
    nextYearBtn.addActionListener(e -> navigateYear(1));
    centerPanel.add(nextYearBtn);

    topPanel.add(centerPanel, BorderLayout.CENTER);

    // Right: Today button
    JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    rightPanel.setBackground(Color.WHITE);

    JButton todayBtn = createStyledButton("📌 Today", "Go to Today");
    todayBtn.addActionListener(e -> goToToday());
    rightPanel.add(todayBtn);

    topPanel.add(rightPanel, BorderLayout.EAST);

    return topPanel;
  }

  /**
   * Creates a styled button with consistent appearance.
   *
   * @param text the button text
   * @param tooltip the tooltip text
   * @return the styled button
   */
  private JButton createStyledButton(String text, String tooltip) {
    JButton button = new JButton(text);
    button.setToolTipText(tooltip);
    button.setPreferredSize(new Dimension(40, 30));
    button.setBackground(new Color(70, 130, 180));
    button.setForeground(Color.WHITE);
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createRaisedBevelBorder());
    return button;
  }

  /**
   * Creates the calendar panel with monthly view.
   *
   * @return the calendar panel
   */
  private JPanel createCalendarPanel() {
    JPanel calendarPanel = new JPanel(new BorderLayout(0, 5));
    calendarPanel.setBackground(Color.WHITE);
    calendarPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(new Color(200, 200, 200)),
        "Monthly View",
        TitledBorder.CENTER,
        TitledBorder.TOP,
        new Font("Arial", Font.BOLD, 14),
        new Color(70, 130, 180)
    ));

    // Day headers with better styling
    JPanel headerPanel = new JPanel(new GridLayout(1, 7, 2, 2));
    headerPanel.setBackground(new Color(240, 240, 240));
    String[] days = {"SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
    for (String day : days) {
      JLabel dayLabel = new JLabel(day, JLabel.CENTER);
      dayLabel.setFont(new Font("Arial", Font.BOLD, 12));
      dayLabel.setOpaque(true);
      dayLabel.setBackground(new Color(70, 130, 180));
      dayLabel.setForeground(Color.WHITE);
      dayLabel.setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createLineBorder(new Color(50, 100, 150)),
          BorderFactory.createEmptyBorder(5, 0, 5, 0)
      ));
      headerPanel.add(dayLabel);
    }
    calendarPanel.add(headerPanel, BorderLayout.NORTH);

    // Calendar grid
    calendarGrid = new JPanel(new GridLayout(6, 7, 3, 3));
    calendarGrid.setBackground(Color.WHITE);
    calendarGrid.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    calendarPanel.add(calendarGrid, BorderLayout.CENTER);

    return calendarPanel;
  }

  /**
   * Creates the side panel with event details and actions.
   *
   * @return the side panel
   */
  private JPanel createSidePanel() {
    JPanel sidePanel = new JPanel(new BorderLayout(10, 10));
    sidePanel.setPreferredSize(new Dimension(400, 0));
    sidePanel.setBackground(Color.WHITE);

    // Event details panel
    JPanel detailsPanel = new JPanel(new BorderLayout());
    detailsPanel.setBackground(Color.WHITE);
    detailsPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(new Color(200, 200, 200)),
        "📋 Event Details",
        TitledBorder.LEFT,
        TitledBorder.TOP,
        new Font("Arial", Font.BOLD, 14),
        new Color(70, 130, 180)
    ));

    eventDetailsArea = new JTextArea(20, 35);
    eventDetailsArea.setEditable(false);
    eventDetailsArea.setLineWrap(true);
    eventDetailsArea.setWrapStyleWord(true);
    eventDetailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
    eventDetailsArea.setBackground(new Color(250, 250, 250));
    eventDetailsArea.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(200, 200, 200)),
        BorderFactory.createEmptyBorder(10, 10, 10, 10)
    ));

    JScrollPane scrollPane = new JScrollPane(eventDetailsArea);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    detailsPanel.add(scrollPane, BorderLayout.CENTER);

    sidePanel.add(detailsPanel, BorderLayout.CENTER);

    // Action buttons panel
    JPanel actionPanel = new JPanel(new GridLayout(9, 1, 8, 8));
    actionPanel.setBackground(Color.WHITE);
    actionPanel.setBorder(BorderFactory.createTitledBorder(
        BorderFactory.createLineBorder(new Color(200, 200, 200)),
        "⚡ Quick Actions",
        TitledBorder.LEFT,
        TitledBorder.TOP,
        new Font("Arial", Font.BOLD, 14),
        new Color(70, 130, 180)
    ));

    // Create styled buttons
    JButton createEventBtn = createActionButton("➕ Create Event", "Create a new event");
    createEventBtn.addActionListener(e -> createNewEvent());
    actionPanel.add(createEventBtn);

    JButton editEventBtn = createActionButton("✏️ Edit Event", "Edit selected event");
    editEventBtn.addActionListener(e -> editSelectedEvent());
    actionPanel.add(editEventBtn);

    JButton deleteEventBtn = createActionButton("🗑️ Delete Event", "Delete selected event");
    deleteEventBtn.addActionListener(e -> deleteSelectedEvent());
    actionPanel.add(deleteEventBtn);

    JButton copyEventBtn = createActionButton("📋 Copy Event", "Copy event to another calendar");
    copyEventBtn.addActionListener(e -> copySelectedEvent());
    actionPanel.add(copyEventBtn);

    JButton viewAllBtn = createActionButton("📊 View All Events", "Show all events this month");
    viewAllBtn.addActionListener(e -> viewAllEvents());
    actionPanel.add(viewAllBtn);

    JButton statusBtn = createActionButton("🕐 Check Availability", "Check if busy or free");
    statusBtn.addActionListener(e -> checkAvailability());
    actionPanel.add(statusBtn);

    JButton exportBtn = createActionButton("💾 Export Calendar", "Export to CSV or iCal");
    exportBtn.addActionListener(e -> exportCalendar());
    actionPanel.add(exportBtn);

    JButton settingsBtn = createActionButton("⚙️ Calendar Settings", "Edit calendar properties");
    settingsBtn.addActionListener(e -> showCalendarSettings());
    actionPanel.add(settingsBtn);

    JButton refreshBtn = createActionButton("🔄 Refresh", "Refresh calendar view");
    refreshBtn.addActionListener(e -> refreshView());
    actionPanel.add(refreshBtn);

    sidePanel.add(actionPanel, BorderLayout.SOUTH);

    return sidePanel;
  }

  /**
   * Creates an action button with consistent styling.
   *
   * @param text the button text
   * @param tooltip the tooltip text
   * @return the styled action button
   */
  private JButton createActionButton(String text, String tooltip) {
    JButton button = new JButton(text);
    button.setToolTipText(tooltip);
    button.setFont(new Font("Arial", Font.PLAIN, 12));
    button.setPreferredSize(new Dimension(180, 35));
    button.setBackground(new Color(70, 130, 180));
    button.setForeground(Color.WHITE);
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(50, 100, 150)),
        BorderFactory.createEmptyBorder(8, 5, 8, 5)
    ));
    return button;
  }

  /**
   * Refreshes the entire view including calendar selector and grid.
   */
  public void refreshView() {
    updateCalendarSelector();
    updateCalendarGrid();
    clearEventDetails();
    selectedEvent = null;
    selectedDayPanel = null;
  }

  /**
   * Updates the calendar selector with available calendars.
   */
  private void updateCalendarSelector() {
    calendarSelector.removeAllItems();
    List<String> calendarNames = controller.getCalendarNames();
    for (String name : calendarNames) {
      calendarSelector.addItem(name);
    }

    // Select current calendar
    String currentCalendar = controller.getCurrentCalendarName();
    calendarSelector.setSelectedItem(currentCalendar);
  }

  /**
   * Updates the calendar grid with days and events for the current month.
   */
  private void updateCalendarGrid() {
    calendarGrid.removeAll();
    dayPanels.clear();
    selectedDayPanel = null;

    LocalDate firstOfMonth = currentYearMonth.atDay(1);
    LocalDate firstDisplayDate = firstOfMonth.with(java.time.DayOfWeek.SUNDAY);

    for (int i = 0; i < 42; i++) { // 6 weeks
      LocalDate currentDate = firstDisplayDate.plusDays(i);
      CalendarDayPanel dayPanel = new CalendarDayPanel(currentDate);
      dayPanels.add(dayPanel);
      calendarGrid.add(dayPanel);
    }

    calendarGrid.revalidate();
    calendarGrid.repaint();
  }

  /**
   * Updates the month label with the current year and month.
   */
  private void updateMonthLabel() {
    currentMonthLabel.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
  }

  /**
   * Handles calendar selection change event.
   */
  public void onCalendarSelected() {
    String selectedCalendar = (String) calendarSelector.getSelectedItem();
    if (selectedCalendar != null && !selectedCalendar.equals(controller.getCurrentCalendarName())) {
      controller.useCalendar(selectedCalendar);
      updateCalendarGrid();
      showMessage("Switched to calendar: " + selectedCalendar);
    }
  }

  /**
   * Navigates to the previous or next month.
   *
   * @param months the number of months to navigate (positive for future, negative for past)
   */
  public void navigateMonth(int months) {
    currentYearMonth = currentYearMonth.plusMonths(months);
    updateMonthLabel();
    updateCalendarGrid();
  }

  /**
   * Navigates to the previous or next year.
   *
   * @param years the number of years to navigate (positive for future, negative for past)
   */
  public void navigateYear(int years) {
    currentYearMonth = currentYearMonth.plusYears(years);
    updateMonthLabel();
    updateCalendarGrid();
  }

  /**
   * Navigates to the current month (today).
   */
  public void goToToday() {
    currentYearMonth = YearMonth.now();
    updateMonthLabel();
    updateCalendarGrid();
    showMessage("Navigated to current month");
  }

  /**
   * Creates a new calendar with user-provided name and timezone.
   */
  public void createNewCalendar() {
    JTextField nameField = new JTextField(20);
    JComboBox<String> timezoneCombo = new JComboBox<>(getCommonTimezones());

    JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
    panel.add(new JLabel("Calendar Name:"));
    panel.add(nameField);
    panel.add(new JLabel("Timezone:"));
    panel.add(timezoneCombo);

    int result = JOptionPane.showConfirmDialog(this, panel,
        "Create New Calendar", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
      String name = nameField.getText().trim();
      String timezone = (String) timezoneCombo.getSelectedItem();

      // FIXED: Use CalendarViewHelper for validation
      if (!CalendarViewHelper.isValidCalendarName(name)) {
        showError("Calendar name cannot be empty");
        return;
      }

      try {
        controller.createCalendar(name, timezone);
        controller.useCalendar(name);
        refreshView();
        showMessage("✅ Calendar '" + name + "' created successfully!");
      } catch (Exception e) {
        showError("❌ Error creating calendar: " + e.getMessage());
      }
    }
  }

  /**
   * Creates a new event using the event dialog.
   */
  public void createNewEvent() {
    LocalDate defaultDate = CalendarViewHelper.getDefaultDate(
        selectedDayPanel != null ? selectedDayPanel.getDate() : null);
    EventDialog dialog = new EventDialog(this, "Create New Event", null, defaultDate);
    dialog.setVisible(true);

    if (dialog.isConfirmed()) {
      try {
        EventDialog.EventData eventData = dialog.getEventData();
        createEventFromData(eventData);
        updateCalendarGrid();
        showMessage("✅ Event created successfully!");
      } catch (IllegalArgumentException e) {
        // This is already handled in createEventFromData
      } catch (Exception e) {
        showError("❌ Unexpected error creating event: " + e.getMessage());
      }
    }
  }

  /**
   * Edits the currently selected event.
   */
  public void editSelectedEvent() {
    if (selectedEvent != null) {
      // Check if this event is part of a series
      boolean isPartOfSeries = selectedEvent.getSeriesId() != null;

      if (isPartOfSeries) {
        // Ask user if they want to edit this event or all in series
        String[] options = {"This event only", "All events in series", "Cancel"};
        int choice = JOptionPane.showOptionDialog(this,
            "This event is part of a recurring series.\nHow would you like to edit it?",
            "Edit Recurring Event",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            options,
            options[0]);

        if (choice == 0) {
          // Edit this event only
          editSingleEventInstance(selectedEvent);
        } else if (choice == 1) {
          // Edit all events in series
          editEventSeries(selectedEvent);
        }
        // choice == 2 or closed dialog: do nothing
      } else {
        // Regular single event - edit normally
        editSingleEventInstance(selectedEvent);
      }
    } else {
      showMessage("ℹ️ Please select an event first by clicking on it in the calendar");
    }
  }

  /**
   * Edits a single event instance.
   */
  private void editSingleEventInstance(Event event) {
    EventDialog dialog = new EventDialog(this, "Edit Event",
        convertEventToEventData(event), event.getStartDateTime().toLocalDate());
    dialog.setVisible(true);

    if (dialog.isConfirmed()) {
      try {
        // Delete old event and create new one
        controller.deleteEvent(event);
        EventDialog.EventData eventData = dialog.getEventData();
        createEventFromData(eventData);
        updateCalendarGrid();
        selectedEvent = null;
        showMessage("✅ Event updated successfully!");
      } catch (Exception e) {
        showError("❌ Error updating event: " + e.getMessage());
      }
    }
  }

  /**
   * Edits all events in a series.
   */
  private void editEventSeries(Event templateEvent) {
    EventDialog dialog = new EventDialog(this, "Edit Event Series",
        convertEventToEventData(templateEvent), templateEvent.getStartDateTime().toLocalDate());
    dialog.setVisible(true);

    if (dialog.isConfirmed()) {
      try {
        // Get all events with the same series ID
        String seriesId = templateEvent.getSeriesId();
        List<Event> allEvents = controller.getAllEvents();
        List<Event> seriesEvents = allEvents.stream()
            .filter(e -> seriesId.equals(e.getSeriesId()))
            .collect(java.util.stream.Collectors.toList());

        // Delete all events in the series
        for (Event event : seriesEvents) {
          controller.deleteEvent(event);
        }

        // Create new series with updated data
        EventDialog.EventData eventData = dialog.getEventData();
        createEventFromData(eventData);

        updateCalendarGrid();
        selectedEvent = null;
        showMessage("✅ Event series updated successfully! " + seriesEvents.size()
            + " events updated.");
      } catch (Exception e) {
        showError("❌ Error updating event series: " + e.getMessage());
      }
    }
  }

  /**
   * Deletes the currently selected event after confirmation.
   */
  public void deleteSelectedEvent() {
    if (selectedEvent != null) {
      int confirm = JOptionPane.showConfirmDialog(this,
          "Are you sure you want to delete this event?\n\n"
              + "Event: " + selectedEvent.getSubject() + "\n"
              + "Date: " + selectedEvent.getStartDateTime().toLocalDate() + "\n"
              + "Time: " + (selectedEvent.isAllDayEvent() ? "All Day"
              : selectedEvent.getStartDateTime().toLocalTime() + " - "
              + selectedEvent.getEndDateTime().toLocalTime()),
          "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

      if (confirm == JOptionPane.YES_OPTION) {
        try {
          controller.deleteEvent(selectedEvent);
          updateCalendarGrid();
          selectedEvent = null;
          clearEventDetails();
          showMessage("✅ Event deleted successfully!");
        } catch (Exception e) {
          showError("❌ Error deleting event: " + e.getMessage());
        }
      }
    } else {
      showMessage("ℹ️ Please select an event first by clicking on it in the calendar");
    }
  }

  /**
   * Displays all events for the current month in the event details area.
   */
  public void viewAllEvents() {
    LocalDate startDate = currentYearMonth.atDay(1);
    LocalDate endDate = currentYearMonth.atEndOfMonth();

    List<Event> allEvents = new ArrayList<>();
    for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
      List<Event> events = controller.getEventsOnDateAsList(date);
      allEvents.addAll(events);
    }

    // FIXED: Use CalendarViewHelper to format month events
    String formattedEvents = CalendarViewHelper.formatMonthEvents(
        allEvents, currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
    eventDetailsArea.setText(formattedEvents);
  }

  /**
   * Exports the calendar to a file.
   */
  public void exportCalendar() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Export Calendar");
    fileChooser.setApproveButtonText("Export");

    int result = fileChooser.showSaveDialog(this);
    if (result == JFileChooser.APPROVE_OPTION) {
      String filePath = fileChooser.getSelectedFile().getAbsolutePath();
      try {
        String exportedPath = controller.exportCalendar(filePath);
        showMessage("✅ Calendar exported successfully to:\n" + exportedPath);
      } catch (Exception e) {
        showError("❌ Error exporting calendar: " + e.getMessage());
      }
    }
  }

  /**
   * Copies the selected event to another calendar.
   */
  public void copySelectedEvent() {
    if (selectedEvent == null) {
      showMessage("ℹ️ Please select an event first by clicking on it in the calendar");
      return;
    }

    // Get list of calendars for dropdown
    List<String> calendarNames = controller.getCalendarNames();
    String currentCalendar = controller.getCurrentCalendarName();

    // Remove current calendar from list (copying to same calendar is allowed but confusing)
    List<String> targetCalendars = new ArrayList<>(calendarNames);

    if (targetCalendars.isEmpty()) {
      showError("No calendars available to copy to. Create another calendar first.");
      return;
    }

    // Create copy dialog
    JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));
    panel.setBorder(new EmptyBorder(15, 15, 15, 15));

    panel.add(new JLabel("📋 Event to Copy:"));
    panel.add(new JLabel(selectedEvent.getSubject()));

    panel.add(new JLabel("📅 Target Calendar:"));
    JComboBox<String> targetCalCombo = new JComboBox<>(targetCalendars.toArray(new String[0]));
    panel.add(targetCalCombo);

    panel.add(new JLabel("📅 New Date:"));
    JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
    dateSpinner.setEditor(dateEditor);
    dateSpinner.setValue(java.sql.Date.valueOf(selectedEvent.getStartDateTime().toLocalDate()));
    panel.add(dateSpinner);

    panel.add(new JLabel("⏰ New Time:"));
    JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
    timeSpinner.setEditor(timeEditor);
    timeSpinner.setValue(java.sql.Time.valueOf(selectedEvent.getStartDateTime().toLocalTime()));
    panel.add(timeSpinner);

    int result = JOptionPane.showConfirmDialog(this, panel,
        "Copy Event to Another Calendar",
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.PLAIN_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
      try {
        String targetCalendar = (String) targetCalCombo.getSelectedItem();
        LocalDate newDate = ((java.util.Date) dateSpinner.getValue()).toInstant()
            .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalTime newTime = ((java.util.Date) timeSpinner.getValue()).toInstant()
            .atZone(java.time.ZoneId.systemDefault()).toLocalTime();

        LocalDateTime newStartTime = LocalDateTime.of(newDate, newTime);

        // Call controller to copy the event
        controller.copyEvent(
            selectedEvent.getSubject(),
            selectedEvent.getStartDateTime(),
            targetCalendar,
            newStartTime
        );

        showMessage("✅ Event copied successfully to " + targetCalendar + "!");
        updateCalendarGrid();
      } catch (Exception e) {
        showError("❌ Error copying event: " + e.getMessage());
      }
    }
  }

  /**
   * Shows calendar settings dialog for editing calendar properties.
   */
  public void showCalendarSettings() {
    String currentCalendarName = controller.getCurrentCalendarName();

    JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
    panel.setBorder(new EmptyBorder(15, 15, 15, 15));

    panel.add(new JLabel("📅 Current Calendar:"));
    panel.add(new JLabel(currentCalendarName));

    panel.add(new JLabel("✏️ New Name:"));
    JTextField nameField = new JTextField(currentCalendarName, 20);
    panel.add(nameField);

    panel.add(new JLabel("🌍 Timezone:"));
    JComboBox<String> timezoneCombo = new JComboBox<>(getCommonTimezones());
    panel.add(timezoneCombo);

    int result = JOptionPane.showConfirmDialog(this, panel,
        "Calendar Settings - " + currentCalendarName,
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.PLAIN_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
      try {
        String newName = nameField.getText().trim();
        String newTimezone = (String) timezoneCombo.getSelectedItem();

        boolean changed = false;

        // Update name if changed
        if (!newName.equals(currentCalendarName) && !newName.isEmpty()) {
          controller.editCalendar(currentCalendarName, "name", newName);
          changed = true;
        }

        // Update timezone
        if (newTimezone != null) {
          String targetName = newName.isEmpty() ? currentCalendarName : newName;
          controller.editCalendar(targetName, "timezone", newTimezone);
          changed = true;
        }

        if (changed) {
          refreshView();
          showMessage("✅ Calendar settings updated successfully!");
        }
      } catch (Exception e) {
        showError("❌ Error updating calendar settings: " + e.getMessage());
      }
    }
  }

  /**
   * Checks availability/status at a specific date and time.
   */
  public void checkAvailability() {
    JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
    panel.setBorder(new EmptyBorder(15, 15, 15, 15));

    panel.add(new JLabel("📅 Date:"));
    JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
    dateSpinner.setEditor(dateEditor);
    dateSpinner.setValue(java.sql.Date.valueOf(
        selectedDayPanel != null ? selectedDayPanel.getDate() : LocalDate.now()));
    panel.add(dateSpinner);

    panel.add(new JLabel("⏰ Time:"));
    JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "HH:mm");
    timeSpinner.setEditor(timeEditor);
    timeSpinner.setValue(java.sql.Time.valueOf(LocalTime.of(12, 0)));
    panel.add(timeSpinner);

    int result = JOptionPane.showConfirmDialog(this, panel,
        "Check Availability",
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE);

    if (result == JOptionPane.OK_OPTION) {
      try {
        LocalDate checkDate = ((java.util.Date) dateSpinner.getValue()).toInstant()
            .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalTime checkTime = ((java.util.Date) timeSpinner.getValue()).toInstant()
            .atZone(java.time.ZoneId.systemDefault()).toLocalTime();

        LocalDateTime checkDateTime = LocalDateTime.of(checkDate, checkTime);

        boolean isBusy = controller.isBusyAt(checkDateTime);

        if (isBusy) {
          // Get events at that time to show what conflicts
          List<Event> eventsAtTime = controller.getEventsOnDateAsList(checkDate).stream()
              .filter(e -> !checkDateTime.isBefore(e.getStartDateTime())
                  && checkDateTime.isBefore(e.getEndDateTime()))
              .collect(java.util.stream.Collectors.toList());

          StringBuilder message = new StringBuilder("❌ BUSY at " + checkTime + "\n\n");
          message.append("Conflicting events:\n");
          for (Event e : eventsAtTime) {
            message.append("• ").append(e.getSubject())
                .append(" (").append(e.getStartDateTime().toLocalTime())
                .append(" - ").append(e.getEndDateTime().toLocalTime())
                .append(")\n");
          }

          JOptionPane.showMessageDialog(this, message.toString(),
              "Availability Check - BUSY",
              JOptionPane.WARNING_MESSAGE);
        } else {
          JOptionPane.showMessageDialog(this,
              "✅ FREE at " + checkDate + " " + checkTime + "\n\n"
                  + "You have no events scheduled at this time.",
              "Availability Check - FREE",
              JOptionPane.INFORMATION_MESSAGE);
        }
      } catch (Exception e) {
        showError("❌ Error checking availability: " + e.getMessage());
      }
    }
  }

  /**
   * Creates an event from dialog data.
   *
   * @param eventData the event data from dialog
   */
  private void createEventFromData(EventDialog.EventData eventData) {
    try {
      if (eventData.isAllDay()) {
        if (eventData.isRecurring()) {
          controller.createAllDayEventSeries(
              eventData.getSubject(),
              eventData.getStartDate(),
              eventData.getRepeatDays(),
              eventData.getOccurrences(),
              eventData.getUntilDate(),
              eventData.getDescription(),
              eventData.getLocation(),
              eventData.getStatus()
          );
        } else {
          controller.createAllDayEvent(
              eventData.getSubject(),
              eventData.getStartDate(),
              eventData.getDescription(),
              eventData.getLocation(),
              eventData.getStatus()
          );
        }
      } else {
        LocalDateTime start = LocalDateTime.of(eventData.getStartDate(), eventData.getStartTime());
        LocalDateTime end = LocalDateTime.of(eventData.getEndDate(), eventData.getEndTime());

        if (eventData.isRecurring()) {
          controller.createEventSeries(
              eventData.getSubject(),
              start, end,
              eventData.getRepeatDays(),
              eventData.getOccurrences(),
              eventData.getUntilDate(),
              eventData.getDescription(),
              eventData.getLocation(),
              eventData.getStatus()
          );
        } else {
          controller.createSingleEvent(
              eventData.getSubject(),
              start, end,
              eventData.getDescription(),
              eventData.getLocation(),
              eventData.getStatus()
          );
        }
      }
    } catch (IllegalArgumentException e) {
      showError("❌ Invalid event data: " + e.getMessage());
      throw e; // Re-throw to let the caller handle it
    } catch (Exception e) {
      showError("❌ Error creating event: " + e.getMessage());
      throw new RuntimeException("Failed to create event", e);
    }
  }

  /**
   * Converts an Event to EventData for editing.
   *
   * @param event the event to convert
   * @return the event data
   */
  public EventDialog.EventData convertEventToEventData(Event event) {
    EventDialog.EventData data = new EventDialog.EventData();
    data.setSubject(event.getSubject());
    data.setAllDay(event.isAllDayEvent());
    data.setStartDate(event.getStartDateTime().toLocalDate());
    data.setEndDate(event.getEndDateTime().toLocalDate());
    data.setStartTime(event.getStartDateTime().toLocalTime());
    data.setEndTime(event.getEndDateTime().toLocalTime());
    data.setDescription(event.getDescription() != null ? event.getDescription() : "");
    data.setLocation(event.getLocation() != null ? event.getLocation() : "");
    data.setStatus(event.getStatus());
    return data;
  }

  /**
   * Gets common timezones for calendar creation.
   *
   * @return array of common timezone IDs
   */
  public String[] getCommonTimezones() {
    return new String[]{
        "America/New_York", "America/Chicago", "America/Denver", "America/Los_Angeles",
        "Europe/London", "Europe/Paris", "Asia/Tokyo", "Australia/Sydney"
    };
  }

  /**
   * Displays an information message dialog.
   *
   * @param message the message to display
   */
  public void showMessage(String message) {
    JOptionPane.showMessageDialog(this, message, "Information",
        JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Displays an error message dialog.
   *
   * @param message the error message to display
   */
  public void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "Error",
        JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Clears the event details area and shows default instructions.
   */
  public void clearEventDetails() {
    eventDetailsArea.setText("👈 Select a day or event to view details.\n\n"
        + "• Click on any day to see its events\n"
        + "• Click on an event to select it\n"
        + "• Use the buttons below to manage events");
  }

  /**
   * Panel representing a single day in the calendar grid.
   */
  public class CalendarDayPanel extends JPanel {
    private final LocalDate date;
    private final JTextArea eventsArea;
    private final List<Event> dayEvents;

    /**
     * Panel representing a single day in the calendar grid.
     */
    public CalendarDayPanel(LocalDate date) {
      this.date = date;
      this.dayEvents = new ArrayList<>();
      setLayout(new BorderLayout());
      setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

      // Day number
      JLabel dayLabel = new JLabel(String.valueOf(date.getDayOfMonth()), JLabel.CENTER);
      dayLabel.setOpaque(true);
      dayLabel.setFont(new Font("Arial", Font.BOLD, 12));

      // Styling based on date
      if (date.equals(LocalDate.now())) {
        dayLabel.setBackground(todayColor);
        dayLabel.setForeground(Color.RED);
      } else if (date.getMonth() != currentYearMonth.getMonth()) {
        dayLabel.setBackground(otherMonthColor);
        dayLabel.setForeground(Color.GRAY);
      } else {
        dayLabel.setBackground(Color.WHITE);
        dayLabel.setForeground(Color.BLACK);
      }

      add(dayLabel, BorderLayout.NORTH);

      // Events area
      eventsArea = new JTextArea(4, 10);
      eventsArea.setEditable(false);
      eventsArea.setLineWrap(true);
      eventsArea.setWrapStyleWord(true);
      eventsArea.setFont(new Font("SansSerif", Font.PLAIN, 9));
      eventsArea.setBackground(getBackground());
      eventsArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

      JScrollPane scrollPane = new JScrollPane(eventsArea);
      scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scrollPane.setBorder(BorderFactory.createEmptyBorder());
      add(scrollPane, BorderLayout.CENTER);

      // Click listeners
      addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          onDaySelected();
        }
      });

      eventsArea.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
          onEventAreaClicked(e);
        }
      });

      updateEvents();
    }

    private void updateEvents() {
      try {
        dayEvents.clear();
        List<Event> events = controller.getEventsOnDateAsList(date);
        dayEvents.addAll(events);

        // Use helper to format events text - FIXED INTEGRATION
        String eventsText = CalendarViewHelper.formatEventsText(events);
        eventsArea.setText(eventsText);

        // Color coding for days with events
        if (!events.isEmpty()) {
          setBackground(new Color(240, 255, 240)); // Light green for days with events
        } else {
          setBackground(Color.WHITE);
        }

      } catch (Exception e) {
        eventsArea.setText("Error loading events");
      }
    }

    /**
     * Handles day selection when the panel is clicked.
     */
    public void onDaySelected() {
      // Reset previous selection
      if (selectedDayPanel != null) {
        selectedDayPanel.setBackground(Color.WHITE);
        selectedDayPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
      }

      // Set this as selected
      selectedDayPanel = this;
      setBackground(selectedDayColor);
      setBorder(BorderFactory.createLineBorder(new Color(70, 130, 180), 2));

      // Update event details - FIXED INTEGRATION
      updateEventDetails();

      // Clear event selection
      selectedEvent = null;
    }

    private void onEventAreaClicked(MouseEvent e) {
      if (dayEvents.isEmpty()) {
        return;
      }

      // Simple event selection - select first event for now
      // In a more advanced version, you could calculate which event was clicked
      selectedEvent = dayEvents.get(0); // Select first event
      showEventDetails(selectedEvent);
    }

    private void updateEventDetails() {
      // Use helper to format events for date - FIXED INTEGRATION
      String formattedDetails = CalendarViewHelper.formatEventsForDate(dayEvents, date);
      eventDetailsArea.setText(formattedDetails);
    }

    private void showEventDetails(Event event) {
      // Use helper to format event details - FIXED INTEGRATION
      String formattedDetails = CalendarViewHelper.formatEventDetails(event);
      eventDetailsArea.setText(formattedDetails);
    }

    /**
     * Gets the date represented by this panel.
     */
    public LocalDate getDate() {
      return date;
    }
  }

  /**
   * Modern Event Dialog for creating/editing events.
   */
  public static class EventDialog extends JDialog {
    private boolean confirmed = false;
    private final EventData eventData;
    private final LocalDate defaultDate;

    private JTextField subjectField;
    private JRadioButton allDayEventRadio;
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    private JSpinner startTimeSpinner;
    private JSpinner endTimeSpinner;
    private JCheckBox recurringCheckbox;
    private JCheckBox mondayCheck;
    private JCheckBox tuesdayCheck;
    private JCheckBox wednesdayCheck;
    private JCheckBox thursdayCheck;
    private JCheckBox fridayCheck;
    private JCheckBox saturdayCheck;
    private JCheckBox sundayCheck;
    private JSpinner occurrencesSpinner;
    private JSpinner untilDateSpinner;
    private JTextArea descriptionArea;
    private JTextField locationField;
    private JComboBox<EventStatus> statusCombo;

    /**
     * Constructs an event dialog for creating or editing events.
     *
     * @param parent the parent frame
     * @param title the dialog title
     * @param existingData existing event data for editing, or null for new event
     * @param defaultDate the default date for the event
     */
    public EventDialog(Frame parent, String title, EventData existingData, LocalDate defaultDate) {
      super(parent, title, true);
      this.eventData = existingData != null ? existingData : new EventData();
      this.defaultDate = defaultDate;
      initializeDialog();
    }

    private void initializeDialog() {
      setLayout(new BorderLayout(10, 10));
      setSize(500, 650);
      setLocationRelativeTo(getParent());
      setResizable(false);

      // Main form panel
      JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
      formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

      // Subject
      formPanel.add(new JLabel("📝 Subject:"));
      subjectField = new JTextField(eventData.getSubject());
      formPanel.add(subjectField);

      // Event type
      formPanel.add(new JLabel("🕒 Event Type:"));
      final JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
      ButtonGroup typeGroup = new ButtonGroup();
      JRadioButton timedEventRadio = new JRadioButton("Timed Event", !eventData.isAllDay());
      allDayEventRadio = new JRadioButton("All Day Event", eventData.isAllDay());
      typeGroup.add(timedEventRadio);
      typeGroup.add(allDayEventRadio);
      timedEventRadio.addActionListener(e -> toggleTimeFields());
      allDayEventRadio.addActionListener(e -> toggleTimeFields());
      typePanel.add(timedEventRadio);
      typePanel.add(allDayEventRadio);
      formPanel.add(typePanel);

      // Start date
      formPanel.add(new JLabel("📅 Start Date:"));
      startDateSpinner = new JSpinner(new SpinnerDateModel());
      JSpinner.DateEditor startDateEditor = new JSpinner.DateEditor(startDateSpinner, "yyyy-MM-dd");
      startDateSpinner.setEditor(startDateEditor);
      startDateSpinner.setValue(java.sql.Date.valueOf(
          eventData.getStartDate() != null ? eventData.getStartDate() : defaultDate));
      formPanel.add(startDateSpinner);

      // End date
      formPanel.add(new JLabel("📅 End Date:"));
      endDateSpinner = new JSpinner(new SpinnerDateModel());
      JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd");
      endDateSpinner.setEditor(endDateEditor);
      endDateSpinner.setValue(java.sql.Date.valueOf(
          eventData.getEndDate() != null ? eventData.getEndDate() : defaultDate));
      formPanel.add(endDateSpinner);

      // Start time
      formPanel.add(new JLabel("⏰ Start Time:"));
      startTimeSpinner = new JSpinner(new SpinnerDateModel());
      JSpinner.DateEditor startTimeEditor = new JSpinner.DateEditor(startTimeSpinner, "HH:mm");
      startTimeSpinner.setEditor(startTimeEditor);
      startTimeSpinner.setValue(java.sql.Time.valueOf(
          eventData.getStartTime() != null ? eventData.getStartTime() : LocalTime.of(9, 0)));
      formPanel.add(startTimeSpinner);

      // End time
      formPanel.add(new JLabel("⏰ End Time:"));
      endTimeSpinner = new JSpinner(new SpinnerDateModel());
      JSpinner.DateEditor endTimeEditor = new JSpinner.DateEditor(endTimeSpinner, "HH:mm");
      endTimeSpinner.setEditor(endTimeEditor);
      endTimeSpinner.setValue(java.sql.Time.valueOf(
          eventData.getEndTime() != null ? eventData.getEndTime() : LocalTime.of(10, 0)));
      formPanel.add(endTimeSpinner);

      // Recurring
      formPanel.add(new JLabel("🔄 Recurring:"));
      recurringCheckbox = new JCheckBox("Repeat event", eventData.isRecurring());
      recurringCheckbox.addActionListener(e -> toggleRecurringFields());
      formPanel.add(recurringCheckbox);

      // Repeat days with checkboxes
      formPanel.add(new JLabel("📆 Repeat Days:"));
      JPanel repeatPanel = new JPanel(new GridLayout(2, 4, 5, 5));
      repeatPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

      // Initialize checkboxes with existing data
      Set<DayOfWeek> existingDays = eventData.getRepeatDays();

      mondayCheck = new JCheckBox("Mon (M)", existingDays.contains(DayOfWeek.MONDAY));
      tuesdayCheck = new JCheckBox("Tue (T)", existingDays.contains(DayOfWeek.TUESDAY));
      wednesdayCheck = new JCheckBox("Wed (W)", existingDays.contains(DayOfWeek.WEDNESDAY));
      thursdayCheck = new JCheckBox("Thu (R)", existingDays.contains(DayOfWeek.THURSDAY));
      fridayCheck = new JCheckBox("Fri (F)", existingDays.contains(DayOfWeek.FRIDAY));
      saturdayCheck = new JCheckBox("Sat (S)", existingDays.contains(DayOfWeek.SATURDAY));
      sundayCheck = new JCheckBox("Sun (U)", existingDays.contains(DayOfWeek.SUNDAY));

      repeatPanel.add(mondayCheck);
      repeatPanel.add(tuesdayCheck);
      repeatPanel.add(wednesdayCheck);
      repeatPanel.add(thursdayCheck);
      repeatPanel.add(fridayCheck);
      repeatPanel.add(saturdayCheck);
      repeatPanel.add(sundayCheck);

      formPanel.add(repeatPanel);

      // Occurrences
      formPanel.add(new JLabel("🔢 Occurrences:"));
      occurrencesSpinner = new JSpinner(new SpinnerNumberModel(
          eventData.getOccurrences() != null ? eventData.getOccurrences() : 5, 1, 365, 1));
      formPanel.add(occurrencesSpinner);

      // Until date
      formPanel.add(new JLabel("📅 Until Date:"));
      untilDateSpinner = new JSpinner(new SpinnerDateModel());
      JSpinner.DateEditor untilDateEditor = new JSpinner.DateEditor(untilDateSpinner, "yyyy-MM-dd");
      untilDateSpinner.setEditor(untilDateEditor);
      if (eventData.getUntilDate() != null) {
        untilDateSpinner.setValue(java.sql.Date.valueOf(eventData.getUntilDate()));
      }
      formPanel.add(untilDateSpinner);

      // Description
      formPanel.add(new JLabel("📋 Description:"));
      descriptionArea = new JTextArea(3, 20);
      if (eventData.getDescription() != null) {
        descriptionArea.setText(eventData.getDescription());
      }
      formPanel.add(new JScrollPane(descriptionArea));

      // Location
      formPanel.add(new JLabel("📍 Location:"));
      locationField = new JTextField(eventData.getLocation());
      formPanel.add(locationField);

      // Status
      formPanel.add(new JLabel("🔒 Status:"));
      statusCombo = new JComboBox<>(EventStatus.values());
      statusCombo.setSelectedItem(eventData.getStatus() != null
          ? eventData.getStatus() : EventStatus.PUBLIC);
      formPanel.add(statusCombo);

      add(formPanel, BorderLayout.CENTER);

      // Buttons
      JPanel buttonPanel = new JPanel(new FlowLayout());
      JButton okButton = new JButton("✅ Save Event");
      okButton.addActionListener(e -> onOk());
      buttonPanel.add(okButton);

      JButton cancelButton = new JButton("❌ Cancel");
      cancelButton.addActionListener(e -> onCancel());
      buttonPanel.add(cancelButton);

      add(buttonPanel, BorderLayout.SOUTH);

      // Initialize field states
      toggleTimeFields();
      toggleRecurringFields();
    }

    private void toggleTimeFields() {
      boolean allDay = allDayEventRadio.isSelected();
      startTimeSpinner.setEnabled(!allDay);
      endTimeSpinner.setEnabled(!allDay);
    }

    private void toggleRecurringFields() {
      boolean recurring = recurringCheckbox.isSelected();
      mondayCheck.setEnabled(recurring);
      tuesdayCheck.setEnabled(recurring);
      wednesdayCheck.setEnabled(recurring);
      thursdayCheck.setEnabled(recurring);
      fridayCheck.setEnabled(recurring);
      saturdayCheck.setEnabled(recurring);
      sundayCheck.setEnabled(recurring);
      occurrencesSpinner.setEnabled(recurring);
      untilDateSpinner.setEnabled(recurring);
    }

    private void onOk() {
      // Validate input - FIXED: Use CalendarViewHelper
      if (!CalendarViewHelper.isValidEventSubject(subjectField.getText())) {
        JOptionPane.showMessageDialog(this, "Subject is required", "Error",
            JOptionPane.ERROR_MESSAGE);
        return;
      }

      // Gather data
      eventData.setSubject(subjectField.getText().trim());
      eventData.setAllDay(allDayEventRadio.isSelected());

      java.util.Date startDate = (java.util.Date) startDateSpinner.getValue();
      eventData.setStartDate(startDate.toInstant()
          .atZone(java.time.ZoneId.systemDefault()).toLocalDate());

      java.util.Date endDate = (java.util.Date) endDateSpinner.getValue();
      eventData.setEndDate(endDate.toInstant()
          .atZone(java.time.ZoneId.systemDefault()).toLocalDate());

      if (!eventData.isAllDay()) {
        java.util.Date startTime = (java.util.Date) startTimeSpinner.getValue();
        eventData.setStartTime(startTime.toInstant()
            .atZone(java.time.ZoneId.systemDefault()).toLocalTime());

        java.util.Date endTime = (java.util.Date) endTimeSpinner.getValue();
        eventData.setEndTime(endTime.toInstant()
            .atZone(java.time.ZoneId.systemDefault()).toLocalTime());
      } else {
        eventData.setStartTime(LocalTime.of(8, 0));
        eventData.setEndTime(LocalTime.of(17, 0));
      }

      eventData.setRecurring(recurringCheckbox.isSelected());
      if (eventData.isRecurring()) {
        // Collect selected days from checkboxes
        Set<DayOfWeek> selectedDays = java.util.EnumSet.noneOf(DayOfWeek.class);
        if (mondayCheck.isSelected()) {
          selectedDays.add(DayOfWeek.MONDAY);
        }
        if (tuesdayCheck.isSelected()) {
          selectedDays.add(DayOfWeek.TUESDAY);
        }
        if (wednesdayCheck.isSelected()) {
          selectedDays.add(DayOfWeek.WEDNESDAY);
        }
        if (thursdayCheck.isSelected()) {
          selectedDays.add(DayOfWeek.THURSDAY);
        }
        if (fridayCheck.isSelected()) {
          selectedDays.add(DayOfWeek.FRIDAY);
        }
        if (saturdayCheck.isSelected()) {
          selectedDays.add(DayOfWeek.SATURDAY);
        }
        if (sundayCheck.isSelected()) {
          selectedDays.add(DayOfWeek.SUNDAY);
        }

        // Validate at least one day selected
        if (selectedDays.isEmpty()) {
          JOptionPane.showMessageDialog(this,
              "Please select at least one day for recurring events",
              "Validation Error",
              JOptionPane.ERROR_MESSAGE);
          return;
        }

        eventData.setRepeatDays(selectedDays);
        eventData.setOccurrences((Integer) occurrencesSpinner.getValue());

        java.util.Date untilDate = (java.util.Date) untilDateSpinner.getValue();
        eventData.setUntilDate(untilDate.toInstant()
            .atZone(java.time.ZoneId.systemDefault()).toLocalDate());
      }

      eventData.setDescription(descriptionArea.getText().trim());
      eventData.setLocation(locationField.getText().trim());
      eventData.setStatus((EventStatus) statusCombo.getSelectedItem());

      confirmed = true;
      dispose();
    }

    /**
     * onCancel button.
     */
    public void onCancel() {
      confirmed = false;
      dispose();
    }

    /**
     * Checks if the dialog was confirmed (OK button pressed).
     *
     * @return true if confirmed, false otherwise
     */
    public boolean isConfirmed() {
      return confirmed;
    }

    /**
     * Gets the event data from the dialog.
     *
     * @return the event data
     */
    public EventData getEventData() {
      return eventData;
    }

    /**
     * Data container for event information.
     */
    public static class EventData {
      private String subject = "";
      private boolean allDay = false;
      private LocalDate startDate = LocalDate.now();
      private LocalDate endDate = LocalDate.now();
      private LocalTime startTime = LocalTime.of(9, 0);
      private LocalTime endTime = LocalTime.of(10, 0);
      private boolean recurring = false;
      private Set<DayOfWeek> repeatDays = java.util.EnumSet.of(DayOfWeek.MONDAY);
      private Integer occurrences = 5;
      private LocalDate untilDate = LocalDate.now().plusWeeks(4);
      private String description = "";
      private String location = "";
      private EventStatus status = EventStatus.PUBLIC;

      // Getters and setters
      /**
       * Gets the event subject.
       *
       * @return the subject
       */
      public String getSubject() {
        return subject;
      }

      /**
       * Sets the event subject.
       *
       * @param subject the subject to set
       */
      public void setSubject(String subject) {
        this.subject = subject;
      }

      /**
       * Checks if the event is all-day.
       *
       * @return true if all-day, false otherwise
       */
      public boolean isAllDay() {
        return allDay;
      }

      /**
       * Sets whether the event is all-day.
       *
       * @param allDay true for all-day, false for timed
       */
      public void setAllDay(boolean allDay) {
        this.allDay = allDay;
      }

      /**
       * Gets the start date.
       *
       * @return the start date
       */
      public LocalDate getStartDate() {
        return startDate;
      }

      /**
       * Sets the start date.
       *
       * @param startDate the start date to set
       */
      public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
      }

      /**
       * Gets the end date.
       *
       * @return the end date
       */
      public LocalDate getEndDate() {
        return endDate;
      }

      /**
       * Sets the end date.
       *
       * @param endDate the end date to set
       */
      public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
      }

      /**
       * Gets the start time.
       *
       * @return the start time
       */
      public LocalTime getStartTime() {
        return startTime;
      }

      /**
       * Sets the start time.
       *
       * @param startTime the start time to set
       */
      public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
      }

      /**
       * Gets the end time.
       *
       * @return the end time
       */
      public LocalTime getEndTime() {
        return endTime;
      }

      /**
       * Sets the end time.
       *
       * @param endTime the end time to set
       */
      public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
      }

      /**
       * Checks if the event is recurring.
       *
       * @return true if recurring, false otherwise
       */
      public boolean isRecurring() {
        return recurring;
      }

      /**
       * Sets whether the event is recurring.
       *
       * @param recurring true for recurring, false for single event
       */
      public void setRecurring(boolean recurring) {
        this.recurring = recurring;
      }

      /**
       * Gets the repeat days.
       *
       * @return the set of repeat days
       */
      public Set<DayOfWeek> getRepeatDays() {
        return repeatDays;
      }

      /**
       * Sets the repeat days.
       *
       * @param repeatDays the repeat days to set
       */
      public void setRepeatDays(Set<DayOfWeek> repeatDays) {
        this.repeatDays = repeatDays;
      }

      /**
       * Gets the repeat days as a string.
       *
       * @return the repeat days string
       */
      public String getRepeatDaysString() {
        // Use helper to format repeat days
        return CalendarViewHelper.formatRepeatDays(repeatDays);
      }

      /**
       * Gets the number of occurrences.
       *
       * @return the number of occurrences
       */
      public Integer getOccurrences() {
        return occurrences;
      }

      /**
       * Sets the number of occurrences.
       *
       * @param occurrences the number of occurrences to set
       */
      public void setOccurrences(Integer occurrences) {
        this.occurrences = occurrences;
      }

      /**
       * Gets the until date.
       *
       * @return the until date
       */
      public LocalDate getUntilDate() {
        return untilDate;
      }

      /**
       * Sets the until date.
       *
       * @param untilDate the until date to set
       */
      public void setUntilDate(LocalDate untilDate) {
        this.untilDate = untilDate;
      }

      /**
       * Gets the description.
       *
       * @return the description
       */
      public String getDescription() {
        return description;
      }

      /**
       * Sets the description.
       *
       * @param description the description to set
       */
      public void setDescription(String description) {
        this.description = description;
      }

      /**
       * Gets the location.
       *
       * @return the location
       */
      public String getLocation() {
        return location;
      }

      /**
       * Sets the location.
       *
       * @param location the location to set
       */
      public void setLocation(String location) {
        this.location = location;
      }

      /**
       * Gets the event status.
       *
       * @return the event status
       */
      public EventStatus getStatus() {
        return status;
      }

      /**
       * Sets the event status.
       *
       * @param status the event status to set
       */
      public void setStatus(EventStatus status) {
        this.status = status;
      }
    }
  }
}