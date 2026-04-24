package calendar.view.gui;

import calendar.controller.features.GuiFeatures;
import calendar.model.InterfaceEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
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
import javax.swing.JSplitPane;

/**
 * Swing-based month view with passive callbacks to the controller.
 */
public class SwingCalendarView {

  private final UiTheme theme;
  private GuiFeatures features;
  private final JFrame frame;
  private final JComboBox<String> calendarPicker;
  private final JLabel activeLabel;
  private final JPanel monthGrid;
  private final DefaultListModel<String> dayModel;
  private final JList<String> dayList;
  private LocalDate shownMonth;
  private ZoneId activeZone;
  private String activeCalendarName;
  private final JLabel monthTitle;
  private final DateTimeFormatter monthFormatter;
  private List<InterfaceEvent> currentDayEvents;
  private final EventDetailsDialog eventDetailsDialog;
  private final Set<LocalDate> daysWithEvents;

  /**
   * Constructs the Swing view and initializes all UI widgets using the given theme.
   *
   * @param theme theme providing colors and fonts for this view
   */
  public SwingCalendarView(UiTheme theme) {
    this.theme = theme;

    frame = new JFrame("Calendar");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(960, 720);
    frame.getContentPane().setBackground(theme.background());

    JPanel top = new JPanel(new BorderLayout());
    top.setBackground(theme.headerBackground());

    calendarPicker = new JComboBox<String>();
    calendarPicker.setPreferredSize(new Dimension(200, 26));
    calendarPicker.setBackground(theme.fieldBackground());
    calendarPicker.setForeground(theme.primaryText());
    calendarPicker.setBorder(BorderFactory.createLineBorder(theme.border()));
    calendarPicker.setFont(theme.bodyFont());

    activeLabel = new JLabel("No calendar");
    activeLabel.setForeground(theme.mutedText());
    activeLabel.setFont(theme.bodyFont());

    JButton addCal = new JButton("New Calendar");
    JButton renameCal = new JButton("Rename");
    JButton tzCal = new JButton("Change TZ");

    stylePrimaryButton(addCal);
    stylePrimaryButton(renameCal);
    stylePrimaryButton(tzCal);

    JPanel actions = new JPanel();
    actions.setBackground(theme.headerBackground());
    actions.add(addCal);
    actions.add(renameCal);
    actions.add(tzCal);

    top.add(calendarPicker, BorderLayout.WEST);
    top.add(activeLabel, BorderLayout.CENTER);
    top.add(actions, BorderLayout.EAST);

    JPanel center = new JPanel(new BorderLayout());
    center.setBackground(theme.panelBackground());

    JPanel nav = new JPanel();
    nav.setBackground(theme.panelBackground());

    JButton prev = new JButton("<");
    JButton next = new JButton(">");

    styleSecondaryButton(prev);
    styleSecondaryButton(next);

    monthTitle = new JLabel();
    monthTitle.setPreferredSize(new Dimension(220, 26));
    monthTitle.setForeground(theme.primaryText());
    monthTitle.setFont(theme.titleFont());

    JButton addEvent = new JButton("Add Event");
    stylePrimaryButton(addEvent);

    nav.add(prev);
    nav.add(next);
    nav.add(monthTitle);
    nav.add(addEvent);
    center.add(nav, BorderLayout.NORTH);

    monthGrid = new JPanel(new GridLayout(6, 7, 4, 4));
    monthGrid.setBackground(theme.panelBackground());

    JPanel weekHeader = new JPanel(new GridLayout(1, 7));
    weekHeader.setBackground(theme.headerBackground());
    String[] dayNames = new String[] {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    for (String n : dayNames) {
      JLabel lbl = new JLabel(n, JLabel.CENTER);
      lbl.setOpaque(true);
      lbl.setForeground(theme.mutedText());
      lbl.setBackground(theme.headerBackground());
      lbl.setBorder(BorderFactory.createMatteBorder(
          0, 0, 1, 0, theme.border()));
      lbl.setFont(theme.captionFont());
      weekHeader.add(lbl);
    }

    JPanel gridWrapper = new JPanel(new BorderLayout());
    gridWrapper.setBackground(theme.panelBackground());
    gridWrapper.add(weekHeader, BorderLayout.NORTH);
    gridWrapper.add(monthGrid, BorderLayout.CENTER);
    center.add(gridWrapper, BorderLayout.CENTER);

    dayModel = new DefaultListModel<String>();
    dayList = new JList<String>(dayModel);
    dayList.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
    dayList.setBackground(theme.listBackground());
    dayList.setForeground(theme.primaryText());
    dayList.setSelectionBackground(theme.accent().darker());
    dayList.setSelectionForeground(Color.WHITE);
    dayList.setFont(theme.bodyFont());

    JScrollPane rightScroll = new JScrollPane(dayList);
    rightScroll.getViewport().setBackground(theme.listBackground());
    rightScroll.setBorder(BorderFactory.createLineBorder(theme.border()));

    JPanel rightPanel = new JPanel(new BorderLayout());
    rightPanel.setBackground(theme.panelBackground());
    JLabel eventsHeader = new JLabel("Events");
    eventsHeader.setFont(theme.headingFont());
    eventsHeader.setForeground(theme.primaryText());
    eventsHeader.setBorder(BorderFactory.createEmptyBorder(8, 8, 6, 8));
    rightPanel.add(eventsHeader, BorderLayout.NORTH);
    rightPanel.add(rightScroll, BorderLayout.CENTER);

    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, center, rightPanel);
    split.setResizeWeight(0.75);
    split.setBackground(theme.background());

    frame.getContentPane().add(top, BorderLayout.NORTH);
    frame.getContentPane().add(split, BorderLayout.CENTER);

    monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy");
    currentDayEvents = new ArrayList<InterfaceEvent>();
    eventDetailsDialog = new EventDetailsDialog(frame);
    daysWithEvents = new HashSet<LocalDate>();

    calendarPicker.setRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(
          JList<?> list,
          Object value,
          int index,
          boolean isSelected,
          boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(
            list, value, index, isSelected, cellHasFocus);
        label.setOpaque(true);
        label.setBackground(isSelected ? theme.tileHighlight() : theme.fieldBackground());
        label.setForeground(theme.primaryText());
        label.setFont(theme.bodyFont());
        if (value != null && value.equals(activeCalendarName)) {
          if (isSelected) {
            label.setBackground(theme.accent());
            label.setForeground(Color.WHITE);
          } else {
            label.setBackground(theme.tileHighlight());
            label.setForeground(theme.primaryText());
          }
        }
        return label;
      }
    });

    dayList.setCellRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(
          JList<?> list,
          Object value,
          int index,
          boolean isSelected,
          boolean cellHasFocus) {

        if (value == null) {
          return super.getListCellRendererComponent(
              list, value, index, isSelected, cellHasFocus);
        }

        if (index == 0) {
          JLabel header = (JLabel) super.getListCellRendererComponent(
              list, value, index, isSelected, cellHasFocus);
          header.setFont(theme.subheadingFont());
          header.setBorder(BorderFactory.createMatteBorder(
              0, 0, 1, 0, theme.border()));
          header.setBackground(theme.listBackground());
          header.setForeground(theme.primaryText());
          if (isSelected) {
            header.setBackground(list.getSelectionBackground());
            header.setForeground(list.getSelectionForeground());
          }
          return header;
        }

        JPanel row = new JPanel(new BorderLayout());
        row.setBackground(theme.listBackground());
        row.setForeground(theme.primaryText());

        JLabel text = new JLabel(value.toString());
        text.setOpaque(false);
        text.setForeground(theme.primaryText());
        text.setFont(theme.bodyFont());

        JButton detailsButton = new JButton("Details");
        styleSecondaryButton(detailsButton);
        detailsButton.setFont(theme.captionFont());
        detailsButton.setFocusable(false);
        detailsButton.setMargin(new java.awt.Insets(1, 4, 1, 4));

        row.add(text, BorderLayout.CENTER);
        row.add(detailsButton, BorderLayout.EAST);

        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(2, 2, 2, 2),
            BorderFactory.createMatteBorder(
                0, 0, 1, 0, theme.border())));

        if (isSelected) {
          Color selBg = list.getSelectionBackground();
          Color selFg = list.getSelectionForeground();
          row.setBackground(selBg);
          row.setForeground(selFg);
          text.setForeground(selFg);
          detailsButton.setBackground(selBg);
          detailsButton.setForeground(selFg);
        }

        row.setOpaque(true);
        return row;
      }
    });

    dayList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        int idx = dayList.locationToIndex(e.getPoint());
        if (idx <= 0) {
          return;
        }
        int eventIndex = idx - 1;
        if (eventIndex < 0 || eventIndex >= currentDayEvents.size()) {
          return;
        }
        InterfaceEvent ev = currentDayEvents.get(eventIndex);
        eventDetailsDialog.show(ev);
      }
    });

    prev.addActionListener(e -> showMonth(shownMonth.minusMonths(1)));
    next.addActionListener(e -> showMonth(shownMonth.plusMonths(1)));
    addCal.addActionListener(e -> promptCreateCalendar());
    renameCal.addActionListener(e -> promptRenameCalendar());
    tzCal.addActionListener(e -> promptChangeTimezone());
    addEvent.addActionListener(e -> promptCreateEvent());
    calendarPicker.addActionListener(e -> {
      Object sel = calendarPicker.getSelectedItem();
      if (sel != null && features != null) {
        features.onUseCalendar(sel.toString());
      }
    });

    shownMonth = LocalDate.now().withDayOfMonth(1);
    activeZone = ZoneId.systemDefault();
    activeCalendarName = null;
    monthTitle.setText(monthFormatter.format(shownMonth));
    rebuildMonthButtons();
  }

  /**
   * Enlarges a button by increasing its preferred width and height.
   *
   * @param button button to enlarge
   */
  private void enlargeButton(JButton button) {
    Dimension size = button.getPreferredSize();
    int extra = 4;
    Dimension larger = new Dimension(size.width + extra, size.height + extra);
    button.setPreferredSize(larger);
  }

  /**
   * Applies the primary accent styling to the given button.
   *
   * @param button button to style
   */
  private void stylePrimaryButton(JButton button) {
    button.setBackground(theme.accent());
    button.setForeground(Color.WHITE);
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createLineBorder(theme.border()));
    button.setFont(theme.bodyFont());
    enlargeButton(button);
  }

  /**
   * Applies secondary styling to the given button, used for less
   * prominent controls.
   *
   * @param button button to style
   */
  private void styleSecondaryButton(JButton button) {
    button.setBackground(theme.fieldBackground());
    button.setForeground(theme.primaryText());
    button.setFocusPainted(false);
    button.setBorder(BorderFactory.createLineBorder(theme.border()));
    button.setFont(theme.bodyFont());
    enlargeButton(button);
  }

  /**
   * Injects controller callbacks used by this view.
   *
   * @param features controller callbacks interface
   */
  public void setFeatures(GuiFeatures features) {
    this.features = features;
    this.eventDetailsDialog.setFeatures(features);
  }

  /**
   * Makes the calendar window visible.
   */
  public void showView() {
    frame.setVisible(true);
  }

  /**
   * Populates the calendar picker with the available calendars.
   *
   * @param names list of calendar names
   */
  public void setCalendars(List<String> names) {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>();
    for (String n : names) {
      model.addElement(n);
    }
    calendarPicker.setModel(model);
    if (activeCalendarName != null) {
      calendarPicker.setSelectedItem(activeCalendarName);
    }
  }

  /**
   * Updates the active calendar label and stores the active timezone.
   *
   * @param name active calendar name
   * @param zone active calendar time zone
   */
  public void setActiveCalendar(String name, ZoneId zone) {
    this.activeZone = zone;
    this.activeCalendarName = name;
    activeLabel.setText("Active: " + name + " [" + zone.getId() + "]");
    activeLabel.setOpaque(true);
    activeLabel.setBackground(theme.tileHighlight());
    activeLabel.setForeground(theme.primaryText());
    calendarPicker.setSelectedItem(name);
    calendarPicker.repaint();
  }

  /**
   * Shows the month that contains the given date and notifies the controller.
   *
   * @param first any day within the month to display
   */
  public void showMonth(LocalDate first) {
    this.shownMonth = first.withDayOfMonth(1);
    monthTitle.setText(monthFormatter.format(this.shownMonth));
    if (features != null) {
      features.onMonthShown(this.shownMonth);
    }
    rebuildMonthButtons();
  }

  /**
   * Marks the given days as containing events within the current month view.
   *
   * @param days list of dates that contain events
   */
  public void markDaysWithEvents(List<LocalDate> days) {
    daysWithEvents.clear();
    if (days != null) {
      daysWithEvents.addAll(days);
    }
    rebuildMonthButtons();
  }

  /**
   * Renders events for the given day into the side list, replacing any
   * previously displayed events.
   *
   * @param date   date whose events are shown
   * @param events events occurring on that date
   * @param zone   timezone used to format times
   */
  public void renderDayEvents(LocalDate date,
                              List<? extends InterfaceEvent> events,
                              ZoneId zone) {
    currentDayEvents = new ArrayList<InterfaceEvent>();
    if (events != null) {
      currentDayEvents.addAll(events);
    }
    dayModel.clear();
    DateTimeFormatter t = DateTimeFormatter.ofPattern("HH:mm").withZone(zone);
    dayModel.addElement(date.toString());
    for (InterfaceEvent e : events) {
      String line = t.format(e.getStart()) + " " + e.getSubject();
      dayModel.addElement(line);
    }
    if (features != null) {
      features.onMonthShown(shownMonth);
    }
  }

  /**
   * Requests that the controller refresh the events for the given day.
   *
   * @param date date to re-fetch and display
   */
  public void refreshDay(LocalDate date) {
    if (features != null) {
      features.onViewDay(date);
    }
  }

  /**
   * Displays an informational dialog on top of the calendar window.
   *
   * @param message content of the information message
   */
  public void info(String message) {
    JOptionPane.showMessageDialog(frame, message,
        "Info", JOptionPane.INFORMATION_MESSAGE);
  }

  /**
   * Displays an error dialog on top of the calendar window.
   *
   * @param message content of the error message
   */
  public void error(String message) {
    JOptionPane.showMessageDialog(frame, message,
        "Error", JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Rebuilds the grid of day buttons for the currently shown month,
   * marking days that have events and wiring up callbacks for day clicks.
   */
  private void rebuildMonthButtons() {
    monthGrid.removeAll();
    LocalDate firstOfMonth = shownMonth.withDayOfMonth(1);
    int offset = firstOfMonth.getDayOfWeek().getValue() % 7;
    int length = shownMonth.lengthOfMonth();
    int cell = 0;

    for (int i = 0; i < 42; i++) {
      JButton b = new JButton("");
      b.setMargin(new java.awt.Insets(1, 1, 1, 1));
      styleSecondaryButton(b);

      if (i >= offset && cell < length) {
        cell++;

        b.setText(Integer.toString(cell));
        b.setBackground(theme.tileBackground());
        b.setForeground(theme.primaryText());

        LocalDate date = firstOfMonth.withDayOfMonth(cell);
        if (daysWithEvents.contains(date)) {
          b.setBackground(theme.tileHighlight());
          b.setForeground(theme.accent());
        }

        b.addActionListener(e -> {
          if (features != null) {
            features.onViewDay(date);
          }
        });
      } else {
        b.setEnabled(false);
        b.setBackground(theme.panelBackground());
        b.setForeground(theme.mutedText());
      }

      monthGrid.add(b);
    }

    monthGrid.revalidate();
    monthGrid.repaint();
  }

  /**
   * Prompts the user to create a new calendar and notifies the controller
   * if a valid name and timezone are chosen.
   */
  private void promptCreateCalendar() {
    String name = JOptionPane.showInputDialog(frame, "Calendar name:");
    if (name == null || name.isBlank()) {
      return;
    }
    ZoneId zone = promptForZoneId(activeZone);
    if (zone == null) {
      return;
    }
    if (features != null) {
      features.onCreateCalendar(name.trim(), zone);
    }
  }

  /**
   * Prompts the user to rename the currently selected calendar,
   * notifying the controller of the change.
   */
  private void promptRenameCalendar() {
    String oldName = (String) calendarPicker.getSelectedItem();
    if (oldName == null) {
      error("No active calendar.");
      return;
    }
    String newName = JOptionPane.showInputDialog(frame, "New name:", oldName);
    if (newName == null || newName.isBlank()) {
      return;
    }
    if (features != null) {
      features.onRenameCalendar(oldName, newName.trim());
    }
  }

  /**
   * Prompts the user to change the timezone for the active calendar,
   * notifying the controller if a new zone is selected.
   */
  private void promptChangeTimezone() {
    String cal = (String) calendarPicker.getSelectedItem();
    if (cal == null) {
      error("No active calendar.");
      return;
    }
    ZoneId zone = promptForZoneId(activeZone);
    if (zone == null) {
      return;
    }
    if (features != null) {
      features.onChangeCalendarTimezone(cal, zone);
    }
  }

  /**
   * Shows a dialog that allows the user to pick a timezone.
   *
   * @param initial initially selected timezone
   * @return chosen {@link ZoneId}, or {@code null} if cancelled
   */
  private ZoneId promptForZoneId(ZoneId initial) {
    List<String> zones = new ArrayList<String>(ZoneId.getAvailableZoneIds());
    Collections.sort(zones);
    JComboBox<String> combo = new JComboBox<String>(zones.toArray(new String[0]));
    combo.setEditable(false);
    combo.setSelectedItem(initial.getId());
    combo.setFont(theme.bodyFont());

    JPanel panel = new JPanel(new BorderLayout());
    panel.setBackground(theme.panelBackground());
    JLabel label = new JLabel("Time zone:");
    label.setForeground(theme.primaryText());
    label.setFont(theme.bodyFont());
    panel.add(label, BorderLayout.WEST);
    panel.add(combo, BorderLayout.CENTER);

    int res = JOptionPane.showConfirmDialog(frame, panel, "Select Timezone",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (res != JOptionPane.OK_OPTION) {
      return null;
    }
    Object sel = combo.getSelectedItem();
    if (sel == null) {
      return null;
    }
    try {
      return ZoneId.of(sel.toString());
    } catch (Exception e) {
      error("Invalid timezone: " + sel.toString());
      return null;
    }
  }

  /**
   * Prompts the user to create new event and issues the appropriate
   * controller callback based on the collected data.
   */
  private void promptCreateEvent() {
    NewEventPanel panel = new NewEventPanel(activeZone, theme);
    int res = JOptionPane.showConfirmDialog(frame, panel, "Create Event",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (res != JOptionPane.OK_OPTION) {
      return;
    }
    String subject = panel.getSubject();
    if (subject == null || subject.isBlank()) {
      error("Subject is required.");
      return;
    }
    Instant start = panel.getStartInstant();
    Instant end = panel.getEndInstant();
    if (start == null || end == null) {
      error("Invalid start or end.");
      return;
    }
    if (panel.isRecurring()) {
      Set<DayOfWeek> days = panel.getSelectedDays();
      if (days.isEmpty()) {
        error("Select at least one weekday.");
        return;
      }
      if (panel.isCountBased()) {
        Integer count = panel.getCount();
        if (count == null || count <= 0) {
          error("Repeat count must be positive.");
          return;
        }
        features.onCreateRecurringByCount(
            subject, start, end, List.copyOf(days), count,
            panel.getDescription(), panel.getLocationText(),
            panel.isPublicSelected(), activeZone);
      } else {
        String until = panel.getUntilDate();
        if (until == null || until.isBlank()) {
          error("Until date is required.");
          return;
        }
        features.onCreateRecurringUntil(
            subject, start, end, List.copyOf(days), until,
            panel.getDescription(), panel.getLocationText(),
            panel.isPublicSelected(), activeZone);
      }
    } else {
      features.onCreateSingleEvent(
          subject, start, end, panel.getDescription(), panel.getLocationText(),
          panel.isPublicSelected(), activeZone);
    }
  }
}
