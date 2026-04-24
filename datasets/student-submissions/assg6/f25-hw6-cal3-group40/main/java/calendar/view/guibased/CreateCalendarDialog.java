package calendar.view.guibased;

import calendar.controller.UiFeatures;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * This class represents the Create Calendar Dialog.
 */
public class CreateCalendarDialog extends JDialog {

  private final UiFeatures features;
  private final JTextField nameField;
  private final JComboBox<String> timezoneBox;
  private final JLabel errorLabel;

  /**
   * Initializes the CreateCalendarDialog object.
   *
   * @param owner The owner window for this Dialog.
   * @param features The features for this Dialog.
   */
  public CreateCalendarDialog(CalendarSwingViewImpl owner, UiFeatures features) {
    super(owner, "Create New Calendar", true);
    this.features = features;

    setLayout(new BorderLayout());
    setSize(400, 250);
    setLocationRelativeTo(owner);

    JPanel formPanel = new JPanel(new GridLayout(0, 1, 10, 10));
    formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

    nameField = new JTextField();

    Set<String> zoneIds = ZoneId.getAvailableZoneIds();
    List<String> zoneList = new ArrayList<>(zoneIds);
    Collections.sort(zoneList);
    timezoneBox = new JComboBox<>(zoneList.toArray(new String[0]));
    timezoneBox.setSelectedItem(ZoneId.systemDefault().getId());

    formPanel.add(new JLabel("Calendar Name:"));
    formPanel.add(nameField);
    formPanel.add(new JLabel("Timezone:"));
    formPanel.add(timezoneBox);

    add(formPanel, BorderLayout.CENTER);


    errorLabel = new JLabel(" ");
    errorLabel.setForeground(Color.RED);
    errorLabel.setBorder(new EmptyBorder(0, 20, 10, 20));

    JButton createButton = new JButton("Create");
    JButton cancelButton = new JButton("Cancel");

    createButton.addActionListener(e -> onCreate());
    cancelButton.addActionListener(e -> dispose());

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(createButton);
    buttonPanel.add(cancelButton);

    JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.add(errorLabel, BorderLayout.NORTH);
    bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

    add(bottomPanel, BorderLayout.SOUTH);
  }

  private void onCreate() {
    String name = nameField.getText().trim();
    String timezone = (String) timezoneBox.getSelectedItem();

    if (name.isEmpty()) {
      errorLabel.setText("Name cannot be empty.");
      return;
    }

    features.createCalendar(name, timezone);
    dispose();
  }
}
