package calendar.view.gui;

import calendar.controller.gui.GuiFeatures;
import calendar.model.Event;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

/**
 * GUI view using Swing.
 */
public class SwingGuiView implements GuiView {
  private JFrame frame;
  private JPanel monthPanel;
  private JLabel monthLabel;
  private JComboBox<String> calendarCombo;
  private GuiFeatures features;
  private YearMonth currentMonth;
  private Map<Integer, JButton> dayButtons = new HashMap<>();
  private JPanel eventsPanel;
  private JLabel statusLabel;
  private Set<Integer> daysWithEvents = new HashSet<>();
  private String currentCalendarName;

  /**
   * Constructor.
   */
  public SwingGuiView() {
    SwingUtilities.invokeLater(this::createAndShowGui);
  }

  private void createAndShowGui() {
    frame = new JFrame("Calendar - GUI");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(900, 650);
    frame.setLocationRelativeTo(null);

    JPanel root = new JPanel(new BorderLayout(8, 8));
    root.setBorder(new EmptyBorder(8, 8, 8, 8));
    frame.setContentPane(root);

    // top bar with buttons
    final JPanel top = new JPanel(new FlowLayout(FlowLayout.LEADING, 8, 8));
    calendarCombo = new JComboBox<>();
    calendarCombo.addActionListener(e -> {
      if (features != null && calendarCombo.getSelectedItem() != null) {
        features.switchCalendar(calendarCombo.getSelectedItem().toString());
      }
    });

    JButton btnNewCalendar = new JButton("New Calendar");
    btnNewCalendar.addActionListener(e -> onCreateCalendar());

    JButton prev = new JButton("<");
    prev.addActionListener(e -> {
      if (features != null) {
        features.previousMonth();
      }
    });
    JButton next = new JButton(">");
    next.addActionListener(e -> {
      if (features != null) {
        features.nextMonth();
      }
    });

    monthLabel = new JLabel();
    monthLabel.setFont(monthLabel.getFont().deriveFont(Font.BOLD, 16f));

    JButton btnNewEvent = new JButton("Create Event");
    btnNewEvent.addActionListener(e -> {
      String s = JOptionPane.showInputDialog(frame,
          "Enter day-of-month to create the event (1-"
              + currentMonth.lengthOfMonth() + "):");
      if (s == null) {
        return;
      }
      try {
        int d = Integer.parseInt(s.trim());
        if (d < 1 || d > currentMonth.lengthOfMonth()) {
          showError("Invalid day. Enter a number between 1 and "
              + currentMonth.lengthOfMonth() + ".");
          return;
        }
        LocalDate date = LocalDate.of(currentMonth.getYear(), currentMonth.getMonthValue(), d);
        if (features != null) {
          features.requestCreateEvent(date);
        }
      } catch (Exception ex) {
        showError("Invalid day. Enter a number between 1 and "
            + currentMonth.lengthOfMonth() + ".");
      }
    });

    top.add(new JLabel("Calendar:"));
    top.add(calendarCombo);
    top.add(btnNewCalendar);
    top.add(Box.createHorizontalStrut(16));
    top.add(prev);
    top.add(monthLabel);
    top.add(next);
    top.add(Box.createHorizontalStrut(16));
    top.add(btnNewEvent);
    root.add(top, BorderLayout.NORTH);

    // center panel
    final JPanel center = new JPanel(new GridLayout(1, 2, 8, 8));
    JPanel calendarPane = new JPanel(new BorderLayout(4, 4));
    calendarPane.setBorder(BorderFactory.createTitledBorder("Month view"));

    JPanel weekdayHeader = new JPanel(new GridLayout(1, 7));
    String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    for (int i = 0; i < 7; i++) {
      JLabel lbl = new JLabel(dayNames[i], SwingConstants.CENTER);
      lbl.setFont(lbl.getFont().deriveFont(Font.BOLD, 12f));
      weekdayHeader.add(lbl);
    }
    calendarPane.add(weekdayHeader, BorderLayout.NORTH);

    monthPanel = new JPanel(new GridLayout(6, 7, 4, 4));
    calendarPane.add(monthPanel, BorderLayout.CENTER);

    center.add(calendarPane);

    eventsPanel = new JPanel();
    eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.Y_AXIS));
    JScrollPane scroll = new JScrollPane(eventsPanel);
    scroll.setBorder(BorderFactory.createTitledBorder("Events on selected day"));
    center.add(scroll);

    root.add(center, BorderLayout.CENTER);

    statusLabel = new JLabel("Ready");
    root.add(statusLabel, BorderLayout.SOUTH);

    currentMonth = YearMonth.now();
    repaintMonth();

    frame.setVisible(true);
  }

  @Override
  public void setFeatures(GuiFeatures features) {
    this.features = features;
  }

  @Override
  public void showMonth(LocalDate monthStart) {
    currentMonth = YearMonth.from(monthStart);
    SwingUtilities.invokeLater(this::repaintMonth);
  }

  private void repaintMonth() {
    String[] months = {"January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"};
    monthLabel.setText(months[currentMonth.getMonthValue() - 1] + " " + currentMonth.getYear());
    monthPanel.removeAll();
    dayButtons.clear();

    LocalDate first = currentMonth.atDay(1);
    int startDow = first.getDayOfWeek().getValue() % 7; // sun=0, mon=1, etc
    int days = currentMonth.lengthOfMonth();

    // empty cells before first day
    for (int i = 0; i < startDow; i++) {
      monthPanel.add(new JLabel(""));
    }

    // day buttons
    for (int d = 1; d <= days; d++) {
      final int day = d;
      JButton b = new JButton(String.valueOf(d));
      b.setMargin(new Insets(4, 4, 4, 4));
      b.setFocusable(false);
      b.addActionListener(e -> onDayClicked(day));
      dayButtons.put(d, b);
      monthPanel.add(b);
    }

    // empty cells after last day
    int total = startDow + days;
    int trailing = 7 * 6 - total;
    for (int i = 0; i < trailing; i++) {
      monthPanel.add(new JLabel(""));
    }

    monthPanel.revalidate();
    monthPanel.repaint();
    updateDayButtonColors();
  }

  private void onDayClicked(int day) {
    LocalDate date = LocalDate.of(currentMonth.getYear(), currentMonth.getMonthValue(), day);
    if (features != null) {
      features.requestViewDay(date);
    }
  }

  private void updateDayButtonColors() {
    for (Map.Entry<Integer, JButton> entry : dayButtons.entrySet()) {
      JButton btn = entry.getValue();
      if (daysWithEvents.contains(entry.getKey())) {
        btn.setBackground(new Color(173, 216, 230)); // light blue
        btn.setOpaque(true);
        btn.setBorderPainted(false);
      } else {
        btn.setBackground(UIManager.getColor("Button.background"));
        btn.setOpaque(false);
        btn.setBorderPainted(true);
      }
    }
  }

  @Override
  public void showEventsForDay(LocalDate day, List<String> eventSummaries,
                                Map<String, Boolean> isSeriesMap) {
    SwingUtilities.invokeLater(() -> {
      eventsPanel.removeAll();
      eventsPanel.add(new JLabel("Events for " + day.toString()));
      eventsPanel.add(Box.createVerticalStrut(8));
      if (eventSummaries == null || eventSummaries.isEmpty()) {
        eventsPanel.add(new JLabel("(no events)"));
      } else {
        // check if any events have same name
        Map<String, List<String>> nameMap = new HashMap<>();
        for (String s : eventSummaries) {
          String name = extractEventName(s);
          if (nameMap.containsKey(name)) {
            nameMap.get(name).add(s);
          } else {
            List<String> list = new ArrayList<>();
            list.add(s);
            nameMap.put(name, list);
          }
        }

        // show events
        Map<String, List<JCheckBox>> checkboxMap = new HashMap<>();
        for (String name : nameMap.keySet()) {
          List<String> summaries = nameMap.get(name);
          boolean multiple = summaries.size() > 1;
          
          List<JCheckBox> checkboxes = new ArrayList<>();
          for (String s : summaries) {
            JPanel p = new JPanel(new BorderLayout());
            
            if (multiple) {
              JCheckBox cb = new JCheckBox();
              cb.setSelected(true);
              checkboxes.add(cb);
              p.add(cb, BorderLayout.WEST);
            }
            
            p.add(new JLabel(s), BorderLayout.CENTER);
            
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
            JButton editBtn = new JButton("Edit");
            String summaryCopy = s;
            editBtn.addActionListener(e -> {
              if (features != null) {
                features.requestEditEvent(day, summaryCopy);
              }
            });
            buttonPanel.add(editBtn);
            
            // add "Edit Series" button if event is part of a series
            Boolean isSeries = isSeriesMap != null ? isSeriesMap.get(s) : false;
            if (isSeries != null && isSeries) {
              JButton editSeriesBtn = new JButton("Edit Series");
              editSeriesBtn.addActionListener(e -> {
                if (features != null) {
                  features.requestEditSeries(day, summaryCopy);
                }
              });
              buttonPanel.add(editSeriesBtn);
            }
            
            p.add(buttonPanel, BorderLayout.EAST);
            p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            eventsPanel.add(p);
            eventsPanel.add(Box.createVerticalStrut(4));
          }
          
          if (multiple) {
            checkboxMap.put(name, checkboxes);
            JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton bulkBtn = new JButton("Bulk Edit Selected");
            String nameCopy = name;
            List<String> summariesCopy = new ArrayList<>(summaries);
            bulkBtn.addActionListener(e -> {
              List<String> selected = new ArrayList<>();
              for (int i = 0; i < checkboxes.size(); i++) {
                if (checkboxes.get(i).isSelected()) {
                  selected.add(summariesCopy.get(i));
                }
              }
              if (selected.isEmpty()) {
                showError("Please select at least one event to edit.");
              } else if (features != null) {
                features.requestBulkEditEvents(day, nameCopy, selected);
              }
            });
            footer.add(bulkBtn);
            eventsPanel.add(footer);
            eventsPanel.add(Box.createVerticalStrut(8));
          } else {
            eventsPanel.add(Box.createVerticalStrut(8));
          }
        }
      }
      eventsPanel.revalidate();
      eventsPanel.repaint();
      statusLabel.setText("Showing events for " + day.toString());
    });
  }

  // get event name from summary string
  private String extractEventName(String summary) {
    int parenIndex = summary.indexOf(" (");
    if (parenIndex > 0) {
      return summary.substring(0, parenIndex).trim();
    }
    return summary;
  }

  @Override
  public void setCalendarNames(List<String> calendars) {
    SwingUtilities.invokeLater(() -> {
      String selected = (String) calendarCombo.getSelectedItem();
      calendarCombo.removeAllItems();
      if (calendars != null) {
        for (String c : calendars) {
          calendarCombo.addItem(c);
        }
      }
      if (selected != null) {
        calendarCombo.setSelectedItem(selected);
      }
      updateWindowTitle();
    });
  }

  @Override
  public void setCurrentCalendarName(String calendarName) {
    this.currentCalendarName = calendarName;
    SwingUtilities.invokeLater(this::updateWindowTitle);
  }

  private void updateWindowTitle() {
    if (frame != null) {
      if (currentCalendarName != null && !currentCalendarName.isEmpty()) {
        frame.setTitle("Calendar - " + currentCalendarName);
      } else {
        frame.setTitle("Calendar - GUI");
      }
    }
  }

  private void onCreateCalendar() {
    String name = JOptionPane.showInputDialog(frame, "Enter calendar name:");
    if (name == null || name.trim().isEmpty()) {
      return;
    }
    String[] zones = TimeZone.getAvailableIDs();
    String zone = (String) JOptionPane.showInputDialog(
        frame, "Choose timezone:", "Timezone",
        JOptionPane.PLAIN_MESSAGE, null, zones, TimeZone.getDefault().getID());
    if (zone == null) {
      return;
    }
    if (features != null) {
      features.createCalendar(name.trim(), zone);
    }
  }

  @Override
  public void showError(String message) {
    SwingUtilities.invokeLater(
        () -> JOptionPane.showMessageDialog(frame, message, "Error",
            JOptionPane.ERROR_MESSAGE));
  }

  @Override
  public void refresh() {
    SwingUtilities.invokeLater(() -> {
      monthPanel.revalidate();
      monthPanel.repaint();
      updateDayButtonColors();
    });
  }

  @Override
  public void highlightDaysWithEvents(Set<Integer> daysWithEvents) {
    this.daysWithEvents = new HashSet<>(daysWithEvents);
    SwingUtilities.invokeLater(this::updateDayButtonColors);
  }

  @Override
  public Optional<EventData> showCreateEventDialog(LocalDate date) {
    final JTextField title = new JTextField();
    final JTextField start = new JTextField(date.toString() + "T09:00");
    final JTextField end = new JTextField(date.toString() + "T10:00");
    final JTextArea desc = new JTextArea(4, 30);
    final JTextField location = new JTextField();
    final JTextField status = new JTextField("public");

    JCheckBox recurringCheck = new JCheckBox("Recurring event");
    JPanel repeatDaysPanel = new JPanel(new GridLayout(2, 4));
    Map<DayOfWeek, JCheckBox> dayChecks = new HashMap<>();
    for (DayOfWeek day : DayOfWeek.values()) {
      JCheckBox cb = new JCheckBox(day.toString().substring(0, 3));
      dayChecks.put(day, cb);
      repeatDaysPanel.add(cb);
    }
    repeatDaysPanel.setVisible(false);
    recurringCheck.addActionListener(
        e -> repeatDaysPanel.setVisible(recurringCheck.isSelected()));

    final JTextField occurrencesField = new JTextField();
    final JTextField untilDateField = new JTextField();
    JLabel occurrencesLabel = new JLabel(
        "Number of occurrences (leave empty for until date):");
    JLabel untilDateLabel = new JLabel(
        "Until date (YYYY-MM-DD, leave empty for occurrences):");
    occurrencesLabel.setVisible(false);
    occurrencesField.setVisible(false);
    untilDateLabel.setVisible(false);
    untilDateField.setVisible(false);

    recurringCheck.addActionListener(e -> {
      boolean visible = recurringCheck.isSelected();
      repeatDaysPanel.setVisible(visible);
      occurrencesLabel.setVisible(visible);
      occurrencesField.setVisible(visible);
      untilDateLabel.setVisible(visible);
      untilDateField.setVisible(visible);
    });

    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.add(new JLabel("Title:"));
    p.add(title);
    p.add(new JLabel("Start (ISO local, e.g. 2025-07-25T09:00):"));
    p.add(start);
    p.add(new JLabel("End (ISO local, e.g. 2025-07-25T10:00):"));
    p.add(end);
    p.add(new JLabel("Description:"));
    p.add(new JScrollPane(desc));
    p.add(new JLabel("Location (optional):"));
    p.add(location);
    p.add(new JLabel("Status (optional, default: public):"));
    p.add(status);
    p.add(recurringCheck);
    p.add(repeatDaysPanel);
    p.add(occurrencesLabel);
    p.add(occurrencesField);
    p.add(untilDateLabel);
    p.add(untilDateField);

    int r = JOptionPane.showConfirmDialog(frame, new JScrollPane(p), "Create Event",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (r != JOptionPane.OK_OPTION) {
      return Optional.empty();
    }

    EventData ed = new EventData();
    ed.title = title.getText().trim();
    ed.description = desc.getText().trim();
    ed.location = location.getText().trim();
    ed.status = status.getText().trim();
    ed.isRecurring = recurringCheck.isSelected();

    if (ed.title.isEmpty()) {
      showError("Event title cannot be empty.");
      return Optional.empty();
    }

    try {
      ed.startDateTime = LocalDateTime.parse(start.getText().trim());
      ed.endDateTime = LocalDateTime.parse(end.getText().trim());
    } catch (Exception ex) {
      showError("Failed to parse dates. Use ISO-local format like 2025-07-25T09:00");
      return Optional.empty();
    }

    if (ed.isRecurring) {
      ed.repeatDays = EnumSet.noneOf(DayOfWeek.class);
      for (Map.Entry<DayOfWeek, JCheckBox> entry : dayChecks.entrySet()) {
        if (entry.getValue().isSelected()) {
          ed.repeatDays.add(entry.getKey());
        }
      }
      if (ed.repeatDays.isEmpty()) {
        showError("Please select at least one day for recurring event.");
        return Optional.empty();
      }

      String occText = occurrencesField.getText().trim();
      String untilText = untilDateField.getText().trim();
      if (!occText.isEmpty()) {
        try {
          ed.occurrences = Integer.parseInt(occText);
          ed.untilDate = null;
        } catch (NumberFormatException ex) {
          showError("Invalid number of occurrences.");
          return Optional.empty();
        }
      } else if (!untilText.isEmpty()) {
        try {
          ed.untilDate = LocalDate.parse(untilText);
          ed.occurrences = null;
        } catch (Exception ex) {
          showError("Invalid until date. Use format YYYY-MM-DD");
          return Optional.empty();
        }
      } else {
        showError("Please specify either number of occurrences or until date.");
        return Optional.empty();
      }
    }

    return Optional.of(ed);
  }

  @Override
  public Optional<EventData> showEditEventDialog(Event existing) {
    JTextField title = new JTextField(existing.subject());
    JTextField start = new JTextField(existing.startDate().toString());
    JTextField end = new JTextField(existing.endDate().toString());
    JTextArea desc = new JTextArea(existing.description(), 4, 30);
    JTextField location = new JTextField(existing.location());
    JTextField status = new JTextField(existing.status() != null ? existing.status() : "public");

    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.add(new JLabel("Title:"));
    p.add(title);
    p.add(new JLabel("Start (ISO local, e.g. 2025-07-25T09:00):"));
    p.add(start);
    p.add(new JLabel("End (ISO local, e.g. 2025-07-25T10:00):"));
    p.add(end);
    p.add(new JLabel("Description:"));
    p.add(new JScrollPane(desc));
    p.add(new JLabel("Location (optional):"));
    p.add(location);
    p.add(new JLabel("Status (optional):"));
    p.add(status);

    int r = JOptionPane.showConfirmDialog(frame, new JScrollPane(p), "Edit Event",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (r != JOptionPane.OK_OPTION) {
      return Optional.empty();
    }

    EventData ed = new EventData();
    ed.title = title.getText().trim();
    ed.description = desc.getText().trim();
    ed.location = location.getText().trim();
    ed.status = status.getText().trim();
    ed.isRecurring = false;

    if (ed.title.isEmpty()) {
      showError("Event title cannot be empty.");
      return Optional.empty();
    }

    try {
      ed.startDateTime = LocalDateTime.parse(start.getText().trim());
      ed.endDateTime = LocalDateTime.parse(end.getText().trim());
    } catch (Exception ex) {
      showError("Failed to parse dates. Use ISO-local format like 2025-07-25T09:00");
      return Optional.empty();
    }

    return Optional.of(ed);
  }
}
