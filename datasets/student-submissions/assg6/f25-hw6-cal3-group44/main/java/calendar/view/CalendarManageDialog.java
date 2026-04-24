package calendar.view;

import calendar.controller.guicontroller.ViewListener;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.swing.BoxLayout;
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
 * Dialog for creating and editing calendars.
 */
public class CalendarManageDialog implements ActionListener {

  private final List<ViewListener> listeners;
  private JTextField nameField;
  private JComboBox<String> timezoneCombo;
  private JDialog dialog;
  private JComboBox<String> propertyCombo;
  private JTextField valueField;

  /**
   * Constructor for CalendarManageDialog.
   *
   * @param listeners the list of ViewListeners to notify
   */
  public CalendarManageDialog(List<ViewListener> listeners) {
    this.listeners = listeners;
  }

  private static List<String> getAllZoneIds() {
    Set<String> zoneIds = ZoneId.getAvailableZoneIds();
    List<String> sortedZoneIds = new ArrayList<>(zoneIds);
    Collections.sort(sortedZoneIds);
    return sortedZoneIds;
  }

  /**
   * Shows the create calendar dialog.
   *
   * @param parent the parent frame
   */
  public void showCreateDialog(JFrame parent) {
    this.dialog = new JDialog(parent, "Create New Calendar", true);
    this.dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));
    this.dialog.setSize(400, 250);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(new EmptyBorder(20, 20, 20, 20));

    panel.add(new JLabel("Calendar Name:"));
    this.nameField = new JTextField(20);
    panel.add(nameField);

    panel.add(new JLabel("Timezone:"));
    List<String> zoneIds = getAllZoneIds();
    this.timezoneCombo = new JComboBox<>(zoneIds.toArray(new String[0]));
    this.timezoneCombo.setEditable(false);
    panel.add(this.timezoneCombo);

    JPanel buttonPanel = new JPanel(new FlowLayout());

    JButton createButton = new JButton("Create");
    createButton.addActionListener(this);
    createButton.setActionCommand("create");
    buttonPanel.add(createButton);

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(this);
    cancelButton.setActionCommand("cancel");
    buttonPanel.add(cancelButton);

    panel.add(buttonPanel);
    dialog.add(panel);
    dialog.setLocationRelativeTo(parent);
    if (timezoneCombo.getItemCount() > 0) {
      timezoneCombo.setSelectedIndex(0);
    }
    dialog.setVisible(true);
  }

  /**
   * Shows the edit calendar dialog.
   *
   * @param parent the parent frame
   */
  public void showEditDialog(JFrame parent) {
    this.dialog = new JDialog(parent, "Edit Calendar", true);
    this.dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.Y_AXIS));
    this.dialog.setSize(400, 250);

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(new EmptyBorder(20, 20, 20, 20));

    panel.add(new JLabel("Property to Edit:"));
    propertyCombo = new JComboBox<>(new String[] {"name", "timezone"});
    panel.add(propertyCombo);

    panel.add(new JLabel("New Value:"));
    valueField = new JTextField(5);
    List<String> zoneIds = getAllZoneIds();
    timezoneCombo = new JComboBox<>(zoneIds.toArray(new String[0]));
    timezoneCombo.setEditable(false);

    timezoneCombo.setVisible(false);
    panel.add(valueField);
    panel.add(timezoneCombo);

    propertyCombo.addActionListener(this);
    propertyCombo.setActionCommand("propertySelect");

    JPanel buttonPanel = new JPanel(new FlowLayout());
    JButton saveButton = new JButton("Save");
    saveButton.addActionListener(this);
    saveButton.setActionCommand("save");
    buttonPanel.add(saveButton);

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(this);
    cancelButton.setActionCommand("cancel");
    buttonPanel.add(cancelButton);

    panel.add(buttonPanel);
    dialog.add(panel);
    dialog.setLocationRelativeTo(parent);
    dialog.setVisible(true);

  }

  @Override
  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    switch (command) {
      case "create":
        String name = nameField.getText();
        if (name == null || name.trim().isEmpty()) {
          JOptionPane.showMessageDialog(dialog, "Calendar name cannot be empty",
              "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }
        String timezone;
        try {
          Object tzObj = timezoneCombo.getSelectedItem();
          if (tzObj == null) {
            timezone = ZoneId.systemDefault().getId();
          } else {
            timezone = tzObj.toString();
          }
        } catch (Exception ex) {
          timezone = ZoneId.systemDefault().getId();
        }
        emitCreateCalendar(name, timezone);
        this.dialog.dispose();
        emitRefresh();
        dialog.dispose();
        break;
      case "save":
        String property = Objects.requireNonNull(propertyCombo.getSelectedItem()).toString();
        String newValue;
        if ("timezone".equals(property)) {
          newValue = (String) timezoneCombo.getSelectedItem();
        } else {
          newValue = valueField.getText();
        }
        if (newValue == null || newValue.isEmpty()) {
          JOptionPane.showMessageDialog(dialog, "Value cannot be empty",
              "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }
        emitEditCalendar(property, newValue);
        emitRefresh();
        dialog.dispose();
        break;
      case "cancel":
        this.dialog.dispose();
        break;
      case "propertySelect":
        String propertyVal = Objects.requireNonNull(propertyCombo.getSelectedItem()).toString();
        if ("timezone".equals(propertyVal)) {
          valueField.setVisible(false);
          timezoneCombo.setVisible(true);
        } else {
          valueField.setVisible(true);
          timezoneCombo.setVisible(false);
        }
        break;
      default:
        break;
    }
  }

  private void emitCreateCalendar(String name, String timezone) {
    for (ViewListener listener : listeners) {
      try {
        listener.handleCreateCalendar(name, timezone);
      } catch (Exception e) {
        JOptionPane.showMessageDialog(dialog, e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void emitEditCalendar(String property, String newValue) {
    for (ViewListener listener : listeners) {
      listener.handleEditCalendar(property, newValue);
    }
  }

  private void emitRefresh() {
    for (ViewListener listener : listeners) {
      listener.handleRefresh();
    }
  }
}
