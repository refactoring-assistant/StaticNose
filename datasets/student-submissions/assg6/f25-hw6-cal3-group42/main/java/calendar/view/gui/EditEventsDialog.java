package calendar.view.gui;

import calendar.controller.gui.Features;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.ZonedDateTime;
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
 * Dialog for editing all events with a given name starting from a specific date.
 */
class EditEventsDialog extends JDialog {

  private final Features features;
  private boolean modified = false;

  private JTextField eventNameField;
  private JSpinner startDateSpinner;
  private JSpinner startTimeSpinner;
  private JTextField newNameField;
  private JComboBox<String> propertyCombo;

  public EditEventsDialog(JFrame parent, Features features) {
    super(parent, "Edit Events From Date", true);
    this.features = features;

    setSize(450, 300);
    setLocationRelativeTo(parent);
    initializeUi();
  }

  private void initializeUi() {
    setLayout(new BorderLayout(10, 10));

    JPanel formPanel = new JPanel();
    formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
    formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

    JLabel titleLabel = new JLabel("Edit all events with same name from a date");
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
    titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(titleLabel);
    formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

    JLabel nameLabel = new JLabel("Event Name:*");
    nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(nameLabel);

    eventNameField = new JTextField(20);
    eventNameField.setMaximumSize(new Dimension(300, 25));
    eventNameField.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(eventNameField);
    formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

    JLabel dateLabel = new JLabel("Start From Date:*");
    dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(dateLabel);

    JPanel dateTimePanel = new JPanel();
    dateTimePanel.setLayout(new BoxLayout(dateTimePanel, BoxLayout.X_AXIS));
    dateTimePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    javax.swing.SpinnerDateModel dateModel = new javax.swing.SpinnerDateModel(
        new java.util.Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
    startDateSpinner = new JSpinner(dateModel);
    javax.swing.JSpinner.DateEditor dateEditor =
        new javax.swing.JSpinner.DateEditor(startDateSpinner, "MM/dd/yyyy");
    startDateSpinner.setEditor(dateEditor);
    startDateSpinner.setMaximumSize(new Dimension(120, 25));

    javax.swing.SpinnerDateModel timeModel = new javax.swing.SpinnerDateModel(
        new java.util.Date(), null, null, java.util.Calendar.MINUTE);
    startTimeSpinner = new JSpinner(timeModel);
    javax.swing.JSpinner.DateEditor timeEditor =
        new javax.swing.JSpinner.DateEditor(startTimeSpinner, "HH:mm");
    startTimeSpinner.setEditor(timeEditor);
    startTimeSpinner.setMaximumSize(new Dimension(80, 25));

    dateTimePanel.add(startDateSpinner);
    dateTimePanel.add(Box.createRigidArea(new Dimension(5, 0)));
    //dateTimePanel.add(startTimeSpinner);
    dateTimePanel.setMaximumSize(new Dimension(400, 25));

    formPanel.add(dateTimePanel);
    formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

    JLabel newNameLabel = new JLabel("New Name:*");
    newNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(newNameLabel);

    newNameField = new JTextField(20);
    newNameField.setMaximumSize(new Dimension(300, 25));
    newNameField.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(newNameField);
    formPanel.add(Box.createRigidArea(new Dimension(0, 10)));

    JLabel noteLabel = new JLabel(
        "<html><i>This will rename all events with the given name<br>"
            + "starting from the specified date onwards</i></html>");
    noteLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(noteLabel);

    add(formPanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton saveButton = new JButton("Save");
    saveButton.addActionListener(e -> saveEdits());
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());

    buttonPanel.add(saveButton);
    buttonPanel.add(cancelButton);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  private void saveEdits() {
    String eventName = eventNameField.getText().trim();
    String newName = newNameField.getText().trim();

    if (eventName.isEmpty()) {
      JOptionPane.showMessageDialog(this,
          "Event name is required", "Validation Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    if (newName.isEmpty()) {
      JOptionPane.showMessageDialog(this,
          "New name is required", "Validation Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    try {
      java.util.Date dateValue = (java.util.Date) startDateSpinner.getValue();

      java.time.LocalDate date = dateValue.toInstant()
          .atZone(java.time.ZoneId.systemDefault()).toLocalDate();

      ZonedDateTime startDateTime = date.atStartOfDay(
          java.time.ZoneId.of(features.getCurrentCalendarTimezone()));

      Map<String, Object> parameters = new HashMap<>();
      parameters.put("subject", eventName);
      parameters.put("start", startDateTime);
      parameters.put("property", "subject");
      parameters.put("value", newName);

      features.editEventsWithName(parameters);

      modified = true;
      JOptionPane.showMessageDialog(this,
          "Events updated successfully!", "Success",
          JOptionPane.INFORMATION_MESSAGE);
      dispose();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this,
          "Error updating events: " + e.getMessage(),
          "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  public boolean wasModified() {
    return modified;
  }
}