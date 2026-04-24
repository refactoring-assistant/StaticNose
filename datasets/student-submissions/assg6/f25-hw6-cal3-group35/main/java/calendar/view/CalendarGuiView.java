package calendar.view;

import calendar.controller.GuiController;
import calendar.model.Event;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
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

/**
 * Graphical user interface for the calendar application.
 * Displays a month view with navigation and event management features.
 */
public class CalendarGuiView extends JFrame {

  private static final int FRAME_WIDTH = 1000;
  private static final int FRAME_HEIGHT = 700;

  private final GuiController controller;
  private JPanel calendarPanel;
  private JLabel monthYearLabel;
  private JTextArea eventDetailsArea;
  private JComboBox<String> calendarSelector;
  private JButton prevMonthButton;
  private JButton nextMonthButton;
  private LocalDate currentDate;
  private LocalDate selectedDate;
  private final Map<String, Color> calendarColorMap;

  /**
   * Constructs the Gui view.
   *
   * @param controller the Gui controller
   * @param colorMap map of calendar names to colors
   */
  public CalendarGuiView(GuiController controller, Map<String, Color> colorMap) {
    this.controller = controller;
    this.currentDate = LocalDate.now();
    this.selectedDate = LocalDate.now();
    this.calendarColorMap = colorMap;

    initializeFrame();
    initializeComponents();
    layoutComponents();
    updateCalendarDisplay();
  }

  /**
   * Initializes the main frame properties.
   */
  private void initializeFrame() {
    setTitle("Calendar Application");
    setSize(FRAME_WIDTH, FRAME_HEIGHT);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);
  }

  /**
   * Initializes all Gui components.
   */
  private void initializeComponents() {
    calendarPanel = new JPanel(new GridLayout(7, 7, 5, 5));
    calendarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    monthYearLabel = new JLabel("", SwingConstants.CENTER);
    monthYearLabel.setFont(new Font("Arial", Font.BOLD, 24));

    prevMonthButton = new JButton("< Previous");
    prevMonthButton.addActionListener(e -> navigateMonth(-1));

    nextMonthButton = new JButton("Next >");
    nextMonthButton.addActionListener(e -> navigateMonth(1));

    eventDetailsArea = new JTextArea(10, 30);
    eventDetailsArea.setEditable(false);
    eventDetailsArea.setLineWrap(true);
    eventDetailsArea.setWrapStyleWord(true);
    eventDetailsArea.setFont(new Font("Dialog", Font.PLAIN, 12));

    calendarSelector = new JComboBox<>();
    calendarSelector.addActionListener(e -> handleCalendarChange());

    updateCalendarSelector();
  }

  /**
   * Lays out all components in the frame.
   */
  private void layoutComponents() {
    setLayout(new BorderLayout(10, 10));

    JPanel topPanel = createTopPanel();
    add(topPanel, BorderLayout.NORTH);

    add(calendarPanel, BorderLayout.CENTER);

    JPanel rightPanel = createRightPanel();
    add(rightPanel, BorderLayout.EAST);
  }

  /**
   * Creates the top panel with navigation and calendar selector.
   *
   * @return the top panel
   */
  private JPanel createTopPanel() {
    JPanel topPanel = new JPanel(new BorderLayout());
    topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
    navPanel.add(prevMonthButton);
    navPanel.add(monthYearLabel);
    navPanel.add(nextMonthButton);

    JPanel calendarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    calendarPanel.add(new JLabel("Calendar: "));
    calendarPanel.add(calendarSelector);

    JButton newCalendarButton = new JButton("New Calendar");
    newCalendarButton.addActionListener(e -> handleNewCalendar());
    calendarPanel.add(newCalendarButton);

    topPanel.add(calendarPanel, BorderLayout.WEST);
    topPanel.add(navPanel, BorderLayout.CENTER);

    return topPanel;
  }

  /**
   * Creates the right panel with event details and action buttons.
   *
   * @return the right panel
   */
  private JPanel createRightPanel() {
    JPanel rightPanel = new JPanel();
    rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
    rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    rightPanel.setPreferredSize(new Dimension(300, 0));

    JLabel detailsLabel = new JLabel("Event Details");
    detailsLabel.setFont(new Font("Arial", Font.BOLD, 16));
    detailsLabel.setAlignmentX(LEFT_ALIGNMENT);
    rightPanel.add(detailsLabel);
    rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));

    JScrollPane scrollPane = new JScrollPane(eventDetailsArea);
    scrollPane.setAlignmentX(LEFT_ALIGNMENT);
    rightPanel.add(scrollPane);
    rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));

    JButton createEventButton = new JButton("Create Event");
    createEventButton.setAlignmentX(LEFT_ALIGNMENT);
    createEventButton.addActionListener(e -> handleCreateEvent());
    rightPanel.add(createEventButton);
    rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));

    JButton editEventButton = new JButton("Edit Events");
    editEventButton.setAlignmentX(LEFT_ALIGNMENT);
    editEventButton.addActionListener(e -> handleEditEvent());
    rightPanel.add(editEventButton);
    rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));

    JButton deleteEventButton = new JButton("Delete Events");
    deleteEventButton.setAlignmentX(LEFT_ALIGNMENT);
    deleteEventButton.addActionListener(e -> handleDeleteEvent());
    rightPanel.add(deleteEventButton);

    return rightPanel;
  }

  /**
   * Updates the calendar display with current month.
   */
  private void updateCalendarDisplay() {
    calendarPanel.removeAll();

    YearMonth yearMonth = YearMonth.of(currentDate.getYear(), currentDate.getMonth());
    monthYearLabel.setText(yearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH)
        + " " + yearMonth.getYear());

    addDayHeaders();
    addDayCells(yearMonth);

    calendarPanel.revalidate();
    calendarPanel.repaint();
  }

  /**
   * Adds day-of-week headers to the calendar.
   */
  private void addDayHeaders() {
    for (DayOfWeek day : DayOfWeek.values()) {
      JLabel header = new JLabel(day.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
          SwingConstants.CENTER);
      header.setFont(new Font("Arial", Font.BOLD, 12));
      header.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
      calendarPanel.add(header);
    }
  }

  /**
   * Adds day cells to the calendar grid.
   *
   * @param yearMonth the year-month to display
   */
  private void addDayCells(YearMonth yearMonth) {
    LocalDate firstOfMonth = yearMonth.atDay(1);
    int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();
    int daysInMonth = yearMonth.lengthOfMonth();

    int startOffset = dayOfWeek - 1;

    for (int i = 0; i < startOffset; i++) {
      calendarPanel.add(createEmptyCell());
    }

    for (int day = 1; day <= daysInMonth; day++) {
      LocalDate date = yearMonth.atDay(day);
      calendarPanel.add(createDayCell(date));
    }

    int totalCells = startOffset + daysInMonth;
    int remainingCells = 42 - totalCells;
    for (int i = 0; i < remainingCells; i++) {
      calendarPanel.add(createEmptyCell());
    }
  }

  /**
   * Creates an empty calendar cell.
   *
   * @return the empty cell panel
   */
  private JPanel createEmptyCell() {
    JPanel cell = new JPanel();
    cell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    return cell;
  }

  /**
   * Creates a calendar cell for a specific date.
   *
   * @param date the date for this cell
   * @return the day cell panel
   */
  private JPanel createDayCell(LocalDate date) {
    JPanel cell = new JPanel(new BorderLayout());
    cell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

    if (date.equals(selectedDate)) {
      cell.setBackground(new Color(220, 240, 255));
    } else if (date.equals(LocalDate.now())) {
      cell.setBackground(new Color(255, 255, 220));
    }

    JLabel dayLabel = new JLabel(String.valueOf(date.getDayOfMonth()));
    dayLabel.setHorizontalAlignment(SwingConstants.CENTER);
    dayLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    cell.add(dayLabel, BorderLayout.NORTH);

    int eventCount = (int) controller.getEventsForDate(date).stream()
        .filter(e -> !e.getSubject().startsWith("DELETED_"))
        .count();
    if (eventCount > 0) {
      JLabel eventIndicator = new JLabel(eventCount + " event" + (eventCount > 1 ? "s" : ""),
          SwingConstants.CENTER);
      eventIndicator.setForeground(getActiveCalendarColor());
      eventIndicator.setFont(new Font("Dialog", Font.PLAIN, 10));
      cell.add(eventIndicator, BorderLayout.CENTER);
    }

    cell.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        handleDayClick(date);
      }

      @Override
      public void mouseEntered(java.awt.event.MouseEvent e) {
        if (!date.equals(selectedDate) && !date.equals(LocalDate.now())) {
          cell.setBackground(new Color(240, 240, 240));
        }
      }

      @Override
      public void mouseExited(java.awt.event.MouseEvent e) {
        if (!date.equals(selectedDate)) {
          if (date.equals(LocalDate.now())) {
            cell.setBackground(new Color(255, 255, 220));
          } else {
            cell.setBackground(null);
          }
        }
      }
    });

    return cell;
  }

  /**
   * Handles navigation between months.
   *
   * @param monthOffset number of months to navigate
   */
  private void navigateMonth(int monthOffset) {
    currentDate = currentDate.plusMonths(monthOffset);
    updateCalendarDisplay();
  }

  /**
   * Handles day cell click event.
   *
   * @param date the clicked date
   */
  private void handleDayClick(LocalDate date) {
    selectedDate = date;
    updateCalendarDisplay();
    displayEventsForDate(date);
  }

  /**
   * Displays events for a selected date.
   *
   * @param date the date to display events for
   */
  private void displayEventsForDate(LocalDate date) {
    List<Event> events = controller.getEventsForDate(date);

    List<Event> visibleEvents = events.stream()
        .filter(e -> !e.getSubject().startsWith("DELETED_"))
        .collect(Collectors.toList());

    if (visibleEvents.isEmpty()) {
      eventDetailsArea.setText("No events on " + date + "\n\n"
          + "Click 'Create Event' to add a new event.");
    } else {
      StringBuilder sb = new StringBuilder();
      sb.append("Events on ").append(date).append(":\n\n");

      int index = 1;
      for (Event event : visibleEvents) {
        sb.append(index++).append(". ").append(event.getSubject()).append("\n");
        sb.append("   Time: ").append(event.getStart().toLocalTime())
            .append(" - ").append(event.getEnd().toLocalTime()).append("\n");

        if (event.getLocation().isPresent()) {
          sb.append("   Location: ").append(event.getLocation().get()).append("\n");
        }

        if (event.getDescription().isPresent()) {
          sb.append("   Description: ").append(event.getDescription().get()).append("\n");
        }

        sb.append("\n");
      }

      eventDetailsArea.setText(sb.toString());
    }
  }

  /**
   * Handles calendar selection change.
   */
  private void handleCalendarChange() {
    String selectedCalendar = (String) calendarSelector.getSelectedItem();
    if (selectedCalendar != null) {
      controller.useCalendar(selectedCalendar);
      updateCalendarDisplay();
      displayEventsForDate(selectedDate);
    }
  }

  /**
   * Handles new calendar creation.
   */
  private void handleNewCalendar() {
    CreateCalendarDialog dialog = new CreateCalendarDialog(this);
    dialog.setVisible(true);

    if (dialog.isConfirmed()) {
      String name = dialog.getCalendarName();
      String timezone = dialog.getTimezone();

      try {
        controller.createCalendar(name, timezone);
        updateCalendarSelector();
        calendarSelector.setSelectedItem(name);
        showMessage("Calendar '" + name + "' created successfully!");
      } catch (IllegalArgumentException e) {
        showError("Failed to create calendar: " + e.getMessage());
      }
    }
  }

  /**
   * Handles event creation.
   */
  private void handleCreateEvent() {
    CreateEventDialog dialog = new CreateEventDialog(this, selectedDate);
    dialog.setVisible(true);

    if (dialog.isConfirmed()) {
      try {
        controller.createEvent(
            dialog.getEventSubject(),
            dialog.getStartDateTime(),
            dialog.getEndDateTime(),
            dialog.getEventLocation(),
            dialog.getDescription(),
            dialog.isRecurring(),
            dialog.getRecurringDays(),
            dialog.getRecurrenceCount(),
            dialog.getRecurrenceEndDate()
        );

        updateCalendarDisplay();
        displayEventsForDate(selectedDate);
        showMessage("Event created successfully!");
      } catch (IllegalArgumentException e) {
        showError("Failed to create event: " + e.getMessage());
      }
    }
  }

  /**
   * Handles event editing.
   */
  private void handleEditEvent() {
    List<Event> events = controller.getEventsForDate(selectedDate);

    List<Event> visibleEvents = events.stream()
        .filter(e -> !e.getSubject().startsWith("DELETED_"))
        .collect(Collectors.toList());

    if (visibleEvents.isEmpty()) {
      showError("No events to edit on " + selectedDate);
      return;
    }

    String[] eventNames = visibleEvents.stream()
        .map(Event::getSubject)
        .distinct()
        .toArray(String[]::new);

    String selectedEvent = (String) JOptionPane.showInputDialog(
        this,
        "Select event to edit:",
        "Edit Event",
        JOptionPane.QUESTION_MESSAGE,
        null,
        eventNames,
        eventNames[0]
    );

    if (selectedEvent != null) {
      Event eventToEdit = visibleEvents.stream()
          .filter(e -> e.getSubject().equals(selectedEvent))
          .findFirst()
          .orElse(null);

      if (eventToEdit == null) {
        showError("Could not find event: " + selectedEvent);
        return;
      }

      EditEventDialog dialog = new EditEventDialog(this, selectedEvent, visibleEvents);
      dialog.setVisible(true);

      if (dialog.isConfirmed()) {
        try {
          controller.editEvent(
              selectedEvent,
              eventToEdit.getStart(),
              dialog.getEditScope(),
              dialog.getProperty(),
              dialog.getNewValue()
          );

          updateCalendarDisplay();
          displayEventsForDate(selectedDate);
          showMessage("Event updated successfully!");
        } catch (IllegalArgumentException e) {
          showError("Failed to edit event: " + e.getMessage());
        }
      }
    }
  }

  /**
   * Handles event deletion.
   */
  private void handleDeleteEvent() {
    List<Event> events = controller.getEventsForDate(selectedDate);

    List<Event> visibleEvents = events.stream()
        .filter(e -> !e.getSubject().startsWith("DELETED_"))
        .collect(Collectors.toList());

    if (visibleEvents.isEmpty()) {
      showError("No events to delete on " + selectedDate);
      return;
    }

    String[] eventNames = visibleEvents.stream()
        .map(Event::getSubject)
        .distinct()
        .toArray(String[]::new);

    String selectedEvent = (String) JOptionPane.showInputDialog(
        this,
        "Select event to delete:",
        "Delete Event",
        JOptionPane.QUESTION_MESSAGE,
        null,
        eventNames,
        eventNames[0]
    );

    if (selectedEvent != null) {
      Event eventToDelete = visibleEvents.stream()
          .filter(e -> e.getSubject().equals(selectedEvent))
          .findFirst()
          .orElse(null);

      if (eventToDelete == null) {
        showError("Could not find event: " + selectedEvent);
        return;
      }

      int confirm = JOptionPane.showConfirmDialog(
          this,
          "Delete all occurrences of '" + selectedEvent + "'?",
          "Confirm Delete",
          JOptionPane.YES_NO_OPTION
      );

      if (confirm == JOptionPane.YES_OPTION) {
        try {
          controller.deleteEvent(selectedEvent, eventToDelete.getStart());
          updateCalendarDisplay();
          displayEventsForDate(selectedDate);
          showMessage("Event deleted successfully!");
        } catch (IllegalArgumentException e) {
          showError("Failed to delete event: " + e.getMessage());
        }
      }
    }
  }

  /**
   * Updates the calendar selector with available calendars.
   */
  private void updateCalendarSelector() {
    calendarSelector.removeAllItems();
    for (String name : controller.getAllCalendarNames()) {
      calendarSelector.addItem(name);
    }

    String activeCalendar = controller.getActiveCalendarName();
    if (activeCalendar != null) {
      calendarSelector.setSelectedItem(activeCalendar);
    }
  }

  /**
   * Gets the color for the active calendar.
   *
   * @return the color
   */
  private Color getActiveCalendarColor() {
    String activeName = controller.getActiveCalendarName();
    return calendarColorMap.getOrDefault(activeName, Color.BLUE);
  }

  /**
   * Shows an information message.
   *
   * @param message the message to display
   */
  private void showMessage(String message) {
    JOptionPane.showMessageDialog(this, message, "Success",
        JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Shows an error message.
   *
   * @param message the error message
   */
  private void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "Error",
        JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Displays the Gui.
   */
  public void display() {
    SwingUtilities.invokeLater(() -> setVisible(true));
  }
}