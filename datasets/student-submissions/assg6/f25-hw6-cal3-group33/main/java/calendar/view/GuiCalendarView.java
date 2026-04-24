package calendar.view;

import calendar.controller.CalendarFeatures;
import calendar.model.event.EventInterface;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

/**
 * Main GUI window for the calendar application.
 * Implements CalendarView interface for command compatibility.
 */
public class GuiCalendarView extends JFrame implements GuiCalendarInterface {

  private static final DateTimeFormatter MONTH_YEAR_FORMATTER =
      DateTimeFormatter.ofPattern("MMMM yyyy");

  private JPanel toolbarPanel;
  private JPanel leftPanel;
  private JPanel mainPanel;

  private JButton createEventBtn;
  private JButton exportBtn;
  private JButton prevMonthBtn;
  private JLabel monthYearLabel;
  private JButton nextMonthBtn;

  private JLabel selectedNameLabel;
  private JLabel selectedTzLabel;
  private JButton editPropsBtn;
  private JButton createCalendarBtn;

  private CalendarFeatures controller;
  private MonthViewPanel monthViewPanel;

  private final Map<String, Color> calendarColors;

  private final Map<String, String> calendarTimezones;

  private String currentCalendarName = "Default";

  /**
   * Constructs the main calendar GUI window.
   */
  public GuiCalendarView() {
    super("Calendar Application");

    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1200, 800);
    setMinimumSize(new Dimension(900, 600));
    setLocationRelativeTo(null);
    setLayout(new BorderLayout());

    this.calendarColors = new HashMap<>();

    this.calendarColors.put("Default", new Color(66, 133, 244));

    this.calendarTimezones = new HashMap<>();
    this.calendarTimezones.put("Default", ZoneId.systemDefault().getId());

    initializeToolbar();
    initializeLeftPanel();
    initializeMainPanel();

    add(toolbarPanel, BorderLayout.NORTH);
    add(leftPanel, BorderLayout.WEST);
    add(mainPanel, BorderLayout.CENTER);

    setupBasicListeners();
  }

  /**
   * Main method for testing the GUI independently.
   */
  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      GuiCalendarView gui = new GuiCalendarView();
      gui.setVisible(true);
    });
  }

  @Override
  public void setFeatures(CalendarFeatures controller) {
    this.controller = controller;
    setupControllerListeners();

    try {
      List<String> calendars = controller.getAllCalendarNames();
      String currentCalendar = controller.getCurrentCalendarName();
      updateCalendarList(calendars, currentCalendar);

      controller.refreshCurrentMonth();
    } catch (Exception e) {
      System.out.println("Startup: " + e.getMessage());
    }
  }

  /**
   * Initializes the top toolbar with styled components.
   */
  private void initializeToolbar() {
    toolbarPanel = new JPanel(new BorderLayout());
    toolbarPanel.setPreferredSize(new Dimension(0, 50));
    toolbarPanel.setBackground(Color.WHITE);
    toolbarPanel.setOpaque(true);
    toolbarPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

    JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
    leftButtons.setBackground(Color.WHITE);
    leftButtons.setOpaque(true);

    createEventBtn = new JButton("+ Event");
    createEventBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
    createEventBtn.setBackground(new Color(66, 133, 244));
    createEventBtn.setForeground(Color.WHITE);
    createEventBtn.setOpaque(true);
    createEventBtn.setContentAreaFilled(true);
    createEventBtn.setFocusPainted(false);
    createEventBtn.setBorderPainted(false);
    createEventBtn.setPreferredSize(new Dimension(100, 32));
    createEventBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

    exportBtn = new JButton("Export ▼");
    exportBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
    exportBtn.setOpaque(true);
    exportBtn.setContentAreaFilled(true);
    exportBtn.setFocusPainted(false);
    exportBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

    leftButtons.add(createEventBtn);
    leftButtons.add(exportBtn);

    JPanel rightNav = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
    rightNav.setBackground(Color.WHITE);
    rightNav.setOpaque(true);

    prevMonthBtn = new JButton("<");
    prevMonthBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
    prevMonthBtn.setOpaque(true);
    prevMonthBtn.setContentAreaFilled(true);
    prevMonthBtn.setFocusPainted(false);
    prevMonthBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

    monthYearLabel = new JLabel("November 2025");
    monthYearLabel.setFont(new Font("SansSerif", Font.BOLD, 18));

    nextMonthBtn = new JButton(">");
    nextMonthBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
    nextMonthBtn.setOpaque(true);
    nextMonthBtn.setContentAreaFilled(true);
    nextMonthBtn.setFocusPainted(false);
    nextMonthBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

    rightNav.add(prevMonthBtn);
    rightNav.add(monthYearLabel);
    rightNav.add(nextMonthBtn);

    toolbarPanel.add(leftButtons, BorderLayout.WEST);
    toolbarPanel.add(rightNav, BorderLayout.EAST);
  }

  /**
   * Initializes the left panel with styled components.
   */
  private void initializeLeftPanel() {
    leftPanel = new JPanel(new BorderLayout());
    leftPanel.setPreferredSize(new Dimension(260, 0));
    leftPanel.setMinimumSize(new Dimension(260, 0));
    leftPanel.setBackground(new Color(245, 245, 245));
    leftPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY));

    JPanel selectedCalendarPanel = createSelectedCalendarPanel();
    JPanel calendarListContainer = createCalendarListPanel();
    JPanel bottomPanel = createBottomPanel();

    leftPanel.add(selectedCalendarPanel, BorderLayout.NORTH);
    leftPanel.add(calendarListContainer, BorderLayout.CENTER);
    leftPanel.add(bottomPanel, BorderLayout.SOUTH);
  }

  /**
   * Creates the selected calendar info section.
   */
  private JPanel createSelectedCalendarPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBackground(new Color(245, 245, 245));
    panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

    JLabel header = new JLabel("SELECTED CALENDAR");
    header.setFont(new Font("SansSerif", Font.BOLD, 11));
    header.setForeground(new Color(100, 100, 100));
    header.setAlignmentX(Component.LEFT_ALIGNMENT);

    selectedNameLabel = new JLabel("Name: Default");
    selectedNameLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
    selectedNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    selectedTzLabel = new JLabel("Timezone: " + ZoneId.systemDefault().getId());
    selectedTzLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
    selectedTzLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

    editPropsBtn = new JButton("Edit Properties");
    editPropsBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
    editPropsBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
    editPropsBtn.setMaximumSize(new Dimension(230, 30));
    editPropsBtn.setFocusPainted(false);
    editPropsBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

    panel.add(header);
    panel.add(Box.createVerticalStrut(10));
    panel.add(selectedNameLabel);
    panel.add(Box.createVerticalStrut(5));
    panel.add(selectedTzLabel);
    panel.add(Box.createVerticalStrut(10));
    panel.add(editPropsBtn);

    return panel;
  }

  /**
   * Creates the scrollable calendar list section.
   */
  private JPanel createCalendarListPanel() {
    JPanel container = new JPanel(new BorderLayout());
    container.setBackground(Color.WHITE);

    JPanel listPanel = new JPanel();
    listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
    listPanel.setBackground(Color.WHITE);
    listPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

    JLabel header = new JLabel("CALENDARS");
    header.setFont(new Font("SansSerif", Font.BOLD, 11));
    header.setForeground(new Color(100, 100, 100));
    header.setAlignmentX(Component.LEFT_ALIGNMENT);

    Color blueColor = new Color(66, 133, 244);
    JRadioButton defaultCal = createCalendarRadioButton("Default", blueColor);
    defaultCal.setSelected(true);

    ButtonGroup group = new ButtonGroup();
    group.add(defaultCal);

    defaultCal.addActionListener(e -> {
      if (defaultCal.isSelected()) {
        handleCalendarSelection("Default", ZoneId.systemDefault().getId(), blueColor);
      }
    });

    listPanel.add(header);
    listPanel.add(Box.createVerticalStrut(8));
    listPanel.add(defaultCal);
    listPanel.add(Box.createVerticalGlue());

    JScrollPane scrollPane = new JScrollPane(listPanel);
    scrollPane.setBorder(null);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);

    container.add(scrollPane, BorderLayout.CENTER);
    return container;
  }

  /**
   * Creates a styled radio button for a calendar with color indicator.
   */
  private JRadioButton createCalendarRadioButton(String name, Color color) {
    JRadioButton button = new JRadioButton(name);
    button.setIcon(createOutlinedCircleIcon(color, 12));
    button.setSelectedIcon(createFilledCircleIcon(color, 12));
    button.setFont(new Font("SansSerif", Font.PLAIN, 13));
    button.setBackground(Color.WHITE);
    button.setFocusPainted(false);
    button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    button.setAlignmentX(Component.LEFT_ALIGNMENT);
    return button;
  }

  /**
   * Creates the bottom panel with "+ Create Calendar" button.
   */
  private JPanel createBottomPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 15));
    panel.setBackground(new Color(245, 245, 245));
    panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));

    createCalendarBtn = new JButton("+ Create Calendar");
    createCalendarBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
    createCalendarBtn.setPreferredSize(new Dimension(180, 32));
    createCalendarBtn.setFocusPainted(false);
    createCalendarBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

    panel.add(createCalendarBtn);
    return panel;
  }

  /**
   * Initializes the main calendar panel with month view.
   */
  private void initializeMainPanel() {
    mainPanel = new JPanel(new BorderLayout());
    mainPanel.setBackground(Color.WHITE);
    mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    monthViewPanel = new MonthViewPanel();
    mainPanel.add(monthViewPanel, BorderLayout.CENTER);

    updateMonthLabel();
  }

  /**
   * Creates a filled circle icon for selected calendar state.
   */
  private Icon createFilledCircleIcon(Color color, int size) {
    return new Icon() {
      @Override
      public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.fillOval(x, y, size, size);
        g2.dispose();
      }

      @Override
      public int getIconWidth() {
        return size;
      }

      @Override
      public int getIconHeight() {
        return size;
      }
    };
  }

  /**
   * Creates an outlined circle icon for unselected calendar state.
   */
  private Icon createOutlinedCircleIcon(Color color, int size) {
    return new Icon() {
      @Override
      public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new BasicStroke(2.0f));
        g2.drawOval(x + 1, y + 1, size - 2, size - 2);
        g2.dispose();
      }

      @Override
      public int getIconWidth() {
        return size;
      }

      @Override
      public int getIconHeight() {
        return size;
      }
    };
  }

  /**
   * Sets up basic listeners that work without controller.
   */
  private void setupBasicListeners() {
    prevMonthBtn.addActionListener(e -> {
      monthViewPanel.previousMonth();
      updateMonthLabel();
      if (controller != null) {
        controller.refreshCurrentMonth();
      }
    });

    nextMonthBtn.addActionListener(e -> {
      monthViewPanel.nextMonth();
      updateMonthLabel();
      if (controller != null) {
        controller.refreshCurrentMonth();
      }
    });

    DayPanel[][] dayPanels = monthViewPanel.getDayPanels();
    for (int row = 0; row < 6; row++) {
      for (int col = 0; col < 7; col++) {
        DayPanel dayPanel = dayPanels[row][col];
        dayPanel.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            handleDayClick(dayPanel);
          }
        });
      }
    }
  }

  /**
   * Sets up listeners that require the controller.
   */
  private void setupControllerListeners() {
    if (controller == null) {
      return;
    }

    createEventBtn.addActionListener(e -> showCreateEventDialog(null));
    exportBtn.addActionListener(e -> showExportMenu());
    createCalendarBtn.addActionListener(e -> showCreateCalendarDialog());

    editPropsBtn.addActionListener(e -> {
      try {

        String calendarName = currentCalendarName;
        String timezone = controller.getCurrentCalendarTimezone();
        Color color = calendarColors.get(calendarName);

        showEditCalendarDialog(calendarName, timezone, color);
      } catch (Exception ex) {
        displayError(ex.getMessage());
      }
    });
  }

  /**
   * Handles click on a day panel.
   * Updates selection and opens day events dialog.
   *
   * @param dayPanel the clicked day panel
   */
  private void handleDayClick(DayPanel dayPanel) {
    LocalDate clickedDate = dayPanel.getDate();

    if (clickedDate == null) {
      return;
    }

    monthViewPanel.setSelectedDate(clickedDate);

    if (controller != null) {
      controller.selectDay(clickedDate);
    } else {
    }
  }

  /**
   * Handles calendar selection change from left panel.
   * Updates displayed info and notifies controller.
   *
   * @param calendarName name of the selected calendar
   * @param timezone     timezone of the selected calendar
   * @param color        color of the selected calendar
   */
  private void handleCalendarSelection(String calendarName, String timezone, Color color) {
    selectedNameLabel.setText("Name: " + calendarName);
    selectedTzLabel.setText("Timezone: " + timezone);

    this.currentCalendarName = calendarName;

    if (controller != null) {
      controller.selectCalendar(calendarName);
      controller.refreshCurrentMonth();
    }
  }

  /**
   * Converts Set[DayOfWeek] to weekday string format expected by commands.
   *
   * @param days set of days of the week
   * @return string representation (e.g., "MWF" for Mon/Wed/Fri)
   */
  private String convertDaysToString(Set<DayOfWeek> days) {
    if (days == null || days.isEmpty()) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    if (days.contains(DayOfWeek.MONDAY)) {
      sb.append('M');
    }
    if (days.contains(DayOfWeek.TUESDAY)) {
      sb.append('T');
    }
    if (days.contains(DayOfWeek.WEDNESDAY)) {
      sb.append('W');
    }
    if (days.contains(DayOfWeek.THURSDAY)) {
      sb.append('R');
    }
    if (days.contains(DayOfWeek.FRIDAY)) {
      sb.append('F');
    }
    if (days.contains(DayOfWeek.SATURDAY)) {
      sb.append('S');
    }
    if (days.contains(DayOfWeek.SUNDAY)) {
      sb.append('U');
    }
    return sb.toString();
  }

  /**
   * Handles export action for the selected format.
   *
   * @param format either "ical" or "csv"
   */
  private void handleExport(String format) {
    if (controller != null) {
      controller.exportCalendar(format);
    } else {
      String extension = format.equals("ical") ? ".ics" : ".csv";
      String fileName = currentCalendarName + "_export" + extension;
      String projectDir = System.getProperty("user.dir");
      String filePath = projectDir + "/" + fileName;

      showExportSuccess(filePath);
    }
  }

  /**
   * Shows day events dialog - converts EventInterface to display format.
   * This method acts as an adapter between controller and view.
   *
   * @param date        the date
   * @param modelEvents list of events from model (EventInterface)
   */
  @Override
  public void showDayEventsFromModel(LocalDate date, List<EventInterface> modelEvents) {
    Color calendarColor = calendarColors.getOrDefault(
        currentCalendarName,
        new Color(66, 133, 244)
    );

    List<EventDisplayInfo> displayEvents = new ArrayList<>();
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

    for (EventInterface event : modelEvents) {
      String timeRange = event.getStartDateTime().format(timeFormatter) + " - "
          + event.getEndDateTime().format(timeFormatter);

      LocalDateTime startDateTime = event.getStartDateTime().toLocalDateTime();
      LocalDateTime endDateTime = event.getEndDateTime().toLocalDateTime();

      LocalDate eventEndDate = endDateTime.toLocalDate();

      String status = event.getStatus() != null ? event.getStatus().toString() : "PUBLIC";

      String recurrenceInfo = null;
      if (event.getSeriesId() != null) {
        recurrenceInfo = "Repeats weekly";
      }

      displayEvents.add(new EventDisplayInfo(
          event.getSubject(),
          timeRange,
          startDateTime,
          endDateTime,
          eventEndDate,
          event.getLocation() != null ? event.getLocation() : "",
          event.getDescription() != null ? event.getDescription() : "",
          status,
          event.getSeriesId() != null,
          recurrenceInfo,
          calendarColor
      ));
    }

    showDayEventsDialog(date, displayEvents);
  }

  /**
   * Shows the day events dialog for a specific date.
   *
   * @param date   the date to show events for
   * @param events list of events to display
   */
  public void showDayEventsDialog(LocalDate date, List<EventDisplayInfo> events) {
    DayEventsDialog dialog = new DayEventsDialog(this, date);

    dialog.setOnCreateEvent(() -> {
      dialog.dispose();
      showCreateEventDialog(date);
    });

    dialog.setOnEditEvent(eventToEdit -> {
      dialog.dispose();
      showEditEventDialog(eventToEdit, date);
    });

    dialog.displayEvents(events);
    dialog.setVisible(true);
  }

  /**
   * Shows the create event dialog.
   *
   * @param initialDate the date to pre-fill (null for empty)
   */
  public void showCreateEventDialog(LocalDate initialDate) {
    CreateEventDialog dialog = new CreateEventDialog(this, initialDate);
    dialog.setVisible(true);

    if (dialog.wasConfirmed()) {
      CreateEventDialog.EventFormData data = dialog.getEventData();

      if (controller != null) {
        LocalDateTime start = LocalDateTime.of(data.getDate(), data.getStartTime());

        LocalDate endDate = data.getEndDate() != null ? data.getEndDate() : data.getDate();
        LocalDateTime end = LocalDateTime.of(endDate, data.getEndTime());

        String status = data.getStatus() != null ? data.getStatus().toString() : "PUBLIC";

        if (data.isRecurring()) {
          String weekdays = convertDaysToString(data.getRecurringDays());

          if (data.getEndType() == CreateEventDialog.RecurrenceEndType.BY_COUNT) {
            Integer count = (Integer) data.getEndValue();
            controller.createRecurringEvent(
                data.getName(), start, end,
                weekdays, count, null,
                data.getLocation(),
                data.getDescription(),
                status
            );
          } else {
            String untilDate = ((LocalDate) data.getEndValue()).toString();
            controller.createRecurringEvent(
                data.getName(), start, end,
                weekdays, null, untilDate,
                data.getLocation(),
                data.getDescription(),
                status
            );
          }
        } else {
          controller.createSingleEvent(
              data.getName(), start, end,
              data.getLocation(),
              data.getDescription(),
              status
          );
        }
        controller.refreshCurrentMonth();
      }
    }
  }

  /**
   * Shows the edit event dialog.
   *
   * @param eventData the current event data
   * @param eventDate the date of this occurrence
   */
  public void showEditEventDialog(EventDisplayInfo eventData, LocalDate eventDate) {
    EditEventDialog dialog = new EditEventDialog(this, eventData, eventDate);
    dialog.setVisible(true);

    if (dialog.wasConfirmed()) {
      EditEventDialog.EventEditData data = dialog.getEditData();

      if (controller != null) {
        LocalDateTime originalStart = data.getOriginalStartDateTime();
        LocalDateTime originalEnd = data.getOriginalEndDateTime();

        LocalDateTime newStart;
        LocalDateTime newEnd;

        if (data.getDate() != null) {
          newStart = LocalDateTime.of(data.getDate(), data.getStartTime());

          if (data.getEventEndDate() != null) {
            newEnd = LocalDateTime.of(data.getEventEndDate(), data.getEndTime());
          } else {
            newEnd = LocalDateTime.of(data.getDate(), data.getEndTime());
          }
        } else {
          LocalDate originalDate = originalStart.toLocalDate();
          newStart = LocalDateTime.of(originalDate, data.getStartTime());

          if (data.getEventEndDate() != null) {
            newEnd = LocalDateTime.of(data.getEventEndDate(), data.getEndTime());
          } else {
            LocalDate originalEndDate = originalEnd.toLocalDate();
            if (originalEndDate.equals(originalDate)) {
              newEnd = LocalDateTime.of(originalDate, data.getEndTime());
            } else {
              newEnd = LocalDateTime.of(originalEndDate, data.getEndTime());
            }
          }
        }

        calendar.model.calendar.EditScope scope;
        switch (data.getEditScope()) {
          case SINGLE:
          case THIS_ONLY:
            scope = calendar.model.calendar.EditScope.SINGLE;
            break;
          case FUTURE_OCCURRENCES:
            scope = calendar.model.calendar.EditScope.FROM_POINT;
            break;
          case ALL_OCCURRENCES:
            scope = calendar.model.calendar.EditScope.ENTIRE_SERIES;
            break;
          default:
            throw new IllegalArgumentException("Unknown edit scope: " + data.getEditScope());
        }

        controller.editEvent(
            data.getOriginalName(), originalStart, originalEnd,
            data.getName(), newStart, newEnd,
            data.getLocation() != null ? data.getLocation() : "",
            data.getDescription() != null ? data.getDescription() : "",
            data.getStatus(),
            scope
        );

        controller.refreshCurrentMonth();
      }
    }
  }

  /**
   * Shows the create calendar dialog.
   */
  public void showCreateCalendarDialog() {
    CreateCalendarDialog dialog = new CreateCalendarDialog(this);
    dialog.setVisible(true);

    if (dialog.wasConfirmed()) {
      CreateCalendarDialog.CalendarFormData data = dialog.getCalendarData();
      Color selectedColor = dialog.getSelectedColor();

      if (controller != null) {

        controller.createCalendar(data.getName(), data.getTimezone());

        calendarColors.put(data.getName(), selectedColor);
        calendarTimezones.put(data.getName(), data.getTimezone());

        List<String> calendars = controller.getAllCalendarNames();
        updateCalendarList(calendars, data.getName());

        controller.selectCalendar(data.getName());
        controller.refreshCurrentMonth();
      }
    }
  }

  /**
   * Shows the edit calendar properties dialog.
   *
   * @param calendarName    the name of the calendar to edit
   * @param currentTimezone current timezone
   * @param currentColor    current color
   */
  public void showEditCalendarDialog(String calendarName,
                                     String currentTimezone, Color currentColor) {
    EditCalendarDialog dialog = new EditCalendarDialog(
        this, calendarName, currentTimezone, currentColor);
    dialog.setVisible(true);

    if (dialog.wasConfirmed()) {
      EditCalendarDialog.CalendarEditData data = dialog.getEditData();
      Color selectedColor = dialog.getSelectedColor();

      if (controller != null) {
        controller.editCalendar(
            data.getOriginalName(),
            data.getName(),
            data.getTimezone()
        );

        boolean nameChanged = !data.getOriginalName().equals(data.getName());

        if (nameChanged) {
          calendarColors.remove(data.getOriginalName());
          calendarTimezones.remove(data.getOriginalName());

          calendarColors.put(data.getName(), selectedColor);
          calendarTimezones.put(data.getName(), data.getTimezone());

          this.currentCalendarName = data.getName();

          List<String> calendars = controller.getAllCalendarNames();
          updateCalendarList(calendars, data.getName());
          controller.refreshCurrentMonth();
        } else {
          calendarColors.put(data.getName(), selectedColor);
          calendarTimezones.put(data.getName(), data.getTimezone());

          List<String> calendars = controller.getAllCalendarNames();
          updateCalendarList(calendars, data.getName());
          controller.refreshCurrentMonth();
        }

        selectedNameLabel.setText("Name: " + data.getName());
        selectedTzLabel.setText("Timezone: " + data.getTimezone());
      }
    }
  }

  /**
   * Shows the export dropdown menu.
   * Displays options for iCal and CSV export.
   */
  private void showExportMenu() {
    JPopupMenu exportMenu = new JPopupMenu();

    JMenuItem exportIcal = new JMenuItem("Export to iCal");
    JMenuItem exportCsv = new JMenuItem("Export to CSV");

    exportIcal.addActionListener(e -> handleExport("ical"));
    exportCsv.addActionListener(e -> handleExport("csv"));

    exportMenu.add(exportIcal);
    exportMenu.add(exportCsv);

    exportMenu.show(exportBtn, 0, exportBtn.getHeight());
  }

  /**
   * Updates the event indicators for all days in current month.
   *
   * @param eventsMap     map of LocalDate to number of events on that day
   * @param calendarColor the color of the currently selected calendar
   */
  @Override
  public void updateEventIndicators(Map<LocalDate, Integer> eventsMap, Color calendarColor) {
    DayPanel[][] dayPanels = monthViewPanel.getDayPanels();

    for (int row = 0; row < 6; row++) {
      for (int col = 0; col < 7; col++) {
        DayPanel dayPanel = dayPanels[row][col];
        LocalDate date = dayPanel.getDate();

        if (date != null && eventsMap.containsKey(date)) {
          int eventCount = eventsMap.get(date);

          List<Color> eventColors = new ArrayList<>();
          for (int i = 0; i < eventCount; i++) {
            eventColors.add(calendarColor);
          }
          dayPanel.setEvents(eventColors);
        } else {
          dayPanel.setEvents(new ArrayList<>());
        }
      }
    }
  }

  /**
   * Updates the calendar list in the left panel.
   * Rebuilds the radio button list with all calendars.
   *
   * @param calendarNames        list of all calendar names
   * @param selectedCalendarName the currently selected calendar name
   */
  @Override
  public void updateCalendarList(List<String> calendarNames, String selectedCalendarName) {

    JPanel container = new JPanel(new BorderLayout());
    container.setBackground(Color.WHITE);

    JPanel listPanel = new JPanel();
    listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
    listPanel.setBackground(Color.WHITE);
    listPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

    JLabel header = new JLabel("CALENDARS");
    header.setFont(new Font("SansSerif", Font.BOLD, 11));
    header.setForeground(new Color(100, 100, 100));
    header.setAlignmentX(Component.LEFT_ALIGNMENT);

    listPanel.add(header);
    listPanel.add(Box.createVerticalStrut(8));

    ButtonGroup group = new ButtonGroup();
    Color[] colors = CreateCalendarDialog.getAvailableColors();
    int colorIndex = 0;

    for (String calendarName : calendarNames) {
      Color color = calendarColors.get(calendarName);
      if (color == null) {
        color = colors[colorIndex % colors.length];
        calendarColors.put(calendarName, color);
        colorIndex++;
      }

      JRadioButton radioBtn = createCalendarRadioButton(calendarName, color);

      if (calendarName.equals(selectedCalendarName)) {
        radioBtn.setSelected(true);
      }

      Color finalColor = color;
      String finalCalendarName = calendarName;

      radioBtn.addActionListener(e -> {
        if (radioBtn.isSelected() && controller != null) {
          String timezone = calendarTimezones.getOrDefault(
              finalCalendarName,
              ZoneId.systemDefault().getId()
          );
          handleCalendarSelection(finalCalendarName, timezone, finalColor);
        }
      });

      group.add(radioBtn);
      listPanel.add(radioBtn);
      listPanel.add(Box.createVerticalStrut(5));
    }

    listPanel.add(Box.createVerticalGlue());

    JScrollPane scrollPane = new JScrollPane(listPanel);
    scrollPane.setBorder(null);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);

    container.add(scrollPane, BorderLayout.CENTER);

    LayoutManager layout = leftPanel.getLayout();
    if (layout instanceof BorderLayout) {
      Component oldCenter = ((BorderLayout) layout).getLayoutComponent(BorderLayout.CENTER);
      if (oldCenter != null) {
        leftPanel.remove(oldCenter);
      }
    }

    leftPanel.add(container, BorderLayout.CENTER);
    leftPanel.revalidate();
    leftPanel.repaint();

  }

  /**
   * Updates the month/year label in the toolbar.
   */
  private void updateMonthLabel() {
    YearMonth currentMonth = monthViewPanel.getCurrentMonth();
    String formattedMonth = currentMonth.format(MONTH_YEAR_FORMATTER);
    monthYearLabel.setText(formattedMonth);
  }

  /**
   * Shows export success dialog with file path.
   * Called by controller after successful export.
   *
   * @param filePath the full path to the exported file
   */
  @Override
  public void showExportSuccess(String filePath) {
    String message = "Calendar exported successfully!\n\nFile saved to:\n" + filePath;
    JOptionPane.showMessageDialog(
        this,
        message,
        "Export Successful",
        JOptionPane.INFORMATION_MESSAGE
    );
  }

  /**
   * Gets the currently displayed month.
   *
   * @return the current YearMonth being displayed
   */
  @Override
  public YearMonth getCurrentMonth() {
    return monthViewPanel.getCurrentMonth();
  }

  /**
   * Gets the color for a calendar.
   *
   * @param calendarName the calendar name
   * @return the color, or default blue if not found
   */
  @Override
  public Color getCalendarColor(String calendarName) {
    return calendarColors.getOrDefault(calendarName, new Color(66, 133, 244));
  }

  @Override
  public void displayMessage(String message) {
    System.out.println("Message: " + message);
  }

  @Override
  public void displayError(String message) {
    JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
  }

  @Override
  public void displayWelcome() {
  }

  @Override
  public void displayGoodbye() {
  }

  @Override
  public void displayCommandOptions() {
  }

  @Override
  public void displayPrompt() {
  }

  @Override
  public void displayFileNotFound(String filePath) {
    displayError("File not found: " + filePath);
  }

  @Override
  public void displayFileReadError(String message) {
    displayError("File read error: " + message);
  }

  @Override
  public void displayNoExitCommand() {
  }

  @Override
  public void displayEvents(calendar.model.calendar.ReadOnlyCalendar calendar, String date) {
    displayMessage("Display events called for date: " + date);
  }

  @Override
  public void displayEventsInRange(calendar.model.calendar.ReadOnlyCalendar calendar,
                                   String startDate, String endDate) {
    displayMessage("Display events in range called");
  }

  @Override
  public void displayBusyStatus(calendar.model.calendar.ReadOnlyCalendar calendar,
                                String dateTime) {
    displayMessage("Display busy status called");
  }

  @Override
  public void displayFatalError(String message) {
    displayError("Fatal error: " + message);
    System.exit(1);
  }

  @Override
  public void displayUsageInformation() {
  }

  @Override
  public void close() {
    dispose();
  }
}