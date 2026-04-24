package calendar.view;

import calendar.controller.guicontroller.ViewListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.border.EmptyBorder;

/**
 * Dialog for creating and editing events.
 */
public class CreateEventDialog {

  private final List<ViewListener> listeners;


  private JTextField subjectField;
  private JSpinner startDateSpinner;
  private JSpinner endDateSpinner;
  private JSpinner startTimeSpinner;
  private JSpinner endTimeSpinner;
  private JSpinner untilDateSpinner;
  private JCheckBox allDayCheck;
  private JCheckBox recurringCheck;
  private JTextArea descriptionArea;
  private JComboBox<String> locationCombo;
  private JComboBox<String> statusCombo;
  private JPanel daysPanel;
  private List<JCheckBox> dayCheckBoxes;


  private JComboBox<String> repeatModeCombo;
  private JTextField repeatCountField;
  private JPanel repeatConfigPanel;

  /**
   * Constructor for create event dialog with the list of listeners or controllers.
   *
   * @param listeners list of all the controllers.
   */
  public CreateEventDialog(List<ViewListener> listeners) {
    this.listeners = listeners;
  }

  /**
   * Shows a dialog for creating a new event.
   */
  public void showCreateDialog(JFrame parent, LocalDate selectedDate) {
    JDialog dialog = new JDialog(parent, "Create Event", true);
    dialog.setSize(500, 700);
    dialog.setLayout(new BorderLayout());


    initializeComponents(selectedDate);


    JPanel formPanel = new JPanel();
    formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
    formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
    addComponentsToForm(formPanel);


    setupInteractivity();


    JPanel buttonPanel = createButtonPanel(dialog);


    dialog.add(new JScrollPane(formPanel), BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);
    dialog.setLocationRelativeTo(parent);
    dialog.setVisible(true);
  }


  private void initializeComponents(LocalDate selectedDate) {
    subjectField = new JTextField(30);


    startDateSpinner = createDateSpinner(selectedDate);
    endDateSpinner = createDateSpinner(selectedDate);


    startTimeSpinner = createTimeSpinner(9);
    endTimeSpinner = createTimeSpinner(10);


    allDayCheck = new JCheckBox("All Day Event");
    recurringCheck = new JCheckBox("Repeat");


    descriptionArea = new JTextArea(3, 30);
    descriptionArea.setLineWrap(true);
    descriptionArea.setWrapStyleWord(true);

    locationCombo = new JComboBox<>(new String[] {"UNKNOWN", "PHYSICAL", "ONLINE"});
    statusCombo = new JComboBox<>(new String[] {"UNKNOWN", "PUBLIC", "PRIVATE"});


    initDayCheckBoxes();


    repeatModeCombo = new JComboBox<>(new String[] {"Enter Select upto date",
        "Enter no of repeats"});
    repeatModeCombo.setEnabled(false);

    untilDateSpinner = createDateSpinner(selectedDate.plusMonths(1));
    untilDateSpinner.setVisible(false);

    repeatCountField = new JTextField(5);
    repeatCountField.setVisible(false);

    repeatConfigPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
  }

  private void initDayCheckBoxes() {
    dayCheckBoxes = new ArrayList<>();
    String[] dayLabels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    daysPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    daysPanel.add(new JLabel("Repeat on:"));

    for (String day : dayLabels) {
      JCheckBox box = new JCheckBox(day);
      dayCheckBoxes.add(box);
      daysPanel.add(box);
    }
    daysPanel.setEnabled(false);
    for (Component c : daysPanel.getComponents()) {
      c.setEnabled(false);
    }
  }

  private JSpinner createDateSpinner(LocalDate date) {
    SpinnerDateModel model = new SpinnerDateModel(
        Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant()),
        null, null, Calendar.DAY_OF_MONTH);
    JSpinner spinner = new JSpinner(model);
    spinner.setEditor(new JSpinner.DateEditor(spinner, "yyyy-MM-dd"));
    return spinner;
  }

  private JSpinner createTimeSpinner(int hour) {
    Calendar cal = new GregorianCalendar();
    cal.set(Calendar.HOUR_OF_DAY, hour);
    cal.set(Calendar.MINUTE, 0);

    SpinnerDateModel model = new SpinnerDateModel(
        cal.getTime(), null, null, Calendar.HOUR_OF_DAY);
    JSpinner spinner = new JSpinner(model);
    spinner.setEditor(new JSpinner.DateEditor(spinner, "HH:mm"));
    return spinner;
  }


  private void addComponentsToForm(JPanel panel) {

    addLabelAndComp(panel, "Subject:", subjectField);
    addLabelAndComp(panel, "Start Date:", startDateSpinner);
    addLabelAndComp(panel, "End Date:", endDateSpinner);

    JPanel allDayPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    allDayPanel.add(allDayCheck);
    panel.add(allDayPanel);

    addLabelAndComp(panel, "Start Time:", startTimeSpinner);
    addLabelAndComp(panel, "End Time:", endTimeSpinner);

    panel.add(new JLabel("Description:"));
    panel.add(new JScrollPane(descriptionArea));

    addLabelAndComp(panel, "Location:", locationCombo);
    addLabelAndComp(panel, "Status:", statusCombo);

    panel.add(new JLabel("Repeat:"));
    panel.add(recurringCheck);
    panel.add(daysPanel);

    addRepeatComponents(panel);
  }

  private void addRepeatComponents(JPanel panel) {
    addLabelAndComp(panel, "Repeat Mode:", repeatModeCombo);


    repeatConfigPanel.add(new JLabel("End Condition:"));
    repeatConfigPanel.add(untilDateSpinner);
    repeatConfigPanel.add(repeatCountField);

    panel.add(repeatConfigPanel);
  }

  private void addLabelAndComp(JPanel panel, String labelText, Component comp) {
    panel.add(new JLabel(labelText));
    panel.add(comp);
  }


  private void setupInteractivity() {
    allDayCheck.addActionListener(e -> {
      boolean enabled = !allDayCheck.isSelected();
      startTimeSpinner.setEnabled(enabled);
      endTimeSpinner.setEnabled(enabled);
    });

    recurringCheck.addActionListener(e -> {
      boolean enabled = recurringCheck.isSelected();
      daysPanel.setEnabled(enabled);
      for (Component comp : daysPanel.getComponents()) {
        comp.setEnabled(enabled);
      }
      repeatModeCombo.setEnabled(enabled);
      updateRepeatInputVisibility();
    });

    repeatModeCombo.addActionListener(e -> updateRepeatInputVisibility());
  }

  private void updateRepeatInputVisibility() {
    boolean isRecurring = recurringCheck.isSelected();
    boolean isDateMode = repeatModeCombo.getSelectedIndex() == 0;


    untilDateSpinner.setVisible(isRecurring && isDateMode);
    repeatCountField.setVisible(isRecurring && !isDateMode);

    repeatConfigPanel.revalidate();
    repeatConfigPanel.repaint();
  }

  private JPanel createButtonPanel(JDialog dialog) {
    JPanel buttonPanel = new JPanel(new FlowLayout());

    JButton createButton = new JButton("Create");
    createButton.addActionListener(e -> handleCreateAction(dialog));

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dialog.dispose());

    buttonPanel.add(createButton);
    buttonPanel.add(cancelButton);
    return buttonPanel;
  }

  /**
   * Validates input and emits the event.
   * Reads from instance fields, so no parameters needed except the dialog for alerts.
   */
  private void handleCreateAction(JDialog dialog) {
    try {
      String subject = subjectField.getText().trim();
      if (subject.isEmpty()) {
        showError(dialog, "Subject cannot be empty");
        return;
      }


      LocalDate startDate = convertToLocalDate((Date) startDateSpinner.getValue());
      LocalDate endDate = convertToLocalDate((Date) endDateSpinner.getValue());


      boolean allDay = allDayCheck.isSelected();
      LocalTime startTime;
      LocalTime endTime;

      if (allDay) {
        startTime = LocalTime.of(8, 0);
        endTime = LocalTime.of(17, 0);
      } else {
        startTime = convertToLocalTime((Date) startTimeSpinner.getValue());
        endTime = convertToLocalTime((Date) endTimeSpinner.getValue());
      }

      LocalDateTime startDateTime = LocalDateTime.of(startDate, startTime);
      LocalDateTime endDateTime = LocalDateTime.of(endDate, endTime);

      if (endDateTime.isBefore(startDateTime)) {
        showError(dialog, "End time cannot be before start time");
        return;
      }

      String description = descriptionArea.getText().trim();
      if (description.isEmpty()) {
        description = "No description given";
      }

      String location = (String) locationCombo.getSelectedItem();
      String status = (String) statusCombo.getSelectedItem();


      boolean isRepeating = recurringCheck.isSelected();
      String repeatDays = null;

      if (isRepeating) {
        repeatDays = buildRepeatString();
        if (repeatDays.isEmpty()) {
          showError(dialog, "Please select at least one day for recurring events");
          return;
        }
        if (repeatModeCombo.getSelectedIndex() == 0) {
          LocalDate repeatEndDate = convertToLocalDate((Date) untilDateSpinner.getValue());
          CreateEventParams params = new CreateEventParams.Builder()
              .setSubject(subject)
              .setStartDateTime(startDateTime)
              .setEndDateTime(endDateTime)
              .setDescription(description)
              .setLocation(location)
              .setStatus(status)
              .setRepeating(true)
              .setRepeatDays(repeatDays)
              .setType("until")
              .setRepeatEndDate(repeatEndDate)
              .build();
          emitCreateEvent(params);
        } else {
          String countText = repeatCountField.getText().trim();
          int repeatTimes = Integer.parseInt(countText);
          CreateEventParams params = new CreateEventParams.Builder()
              .setSubject(subject)
              .setStartDateTime(startDateTime)
              .setEndDateTime(endDateTime)
              .setDescription(description)
              .setLocation(location)
              .setStatus(status)
              .setRepeating(true)
              .setRepeatDays(repeatDays)
              .setType("for")
              .setRepeats(repeatTimes)
              .build();
          emitCreateEvent(params);
        }
      } else {
        CreateEventParams params = new CreateEventParams.Builder()
            .setSubject(subject)
            .setStartDateTime(startDateTime)
            .setEndDateTime(endDateTime)
            .setDescription(description)
            .setLocation(location)
            .setStatus(status)
            .setRepeating(false)
            .build();
        emitCreateEvent(params);
      }

      dialog.dispose();

    } catch (NumberFormatException nfe) {
      showError(dialog, "Repeat count must be a valid number.");
    } catch (Exception ex) {
      showError(dialog, "Failed to create event: " + ex.getMessage());
    }
  }


  private String buildRepeatString() {
    StringBuilder daysBuilder = new StringBuilder();
    String[] codes = {"M", "T", "W", "R", "F", "S", "U"};

    for (int i = 0; i < dayCheckBoxes.size(); i++) {
      if (dayCheckBoxes.get(i).isSelected()) {
        daysBuilder.append(codes[i]);
      }
    }
    return daysBuilder.toString();
  }

  private LocalDate convertToLocalDate(Date date) {
    return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
  }

  private LocalTime convertToLocalTime(Date date) {
    Calendar cal = new GregorianCalendar();
    cal.setTime(date);
    return LocalTime.of(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE));
  }

  private void showError(Component parent, String message) {
    JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
  }

  private void emitCreateEvent(CreateEventParams params) {
    for (ViewListener listener : listeners) {
      listener.handleCreateEvent(params);
    }
  }
}