package calendar.view;

import calendar.model.Event;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Dialog for editing an event.
 */
public class EditEventDialog extends JDialog {

  private final JComboBox<String> scopeComboBox;
  private final JComboBox<String> propertyComboBox;
  private final JTextField newValueField;
  private boolean confirmed;

  /**
   * Constructs the dialog.
   *
   * @param parent the parent frame
   * @param eventName the event name being edited
   * @param events list of events with this name
   */
  public EditEventDialog(JFrame parent, String eventName, List<Event> events) {
    super(parent, "Edit Event", true);

    setSize(400, 250);
    setLocationRelativeTo(parent);
    setLayout(new BorderLayout(10, 10));

    JPanel fieldsPanel = new JPanel(new GridLayout(4, 2, 10, 10));
    fieldsPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

    fieldsPanel.add(new JLabel("Event:"));
    fieldsPanel.add(new JLabel(eventName));

    fieldsPanel.add(new JLabel("Edit Scope:"));
    String[] scopes = {"single", "from", "series"};
    scopeComboBox = new JComboBox<>(scopes);
    fieldsPanel.add(scopeComboBox);

    fieldsPanel.add(new JLabel("Property:"));
    String[] properties = {"subject", "location", "description"};
    propertyComboBox = new JComboBox<>(properties);
    fieldsPanel.add(propertyComboBox);

    fieldsPanel.add(new JLabel("New Value:"));
    newValueField = new JTextField(20);
    fieldsPanel.add(newValueField);

    add(fieldsPanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

    JButton updateButton = new JButton("Update");
    updateButton.addActionListener(e -> {
      if (validateInput()) {
        confirmed = true;
        dispose();
      }
    });

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());

    buttonPanel.add(updateButton);
    buttonPanel.add(cancelButton);

    add(buttonPanel, BorderLayout.SOUTH);

    this.confirmed = false;
  }

  /**
   * Validates user input.
   *
   * @return true if valid
   */
  private boolean validateInput() {
    if (newValueField.getText().trim().isEmpty()) {
      javax.swing.JOptionPane.showMessageDialog(this,
          "New value cannot be empty", "Validation Error",
          javax.swing.JOptionPane.ERROR_MESSAGE);
      return false;
    }
    return true;
  }

  /**
   * Checks if the dialog was confirmed.
   *
   * @return true if confirmed
   */
  public boolean isConfirmed() {
    return confirmed;
  }

  /**
   * Gets the edit scope.
   *
   * @return the scope
   */
  public String getEditScope() {
    return (String) scopeComboBox.getSelectedItem();
  }

  /**
   * Gets the property to edit.
   *
   * @return the property
   */
  public String getProperty() {
    return (String) propertyComboBox.getSelectedItem();
  }

  /**
   * Gets the new value.
   *
   * @return the new value
   */
  public String getNewValue() {
    return newValueField.getText().trim();
  }
}