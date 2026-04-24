package calendar.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * This class opens the edit calendar dialog.
 */
public class EditCalDialog extends JDialog {

  private final JComboBox<String> propertyDropdown;
  private final JTextField newValueField;
  private final JButton saveButton;
  private final JButton cancelButton;
  private boolean confirmed = false;

  /**
   * Constructor for Edit Calendar Dialog.
   *
   * @param owner               The main window.
   * @param currentCalendarName The name of the calendar being edited (for context).
   */
  public EditCalDialog(Frame owner, String currentCalendarName) {
    super(owner, "Edit Calendar Settings", true); // Modal
    this.setSize(400, 250);
    this.setLayout(new BorderLayout(10, 10));

    // --- 1. NORTH: Context Label ---
    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    JLabel contextLabel = new JLabel("Editing Calendar: " + currentCalendarName);
    contextLabel.setFont(new Font("Arial", Font.BOLD, 12));
    contextLabel.setForeground(new Color(0, 102, 204));
    topPanel.add(contextLabel);
    this.add(topPanel, BorderLayout.NORTH);

    // --- 2. CENTER: Input Form ---
    JPanel formPanel = new JPanel(new GridLayout(2, 1, 10, 10));
    formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

    // Row 1: Property Selector
    JPanel propPanel = new JPanel(new BorderLayout(5, 5));
    propPanel.add(new JLabel("Property to Edit:"), BorderLayout.NORTH);
    propertyDropdown = new JComboBox<>(new String[] {"Name", "Timezone"});
    propPanel.add(propertyDropdown, BorderLayout.CENTER);
    formPanel.add(propPanel);

    // Row 2: New Value Input
    JPanel valuePanel = new JPanel(new BorderLayout(5, 5));
    valuePanel.add(new JLabel("New Value:"), BorderLayout.NORTH);
    newValueField = new JTextField();
    valuePanel.add(newValueField, BorderLayout.CENTER);
    formPanel.add(valuePanel);

    this.add(formPanel, BorderLayout.CENTER);

    // --- 3. SOUTH: Buttons ---
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    saveButton = new JButton("Save");
    cancelButton = new JButton("Cancel");

    buttonPanel.add(saveButton);
    buttonPanel.add(cancelButton);
    this.add(buttonPanel, BorderLayout.SOUTH);

    // --- Logic ---
    saveButton.addActionListener(e -> {
      if (newValueField.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Value cannot be empty.", "Error",
            JOptionPane.WARNING_MESSAGE);
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

  /**
   * Returns true if the user selected "Name" from the dropdown.
   */
  public boolean isNameEdit() {
    return "Name".equals(propertyDropdown.getSelectedItem());
  }

  /**
   * Returns the value typed by the user.
   */
  public String getNewValue() {
    return newValueField.getText().trim();
  }
}