package calendar.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A dialog window for performing bulk updates on events sharing the same name.
 * This allows the user to specify a target event name, an optional date range filter (from/to),
 * and the specific property change to apply to all matching events.
 */
public class BulkEditDialog extends JDialog {

  private final JTextField eventNameField;
  private final JTextField startDateField;
  private final JTextField endDateField;
  private final JComboBox<String> propertyDropdown;
  private final JTextField newValueField;

  private final JButton updateButton;
  private final JButton cancelButton;

  private boolean confirmed = false;

  /**
   * Constructs the BulkEditDialog.
   * Initializes the layout with input fields for event name searching, date range filtering,
   * and property modification.
   *
   * @param owner the parent frame
   */
  public BulkEditDialog(Frame owner) {
    super(owner, "Bulk Edit by Name", true);
    this.setLayout(new BorderLayout(10, 10));
    this.setSize(450, 500);

    JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
    headerPanel.add(new JLabel("Update all events with a specific name:"));
    this.add(headerPanel, BorderLayout.NORTH);

    JPanel formPanel = new JPanel(new GridLayout(5, 1, 10, 10));
    formPanel.setBorder(BorderFactory.createTitledBorder("Search & Replace Criteria"));

    JPanel namePanel = new JPanel(new BorderLayout(5, 5));
    namePanel.add(new JLabel("Target Event Name (Required):"), BorderLayout.NORTH);
    eventNameField = new JTextField();
    namePanel.add(eventNameField, BorderLayout.CENTER);
    formPanel.add(namePanel);

    JPanel fromDatePanel = new JPanel(new BorderLayout(5, 5));
    fromDatePanel.add(new JLabel("From Date (YYYY-MM-DD) [Optional]:"),
        BorderLayout.NORTH);
    startDateField = new JTextField();
    fromDatePanel.add(startDateField, BorderLayout.CENTER);
    formPanel.add(fromDatePanel);

    JPanel toDatePanel = new JPanel(new BorderLayout(5, 5));
    toDatePanel.add(new JLabel("To Date (YYYY-MM-DD) [Optional]:"),
        BorderLayout.NORTH);
    endDateField = new JTextField();
    toDatePanel.add(endDateField, BorderLayout.CENTER);
    formPanel.add(toDatePanel);

    JPanel propPanel = new JPanel(new BorderLayout(5, 5));
    propPanel.add(new JLabel("Property to change:"), BorderLayout.NORTH);

    String[] props = {
        "Subject", "Start Date", "Start Time",
        "End Date", "End Time",
        "Location", "Description", "Status"
    };
    propertyDropdown = new JComboBox<>(props);
    propPanel.add(propertyDropdown, BorderLayout.CENTER);
    formPanel.add(propPanel);

    JPanel valPanel = new JPanel(new BorderLayout(5, 5));
    valPanel.add(new JLabel("New Value:"), BorderLayout.NORTH);
    newValueField = new JTextField();
    valPanel.add(newValueField, BorderLayout.CENTER);
    formPanel.add(valPanel);

    this.add(formPanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    updateButton = new JButton("Update All");
    cancelButton = new JButton("Cancel");
    buttonPanel.add(updateButton);
    buttonPanel.add(cancelButton);
    this.add(buttonPanel, BorderLayout.SOUTH);

    updateButton.addActionListener(e -> handleUpdate());
    cancelButton.addActionListener(e -> {
      confirmed = false;
      this.setVisible(false);
    });

    this.setLocationRelativeTo(owner);
  }

  private void handleUpdate() {

    if (eventNameField.getText().trim().isEmpty()) {
      JOptionPane.showMessageDialog(this, "Event Name is required.",
          "Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    if (newValueField.getText().trim().isEmpty()) {
      JOptionPane.showMessageDialog(this, "New Value is required.",
          "Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    LocalDate start = null;
    LocalDate end = null;

    if (!startDateField.getText().trim().isEmpty()) {
      try {
        start = LocalDate.parse(startDateField.getText().trim());
      } catch (DateTimeParseException ex) {
        JOptionPane.showMessageDialog(this, "Invalid 'From Date' Format."
                + " Use YYYY-MM-DD", "Error",
            JOptionPane.ERROR_MESSAGE);
        return;
      }
    }

    if (!endDateField.getText().trim().isEmpty()) {
      try {
        end = LocalDate.parse(endDateField.getText().trim());
      } catch (DateTimeParseException ex) {
        JOptionPane.showMessageDialog(this, "Invalid 'To Date' Format."
                + " Use YYYY-MM-DD", "Error",
            JOptionPane.ERROR_MESSAGE);
        return;
      }
    }

    if (start != null && end != null && end.isBefore(start)) {
      JOptionPane.showMessageDialog(this, "'To Date' cannot be before "
              + "'From Date'.", "Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    String selectedProp = (String) propertyDropdown.getSelectedItem();
    if ("Start Date".equals(selectedProp) || "End Date".equals(selectedProp)) {
      try {
        LocalDate.parse(newValueField.getText().trim());
      } catch (DateTimeParseException ex) {
        JOptionPane.showMessageDialog(this, "Invalid New Value Date "
                + "Format. Use YYYY-MM-DD",
            "Error",
            JOptionPane.ERROR_MESSAGE);
        return;
      }
    }

    confirmed = true;
    this.setVisible(false);
  }

  /**
   * return true if the dialog was built.
   *
   * @return true or false.
   */
  public boolean isConfirmed() {
    return confirmed;
  }

  /**
   * Return the name of the target event.
   *
   * @return name of target event.
   */
  public String getTargetEventName() {
    return eventNameField.getText().trim();
  }

  /**
   * get the start date of the bulk edit.
   *
   * @return start date.
   */
  public LocalDate getFilterStartDate() {
    String text = startDateField.getText().trim();
    if (text.isEmpty()) {
      return null;
    }
    return LocalDate.parse(text);
  }

  /**
   * Get the end date of the bulk edit.
   *
   * @return end date.
   */
  public LocalDate getFilterEndDate() {
    String text = endDateField.getText().trim();
    if (text.isEmpty()) {
      return null;
    }
    return LocalDate.parse(text);
  }

  /**
   * Get the property input by the user to change.
   *
   * @return the String property.
   */
  public String getSelectedProperty() {
    return (String) propertyDropdown.getSelectedItem();
  }

  /**
   * The new value used to updated the selected property.
   *
   * @return the new value input by the user.
   */
  public String getNewValue() {
    return newValueField.getText().trim();
  }
}