package calendar.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * A dialog window that facilitates the editing of an existing event.
 * This class extends JDialog and provides a user interface for modifying specific
 * properties of an event, such as the subject, date, time, or location.
 * It also handles the scope of the edit operation, allowing users to choose
 * whether to update a single event, future events in a series, or the entire series
 * if the event is recurring.
 */
public class EditEventDialog extends JDialog {

  private final JComboBox<String> propertyDropdown;
  private final JTextField newValueField;
  private final JLabel formatHintLabel;

  private final JRadioButton eventRadio;
  private final JRadioButton eventsRadio;
  private final JRadioButton seriesRadio;

  private final JButton saveButton;
  private final JButton cancelButton;

  private boolean confirmed = false;

  /**
   * Constructs a new EditEventDialog.
   * Initializes the modal dialog with the necessary input fields for selecting
   * the property to modify and entering the new value. It also sets up scope selection
   * radio buttons (Single, Future, Series) based on whether the event is recurring.
   *
   * @param owner       the parent Frame to which this dialog is attached
   * @param eventInfo   a string containing summary information about the event being edited
   * @param isRecurring true if the event is part of a recurring series, false otherwise
   */
  public EditEventDialog(Frame owner, String eventInfo, boolean isRecurring) {
    super(owner, "Edit Event", true);

    this.setLayout(new BorderLayout(10, 10));

    JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
    JLabel label = new JLabel("Editing: " + eventInfo);
    label.setFont(new Font("Arial", Font.BOLD, 12));
    infoPanel.add(label);
    this.add(infoPanel, BorderLayout.NORTH);

    JPanel formPanel = new JPanel(new BorderLayout(0, 10));
    formPanel.setBorder(BorderFactory.createTitledBorder("Edit Details"));

    JPanel propPanel = new JPanel(new BorderLayout(5, 5));
    propPanel.add(new JLabel("Property to change:"), BorderLayout.NORTH);

    String[] props = {
        "Subject",
        "Start Date", "Start Time",
        "End Date", "End Time",
        "Location", "Description", "Status"
    };
    propertyDropdown = new JComboBox<>(props);
    propPanel.add(propertyDropdown, BorderLayout.CENTER);
    JPanel inputsPanel = new JPanel(new GridLayout(3, 1, 5, 5));
    inputsPanel.add(propPanel);

    JPanel valuePanel = new JPanel(new BorderLayout(5, 5));
    valuePanel.add(new JLabel("New Value:"), BorderLayout.NORTH);
    newValueField = new JTextField();
    valuePanel.add(newValueField, BorderLayout.CENTER);
    inputsPanel.add(valuePanel);

    formatHintLabel = new JLabel("Format: Text");
    formatHintLabel.setFont(new Font("Arial", Font.ITALIC, 11));
    formatHintLabel.setForeground(Color.GRAY);
    JPanel hintPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    hintPanel.add(formatHintLabel);
    inputsPanel.add(hintPanel);

    formPanel.add(inputsPanel, BorderLayout.NORTH);

    JPanel scopePanel = new JPanel(new GridLayout(3, 1, 0, 5));
    scopePanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

    eventRadio = new JRadioButton("This event only", true);
    eventsRadio = new JRadioButton("This and following events");
    seriesRadio = new JRadioButton("Entire series");

    ButtonGroup group = new ButtonGroup();
    group.add(eventRadio);
    group.add(eventsRadio);
    group.add(seriesRadio);

    scopePanel.add(eventRadio);
    scopePanel.add(eventsRadio);
    scopePanel.add(seriesRadio);

    if (!isRecurring) {
      eventsRadio.setEnabled(false);
      seriesRadio.setEnabled(false);
      eventsRadio.setToolTipText("Only available for recurring events");
      seriesRadio.setToolTipText("Only available for recurring events");
    }

    formPanel.add(scopePanel, BorderLayout.CENTER);
    this.add(formPanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    saveButton = new JButton("Save");
    cancelButton = new JButton("Cancel");

    buttonPanel.add(saveButton);
    buttonPanel.add(cancelButton);
    this.add(buttonPanel, BorderLayout.SOUTH);

    propertyDropdown.addActionListener(e -> updateHintLabel());
    updateHintLabel();

    saveButton.addActionListener(e -> {
      if (newValueField.getText().trim().isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter a new value.", "Error",
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

    this.setPreferredSize(new Dimension(400, 450));
    this.pack();
    this.setLocationRelativeTo(owner);
  }

  /**
   * Updates the helper text based on the selected property.
   */
  private void updateHintLabel() {
    String selected = (String) propertyDropdown.getSelectedItem();
    if (selected == null) {
      return;
    }

    switch (selected) {
      case "Start Date":
      case "End Date":
        formatHintLabel.setText("Format: YYYY-MM-DD (e.g., 2025-11-25)");
        break;
      case "Start Time":
      case "End Time":
        formatHintLabel.setText("Format: HH:MM (24-hour, e.g., 14:30)");
        break;
      case "Status":
        formatHintLabel.setText("Options: Public, Private, Tentative");
        break;
      default:
        formatHintLabel.setText("Format: Any Text");
        break;
    }
  }

  public boolean isConfirmed() {
    return confirmed;
  }

  public String getSelectedProperty() {
    return (String) propertyDropdown.getSelectedItem();
  }

  public String getNewValue() {
    return newValueField.getText();
  }

  public boolean isSingleEventScope() {
    return eventRadio.isSelected();
  }

  public boolean isFutureEventsScope() {
    return eventsRadio.isSelected();
  }

  public boolean isSeriesScope() {
    return seriesRadio.isSelected();
  }
}