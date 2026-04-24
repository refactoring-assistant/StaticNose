package calendar.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Dialog for creating a new calendar.
 */
public class CreateCalendarDialog extends JDialog {

  private final JTextField nameField;
  private final JComboBox<String> timezoneCombo;
  private boolean confirmed;

  /**
   * Constructs the dialog.
   *
   * @param parent the parent frame
   */
  public CreateCalendarDialog(JFrame parent) {
    super(parent, "Create New Calendar", true);

    setSize(400, 200);
    setLocationRelativeTo(parent);
    setLayout(new BorderLayout(10, 10));

    JPanel fieldsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
    fieldsPanel.setBorder(new EmptyBorder(20, 20, 10, 20));

    fieldsPanel.add(new JLabel("Calendar Name:"));
    nameField = new JTextField(20);
    fieldsPanel.add(nameField);

    fieldsPanel.add(new JLabel("Timezone:"));
    List<String> timezones = ZoneId.getAvailableZoneIds().stream()
        .sorted()
        .collect(Collectors.toList());
    timezoneCombo = new JComboBox<>(timezones.toArray(new String[0]));
    timezoneCombo.setSelectedItem(ZoneId.systemDefault().getId());
    fieldsPanel.add(timezoneCombo);

    add(fieldsPanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel();
    buttonPanel.setBorder(new EmptyBorder(10, 20, 20, 20));

    JButton createButton = new JButton("Create");
    createButton.addActionListener(e -> {
      confirmed = true;
      dispose();
    });

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());

    buttonPanel.add(createButton);
    buttonPanel.add(cancelButton);

    add(buttonPanel, BorderLayout.SOUTH);

    this.confirmed = false;
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
   * Gets the entered calendar name.
   *
   * @return the calendar name
   */
  public String getCalendarName() {
    return nameField.getText().trim();
  }

  /**
   * Gets the selected timezone.
   *
   * @return the timezone ID
   */
  public String getTimezone() {
    return (String) timezoneCombo.getSelectedItem();
  }
}