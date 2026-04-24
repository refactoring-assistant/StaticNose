package calendar.view.gui;

import calendar.controller.gui.Features;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Dialog for creating a new calendar.
 */
class CreateCalendarDialog extends JDialog {

  private final Features features;
  private boolean created = false;
  private final JPanel formPanel;

  private JTextField nameField;
  private JComboBox<String> timezoneCombo;

  /**
   * Constructor for creating a calendar.
   *
   * @param parent   the main view panel
   * @param features controller with callback features
   */
  public CreateCalendarDialog(JFrame parent, Features features) {
    super(parent, "Create New Calendar", true);
    this.features = features;
    setLayout(new BorderLayout(10, 10));
    this.formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
    formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

    setSize(400, 200);
    setLocationRelativeTo(parent);
    initializeUi();
  }

  /**
   * Create the UI for this dialogue box.
   */
  private void initializeUi() {
    formPanel.add(new JLabel("Calendar Name:*"));
    nameField = new JTextField(20);
    formPanel.add(nameField);

    formPanel.add(new JLabel("Timezone:*"));

    String[] commonTimezones = {
        "America/New_York",
        "America/Chicago",
        "America/Denver",
        "America/Los_Angeles",
        "Europe/London",
        "Europe/Paris",
        "Asia/Tokyo",
        "Asia/Shanghai",
        "Australia/Sydney"
    };
    timezoneCombo = new JComboBox<>(commonTimezones);
    timezoneCombo.setEditable(true);
    timezoneCombo.setSelectedItem(java.util.TimeZone.getDefault().getID());
    formPanel.add(timezoneCombo);

    add(formPanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton createButton = new JButton("Create");
    createButton.addActionListener(e -> createCalendar());
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());

    buttonPanel.add(createButton);
    buttonPanel.add(cancelButton);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  /**
   * Action method that calls the create calendar feature.
   */
  private void createCalendar() {
    String name = nameField.getText().trim();
    if (name.isEmpty()) {
      JOptionPane.showMessageDialog(this,
          "Calendar name is required", "Validation Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    String timezone = (String) timezoneCombo.getSelectedItem();
    if (timezone == null || timezone.trim().isEmpty()) {
      JOptionPane.showMessageDialog(this,
          "Timezone is required", "Validation Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    try {
      features.createCalendar(name, timezone.trim());
      created = true;
      JOptionPane.showMessageDialog(this,
          "Calendar created successfully!", "Success",
          JOptionPane.INFORMATION_MESSAGE);
      dispose();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this,
          "Error creating calendar: " + e.getMessage(),
          "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Returns if the calendar was created or not.
   *
   * @return true if the calendar was created
   */
  public boolean wasCreated() {
    return created;
  }
}
