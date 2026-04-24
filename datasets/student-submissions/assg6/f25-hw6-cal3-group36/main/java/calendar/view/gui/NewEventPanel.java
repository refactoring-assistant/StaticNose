package calendar.view.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;

/**
 * Panel used by the GUI to collect information for creating a new event.
 * Provides fields for subject, dates, times, location, description and
 * optional recurrence configuration.
 */
public class NewEventPanel extends JPanel {

  private final ZoneId zone;
  private final UiTheme theme;

  private final JTextField subjectField;
  private final DatePickerField startDateField;
  private final DatePickerField endDateField;
  private final JSpinner startTimeSpinner;
  private final JSpinner endTimeSpinner;
  private final JTextField locationField;
  private final JTextArea descriptionArea;

  private final JCheckBox recurringCheck;
  private final JPanel recurrencePanel;
  private final JCheckBox[] dayChecks;
  private final JRadioButton countRadio;
  private final JRadioButton untilRadio;
  private final JSpinner countSpinner;
  private final JFormattedTextField untilDateField;

  private final JCheckBox publicCheck;

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

  /**
   * Constructs the event creation panel bound to a specific timezone and theme.
   *
   * @param zone  timezone used to interpret the entered dates and times
   * @param theme theme providing colors and fonts
   */
  public NewEventPanel(ZoneId zone, UiTheme theme) {
    this.zone = zone;
    this.theme = theme;
    this.setLayout(new BorderLayout());
    this.setBackground(theme.background());

    JPanel form = new JPanel(new GridBagLayout());
    form.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    form.setBackground(theme.panelBackground());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(4, 4, 4, 4);
    gbc.weightx = 0.0;

    JLabel subjectLabel = new JLabel("Subject:");
    subjectLabel.setForeground(theme.primaryText());
    subjectLabel.setFont(theme.bodyFont());
    form.add(subjectLabel, gbc);

    subjectField = new JTextField(30);
    subjectField.setBackground(theme.fieldBackground());
    subjectField.setForeground(theme.primaryText());
    subjectField.setCaretColor(theme.primaryText());
    subjectField.setFont(theme.bodyFont());
    gbc.gridx = 1;
    gbc.gridwidth = 3;
    gbc.weightx = 1.0;
    form.add(subjectField, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.gridwidth = 1;
    gbc.weightx = 0.0;

    JLabel startDateLabel = new JLabel("Start date:");
    startDateLabel.setForeground(theme.primaryText());
    startDateLabel.setFont(theme.bodyFont());
    form.add(startDateLabel, gbc);

    startDateField = new DatePickerField();
    gbc.gridx = 1;
    gbc.weightx = 0.5;
    form.add(startDateField, gbc);

    JLabel endDateLabel = new JLabel("End date:");
    endDateLabel.setForeground(theme.primaryText());
    endDateLabel.setFont(theme.bodyFont());
    gbc.gridx = 2;
    gbc.weightx = 0.0;
    form.add(endDateLabel, gbc);

    endDateField = new DatePickerField();
    gbc.gridx = 3;
    gbc.weightx = 0.5;
    form.add(endDateField, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.gridwidth = 1;
    gbc.weightx = 0.0;

    JLabel startTimeLabel = new JLabel("Start time (HH:mm):");
    startTimeLabel.setForeground(theme.primaryText());
    startTimeLabel.setFont(theme.bodyFont());
    form.add(startTimeLabel, gbc);

    startTimeSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startTimeSpinner, "HH:mm");
    startTimeSpinner.setEditor(startEditor);
    startTimeSpinner.getEditor().getComponent(0).setBackground(theme.fieldBackground());
    startTimeSpinner.getEditor().getComponent(0).setForeground(theme.primaryText());
    startTimeSpinner.getEditor().getComponent(0).setFont(theme.bodyFont());
    gbc.gridx = 1;
    gbc.weightx = 0.5;
    form.add(startTimeSpinner, gbc);

    JLabel endTimeLabel = new JLabel("End time (HH:mm):");
    endTimeLabel.setForeground(theme.primaryText());
    endTimeLabel.setFont(theme.bodyFont());
    gbc.gridx = 2;
    gbc.weightx = 0.0;
    form.add(endTimeLabel, gbc);

    endTimeSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endTimeSpinner, "HH:mm");
    endTimeSpinner.setEditor(endEditor);
    endTimeSpinner.getEditor().getComponent(0).setBackground(theme.fieldBackground());
    endTimeSpinner.getEditor().getComponent(0).setForeground(theme.primaryText());
    endTimeSpinner.getEditor().getComponent(0).setFont(theme.bodyFont());
    gbc.gridx = 3;
    gbc.weightx = 0.5;
    form.add(endTimeSpinner, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.gridwidth = 1;
    gbc.weightx = 0.0;

    JLabel locationLabel = new JLabel("Location:");
    locationLabel.setForeground(theme.primaryText());
    locationLabel.setFont(theme.bodyFont());
    form.add(locationLabel, gbc);

    locationField = new JTextField(30);
    locationField.setBackground(theme.fieldBackground());
    locationField.setForeground(theme.primaryText());
    locationField.setCaretColor(theme.primaryText());
    locationField.setFont(theme.bodyFont());
    gbc.gridx = 1;
    gbc.gridwidth = 3;
    gbc.weightx = 1.0;
    form.add(locationField, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.gridwidth = 1;
    gbc.weightx = 0.0;
    gbc.anchor = GridBagConstraints.NORTHWEST;
    gbc.fill = GridBagConstraints.NONE;

    JLabel descriptionLabel = new JLabel("Description:");
    descriptionLabel.setForeground(theme.primaryText());
    descriptionLabel.setFont(theme.bodyFont());
    form.add(descriptionLabel, gbc);

    descriptionArea = new JTextArea(4, 30);
    descriptionArea.setBackground(theme.fieldBackground());
    descriptionArea.setForeground(theme.primaryText());
    descriptionArea.setCaretColor(theme.primaryText());
    descriptionArea.setFont(theme.bodyFont());
    JScrollPane descScroll = new JScrollPane(descriptionArea);
    descScroll.getViewport().setBackground(theme.fieldBackground());
    gbc.gridx = 1;
    gbc.gridwidth = 3;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.BOTH;
    form.add(descScroll, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.gridwidth = 4;
    gbc.weightx = 1.0;
    gbc.fill = GridBagConstraints.NONE;
    gbc.anchor = GridBagConstraints.WEST;

    publicCheck = new JCheckBox("Public event", true);
    publicCheck.setOpaque(false);
    publicCheck.setForeground(theme.primaryText());
    publicCheck.setFont(theme.bodyFont());
    form.add(publicCheck, gbc);

    gbc.gridy++;
    gbc.gridx = 0;

    recurringCheck = new JCheckBox("Recurring event");
    recurringCheck.setOpaque(false);
    recurringCheck.setForeground(theme.primaryText());
    recurringCheck.setFont(theme.bodyFont());
    form.add(recurringCheck, gbc);

    recurrencePanel = new JPanel(new GridBagLayout());
    recurrencePanel.setBorder(
        BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(theme.border()),
            "Recurrence",
            TitledBorder.LEFT,
            TitledBorder.TOP));
    recurrencePanel.setBackground(theme.panelBackground());

    GridBagConstraints rg = new GridBagConstraints();
    rg.gridx = 0;
    rg.gridy = 0;
    rg.insets = new Insets(2, 2, 2, 2);
    rg.anchor = GridBagConstraints.WEST;

    JLabel daysLabel = new JLabel("Days of week:");
    daysLabel.setForeground(theme.primaryText());
    daysLabel.setFont(theme.bodyFont());
    recurrencePanel.add(daysLabel, rg);

    rg.gridy++;
    JPanel daysPanel = new JPanel();
    daysPanel.setBackground(theme.panelBackground());
    DayOfWeek[] allDays = DayOfWeek.values();
    dayChecks = new JCheckBox[allDays.length];
    for (int i = 0; i < allDays.length; i++) {
      DayOfWeek d = allDays[i];
      String name = d.toString().substring(0, 1)
          + d.toString().substring(1).toLowerCase();
      JCheckBox cb = new JCheckBox(name);
      cb.setOpaque(false);
      cb.setForeground(theme.primaryText());
      cb.setFont(theme.bodyFont());
      dayChecks[i] = cb;
      daysPanel.add(cb);
    }
    recurrencePanel.add(daysPanel, rg);

    rg.gridy++;
    JLabel repeatLabel = new JLabel("Repeat mode:");
    repeatLabel.setForeground(theme.primaryText());
    repeatLabel.setFont(theme.bodyFont());
    recurrencePanel.add(repeatLabel, rg);

    rg.gridy++;
    JPanel modePanel = new JPanel();
    modePanel.setBackground(theme.panelBackground());
    countRadio = new JRadioButton("By count");
    untilRadio = new JRadioButton("Until date");
    countRadio.setOpaque(false);
    untilRadio.setOpaque(false);
    countRadio.setForeground(theme.primaryText());
    untilRadio.setForeground(theme.primaryText());
    countRadio.setFont(theme.bodyFont());
    untilRadio.setFont(theme.bodyFont());
    ButtonGroup group = new ButtonGroup();
    group.add(countRadio);
    group.add(untilRadio);
    countRadio.setSelected(true);
    modePanel.add(countRadio);
    modePanel.add(untilRadio);
    recurrencePanel.add(modePanel, rg);

    rg.gridy++;
    JPanel countPanel = new JPanel();
    countPanel.setBackground(theme.panelBackground());
    JLabel occLabel = new JLabel("Occurrences:");
    occLabel.setForeground(theme.primaryText());
    occLabel.setFont(theme.bodyFont());
    countPanel.add(occLabel);
    countSpinner = new JSpinner(new javax.swing.SpinnerNumberModel(1, 1, 1000, 1));
    countSpinner.getEditor().getComponent(0).setBackground(theme.fieldBackground());
    countSpinner.getEditor().getComponent(0).setForeground(theme.primaryText());
    countSpinner.getEditor().getComponent(0).setFont(theme.bodyFont());
    countPanel.add(countSpinner);
    recurrencePanel.add(countPanel, rg);

    rg.gridy++;
    JPanel untilPanel = new JPanel();
    untilPanel.setBackground(theme.panelBackground());
    JLabel untilLabel = new JLabel("Until (yyyy-MM-dd):");
    untilLabel.setForeground(theme.primaryText());
    untilLabel.setFont(theme.bodyFont());
    untilPanel.add(untilLabel);
    untilDateField = new JFormattedTextField();
    untilDateField.setColumns(10);
    untilDateField.setBackground(theme.fieldBackground());
    untilDateField.setForeground(theme.primaryText());
    untilDateField.setCaretColor(theme.primaryText());
    untilDateField.setFont(theme.bodyFont());
    untilPanel.add(untilDateField);
    recurrencePanel.add(untilPanel, rg);

    setRecurrenceControlsEnabled(false);

    recurringCheck.addActionListener(e -> {
      boolean enabled = recurringCheck.isSelected();
      setRecurrenceControlsEnabled(enabled);
    });

    countRadio.addActionListener(e -> syncRecurrenceModeEnabled());
    untilRadio.addActionListener(e -> syncRecurrenceModeEnabled());

    gbc.gridy++;
    gbc.gridx = 0;
    gbc.gridwidth = 4;
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1.0;
    form.add(recurrencePanel, gbc);

    this.add(form, BorderLayout.CENTER);

    LocalDate today = LocalDate.now(zone);
    startDateField.setDate(today);
    endDateField.setDate(today);

    java.util.Date now = java.util.Date.from(
        LocalDateTime.now(zone).withSecond(0).withNano(0).atZone(zone).toInstant());
    java.util.Date later = java.util.Date.from(
        LocalDateTime.now(zone).plusHours(1).withSecond(0).withNano(0).atZone(zone).toInstant());
    startTimeSpinner.setValue(now);
    endTimeSpinner.setValue(later);
  }

  /**
   * Enables or disables all recurrence-related controls,
   * including weekday checkboxes and mode-specific inputs.
   *
   * @param enabled true to enable recurrence controls; false to disable them
   */
  private void setRecurrenceControlsEnabled(boolean enabled) {
    recurrencePanel.setEnabled(enabled);
    for (JCheckBox cb : dayChecks) {
      cb.setEnabled(enabled);
    }
    countRadio.setEnabled(enabled);
    untilRadio.setEnabled(enabled);
    countSpinner.setEnabled(enabled && countRadio.isSelected());
    untilDateField.setEnabled(enabled && untilRadio.isSelected());
  }

  /**
   * Updates which recurrence sub-controls are enabled based on the
   * currently selected recurrence mode.
   */
  private void syncRecurrenceModeEnabled() {
    boolean enabled = recurringCheck.isSelected();
    countSpinner.setEnabled(enabled && countRadio.isSelected());
    untilDateField.setEnabled(enabled && untilRadio.isSelected());
  }

  /**
   * Returns the subject text entered for the event.
   *
   * @return subject text, possibly empty but never {@code null}
   */
  public String getSubject() {
    return subjectField.getText();
  }

  /**
   * Returns the description text entered for the event.
   *
   * @return description text, possibly empty but never {@code null}
   */
  public String getDescription() {
    return descriptionArea.getText();
  }

  /**
   * Returns the location text entered for the event.
   *
   * @return location text, possibly empty but never {@code null}
   */
  public String getLocationText() {
    return locationField.getText();
  }

  /**
   * Indicates whether the event is marked as public.
   *
   * @return true if public is selected; false otherwise
   */
  public boolean isPublicSelected() {
    return publicCheck.isSelected();
  }

  /**
   * Indicates whether recurrence is enabled for this event.
   *
   * @return true if recurrence is enabled; false otherwise
   */
  public boolean isRecurring() {
    return recurringCheck.isSelected();
  }

  /**
   * Returns the set of weekdays selected for recurrence.
   *
   * @return set of selected {@link DayOfWeek} values (possibly empty)
   */
  public Set<DayOfWeek> getSelectedDays() {
    Set<DayOfWeek> days = EnumSet.noneOf(DayOfWeek.class);
    DayOfWeek[] all = DayOfWeek.values();
    for (int i = 0; i < dayChecks.length && i < all.length; i++) {
      if (dayChecks[i].isSelected()) {
        days.add(all[i]);
      }
    }
    return days;
  }

  /**
   * Indicates whether recurrence is configured using a repeat count
   * rather than an until date.
   *
   * @return true if count-based mode is selected; false otherwise
   */
  public boolean isCountBased() {
    return countRadio.isSelected();
  }

  /**
   * Returns the repeat count when count-based recurrence is selected.
   *
   * @return repeat count, or {@code null} if not applicable
   */
  public Integer getCount() {
    if (!isRecurring() || !countRadio.isSelected()) {
      return null;
    }
    Object v = countSpinner.getValue();
    if (v instanceof Number) {
      return ((Number) v).intValue();
    }
    return null;
  }

  /**
   * Returns the until-date string (yyyy-MM-dd) when until-based
   * recurrence is selected.
   *
   * @return until-date text, or {@code null} if not applicable
   */
  public String getUntilDate() {
    if (!isRecurring() || !untilRadio.isSelected()) {
      return null;
    }
    String text = untilDateField.getText();
    if (text == null || text.isBlank()) {
      return null;
    }
    return text.trim();
  }

  /**
   * Computes the event start instant from the date and time fields.
   *
   * @return start instant, or {@code null} if parsing fails or date is missing
   */
  public Instant getStartInstant() {
    try {
      LocalDate date = startDateField.getDate();
      if (date == null) {
        return null;
      }
      java.util.Date timeVal = (java.util.Date) startTimeSpinner.getValue();
      LocalTime time = LocalDateTime.ofInstant(
          timeVal.toInstant(), zone).toLocalTime();
      LocalDateTime ldt = LocalDateTime.of(date, time);
      return ldt.atZone(zone).toInstant();
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Computes the event end instant from the date and time fields.
   *
   * @return end instant, or {@code null} if parsing fails or date is missing
   */
  public Instant getEndInstant() {
    try {
      LocalDate date = endDateField.getDate();
      if (date == null) {
        return null;
      }
      java.util.Date timeVal = (java.util.Date) endTimeSpinner.getValue();
      LocalTime time = LocalDateTime.ofInstant(
          timeVal.toInstant(), zone).toLocalTime();
      LocalDateTime ldt = LocalDateTime.of(date, time);
      return ldt.atZone(zone).toInstant();
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Composite field made of a non-editable text field and a button
   * that opens a popup calendar for selecting a date.
   */
  private final class DatePickerField extends JPanel {

    private LocalDate date;
    private final JTextField field;
    private final JButton button;
    private final DateTimeFormatter outFormatter = DATE_FMT;
    private final DateTimeFormatter monthFormatter =
        DateTimeFormatter.ofPattern("MMMM yyyy");

    /**
     * Constructs a new date picker field with a non-editable text
     * display and a calendar button.
     */
    DatePickerField() {
      super(new BorderLayout(4, 0));
      setBackground(theme.panelBackground());
      field = new JTextField(10);
      field.setEditable(false);
      field.setBackground(theme.fieldBackground());
      field.setForeground(theme.primaryText());
      field.setCaretColor(theme.primaryText());
      field.setFont(theme.bodyFont());
      button = new JButton("📅");
      button.setBackground(theme.accent());
      button.setForeground(Color.WHITE);
      button.setFocusPainted(false);
      button.setBorder(BorderFactory.createLineBorder(theme.border()));
      button.setFont(theme.bodyFont());
      this.add(field, BorderLayout.CENTER);
      this.add(button, BorderLayout.EAST);

      button.addActionListener(e -> {
        LocalDate base = date != null ? date : LocalDate.now();
        LocalDate chosen = showDatePickerDialog(base);
        if (chosen != null) {
          setDate(chosen);
        }
      });
    }

    /**
     * Sets the current date and updates the read-only text field.
     *
     * @param d date to set; may be {@code null} to clear the field
     */
    void setDate(LocalDate d) {
      this.date = d;
      if (d != null) {
        field.setText(outFormatter.format(d));
      } else {
        field.setText("");
      }
    }

    /**
     * Returns the currently selected date.
     *
     * @return active date, or {@code null} if none selected
     */
    LocalDate getDate() {
      return date;
    }

    /**
     * Shows a modal date picker dialog and returns the selected date.
     *
     * @param initial initial date used to seed the calendar display
     * @return chosen date, or {@code null} if the dialog was cancelled
     */
    private LocalDate showDatePickerDialog(LocalDate initial) {
      java.awt.Window owner = SwingUtilities.getWindowAncestor(this);
      JDialog dialog = owner instanceof java.awt.Frame
          ? new JDialog((java.awt.Frame) owner, "Select date", true)
          : new JDialog(owner, "Select date",
          java.awt.Dialog.ModalityType.APPLICATION_MODAL);
      dialog.setResizable(false);

      JPanel root = new JPanel(new BorderLayout(4, 4));
      root.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
      root.setBackground(theme.background());

      JPanel header = new JPanel();
      header.setBackground(theme.headerBackground());
      JButton prev = new JButton("<");
      prev.setBackground(theme.fieldBackground());
      prev.setForeground(theme.primaryText());
      prev.setFocusPainted(false);
      prev.setBorder(BorderFactory.createLineBorder(theme.border()));
      prev.setFont(theme.bodyFont());
      JButton next = new JButton(">");
      next.setBackground(theme.fieldBackground());
      next.setForeground(theme.primaryText());
      next.setFocusPainted(false);
      next.setBorder(BorderFactory.createLineBorder(theme.border()));
      next.setFont(theme.bodyFont());

      JLabel monthLabel = new JLabel();
      monthLabel.setForeground(theme.primaryText());
      monthLabel.setFont(theme.subheadingFont());
      header.add(prev);
      header.add(monthLabel);
      header.add(next);
      root.add(header, BorderLayout.NORTH);

      JPanel grid = new JPanel(new GridLayout(7, 7, 2, 2));
      grid.setBackground(theme.panelBackground());
      root.add(grid, BorderLayout.CENTER);

      final LocalDate[] shownMonth = new LocalDate[] { initial.withDayOfMonth(1) };
      final LocalDate[] selected = new LocalDate[1];

      Runnable rebuild = new Runnable() {
        @Override
        public void run() {
          grid.removeAll();
          String[] names = new String[] {
              "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
          };
          for (String n : names) {
            JLabel lbl = new JLabel(n, JLabel.CENTER);
            lbl.setBorder(BorderFactory.createMatteBorder(
                0, 0, 1, 0, theme.border()));
            lbl.setOpaque(true);
            lbl.setBackground(theme.headerBackground());
            lbl.setForeground(theme.mutedText());
            lbl.setFont(theme.captionFont());
            grid.add(lbl);
          }
          LocalDate firstOfMonth = shownMonth[0].withDayOfMonth(1);
          monthLabel.setText(monthFormatter.format(firstOfMonth));
          int offset = firstOfMonth.getDayOfWeek().getValue() % 7;
          int length = firstOfMonth.lengthOfMonth();
          int cell = 0;
          for (int i = 0; i < 42; i++) {
            JButton b = new JButton("");
            b.setMargin(new java.awt.Insets(1, 1, 1, 1));
            b.setBackground(theme.fieldBackground());
            b.setForeground(theme.primaryText());
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createLineBorder(theme.border()));
            b.setFont(theme.bodyFont());
            if (i >= offset && cell < length) {
              cell++;
              LocalDate d = firstOfMonth.withDayOfMonth(cell);
              b.setText(Integer.toString(cell));
              b.addActionListener(ev -> {
                selected[0] = d;
                dialog.dispose();
              });
            } else {
              b.setEnabled(false);
              b.setBackground(theme.panelBackground());
              b.setForeground(theme.mutedText());
            }
            grid.add(b);
          }
          grid.revalidate();
          grid.repaint();
        }
      };

      prev.addActionListener(e -> {
        shownMonth[0] = shownMonth[0].minusMonths(1);
        rebuild.run();
      });
      next.addActionListener(e -> {
        shownMonth[0] = shownMonth[0].plusMonths(1);
        rebuild.run();
      });

      rebuild.run();

      dialog.getContentPane().add(root);
      dialog.pack();
      dialog.setLocationRelativeTo(this);
      dialog.setVisible(true);
      return selected[0];
    }
  }
}
