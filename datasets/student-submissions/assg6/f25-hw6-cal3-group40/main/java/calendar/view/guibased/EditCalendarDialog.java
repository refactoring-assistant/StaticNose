package calendar.view.guibased;

import calendar.controller.UiFeatures;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.ZoneId;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * This class represents the Edit Calendar Dialog.
 */
public class EditCalendarDialog extends JDialog {

  private final JTextField nameField;
  private final JComboBox<String> timezoneBox;
  private final UiFeatures features;

  /**
   * Initializes the EditCalendarDialog object.
   *
   * @param owner The owner for this dialog.
   * @param features The features in this dialog.
   * @param currentName The current name of the calendar.
   * @param currentTimezone The current time zone of the calendar.
     */
  public EditCalendarDialog(CalendarSwingViewImpl owner, UiFeatures features,
                            String currentName, ZoneId currentTimezone) {
    super(owner, "Edit Calendar", true);
    this.features = features;

    setLayout(new BorderLayout());
    setSize(350, 200);
    setLocationRelativeTo(owner);

    JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
    formPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));

    nameField = new JTextField(currentName);

    Set<String> zoneIds = new TreeSet<>(ZoneId.getAvailableZoneIds());
    timezoneBox = new JComboBox<>(zoneIds.toArray(new String[0]));
    if (currentTimezone != null) {
      timezoneBox.setSelectedItem(currentTimezone.getId());
    } else {
      timezoneBox.setSelectedItem(ZoneId.systemDefault().getId());
    }

    formPanel.add(new JLabel("Calendar Name:"));
    formPanel.add(nameField);
    formPanel.add(new JLabel("Timezone:"));
    formPanel.add(timezoneBox);

    add(formPanel, BorderLayout.CENTER);

    JButton saveButton = new JButton("Save");
    saveButton.addActionListener(e -> onSave());
    add(saveButton, BorderLayout.SOUTH);
  }

  private void onSave() {
    String newName = nameField.getText().trim();
    String newTimezone = (String) timezoneBox.getSelectedItem();

    if (newName.isEmpty()) {
      javax.swing.JOptionPane.showMessageDialog(this, "Name cannot be empty.", "Error",
          javax.swing.JOptionPane.ERROR_MESSAGE);
      return;
    }

    features.editCalendar(newName, newTimezone);
    dispose();
  }
}
