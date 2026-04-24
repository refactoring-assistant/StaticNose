package calendar.view.gui;

import calendar.controller.gui.Features;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

/**
 * Dialog for creating new events (single or recurring).
 */
public class CreateEventDialog extends JDialog {

  private final JPanel mainPanel;
  private final Features features;
  private boolean created = false;

  private JTextField subjectField;
  private JSpinner dateSpinner;
  private JSpinner startTimeSpinner;
  private JSpinner endTimeSpinner;
  private JCheckBox allDayCheckbox;
  private JCheckBox recurringCheckbox;
  private JPanel recurringPanel;

  private JCheckBox[] weekdayCheckboxes;
  private JRadioButton fornTimesRadio;
  private JSpinner occurrencesSpinner;
  private JSpinner endDateSpinner;

  private JTextField descriptionField;
  private JComboBox<String> locationCombo;
  private JComboBox<String> statusCombo;

  /**
   * Constructor to initialize the create event dialogue box.
   *
   * @param parent          the caller JFrame who called this dialogue box
   * @param features        the interface defining all callbacks
   * @param preselectedDate the date on which this dialogue was run
   */
  public CreateEventDialog(JFrame parent, Features features, LocalDate preselectedDate) {
    super(parent, "Create Event", true);
    this.features = features;
    setLayout(new BorderLayout(10, 10));
    this.mainPanel = new JPanel();

    setSize(500, 650);
    setLocationRelativeTo(parent);
    initializeUi(preselectedDate);
  }

  /**
   * Construct the UI for this dialogue box.
   *
   * @param preselectedDate the date on which this dialogue was run
   */
  private void initializeUi(LocalDate preselectedDate) {
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
    mainPanel.add(createFieldPanel("Subject:*",
        subjectField = new JTextField(20)));

    datePanel(preselectedDate);
    allDayPanel();
    startTimePanel();
    endTimePanel();
    recurringPanel();
    optionalPanel();

    JScrollPane scrollPane = new JScrollPane(mainPanel);
    scrollPane.setBorder(null);
    add(scrollPane, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton createButton = new JButton("Create");
    createButton.addActionListener(e -> createEvent());
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());

    buttonPanel.add(createButton);
    buttonPanel.add(cancelButton);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  /**
   * Date selection panel to create event on.
   *
   * @param preselectedDate an already selected date by clicking on a day to create event on
   */
  private void datePanel(LocalDate preselectedDate) {
    LocalDate initialDate = preselectedDate != null ? preselectedDate : LocalDate.now();
    SpinnerDateModel dateModel = new SpinnerDateModel(
        Date.from(initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
        null, null, Calendar.DAY_OF_MONTH);
    dateSpinner = new JSpinner(dateModel);
    JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy");
    dateSpinner.setEditor(dateEditor);
    mainPanel.add(createFieldPanel("Date:*", dateSpinner));
  }

  /**
   * Select if the event is an all day event.
   */
  private void allDayPanel() {
    allDayCheckbox = new JCheckBox("All-day event (08:00 - 17:00)");
    allDayCheckbox.addActionListener(e -> toggleTimeFields());
    JPanel allDayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    allDayPanel.add(allDayCheckbox);
    allDayPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    mainPanel.add(allDayPanel);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
  }

  /**
   * Select the start time of the event.
   */
  private void startTimePanel() {
    SpinnerDateModel startTimeModel = new SpinnerDateModel(
        new Date(), null, null, Calendar.MINUTE);
    startTimeSpinner = new JSpinner(startTimeModel);
    JSpinner.DateEditor startTimeEditor = new JSpinner.DateEditor(
        startTimeSpinner, "HH:mm");
    startTimeSpinner.setEditor(startTimeEditor);
    mainPanel.add(createFieldPanel("Start Time:*", startTimeSpinner));
  }

  /**
   * Select the end time of the event, defaults to one hour ahead of start time.
   */
  private void endTimePanel() {
    startTimeSpinner.addChangeListener(e -> {
      Date startTime = (Date) startTimeSpinner.getValue();
      Calendar cal = Calendar.getInstance();
      cal.setTime(startTime);
      cal.add(Calendar.HOUR_OF_DAY, 1);
      endTimeSpinner.setValue(cal.getTime());
    });

    Calendar endTimeCal = Calendar.getInstance();
    endTimeCal.add(Calendar.HOUR_OF_DAY, 1);

    SpinnerDateModel endTimeModel = new SpinnerDateModel(
        endTimeCal.getTime(), null, null, Calendar.MINUTE);
    endTimeSpinner = new JSpinner(endTimeModel);
    JSpinner.DateEditor endTimeEditor = new JSpinner.DateEditor(
        endTimeSpinner, "HH:mm");
    endTimeSpinner.setEditor(endTimeEditor);
    mainPanel.add(createFieldPanel("End Time:*", endTimeSpinner));
  }

  /**
   * Select if event is recurring, and create a recurring dialogue if it is.
   */
  private void recurringPanel() {
    recurringCheckbox = new JCheckBox("Recurring event");
    recurringCheckbox.addActionListener(e -> toggleRecurringPanel());
    JPanel recurringCheckPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    recurringCheckPanel.add(recurringCheckbox);
    recurringCheckPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    mainPanel.add(recurringCheckPanel);

    recurringPanel = createRecurringPanel();
    recurringPanel.setVisible(false);
    mainPanel.add(recurringPanel);
  }

  /**
   * Select and fill the optional parameters if required.
   */
  private void optionalPanel() {
    mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    JLabel optionalLabel = new JLabel("Optional Fields:");
    optionalLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
    optionalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    mainPanel.add(optionalLabel);

    mainPanel.add(createFieldPanel("Description:",
        descriptionField = new JTextField(20)));

    locationCombo = new JComboBox<>(new String[] {"", "physical", "online"});
    mainPanel.add(createFieldPanel("Location:", locationCombo));

    statusCombo = new JComboBox<>(new String[] {"", "public", "private"});
    mainPanel.add(createFieldPanel("Status:", statusCombo));
  }


  /**
   * Helper to create the UI panel element with given data.
   *
   * @param labelText the label display of this panel
   * @param field     the data field which this panel is used to fill out
   * @return the panel itself after creation
   */
  private JPanel createFieldPanel(String labelText, JComponent field) {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JLabel label = new JLabel(labelText);
    label.setPreferredSize(new Dimension(100, 25));
    panel.add(label);

    if (field instanceof JTextField) {
      field.setPreferredSize(new Dimension(300, 25));
    } else if (field instanceof JSpinner) {
      field.setPreferredSize(new Dimension(150, 25));
    }

    panel.add(field);
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    return panel;
  }

  /**
   * Panel to include the recurring event values for creating a series event.
   *
   * @return the panel itself after creation
   */
  private JPanel createRecurringPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(BorderFactory.createTitledBorder("Recurring Options"));
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel weekdaysLabel = new JLabel("Repeat on:");
    weekdaysLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(weekdaysLabel);

    weekdaysPanel(panel);
    terminatingPanel(panel);

    return panel;
  }

  /**
   * Weekdays selection for recurring events.
   *
   * @param panel parent recurring events panel
   */
  private void weekdaysPanel(JPanel panel) {
    JPanel weekdaysPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    weekdayCheckboxes = new JCheckBox[7];
    for (int i = 0; i < 7; i++) {
      weekdayCheckboxes[i] = new JCheckBox(dayNames[i]);
      weekdaysPanel.add(weekdayCheckboxes[i]);
    }
    weekdaysPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(weekdaysPanel);
  }

  /**
   * End date decision panel, for number of times or for an until date.
   *
   * @param panel parent recurring events panel
   */
  private void terminatingPanel(JPanel panel) {
    ButtonGroup terminationGroup = new ButtonGroup();

    fornTimesRadio = new JRadioButton("For");
    fornTimesRadio.setSelected(true);
    terminationGroup.add(fornTimesRadio);

    JPanel fornPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    fornPanel.add(fornTimesRadio);

    occurrencesSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 365, 1));
    occurrencesSpinner.setPreferredSize(new Dimension(60, 25));
    fornPanel.add(occurrencesSpinner);
    fornPanel.add(new JLabel(" occurrences"));
    fornPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(fornPanel);

    JPanel untilPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JRadioButton untilDateRadio = new JRadioButton("Until");
    terminationGroup.add(untilDateRadio);
    untilPanel.add(untilDateRadio);

    SpinnerDateModel endDateModel = new SpinnerDateModel(
        Date.from(LocalDate.now().plusMonths(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant()),
        null, null, Calendar.DAY_OF_MONTH);
    endDateSpinner = new JSpinner(endDateModel);
    JSpinner.DateEditor endDateEditor = new JSpinner.DateEditor(
        endDateSpinner, "MM/dd/yyyy");
    endDateSpinner.setEditor(endDateEditor);
    endDateSpinner.setPreferredSize(new Dimension(100, 25));
    untilPanel.add(endDateSpinner);
    untilPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.add(untilPanel);
  }

  /**
   * Toggle between all day event and timed event.
   */
  private void toggleTimeFields() {
    boolean allDay = allDayCheckbox.isSelected();
    startTimeSpinner.setEnabled(!allDay);
    endTimeSpinner.setEnabled(!allDay);
  }

  /**
   * Toggle between show and hide the recurring panel.
   */
  private void toggleRecurringPanel() {
    recurringPanel.setVisible(recurringCheckbox.isSelected());
    pack();
  }

  /**
   * Set the parameters to create the event and call the feature.
   */
  private void createEvent() {
    try {
      String subject = subjectField.getText().trim();
      if (subject.isEmpty()) {
        JOptionPane.showMessageDialog(this,
            "Subject is required", "Validation Error",
            JOptionPane.ERROR_MESSAGE);
        return;
      }

      Date dateValue = (Date) dateSpinner.getValue();
      LocalDate eventDate = dateValue.toInstant()
          .atZone(ZoneId.systemDefault()).toLocalDate();

      Map<String, Object> parameters = new HashMap<>();
      parameters.put("subject", subject);

      addOptionalParameters(parameters);

      if (allDayCheckbox.isSelected()) {
        parameters.put("ondate", eventDate);

        if (recurringCheckbox.isSelected()) {
          addRecurringParameters(parameters);
          features.createAllDayEventSeries(parameters);
        } else {
          features.createAllDayEvent(parameters);
        }
      } else {
        addTimedParameters(eventDate, parameters);

        if (recurringCheckbox.isSelected()) {
          addRecurringParameters(parameters);
          features.createEventSeries(parameters);
        } else {
          features.createSingleEvent(parameters);
        }
      }

      created = true;
      JOptionPane.showMessageDialog(this,
          "Event created successfully!", "Success",
          JOptionPane.INFORMATION_MESSAGE);
      dispose();

    } catch (Exception e) {
      JOptionPane.showMessageDialog(this,
          "Error creating event: " + e.getMessage(),
          "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Add parameters for the optional values.
   *
   * @param parameters map of all parameters to create the event
   */
  private void addOptionalParameters(Map<String, Object> parameters) {
    if (!descriptionField.getText().trim().isEmpty()) {
      parameters.put("description", descriptionField.getText().trim());
    }
    if (locationCombo.getSelectedIndex() > 0) {
      parameters.put("location", locationCombo.getSelectedItem());
    }
    if (statusCombo.getSelectedIndex() > 0) {
      parameters.put("status", statusCombo.getSelectedItem());
    }
  }

  /**
   * Add time and date parameters to the map.
   *
   * @param eventDate  the date on which the event is created first
   * @param parameters map of all parameters to create the event
   */
  private void addTimedParameters(LocalDate eventDate, Map<String, Object> parameters) {
    Date startTime = (Date) startTimeSpinner.getValue();
    Date endTime = (Date) endTimeSpinner.getValue();

    LocalTime start = startTime.toInstant()
        .atZone(ZoneId.systemDefault()).toLocalTime();
    LocalTime end = endTime.toInstant()
        .atZone(ZoneId.systemDefault()).toLocalTime();

    ZonedDateTime startDateTime = ZonedDateTime.of(eventDate, start,
        ZoneId.of(features.getCurrentCalendarTimezone()));
    ZonedDateTime endDateTime = ZonedDateTime.of(eventDate, end,
        ZoneId.of(features.getCurrentCalendarTimezone()));

    parameters.put("start", startDateTime);
    parameters.put("end", endDateTime);
  }


  /**
   * Add recurring event parameters to the map.
   *
   * @param parameters map of all parameters to create the event
   */
  private void addRecurringParameters(Map<String, Object> parameters) {
    String weekdays = "";
    String[] dayNames = {"U", "M", "T", "W", "R", "F", "S"};
    for (int i = 0; i < 7; i++) {
      if (weekdayCheckboxes[i].isSelected()) {
        weekdays += dayNames[i];
      }
    }

    if (weekdays.isEmpty()) {
      throw new IllegalArgumentException(
          "Please select at least one weekday for recurring event");
    }

    parameters.put("weekdays", weekdays);

    if (fornTimesRadio.isSelected()) {
      parameters.put("ndays", occurrencesSpinner.getValue());
    } else {
      Date endDate = (Date) endDateSpinner.getValue();
      LocalDate untilDate = endDate.toInstant()
          .atZone(ZoneId.systemDefault()).toLocalDate();
      parameters.put("untildate", untilDate);
    }
  }

  /**
   * Return if event is created. Stored as private class variable to have access
   * across the class once an event is successfully created.
   *
   * @return boolean true if event is created, otherwise false
   */
  public boolean wasCreated() {
    return created;
  }
}