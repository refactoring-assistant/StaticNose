package calendar.view;

import calendar.controller.Features;
import calendar.model.InterfaceEvent;
import calendar.view.dto.AvailabilityDto;
import calendar.view.dto.CopyEventDto;
import calendar.view.dto.CreateCalDto;
import calendar.view.dto.CreateEventDto;
import calendar.view.dto.EditCalDto;
import calendar.view.dto.EditEventDto;
import calendar.view.dto.ExportCalDto;
import calendar.view.dto.QueryEventDto;
import calendar.view.dto.SelectCalDto;
import calendar.view.dto.SelectDayDto;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

/**
 * The implementation of the GUI View for the Calendar Application.
 * This class extends JFrame to provide a visual interface for the user to interact
 * with calendars and events. It includes a monthly grid view, an event list side panel,
 * and various control buttons for managing the application state.
 */
public class GuiViewImpl extends JFrame implements GuiView {

  private final JLabel currentCalendarLabel;
  private final JComboBox<String> calendarDropdown;
  private final JLabel monthYearLabel;
  private final JButton prevButton;
  private final JButton nextButton;
  private final JButton createCalButton;
  private final JButton editCalButton;
  private final JButton createEventButton;
  private final JButton editEventButton;
  private final JButton copyEventButton;
  private final JButton checkAvailabilityButton;
  private final JButton queryButton;
  private final JButton exportButton;
  //  private final JButton bulkEditButton;
  private final List<JButton> dayButtons = new ArrayList<>();
  private final JList<InterfaceEvent> eventList;
  private final DefaultListModel<InterfaceEvent> eventListModel;
  private Features features;
  private int currentYear;
  private int currentMonth;

  /**
   * Constructs a new GuiViewImpl.
   * Initializes the main JFrame, sets up the layout, creates the header panel,
   * the calendar grid panel, and the event list side panel. It also configures
   * the custom cell renderer for the event list to display HTML-formatted event details.
   */
  public GuiViewImpl() {
    super("Calendar Application");
    this.setSize(1400, 850);
    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.setLayout(new BorderLayout(10, 10));

    JPanel mainHeaderPanel = new JPanel(new BorderLayout(10, 0));
    mainHeaderPanel.setBorder(BorderFactory.createEtchedBorder());

    currentCalendarLabel = new JLabel("Current: None");
    currentCalendarLabel.setFont(new Font("Arial", Font.BOLD, 14));
    currentCalendarLabel.setForeground(new Color(0, 102, 204));
    currentCalendarLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 0));

    calendarDropdown = new JComboBox<>();
    calendarDropdown.setPreferredSize(new Dimension(200, 30));

    createCalButton = new JButton("New Calendar");
    editCalButton = new JButton("Edit Calendar");

    prevButton = new JButton("<");
    nextButton = new JButton(">");
    monthYearLabel = new JLabel("Month");
    monthYearLabel.setFont(new Font("Arial", Font.BOLD, 24));

    JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
    controlsPanel.add(new JLabel("Select:"));
    controlsPanel.add(calendarDropdown);
    controlsPanel.add(createCalButton);
    controlsPanel.add(editCalButton);
    controlsPanel.add(Box.createHorizontalStrut(30));
    controlsPanel.add(prevButton);
    controlsPanel.add(monthYearLabel);
    controlsPanel.add(nextButton);

    mainHeaderPanel.add(currentCalendarLabel, BorderLayout.WEST);
    mainHeaderPanel.add(controlsPanel, BorderLayout.CENTER);
    this.add(mainHeaderPanel, BorderLayout.NORTH);

    JPanel gridPanel = new JPanel(new GridLayout(7, 7, 0, 0));
    gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    for (String d : days) {
      JLabel dayLabel = new JLabel(d, SwingConstants.CENTER);
      dayLabel.setFont(new Font("Arial", Font.BOLD, 16));
      gridPanel.add(dayLabel);
    }

    for (int i = 0; i < 42; i++) {
      JButton btn = new JButton();
      btn.setFont(new Font("Arial", Font.PLAIN, 14));
      btn.setBackground(Color.WHITE);
      btn.setOpaque(true);
      btn.setFocusPainted(false);
      btn.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
      dayButtons.add(btn);
      gridPanel.add(btn);
    }
    this.add(gridPanel, BorderLayout.CENTER);

    JPanel sidePanel = new JPanel(new BorderLayout(10, 10));
    sidePanel.setBorder(BorderFactory.createTitledBorder("Events"));
    sidePanel.setPreferredSize(new Dimension(350, 0));

    eventListModel = new DefaultListModel<>();
    eventList = new JList<>(eventListModel);
    eventList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    eventList.setCellRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                    boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
            cellHasFocus);

        if (value instanceof InterfaceEvent) {
          InterfaceEvent e =  (InterfaceEvent) value;
          if (isSelected) {
            label.setBackground(new Color(200, 220, 255));
            label.setForeground(Color.BLACK);
          } else {
            label.setBackground(Color.WHITE);
            label.setForeground(Color.BLACK);
          }

          String subject = e.getSubject() == null ? "(No Subject)" : e.getSubject();
          String start = e.getStartDate()
              + " "
              + e.getStartTime();
          String end = e.getEndDate()
              + " "
              + e.getEndTime();
          String loc = (e.getLocation() == null || e.getLocation().isEmpty())
              ? "N/A"
              : e.getLocation();
          String status = e.getStatus() == null ? "" : e.getStatus();
          String desc = e.getDescription() == null ? "" : e.getDescription();

          String html = String.format(
              "<html><body style='width: 280px; padding: 5px;'>"
                  + "<b style='font-size: 110%%; color: #003366;'>%s</b>"
                  + "<br/><span style='color: #333333;'>%s &#8594; %s</span>"
                  + "<br/>Location: <b>%s</b> &nbsp;|&nbsp; Status: <b>%s</b>"
                  + "<br/><i style='color: #555555;'>%s</i>"
                  + "</body></html>",
              subject, start, end, loc, status, desc
          );

          label.setText(html);

          label.setBorder(BorderFactory.createCompoundBorder(
              BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
              BorderFactory.createEmptyBorder(5, 5, 5, 5)
          ));
        }
        return label;
      }
    });

    createEventButton = new JButton("Create Event");
    editEventButton = new JButton("Edit Event");
    copyEventButton = new JButton("Copy Event");
    checkAvailabilityButton = new JButton("Check Availability");
    queryButton = new JButton("Query Events");
    exportButton = new JButton("Export");
    //    bulkEditButton = new JButton("Bulk Edit");

    JPanel buttonPanel = new JPanel(new GridLayout(3, 2, 5, 5));
    buttonPanel.add(createEventButton);
    buttonPanel.add(editEventButton);
    buttonPanel.add(copyEventButton);
    buttonPanel.add(checkAvailabilityButton);
    buttonPanel.add(queryButton);
    buttonPanel.add(exportButton);
    //    buttonPanel.add(bulkEditButton);

    sidePanel.add(new JScrollPane(eventList), BorderLayout.CENTER);
    sidePanel.add(buttonPanel, BorderLayout.SOUTH);

    this.add(sidePanel, BorderLayout.EAST);
  }

  @Override
  public void setFeatures(Features features) {
    this.features = features;

    prevButton.addActionListener(e -> features.prevMonth());
    nextButton.addActionListener(e -> features.nextMonth());

    calendarDropdown.addActionListener(e -> handleSelectCalendar());
    createCalButton.addActionListener(e -> handleCreateCalendar());
    editCalButton.addActionListener(e -> handleEditCalendar());

    createEventButton.addActionListener(e -> handleCreateEvent());
    editEventButton.addActionListener(e -> handleEditEvent());
    copyEventButton.addActionListener(e -> handleCopyEvent());

    checkAvailabilityButton.addActionListener(e -> handleCheckAvailability());
    queryButton.addActionListener(e -> handleQuery());
    exportButton.addActionListener(e -> handleExport());
    //    bulkEditButton.addActionListener(e -> handleBulkEdit());

    for (JButton btn : dayButtons) {
      for (var l : btn.getActionListeners()) {
        btn.removeActionListener(l);
      }
      btn.addActionListener(e -> handleDayClick(btn));
    }
  }

  @Override
  public void updateMonthDisplay(String monthYear, String[] days) {
    monthYearLabel.setText(monthYear);
    try {
      String[] parts = monthYear.split(" ");
      this.currentMonth = parseMonth(parts[0]);
      this.currentYear = Integer.parseInt(parts[1]);
    } catch (Exception e) {
      this.currentYear = java.time.LocalDate.now().getYear();
      this.currentMonth = java.time.LocalDate.now().getMonthValue();
    }

    for (int i = 0; i < 42; i++) {
      JButton btn = dayButtons.get(i);
      String dayText = days[i];
      btn.setText(dayText);

      if (dayText == null || dayText.isEmpty()) {
        btn.setEnabled(false);
        btn.setBackground(Color.LIGHT_GRAY);
      } else {
        btn.setEnabled(true);
        btn.setBackground(Color.WHITE);
      }
    }
  }

  @Override
  public void displayEventList(List<InterfaceEvent> events) {
    eventListModel.clear();
    if (events != null) {
      for (InterfaceEvent e : events) {
        eventListModel.addElement(e);
      }
    }
  }

  @Override
  public void updateCalendarList(List<String> calendarNames) {
    var listeners = calendarDropdown.getActionListeners();
    for (var l : listeners) {
      calendarDropdown.removeActionListener(l);
    }

    calendarDropdown.removeAllItems();
    for (String name : calendarNames) {
      calendarDropdown.addItem(name);
    }

    for (var l : listeners) {
      calendarDropdown.addActionListener(l);
    }
  }

  @Override
  public void highlightActiveCalendar(String calendarName) {
    var listeners = calendarDropdown.getActionListeners();
    for (var l : listeners) {
      calendarDropdown.removeActionListener(l);
    }

    calendarDropdown.setSelectedItem(calendarName);

    for (var l : listeners) {
      calendarDropdown.addActionListener(l);
    }
    currentCalendarLabel.setText("Current: "
        + calendarName);
  }

  @Override
  public void showErrorPopup(String message) {
    ErrorDialog dialog = new ErrorDialog(this, message);
    dialog.setVisible(true);
  }

  @Override
  public void showMessagePopup(String message) {
    JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
  }

  @Override
  public void setVisible(boolean visible) {
    super.setVisible(visible);
  }

  private void handleSelectCalendar() {
    String selected = (String) calendarDropdown.getSelectedItem();
    if (selected != null) {
      features.selectCalendar(new SelectCalDto(selected));
    }
  }

  private void handleCreateCalendar() {
    CreateCalendarDialog dialog = new CreateCalendarDialog(this);
    dialog.setVisible(true);
    if (dialog.isConfirmed()) {
      features.createCalendar(new CreateCalDto(dialog.getCalendarName(), dialog.getTimezone()));
    }
  }

  private void handleEditCalendar() {
    String currentName = (String) calendarDropdown.getSelectedItem();
    if (currentName == null) {
      showErrorPopup("No calendar selected.");
      return;
    }
    EditCalDialog dialog = new EditCalDialog(this, currentName);
    dialog.setVisible(true);
    if (dialog.isConfirmed()) {
      String value = dialog.getNewValue();
      String newName = null;
      String newZone = null;
      if (dialog.isNameEdit()) {
        newName = value;
      } else {
        newZone = value;
      }

      features.editCalendar(new EditCalDto(newName, newZone));
    }
  }

  private void handleCreateEvent() {
    CreateEventDialog dialog = new CreateEventDialog(this);
    dialog.setTitle("Create New Event");
    dialog.setVisible(true);
    if (dialog.isConfirmed()) {
      try {

        CreateEventDto dto = new CreateEventDto.Builder().eventName(dialog.getEventName())
            .startDate(dialog.getStartDate()).startTime(dialog.getStartTime())
            .endDate(dialog.getEndDate()).endTime(dialog.getEndTime())
            .location(dialog.getEventLocation()).description(dialog.getDescription())
            .status(dialog.getEventStatus()).isRecurring(dialog.isRecurring())
            .recurrenceDays(dialog.getRecurrenceDays()).recurrenceEnd(dialog.getRecurrenceEnd())
            .isAllDay(dialog.isAllDay()).build();

        features.createEvent(dto);
      } catch (Exception ex) {
        showErrorPopup("Invalid Input: " + ex.getMessage());
      }
    }
  }

  private void handleEditEvent() {
    InterfaceEvent selected = eventList.getSelectedValue();
    if (selected == null) {
      showErrorPopup("Please select an event to edit.");
      return;
    }
    String info = selected.getSubject() + " (" + selected.getStartDate() + ")";
    boolean isRecurring = selected.getSeriesId() != -1;
    EditEventDialog dialog = new EditEventDialog(this, info, isRecurring);
    dialog.setVisible(true);
    if (dialog.isConfirmed()) {
      try {
        EditEventDto.Scope scope = EditEventDto.Scope.SINGLE_EVENT;
        if (dialog.isFutureEventsScope()) {
          scope = EditEventDto.Scope.FUTURE_EVENTS;
        }
        if (dialog.isSeriesScope()) {
          scope = EditEventDto.Scope.ENTIRE_SERIES;
        }

        EditEventDto dto = new EditEventDto.Builder().subject(selected.getSubject())
            .startDate(selected.getStartDate()).startTime(selected.getStartTime())
            .propertyToEdit(dialog.getSelectedProperty()).newValue(dialog.getNewValue())
            .scope(scope).endTime(null).endDate(null).endDate(selected.getEndDate())
            .endTime(selected.getEndTime()).build();
        features.editEvent(dto);
      } catch (Exception ex) {
        showErrorPopup("Error updating event: " + ex.getMessage());
      }
    }
  }

  private List<String> getAvailableCalendarNames() {
    List<String> names = new ArrayList<>();
    for (int i = 0; i < calendarDropdown.getItemCount(); i++) {
      names.add(calendarDropdown.getItemAt(i));
    }
    return names;
  }

  private void handleCopyEvent() {
    List<String> calNames = getAvailableCalendarNames();
    CopyDialog dialog = new CopyDialog(this, calNames);
    dialog.setVisible(true);
    if (dialog.isConfirmed()) {
      String startDate;
      String startTime = null;
      String targetTime = null;
      String endDate = null;
      String subject = null;
      InterfaceEvent selected;
      try {
        CopyEventDto.CopyMode mode;
        if (dialog.isAllOnDateMode()) {
          mode = CopyEventDto.CopyMode.ALL_ON_DATE;
          startDate = dialog.getSourceDate();
        } else if (dialog.isRangeMode()) {
          mode = CopyEventDto.CopyMode.DATE_RANGE;
          startDate = dialog.getRangeStart();
          endDate = dialog.getRangeEnd();
        } else {
          mode = CopyEventDto.CopyMode.SELECTED_EVENT;
          selected = eventList.getSelectedValue();
          if (selected == null) {
            throw new IllegalArgumentException("Please select an event to copy.");
          }
          startDate = selected.getStartDate().toString();
          startTime = selected.getStartTime().toString();
          targetTime = dialog.getTargetTime();
          subject = selected.getSubject();
        }

        CopyEventDto dto =
            new CopyEventDto.Builder().startDate(startDate).startTime(startTime).endDate(endDate)
                .targetCalendarName(dialog.getTargetCalendar()).targetDate(dialog.getTargetDate())
                .targetTime(targetTime).mode(mode).subject(subject).build();
        features.copyEvent(dto);
      } catch (Exception ex) {
        showErrorPopup("Error copying event: " + ex.getMessage());
      }
    }
  }

  private void handleCheckAvailability() {
    AvailabilityDialog dialog = new AvailabilityDialog(this);
    dialog.addCheckListener(e -> {
      try {
        java.time.LocalDate date = java.time.LocalDate.parse(dialog.getDateText());
        java.time.LocalTime time = java.time.LocalTime.parse(dialog.getTimeText());
        java.time.LocalDateTime dt = java.time.LocalDateTime.of(date, time);
        AvailabilityDto dto = new AvailabilityDto(dt);
        boolean isBusy = features.checkAvailability(dto);
        dialog.setStatusResult(isBusy);
      } catch (Exception ex) {
        dialog.setStatusError("Invalid Date/Time");
      }
    });
    dialog.setVisible(true);
  }

  private void handleQuery() {
    QueryEventsDialog dialog = new QueryEventsDialog(this);
    dialog.setVisible(true);
    if (dialog.isConfirmed()) {
      QueryEventDto dto = new QueryEventDto(
          dialog.isRangeQuery(), dialog.getSingleDate(), dialog.getStartDate(),
          dialog.getStartTime(), dialog.getEndDate(), dialog.getEndTime()
      );
      features.querEvents(dto);
    }
  }

  private void handleExport() {
    ExportDialog dialog = new ExportDialog(this);
    dialog.addExportListener(e -> {
      String fname = dialog.getFilename();
      if (fname == null || fname.trim().isEmpty()) {
        showErrorPopup("Filename cannot be empty.");
        return;
      }
      features.exportCalendar(new ExportCalDto(fname, dialog.getFormat()));
      dialog.setVisible(false);
    });
    dialog.setVisible(true);
  }

  private void handleDayClick(JButton btn) {
    if (!btn.getText().isEmpty()) {
      int day = Integer.parseInt(btn.getText());
      LocalDate date = LocalDate.of(currentYear, currentMonth, day);
      features.selectDay(new SelectDayDto(date));
      resetButtonColors();
      btn.setBackground(Color.CYAN);
    }
  }

  private void resetButtonColors() {
    for (JButton b : dayButtons) {
      if (b.isEnabled()) {
        b.setBackground(Color.WHITE);
      }
    }
  }

  private int parseMonth(String monthName) {
    for (java.time.Month m : java.time.Month.values()) {
      if (m.name().equalsIgnoreCase(monthName)) {
        return m.getValue();
      }
    }
    return 1;
  }
}