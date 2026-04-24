package calendar.view.gui;

import calendar.controller.gui.Features;
import calendar.model.event.CalendarEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Dialog for editing an existing event.
 */
class EditEventDialog extends JDialog {

  private final JPanel mainPanel;
  private final CalendarEvent event;
  private final Features features;
  private boolean modified = false;

  private JComboBox<String> propertyCombo;
  private JTextField valueField;
  private JComboBox<String> editTypeCombo;
  private JPanel valuePanel;
  private JSpinner dateSpinner;
  private JSpinner timeSpinner;

  /**
   * Constructor to initialize the create event dialogue box.
   *
   * @param parent   the caller JFrame who called this dialogue box
   * @param event    the event to edit
   * @param features the interface defining all callbacks
   */
  public EditEventDialog(JFrame parent, CalendarEvent event, Features features) {
    super(parent, "Edit Event", true);
    this.event = event;
    this.features = features;
    setLayout(new BorderLayout(10, 10));
    mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

    setSize(450, 350);
    setLocationRelativeTo(parent);
    initializeUi();
  }

  /**
   * Create the UI for edit event dialogue box.
   */
  private void initializeUi() {
    editSubject();
    editStart();
    editSeriesOptions();
    propertyCombo();
    newValue();

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton saveButton = new JButton("Save");
    saveButton.addActionListener(e -> saveEdit());
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());

    buttonPanel.add(saveButton);
    buttonPanel.add(cancelButton);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  /**
   * Panel to show the subject of event to edit.
   */
  private void editSubject() {
    JLabel eventLabel = new JLabel("Editing: " + event.getSubject());
    eventLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
    eventLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    mainPanel.add(eventLabel);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
  }

  /**
   * Panel that shows the property to edit.
   */
  private void propertyCombo() {
    JLabel propertyLabel = new JLabel("Property to edit:");
    propertyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    mainPanel.add(propertyLabel);

    propertyCombo = new JComboBox<>(new String[] {
        "--select property--", "subject", "description", "location", "status", "start", "end"
    });
    propertyCombo.setMaximumSize(new Dimension(200, 25));
    propertyCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

    propertyCombo.addActionListener(e -> {
      String selected = (String) propertyCombo.getSelectedItem();
      updateValuePanel(selected);
    });

    mainPanel.add(propertyCombo);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
  }

  /**
   * Panel to show edit options for events.
   */
  private void editSeriesOptions() {
    if (event.isPartOfSeries()) {
      JLabel editTypeLabel = new JLabel("Edit:");
      editTypeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
      mainPanel.add(editTypeLabel);

      editTypeCombo = new JComboBox<>(new String[] {
          "This event only",
          "All events in series",
          "This and following events"
      });
      editTypeCombo.setMaximumSize(new Dimension(300, 25));
      editTypeCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
      mainPanel.add(editTypeCombo);
      mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    }
  }

  /**
   * Panel for start date time to edit.
   */
  private void editStart() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy h:mm a");
    JLabel timeLabel = new JLabel("Start: "
        + event.getStartDateTime().format(formatter));
    timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    mainPanel.add(timeLabel);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
  }

  /**
   * Panel that has the new value of the to be edited property.
   */
  private void newValue() {
    JLabel valueLabel = new JLabel("New value:");
    valueLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    mainPanel.add(valueLabel);

    valuePanel = new JPanel();
    valuePanel.setLayout(new BoxLayout(valuePanel, BoxLayout.X_AXIS));
    valuePanel.setMaximumSize(new Dimension(400, 30));
    valuePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    mainPanel.add(valuePanel);
    add(mainPanel, BorderLayout.CENTER);
  }

  /**
   * Updates the value panel based on selected property.
   */
  public void updateValuePanel(String property) {
    valuePanel.removeAll();
    if ("start".equals(property) || "end".equals(property)) {
      java.util.Calendar cal = java.util.Calendar.getInstance();
      cal.setTime(java.util.Date.from(event.getStartDateTime().toInstant()));

      javax.swing.SpinnerDateModel dateModel = new javax.swing.SpinnerDateModel(
          cal.getTime(), null, null, java.util.Calendar.DAY_OF_MONTH);
      dateSpinner = new JSpinner(dateModel);
      javax.swing.JSpinner.DateEditor dateEditor =
          new javax.swing.JSpinner.DateEditor(dateSpinner, "MM/dd/yyyy");
      dateSpinner.setEditor(dateEditor);
      dateSpinner.setMaximumSize(new Dimension(120, 25));

      javax.swing.SpinnerDateModel timeModel = new javax.swing.SpinnerDateModel(
          cal.getTime(), null, null, java.util.Calendar.MINUTE);
      timeSpinner = new JSpinner(timeModel);
      javax.swing.JSpinner.DateEditor timeEditor =
          new javax.swing.JSpinner.DateEditor(timeSpinner, "HH:mm");
      timeSpinner.setEditor(timeEditor);
      timeSpinner.setMaximumSize(new Dimension(80, 25));

      valuePanel.add(dateSpinner);
      valuePanel.add(Box.createRigidArea(new Dimension(5, 0)));
      valuePanel.add(timeSpinner);
    } else {
      valueField = new JTextField(20);
      valueField.setMaximumSize(new Dimension(300, 25));
      valuePanel.add(valueField);
    }
    valuePanel.revalidate();
    valuePanel.repaint();
  }

  /**
   * Action method that calls the edit feature.
   */
  private void saveEdit() {
    String property = (String) propertyCombo.getSelectedItem();

    if (property == null || "--select property--".equals(property)) {
      JOptionPane.showMessageDialog(this,
          "Property cannot be empty", "Validation Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    try {
      Map<String, Object> parameters = new HashMap<>();
      parameters.put("subject", event.getSubject());
      parameters.put("start", event.getStartDateTime());
      parameters.put("end", event.getEndDateTime());
      parameters.put("property", property);
      parameters.put("value", getConvertedValue(property));

      if (event.isPartOfSeries() && editTypeCombo != null) {
        int editType = editTypeCombo.getSelectedIndex();
        if (editType == 0) {
          features.editSingleEvent(parameters);
        } else if (editType == 1) {
          features.editSeries(parameters);
        } else {
          features.editEvents(parameters);
        }
      } else {
        features.editSingleEvent(parameters);
      }

      modified = true;
      JOptionPane.showMessageDialog(this,
          "Event edited successfully!", "Success",
          JOptionPane.INFORMATION_MESSAGE);
      dispose();

    } catch (Exception e) {
      JOptionPane.showMessageDialog(this,
          "Error updating event: " + e.getMessage(),
          "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Convert the value to the correct datatype for editing.
   *
   * @param property the property to edit
   * @return the value which is converted to the datatype
   */
  private Object getConvertedValue(String property) {
    if (property.equals("start") || property.equals("end")) {
      if (dateSpinner == null || timeSpinner == null) {
        throw new IllegalArgumentException("Date/time pickers not initialized");
      }

      java.util.Date dateValue = (java.util.Date) dateSpinner.getValue();
      java.util.Date timeValue = (java.util.Date) timeSpinner.getValue();

      java.time.LocalDate date = dateValue.toInstant()
          .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
      java.time.LocalTime time = timeValue.toInstant()
          .atZone(java.time.ZoneId.systemDefault()).toLocalTime();

      return ZonedDateTime.of(date, time,
          event.getStartDateTime().getZone());
    } else {
      String valueText = valueField.getText().trim();
      if (valueText.isEmpty()) {
        throw new IllegalArgumentException("Value cannot be empty");
      }
      return valueText;
    }
  }

  /**
   * Returns if the panel was modified.
   *
   * @return true if panel is modified, false otherwise
   */
  public boolean wasModified() {
    return modified;
  }
}

