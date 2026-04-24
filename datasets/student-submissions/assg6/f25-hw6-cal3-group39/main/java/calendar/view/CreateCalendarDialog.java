package calendar.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A dialog for creating a new calendar.
 * This modal dialog prompts the user to enter a name and a timezone for the new calendar.
 * It provides "Create" and "Cancel" buttons to confirm or discard the action.
 */
public class CreateCalendarDialog extends JDialog {

  private final JTextField nameField;
  private final JTextField timezoneField;
  private boolean confirmed = false;

  /**
   * Constructs a new CreateCalendarDialog.
   * Initializes the GUI components, including input fields for calendar name and timezone,
   * and sets up the layout and event listeners for the buttons.
   *
   * @param owner the Frame from which the dialog is displayed
   */
  public CreateCalendarDialog(Frame owner) {
    super(owner, "Create New Calendar", true);
    this.setSize(400, 200);
    this.setLayout(new BorderLayout(10, 10));

    JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
    formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    formPanel.add(new JLabel("Calendar Name:"));
    nameField = new JTextField();
    formPanel.add(nameField);

    formPanel.add(new JLabel("Timezone (e.g. UTC):"));
    timezoneField = new JTextField("UTC");
    formPanel.add(timezoneField);

    this.add(formPanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton createButton = new JButton("Create");
    JButton cancelButton = new JButton("Cancel");

    buttonPanel.add(createButton);
    buttonPanel.add(cancelButton);
    this.add(buttonPanel, BorderLayout.SOUTH);

    createButton.addActionListener(e -> {
      if (nameField.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Error",
            JOptionPane.ERROR_MESSAGE);
        return;
      }
      confirmed = true;
      this.setVisible(false);
    });

    cancelButton.addActionListener(e -> {
      confirmed = false;
      this.setVisible(false);
    });
    this.setLocationRelativeTo(owner);
  }

  // --- Accessors ---

  public boolean isConfirmed() {
    return confirmed;
  }

  public String getCalendarName() {
    return nameField.getText().trim();
  }

  public String getTimezone() {
    return timezoneField.getText().trim();
  }
}