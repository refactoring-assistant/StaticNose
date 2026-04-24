package calendar.view.gui;

import calendar.model.CalendarEvent;
import calendar.model.CalendarModel;
import calendar.model.EventProperty;
import calendar.model.EventStatus;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.text.Format;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * Graphical interface for the calendar application.
 */
public class CalendarGui {
  private static final boolean HEADLESS = GraphicsEnvironment.isHeadless();

  private static final DateTimeFormatter MONTH_TITLE =
      DateTimeFormatter.ofPattern("MMMM yyyy", Locale.US);
  private static final DateTimeFormatter DATE_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US);
  private static final DateTimeFormatter TIME_FORMAT =
      DateTimeFormatter.ofPattern("HH:mm", Locale.US);
  private static final Color[] CALENDAR_COLORS = {
      new Color(0x0072CE),
      new Color(0xAD1457),
      new Color(0x0B8043),
      new Color(0xAF5F00),
      new Color(0x5E35B1),
      new Color(0x3949AB),
      new Color(0x00897B),
      new Color(0x6D4C41)
  };

  private final CalendarModel model;
  private final DefaultListModel<CalendarEvent> eventListModel;
  private final Map<String, Color> calendarColors;
  private JFrame frame;
  private JComboBox<String> calendarSelector;
  private JLabel timezoneLabel;
  private JLabel monthLabel;
  private JPanel dayGrid;
  private final List<DayButton> dayButtons;
  private LocalDate monthCursor;
  private LocalDate selectedDate;
  private JList<CalendarEvent> eventList;
  private JTextArea eventDetails;

  /**
   * Creates a GUI wired to the supplied model.
   *
   * @param model shared calendar model
   */
  public CalendarGui(CalendarModel model) {
    this.model = Objects.requireNonNull(model, "Model is required.");
    this.eventListModel = new DefaultListModel<>();
    this.calendarColors = new LinkedHashMap<>();
    this.dayButtons = new ArrayList<>();
  }

  /**
   * Displays the GUI on the event dispatch thread.
   */
  public void show() {
    SwingUtilities.invokeLater(this::createAndShowUi);
  }

  private void createAndShowUi() {
    frame = new JFrame("Cal3 Calendar");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setMinimumSize(new Dimension(1200, 720));
    frame.setLayout(new BorderLayout(8, 8));
    frame.add(buildCalendarToolbar(), BorderLayout.NORTH);
    JSplitPane splitPane =
        new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT, buildMonthPanel(), buildEventPanel());
    splitPane.setResizeWeight(0.6);
    frame.add(splitPane, BorderLayout.CENTER);
    monthCursor = todayInActiveZone().withDayOfMonth(1);
    selectedDate = todayInActiveZone();
    refreshCalendarList();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  private JPanel buildCalendarToolbar() {
    JPanel panel = new JPanel(new BorderLayout(8, 8));
    panel.setBorder(new EmptyBorder(10, 12, 6, 12));
    JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    JLabel label = createLabel("Calendar:");
    if (!HEADLESS) {
      label.setFont(label.getFont().deriveFont(Font.BOLD));
    }
    left.add(label);
    calendarSelector = new JComboBox<>();
    calendarSelector.setPreferredSize(new Dimension(220, 28));
    calendarSelector.addActionListener(e -> {
      String name = (String) calendarSelector.getSelectedItem();
      if (name != null && (model.getActiveCalendarName() == null
          || !name.equals(model.getActiveCalendarName()))) {
        model.useCalendar(name);
        onActiveCalendarChanged();
      }
    });
    calendarSelector.setRenderer(new CalendarRenderer());
    left.add(calendarSelector);
    timezoneLabel = createLabel(null);
    left.add(timezoneLabel);
    panel.add(left, BorderLayout.WEST);

    JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    JButton newCalendar = new JButton("New Calendar");
    newCalendar.addActionListener(e -> showCreateCalendarDialog());
    JButton editCalendar = new JButton("Edit Calendar");
    editCalendar.addActionListener(e -> showEditCalendarDialog());
    right.add(newCalendar);
    right.add(editCalendar);
    panel.add(right, BorderLayout.EAST);
    return panel;
  }

  private Component buildMonthPanel() {
    JPanel panel = new JPanel(new BorderLayout(6, 6));
    panel.setBorder(new EmptyBorder(0, 12, 12, 12));
    final JPanel header = new JPanel(new BorderLayout());
    JButton previous = new JButton("<");
    previous.addActionListener(e -> {
      monthCursor = monthCursor.minusMonths(1);
      refreshMonthView();
    });
    JButton next = new JButton(">");
    next.addActionListener(e -> {
      monthCursor = monthCursor.plusMonths(1);
      refreshMonthView();
    });
    JButton today = new JButton("Today");
    today.addActionListener(e -> {
      selectedDate = todayInActiveZone();
      monthCursor = selectedDate.withDayOfMonth(1);
      refreshMonthView();
      refreshSelectedDay();
    });
    JPanel nav = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    nav.add(previous);
    nav.add(today);
    nav.add(next);
    header.add(nav, BorderLayout.WEST);
    monthLabel = createLabel(null);
    monthLabel.setHorizontalAlignment(SwingConstants.CENTER);
    if (!HEADLESS) {
      monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD, 18f));
    }
    header.add(monthLabel, BorderLayout.CENTER);
    panel.add(header, BorderLayout.NORTH);

    JPanel gridWrapper = new JPanel(new BorderLayout());
    JPanel daysOfWeek = new JPanel(new GridLayout(1, 7));
    daysOfWeek.setBorder(new EmptyBorder(4, 4, 4, 4));
    for (DayOfWeek day : DayOfWeek.values()) {
      JLabel dowLabel = createLabel(day.getDisplayName(TextStyle.SHORT, Locale.US));
      dowLabel.setHorizontalAlignment(SwingConstants.CENTER);
      if (!HEADLESS) {
        dowLabel.setFont(dowLabel.getFont().deriveFont(Font.BOLD));
      }
      daysOfWeek.add(dowLabel);
    }
    gridWrapper.add(daysOfWeek, BorderLayout.NORTH);
    dayGrid = new JPanel(new GridLayout(6, 7, 4, 4));
    dayGrid.setBorder(new EmptyBorder(4, 4, 4, 4));
    ButtonGroup group = new ButtonGroup();
    dayButtons.clear();
    for (int i = 0; i < 42; i++) {
      DayButton button = newDayButton();
      group.add(button);
      dayButtons.add(button);
      dayGrid.add(button);
      button.addActionListener(e -> {
        DayButton source = (DayButton) e.getSource();
        selectDate(source.getDate());
      });
    }
    gridWrapper.add(dayGrid, BorderLayout.CENTER);
    panel.add(gridWrapper, BorderLayout.CENTER);
    return panel;
  }

  private Component buildEventPanel() {
    JPanel panel = new JPanel(new BorderLayout(6, 6));
    panel.setBorder(new EmptyBorder(0, 0, 12, 12));
    JLabel header = createLabel("Events");
    header.setHorizontalAlignment(SwingConstants.CENTER);
    if (!HEADLESS) {
      header.setFont(header.getFont().deriveFont(Font.BOLD, 16f));
    }
    panel.add(header, BorderLayout.NORTH);

    eventList = new JList<>(eventListModel);
    eventList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    eventList.setCellRenderer(new EventRenderer());
    eventList.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        showEventDetails(eventList.getSelectedValue());
      }
    });
    panel.add(new JScrollPane(eventList), BorderLayout.CENTER);

    eventDetails = createTextArea(8, 30);
    eventDetails.setLineWrap(true);
    eventDetails.setWrapStyleWord(true);
    eventDetails.setEditable(false);
    JScrollPane detailsScroll = new JScrollPane(eventDetails);
    JPanel bottom = new JPanel(new BorderLayout());
    bottom.add(detailsScroll, BorderLayout.CENTER);

    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton newEvent = new JButton("New Event");
    newEvent.addActionListener(e -> showCreateEventDialog());
    JButton editEvent = new JButton("Edit Event");
    editEvent.addActionListener(e -> showEditEventDialog());
    actions.add(newEvent);
    actions.add(editEvent);
    bottom.add(actions, BorderLayout.SOUTH);
    panel.add(bottom, BorderLayout.SOUTH);
    return panel;
  }

  void refreshCalendarList() {
    ensureUiComponents();
    List<String> calendars = model.listCalendars();
    calendarSelector.setModel(new DefaultComboBoxModel<>(calendars.toArray(new String[0])));
    if (!calendars.isEmpty()) {
      String active = model.getActiveCalendarName();
      if (active == null) {
        model.useCalendar(calendars.get(0));
        active = calendars.get(0);
      }
      calendarSelector.setSelectedItem(active);
      for (String name : calendars) {
        calendarColors.computeIfAbsent(name, this::nextColor);
      }
      onActiveCalendarChanged();
    } else {
      monthLabel.setText("No calendars");
      timezoneLabel.setText("");
    }
  }

  private void onActiveCalendarChanged() {
    timezoneLabel.setText("Timezone: " + model.getActiveCalendarZone());
    if (selectedDate == null) {
      selectedDate = todayInActiveZone();
    }
    if (monthCursor == null) {
      monthCursor = selectedDate.withDayOfMonth(1);
    }
    refreshMonthView();
    refreshSelectedDay();
  }

  private void refreshMonthView() {
    if (!model.hasActiveCalendar()) {
      return;
    }
    monthLabel.setText(MONTH_TITLE.format(monthCursor));
    LocalDate firstOfMonth = monthCursor.withDayOfMonth(1);
    int delta = firstOfMonth.getDayOfWeek().getValue() - 1;
    LocalDate start = firstOfMonth.minusDays(delta);
    LocalDate cursor = start;
    for (DayButton button : dayButtons) {
      List<CalendarEvent> events = model.eventsOn(cursor);
      boolean inMonth = cursor.getMonth().equals(monthCursor.getMonth())
          && cursor.getYear() == monthCursor.getYear();
      button.update(cursor, inMonth, events.size());
      if (selectedDate != null && cursor.equals(selectedDate)) {
        button.setSelected(true);
      } else {
        button.setSelected(false);
      }
      cursor = cursor.plusDays(1);
    }
  }

  private void refreshSelectedDay() {
    if (selectedDate == null || !model.hasActiveCalendar()) {
      eventListModel.clear();
      eventDetails.setText("");
      return;
    }
    List<CalendarEvent> events = model.eventsOn(selectedDate);
    eventListModel.clear();
    for (CalendarEvent event : events) {
      eventListModel.addElement(event);
    }
    if (!events.isEmpty()) {
      eventList.setSelectedIndex(0);
    } else {
      eventDetails.setText("No events on " + DATE_FORMAT.format(selectedDate));
    }
  }

  void selectDate(LocalDate date) {
    selectedDate = date;
    if (date.getMonthValue() != monthCursor.getMonthValue()) {
      monthCursor = date.withDayOfMonth(1);
      refreshMonthView();
    }
    refreshSelectedDay();
  }

  void showEventDetails(CalendarEvent event) {
    if (event == null) {
      eventDetails.setText("Select an event to see details.");
      return;
    }
    StringBuilder builder = new StringBuilder();
    builder.append(event.getSubject()).append("\n");
    builder.append(TIME_FORMAT.format(event.getStart().toLocalTime()))
        .append(" - ")
        .append(TIME_FORMAT.format(event.getEnd().toLocalTime()))
        .append('\n');
    builder.append("Status: ").append(event.getStatus()).append('\n');
    event.getLocation().ifPresent(loc -> builder.append("Location: ").append(loc).append('\n'));
    event.getDescription()
        .ifPresent(desc -> builder.append("Description: ").append(desc).append('\n'));
    event.getSeriesId().ifPresent(id -> builder.append("Series: ").append(id).append('\n'));
    builder.append("Starts: ").append(event.getStart()).append('\n');
    builder.append("Ends: ").append(event.getEnd());
    eventDetails.setText(builder.toString());
  }

  private void showCreateCalendarDialog() {
    final JComboBox<String> zoneCombo = new JComboBox<>();
    List<String> zones = new ArrayList<>(ZoneId.getAvailableZoneIds());
    zones.sort(String::compareTo);
    for (String zone : zones) {
      zoneCombo.addItem(zone);
    }
    zoneCombo.setSelectedItem(model.getActiveCalendarZone().getId());
    final JTextField nameField = new JTextField();
    final JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
    panel.add(new JLabel("Name:"));
    panel.add(nameField);
    panel.add(new JLabel("Timezone:"));
    panel.add(zoneCombo);
    int result =
        JOptionPane.showConfirmDialog(
            frame,
            panel,
            "Create Calendar",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
    if (result == JOptionPane.OK_OPTION) {
      try {
        String name = nameField.getText().trim();
        ZoneId zone = ZoneId.of((String) zoneCombo.getSelectedItem());
        model.createCalendar(name, zone);
        model.useCalendar(name);
        refreshCalendarList();
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void showEditCalendarDialog() {
    if (!model.hasActiveCalendar()) {
      return;
    }
    final JComboBox<String> zoneCombo = new JComboBox<>();
    List<String> zones = new ArrayList<>(ZoneId.getAvailableZoneIds());
    zones.sort(String::compareTo);
    for (String zone : zones) {
      zoneCombo.addItem(zone);
    }
    zoneCombo.setSelectedItem(model.getActiveCalendarZone().getId());
    final JTextField nameField = new JTextField(model.getActiveCalendarName());
    final JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
    panel.add(new JLabel("Name:"));
    panel.add(nameField);
    panel.add(new JLabel("Timezone:"));
    panel.add(zoneCombo);
    int result =
        JOptionPane.showConfirmDialog(
            frame,
            panel,
            "Edit Calendar",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
    if (result == JOptionPane.OK_OPTION) {
      try {
        String currentName = model.getActiveCalendarName();
        String desiredName = nameField.getText().trim();
        String zoneText = (String) zoneCombo.getSelectedItem();
        if (!desiredName.equals(currentName)) {
          model.renameCalendar(currentName, desiredName);
        }
        if (!model.getActiveCalendarZone().getId().equals(zoneText)) {
          model.changeCalendarTimezone(desiredName, ZoneId.of(zoneText));
        }
        model.useCalendar(desiredName);
        refreshCalendarList();
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void showCreateEventDialog() {
    if (selectedDate == null) {
      selectedDate = todayInActiveZone();
    }
    EventInputPanel panel = new EventInputPanel(selectedDate);
    int result = JOptionPane.showConfirmDialog(
        frame, panel, "Create Event", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION) {
      return;
    }
    try {
      EventInput input = panel.buildInput();
      if (input.recurrence == Recurrence.NONE) {
        model.createEvent(input.subject, input.start, input.end, input.allDay);
      } else if (input.recurrence == Recurrence.COUNT) {
        model.createRecurringEventsByCount(
            input.subject,
            input.start,
            input.end,
            input.allDay,
            input.weekdays,
            input.occurrences);
      } else {
        model.createRecurringEventsUntil(
            input.subject,
            input.start,
            input.end,
            input.allDay,
            input.weekdays,
            input.untilDate);
      }
      selectedDate = input.start.toLocalDate();
      monthCursor = selectedDate.withDayOfMonth(1);
      refreshMonthView();
      refreshSelectedDay();
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private void showEditEventDialog() {
    CalendarEvent selected = eventList.getSelectedValue();
    if (selected == null) {
      JOptionPane.showMessageDialog(frame, "Select an event to edit.", "Edit Event",
          JOptionPane.INFORMATION_MESSAGE);
      return;
    }
    EventEditPanel panel = new EventEditPanel(selected);
    int result = JOptionPane.showConfirmDialog(
        frame, panel, "Edit Event", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION) {
      return;
    }
    try {
      EventEditRequest request = panel.buildRequest();
      switch (request.scope) {
        case SINGLE:
          model.editSingleEvent(
              selected.getSubject(),
              selected.getStart(),
              selected.getEnd(),
              request.property,
              request.value);
          break;
        case SERIES_FROM:
          model.editEventsFrom(selected.getSubject(), selected.getStart(), request.property,
              request.value);
          break;
        case SERIES_ALL:
          model.editEntireSeries(selected.getSubject(), selected.getStart(), request.property,
              request.value);
          break;
        default:
          throw new IllegalStateException("Unknown scope: " + request.scope);
      }
      refreshMonthView();
      refreshSelectedDay();
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(frame, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  private LocalDate todayInActiveZone() {
    ZoneId zone = model.hasActiveCalendar()
        ? model.getActiveCalendarZone()
        : ZoneId.systemDefault();
    return LocalDate.now(zone);
  }

  private Color nextColor(String name) {
    int index = calendarColors.size() % CALENDAR_COLORS.length;
    return CALENDAR_COLORS[index];
  }

  private class CalendarRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(
        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel label = (JLabel) super.getListCellRendererComponent(
          list, value, index, isSelected, cellHasFocus);
      if (value != null) {
        Color color = calendarColors.computeIfAbsent(value.toString(), CalendarGui.this::nextColor);
        label.setIcon(new ColorIcon(color));
      }
      return label;
    }
  }

  static class ColorIcon implements Icon {
    private final Color color;

    ColorIcon(Color color) {
      this.color = color;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      g.setColor(color);
      g.fillRect(x, y, getIconWidth(), getIconHeight());
    }

    @Override
    public int getIconWidth() {
      return 12;
    }

    @Override
    public int getIconHeight() {
      return 12;
    }
  }

  class DayButton extends javax.swing.JToggleButton {
    private LocalDate date;
    private final boolean renderText;
    private String lastRenderedText;

    DayButton() {
      this(!HEADLESS);
    }

    DayButton(boolean renderText) {
      this.renderText = renderText;
      setFocusPainted(false);
      setHorizontalAlignment(SwingConstants.LEFT);
      setMargin(new Insets(4, 6, 4, 6));
      setBackground(Color.WHITE);
      setBorder(BorderFactory.createLineBorder(new Color(0xDDDDDD)));
    }

    void update(LocalDate date, boolean inMonth, int eventCount) {
      this.date = date;
      StringBuilder text =
          new StringBuilder("<html><b>").append(date.getDayOfMonth()).append("</b>");
      if (eventCount > 0) {
        text.append("<br/><span style='font-size:9px;color:#555555;'>")
            .append(eventCount)
            .append(eventCount == 1 ? " event" : " events")
            .append("</span>");
      }
      text.append("</html>");
      lastRenderedText = text.toString();
      if (renderText) {
        setText(lastRenderedText);
      }
      setEnabled(true);
      if (inMonth) {
        setForeground(Color.BLACK);
      } else {
        setForeground(new Color(0x777777));
      }
    }

    LocalDate getDate() {
      return date;
    }

    String getRenderedText() {
      return lastRenderedText;
    }
  }

  class EventRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(
        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      JLabel label = (JLabel) super.getListCellRendererComponent(
          list, value, index, isSelected, cellHasFocus);
      if (value instanceof CalendarEvent) {
        CalendarEvent event = (CalendarEvent) value;
        String timeRange =
            TIME_FORMAT.format(event.getStart().toLocalTime())
                + " - "
                + TIME_FORMAT.format(event.getEnd().toLocalTime());
        label.setText(timeRange + "  " + event.getSubject());
      }
      return label;
    }
  }

  static class EventInput {
    private String subject;
    private LocalDateTime start;
    private LocalDateTime end;
    private boolean allDay;
    private Recurrence recurrence;
    private Set<DayOfWeek> weekdays;
    private int occurrences;
    private LocalDate untilDate;

    String getSubject() {
      return subject;
    }

    LocalDateTime getStart() {
      return start;
    }

    LocalDateTime getEnd() {
      return end;
    }

    boolean isAllDay() {
      return allDay;
    }

    Recurrence getRecurrence() {
      return recurrence;
    }

    Set<DayOfWeek> getWeekdays() {
      return weekdays;
    }

    int getOccurrences() {
      return occurrences;
    }

    LocalDate getUntilDate() {
      return untilDate;
    }
  }

  enum Recurrence {
    NONE,
    COUNT,
    UNTIL
  }

  static class EventInputPanel extends JPanel {
    private final JTextField subjectField;
    private final JFormattedTextField dateField;
    private final JTextField startTimeField;
    private final JTextField endTimeField;
    private final JCheckBox allDayBox;
    private final JCheckBox[] weekdayBoxes;
    private final JRadioButton noRepeat;
    private final JRadioButton repeatCount;
    private final JRadioButton repeatUntil;
    private final JSpinner occurrenceSpinner;
    private final JFormattedTextField untilDateField;

    EventInputPanel(LocalDate defaultDate) {
      super(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(4, 4, 4, 4);
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.gridx = 0;
      gbc.gridy = 0;
      add(createLabel("Subject:"), gbc);
      subjectField = createTextField();
      gbc.gridx = 1;
      gbc.weightx = 1.0;
      add(subjectField, gbc);

      gbc.gridy++;
      gbc.gridx = 0;
      gbc.weightx = 0;
      add(createLabel("Date (yyyy-MM-dd):"), gbc);
      dateField = createFormattedField(DATE_FORMAT.toFormat());
      dateField.setText(defaultDate.toString());
      gbc.gridx = 1;
      gbc.weightx = 1.0;
      add(dateField, gbc);

      gbc.gridy++;
      gbc.gridx = 0;
      add(createLabel("Start (HH:mm):"), gbc);
      startTimeField = createTextField("09:00");
      gbc.gridx = 1;
      add(startTimeField, gbc);

      gbc.gridy++;
      gbc.gridx = 0;
      add(createLabel("End (HH:mm):"), gbc);
      endTimeField = createTextField("10:00");
      gbc.gridx = 1;
      add(endTimeField, gbc);

      gbc.gridy++;
      gbc.gridx = 0;
      add(createLabel("All day:"), gbc);
      allDayBox = createCheckBox();
      allDayBox.addActionListener(e -> toggleAllDay());
      gbc.gridx = 1;
      add(allDayBox, gbc);

      gbc.gridy++;
      gbc.gridx = 0;
      gbc.gridwidth = 2;
      add(createLabel("Repeats on:"), gbc);
      weekdayBoxes = new JCheckBox[DayOfWeek.values().length];
      JPanel weekdaysPanel = new JPanel(new GridLayout(1, 7));
      int idx = 0;
      for (DayOfWeek day : DayOfWeek.values()) {
        JCheckBox box = createCheckBox(day.getDisplayName(TextStyle.SHORT, Locale.US));
        if (day.equals(defaultDate.getDayOfWeek())) {
          box.setSelected(true);
        }
        weekdayBoxes[idx++] = box;
        weekdaysPanel.add(box);
      }
      gbc.gridy++;
      add(weekdaysPanel, gbc);

      noRepeat = createRadioButton("No recurrence", true);
      repeatCount = createRadioButton("Repeat for N times", false);
      repeatUntil = createRadioButton("Repeat until date", false);
      ButtonGroup group = new ButtonGroup();
      group.add(noRepeat);
      group.add(repeatCount);
      group.add(repeatUntil);
      JPanel recurrencePanel = new JPanel(new GridLayout(0, 1));
      recurrencePanel.setBorder(BorderFactory.createTitledBorder("Recurrence"));
      recurrencePanel.add(noRepeat);
      recurrencePanel.add(repeatCount);
      recurrencePanel.add(repeatUntil);
      occurrenceSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 365, 1));
      untilDateField = createFormattedField(DATE_FORMAT.toFormat());
      untilDateField.setText(defaultDate.plusWeeks(4).toString());
      JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
      countPanel.add(createLabel("Occurrences:"));
      countPanel.add(occurrenceSpinner);
      recurrencePanel.add(countPanel);
      JPanel untilPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
      untilPanel.add(createLabel("Until (yyyy-MM-dd):"));
      untilPanel.add(untilDateField);
      recurrencePanel.add(untilPanel);
      gbc.gridy++;
      add(recurrencePanel, gbc);
    }

    JTextField subjectField() {
      return subjectField;
    }

    JFormattedTextField dateField() {
      return dateField;
    }

    JTextField startTimeField() {
      return startTimeField;
    }

    JTextField endTimeField() {
      return endTimeField;
    }

    JCheckBox allDayBox() {
      return allDayBox;
    }

    JCheckBox[] weekdayBoxes() {
      return weekdayBoxes;
    }

    JRadioButton noRepeatButton() {
      return noRepeat;
    }

    JRadioButton repeatCountButton() {
      return repeatCount;
    }

    JRadioButton repeatUntilButton() {
      return repeatUntil;
    }

    JSpinner occurrenceSpinner() {
      return occurrenceSpinner;
    }

    JFormattedTextField untilDateField() {
      return untilDateField;
    }

    private void toggleAllDay() {
      boolean allDay = allDayBox.isSelected();
      startTimeField.setEnabled(!allDay);
      endTimeField.setEnabled(!allDay);
      if (allDay) {
        startTimeField.setText("08:00");
        endTimeField.setText("17:00");
      }
    }

    EventInput buildInput() {
      EventInput input = new EventInput();
      input.subject = subjectField.getText().trim();
      if (input.subject.isEmpty()) {
        throw new IllegalArgumentException("Subject is required.");
      }
      LocalDate date = LocalDate.parse(dateField.getText().trim());
      input.allDay = allDayBox.isSelected();
      LocalTime start = LocalTime.parse(startTimeField.getText().trim());
      LocalTime end = LocalTime.parse(endTimeField.getText().trim());
      input.start = LocalDateTime.of(date, start);
      input.end = LocalDateTime.of(date, end);
      if (input.allDay) {
        if (!start.equals(LocalTime.of(8, 0)) || !end.equals(LocalTime.of(17, 0))) {
          throw new IllegalArgumentException("All day events must run from 08:00 to 17:00.");
        }
      }
      if (!input.end.isAfter(input.start)) {
        throw new IllegalArgumentException("End time must be after the start time.");
      }
      input.weekdays = EnumSet.noneOf(DayOfWeek.class);
      for (int i = 0; i < weekdayBoxes.length; i++) {
        if (weekdayBoxes[i].isSelected()) {
          input.weekdays.add(DayOfWeek.values()[i]);
        }
      }
      if (noRepeat.isSelected()) {
        input.recurrence = Recurrence.NONE;
      } else if (repeatCount.isSelected()) {
        input.recurrence = Recurrence.COUNT;
      } else {
        input.recurrence = Recurrence.UNTIL;
      }
      if (input.recurrence == Recurrence.NONE) {
        return input;
      }
      if (input.weekdays.isEmpty()) {
        throw new IllegalArgumentException("Select at least one weekday for recurring events.");
      }
      if (!input.weekdays.contains(input.start.getDayOfWeek())) {
        throw new IllegalArgumentException(
            "The start date must be included in the weekday selection.");
      }
      if (input.recurrence == Recurrence.COUNT) {
        input.occurrences = ((Number) occurrenceSpinner.getValue()).intValue();
      } else {
        input.untilDate = LocalDate.parse(untilDateField.getText().trim());
      }
      return input;
    }
  }

  enum EditScope {
    SINGLE,
    SERIES_FROM,
    SERIES_ALL
  }

  static class EventEditRequest {
    private EventProperty property;
    private Object value;
    private EditScope scope;

    EventProperty getProperty() {
      return property;
    }

    Object getValue() {
      return value;
    }

    EditScope getScope() {
      return scope;
    }
  }

  static class EventEditPanel extends JPanel {
    private final JComboBox<EventProperty> propertyCombo;
    private final JTextField textField;
    private final JFormattedTextField dateField;
    private final JTextField timeField;
    private final JComboBox<EventStatus> statusCombo;
    private final CardLayoutPanel cardPanel;
    private final JRadioButton single;
    private final JRadioButton seriesFrom;
    private final JRadioButton seriesAll;
    private final CalendarEvent reference;

    EventEditPanel(CalendarEvent event) {
      super(new BorderLayout(6, 6));
      this.reference = event;
      final JPanel top = new JPanel(new GridBagLayout());
      GridBagConstraints gbc = new GridBagConstraints();
      gbc.insets = new Insets(4, 4, 4, 4);
      gbc.fill = GridBagConstraints.HORIZONTAL;
      gbc.gridx = 0;
      gbc.gridy = 0;
      top.add(createLabel("Property:"), gbc);
      propertyCombo = new JComboBox<>(EventProperty.values());
      gbc.gridx = 1;
      gbc.weightx = 1.0;
      top.add(propertyCombo, gbc);
      cardPanel = new CardLayoutPanel();
      textField = createTextField();
      cardPanel.addComponent("text", textField);
      JPanel dateTimePanel = new JPanel(new GridLayout(1, 2, 6, 6));
      dateField = createFormattedField(DATE_FORMAT.toFormat());
      timeField = createTextField();
      dateTimePanel.add(dateField);
      dateTimePanel.add(timeField);
      cardPanel.addComponent("datetime", dateTimePanel);
      statusCombo = new JComboBox<>(EventStatus.values());
      cardPanel.addComponent("status", statusCombo);
      gbc.gridy++;
      gbc.gridx = 0;
      gbc.gridwidth = 2;
      top.add(cardPanel, gbc);
      add(top, BorderLayout.CENTER);
      propertyCombo.addActionListener(e -> updateCard());
      updateCard();

      JPanel scopePanel = new JPanel(new GridLayout(0, 1));
      scopePanel.setBorder(BorderFactory.createTitledBorder("Apply changes to"));
      single = createRadioButton("Only this event", true);
      seriesFrom = createRadioButton("This and following events", false);
      seriesAll = createRadioButton("Entire series", false);
      if (!event.getSeriesId().isPresent()) {
        seriesFrom.setEnabled(false);
        seriesAll.setEnabled(false);
      }
      ButtonGroup group = new ButtonGroup();
      group.add(single);
      group.add(seriesFrom);
      group.add(seriesAll);
      scopePanel.add(single);
      scopePanel.add(seriesFrom);
      scopePanel.add(seriesAll);
      add(scopePanel, BorderLayout.SOUTH);
    }

    JComboBox<EventProperty> propertyCombo() {
      return propertyCombo;
    }

    JTextField textField() {
      return textField;
    }

    JFormattedTextField dateField() {
      return dateField;
    }

    JTextField timeField() {
      return timeField;
    }

    JComboBox<EventStatus> statusCombo() {
      return statusCombo;
    }

    JRadioButton singleOption() {
      return single;
    }

    JRadioButton seriesFromOption() {
      return seriesFrom;
    }

    JRadioButton seriesAllOption() {
      return seriesAll;
    }

    private void updateCard() {
      EventProperty property = (EventProperty) propertyCombo.getSelectedItem();
      if (property == null) {
        return;
      }
      switch (property) {
        case SUBJECT:
        case DESCRIPTION:
        case LOCATION:
          cardPanel.show("text");
          textField.setText(referenceValue(property));
          break;
        case START:
        case END:
          cardPanel.show("datetime");
          LocalDateTime time = property == EventProperty.START
              ? reference.getStart()
              : reference.getEnd();
          dateField.setText(DATE_FORMAT.format(time.toLocalDate()));
          timeField.setText(TIME_FORMAT.format(time.toLocalTime()));
          break;
        case STATUS:
          cardPanel.show("status");
          statusCombo.setSelectedItem(reference.getStatus());
          break;
        default:
          break;
      }
    }

    private String referenceValue(EventProperty property) {
      switch (property) {
        case SUBJECT:
          return reference.getSubject();
        case DESCRIPTION:
          return reference.getDescription().orElse("");
        case LOCATION:
          return reference.getLocation().orElse("");
        default:
          return "";
      }
    }

    EventEditRequest buildRequest() {
      EventEditRequest request = new EventEditRequest();
      request.property = (EventProperty) propertyCombo.getSelectedItem();
      switch (request.property) {
        case SUBJECT:
        case DESCRIPTION:
        case LOCATION:
          request.value = textField.getText();
          break;
        case START:
        case END:
          LocalDate date = LocalDate.parse(dateField.getText().trim());
          LocalTime time = LocalTime.parse(timeField.getText().trim());
          request.value = LocalDateTime.of(date, time);
          break;
        case STATUS:
          request.value = statusCombo.getSelectedItem();
          break;
        default:
          throw new IllegalStateException("Unsupported property: " + request.property);
      }
      if (single.isSelected()) {
        request.scope = EditScope.SINGLE;
      } else if (seriesFrom.isSelected()) {
        request.scope = EditScope.SERIES_FROM;
      } else {
        request.scope = EditScope.SERIES_ALL;
      }
      return request;
    }
  }

  static class CardLayoutPanel extends JPanel {
    private final CardLayout layout;

    CardLayoutPanel() {
      layout = new CardLayout();
      setLayout(layout);
    }

    void addComponent(String name, Component component) {
      add(component, name);
    }

    void show(String name) {
      layout.show(this, name);
    }
  }

  private static JLabel createLabel(String text) {
    JLabel label = HEADLESS ? new SafeLabel() : new JLabel();
    if (text != null) {
      label.setText(text);
    }
    return label;
  }

  private static JTextArea createTextArea(int rows, int columns) {
    return HEADLESS ? new SafeTextArea(rows, columns) : new JTextArea(rows, columns);
  }

  private static JTextArea createTextArea() {
    return HEADLESS ? new SafeTextArea() : new JTextArea();
  }

  private static JTextField createTextField() {
    return HEADLESS ? new SafeTextField() : new JTextField();
  }

  private static JTextField createTextField(String initialValue) {
    JTextField field = createTextField();
    if (initialValue != null) {
      field.setText(initialValue);
    }
    return field;
  }

  private static JFormattedTextField createFormattedField(Format format) {
    return HEADLESS ? new SafeFormattedTextField(format) : new JFormattedTextField(format);
  }

  private static JCheckBox createCheckBox() {
    return HEADLESS ? new SafeCheckBox() : new JCheckBox();
  }

  private static JCheckBox createCheckBox(String text) {
    JCheckBox box = createCheckBox();
    if (text != null) {
      box.setText(text);
    }
    return box;
  }

  private static JRadioButton createRadioButton(String text, boolean selected) {
    JRadioButton button = HEADLESS ? new SafeRadioButton() : new JRadioButton();
    button.setText(text);
    button.setSelected(selected);
    return button;
  }

  private DayButton newDayButton() {
    return HEADLESS ? new DayButton(false) : new DayButton(true);
  }

  private static class SafeLabel extends JLabel {
    private String headlessText = "";

    @Override
    public void setText(String text) {
      if (HEADLESS) {
        headlessText = text;
      } else {
        super.setText(text);
      }
    }

    @Override
    public String getText() {
      return HEADLESS ? headlessText : super.getText();
    }
  }

  private static class SafeTextArea extends JTextArea {
    private String headlessText = "";

    SafeTextArea() {
      super();
    }

    SafeTextArea(int rows, int columns) {
      super(rows, columns);
    }

    @Override
    public void setText(String text) {
      if (HEADLESS) {
        headlessText = text;
      } else {
        super.setText(text);
      }
    }

    @Override
    public String getText() {
      return HEADLESS ? headlessText : super.getText();
    }
  }

  private static class SafeTextField extends JTextField {
    private String headlessText = "";

    @Override
    public void setText(String text) {
      if (HEADLESS) {
        headlessText = text;
      } else {
        super.setText(text);
      }
    }

    @Override
    public String getText() {
      return HEADLESS ? headlessText : super.getText();
    }
  }

  private static class SafeFormattedTextField extends JFormattedTextField {
    private String headlessText = "";

    SafeFormattedTextField(Format format) {
      super(format);
    }

    @Override
    public void setText(String text) {
      if (HEADLESS) {
        headlessText = text;
      } else {
        super.setText(text);
      }
    }

    @Override
    public String getText() {
      return HEADLESS ? headlessText : super.getText();
    }
  }

  private static class SafeCheckBox extends JCheckBox {
    private String headlessText = "";

    @Override
    public void setText(String text) {
      if (HEADLESS) {
        headlessText = text;
      } else {
        super.setText(text);
      }
    }

    @Override
    public String getText() {
      return HEADLESS ? headlessText : super.getText();
    }
  }

  private static class SafeRadioButton extends JRadioButton {
    private String headlessText = "";

    @Override
    public void setText(String text) {
      if (HEADLESS) {
        headlessText = text;
      } else {
        super.setText(text);
      }
    }

    @Override
    public String getText() {
      return HEADLESS ? headlessText : super.getText();
    }
  }

  void initializeForTesting(LocalDate referenceDate) {
    calendarSelector = new JComboBox<>();
    timezoneLabel = createLabel(null);
    monthLabel = createLabel(null);
    monthLabel.setHorizontalAlignment(SwingConstants.CENTER);
    dayGrid = new JPanel();
    eventList = new JList<>(eventListModel);
    eventDetails = createTextArea();
    eventDetails.setLineWrap(true);
    eventDetails.setWrapStyleWord(true);
    eventDetails.setEditable(false);
    selectedDate = referenceDate;
    monthCursor = referenceDate.withDayOfMonth(1);
    dayButtons.clear();
    for (int i = 0; i < 42; i++) {
      dayButtons.add(new DayButton(false));
    }
  }

  JComboBox<String> getCalendarSelector() {
    return calendarSelector;
  }

  JLabel getTimezoneLabel() {
    return timezoneLabel;
  }

  JLabel getMonthLabel() {
    return monthLabel;
  }

  DefaultListModel<CalendarEvent> getEventListModel() {
    return eventListModel;
  }

  List<DayButton> getDayButtons() {
    return dayButtons;
  }

  Map<String, Color> getCalendarColors() {
    return calendarColors;
  }

  LocalDate getSelectedDate() {
    return selectedDate;
  }

  LocalDate getMonthCursor() {
    return monthCursor;
  }

  JTextArea getEventDetails() {
    return eventDetails;
  }

  /**
   * Lazily initializes UI components so logic can run in headless tests.
   */
  private void ensureUiComponents() {
    if (calendarSelector == null) {
      calendarSelector = new JComboBox<>();
    }
    if (timezoneLabel == null) {
      timezoneLabel = createLabel(null);
    }
    if (monthLabel == null) {
      monthLabel = createLabel(null);
      monthLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }
    if (dayGrid == null) {
      dayGrid = new JPanel(new GridLayout(6, 7));
    }
    if (eventList == null) {
      eventList = new JList<>(eventListModel);
    }
    if (eventDetails == null) {
      eventDetails = createTextArea();
      eventDetails.setLineWrap(true);
      eventDetails.setWrapStyleWord(true);
      eventDetails.setEditable(false);
    }
    if (dayButtons.isEmpty()) {
      for (int i = 0; i < 42; i++) {
        dayButtons.add(newDayButton());
      }
    }
  }
}
