package calendar.view;


import calendar.controller.guicontroller.ViewListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;

/**
 * Dialog for editing properties of an existing event.
 */
public class EditEventDialog {

  private static final java.time.format.DateTimeFormatter TIME_FORMAT =
      java.time.format.DateTimeFormatter.ofPattern("HH:mm");
  private final List<ViewListener> listeners;

  /**
   * Creates a new EditEventDialog and stores listeners for callback events.
   *
   * @param listeners observers that receive edit-event notifications.
   */
  public EditEventDialog(List<ViewListener> listeners) {
    this.listeners = listeners;
  }

  private static void initialRender(
      ViewEvent event,
      JComboBox<String> propertyCombo,
      JPanel dynamicPanel,
      JTextField textField,
      JComboBox<String> locationCombo,
      JComboBox<String> statusCombo,
      JSpinner dateSpinner,
      JSpinner timeSpinner) {

    dynamicPanel.removeAll();
    String prop = (String) propertyCombo.getSelectedItem();

    if ("start".equals(prop) || "end".equals(prop)) {

      dynamicPanel.add(new JLabel("Select Date:"));
      dynamicPanel.add(dateSpinner);
      dynamicPanel.add(new JLabel("Select Time:"));
      dynamicPanel.add(timeSpinner);

      LocalDateTime initial = "start".equals(prop)
          ? event.getStartDateTime()
          : event.getEndDateTime();

      Date initialDate = Date.from(initial.atZone(ZoneId.systemDefault()).toInstant());
      dateSpinner.setValue(initialDate);
      timeSpinner.setValue(initialDate);

    } else if ("subject".equals(prop) || "description".equals(prop)) {

      textField.setText("");
      dynamicPanel.add(new JLabel("New Value:"));
      dynamicPanel.add(textField);

    } else if ("location".equals(prop)) {

      dynamicPanel.add(new JLabel("Location Type:"));
      dynamicPanel.add(locationCombo);

    } else if ("status".equals(prop)) {

      dynamicPanel.add(new JLabel("Status:"));
      dynamicPanel.add(statusCombo);
    }

    dynamicPanel.revalidate();
    dynamicPanel.repaint();
  }

  /**
   * Renders a select event pop up with a combo box containing all events.
   */
  public void showEditEventSelectionDialog(JFrame parent, LocalDate selectedDate,
                                           List<ViewEvent> events) {


    if (events.isEmpty()) {
      JOptionPane.showMessageDialog(parent, "No events on selected date", "Error",
          javax.swing.JOptionPane.ERROR_MESSAGE);
      return;
    }


    String[] eventStrings = new String[events.size()];
    for (int i = 0; i < events.size(); i++) {
      ViewEvent event = events.get(i);
      String timeStr = "All Day";
      if (! event.isAllDay()) {
        String timeStrStart = event.getStartDateTime().toLocalTime().format(TIME_FORMAT);
        String timeStrEnd = event.getEndDateTime().toLocalTime().format(TIME_FORMAT);
        timeStr = timeStrStart + " - " + timeStrEnd;
      }
      eventStrings[i] = timeStr + " - " + event.getSubject();
    }


    JDialog dialog = new JDialog(parent, "Select Event", true);
    dialog.setLayout(new BorderLayout());
    dialog.setSize(400, 150);
    dialog.setLocationRelativeTo(parent);

    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.add(new JLabel("Select event to edit:"), BorderLayout.NORTH);

    JComboBox<String> comboBox = new JComboBox<>(eventStrings);
    centerPanel.add(comboBox, BorderLayout.CENTER);

    dialog.add(centerPanel, BorderLayout.CENTER);


    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    JButton okButton = new JButton("OK");
    JButton cancelButton = new JButton("Cancel");

    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);
    dialog.add(buttonPanel, BorderLayout.SOUTH);


    okButton.addActionListener(e -> {
      int index = comboBox.getSelectedIndex();
      if (index >= 0) {
        ViewEvent selectedEvent = events.get(index);
        dialog.dispose();
        showEditEventPropertyDialog(parent, selectedEvent);
      } else {
        JOptionPane.showMessageDialog(parent, "No event selected", "Error",
            JOptionPane.ERROR_MESSAGE);
      }
    });


    cancelButton.addActionListener(e -> dialog.dispose());

    dialog.setVisible(true);
  }

  private void showEditEventPropertyDialog(JFrame parent, ViewEvent event) {

    JDialog dialog = new JDialog(parent, "Update Details", true);
    dialog.setSize(500, 500);
    dialog.setLayout(new BorderLayout());

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));


    panel.add(new JLabel("Property to Edit:"));
    JComboBox<String> propertyCombo = new JComboBox<>(
        new String[] {"subject", "start", "end", "description", "location", "status"});
    panel.add(propertyCombo);


    JPanel dynamicPanel = new JPanel();
    dynamicPanel.setLayout(new BoxLayout(dynamicPanel, BoxLayout.Y_AXIS));
    panel.add(dynamicPanel);


    JTextField textField = new JTextField(30);

    JComboBox<String> locationCombo = new JComboBox<>(
        new String[] {"Physical", "Online", "Unknown"});

    JComboBox<String> statusCombo = new JComboBox<>(
        new String[] {"public", "private", "unknown"});

    SpinnerDateModel dateModel = new SpinnerDateModel();
    JSpinner dateSpinner = new JSpinner(dateModel);
    dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));

    SpinnerDateModel timeModel = new SpinnerDateModel();
    JSpinner timeSpinner = new JSpinner(timeModel);
    timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "HH:mm"));


    initialRender(event, propertyCombo, dynamicPanel, textField,
        locationCombo, statusCombo, dateSpinner, timeSpinner);


    propertyCombo.addActionListener(
        e -> initialRender(event, propertyCombo, dynamicPanel, textField,
            locationCombo, statusCombo, dateSpinner, timeSpinner)
    );


    panel.add(new JLabel("Edit Scope:"));

    JComboBox<String> scopeCombo;

    if (event.isSeries()) {
      scopeCombo = new JComboBox<>(new String[] {"Single Event",
          "All Events From This", "All Events In Series"});
    } else {
      scopeCombo = new JComboBox<>(new String[] {"Single Event"});
    }

    panel.add(scopeCombo);


    JPanel buttonPanel = new JPanel(new FlowLayout());

    JButton saveButton = new JButton("Save");
    saveButton.addActionListener(e -> {
      try {
        String property = (String) propertyCombo.getSelectedItem();
        String newValue;

        if ("start".equals(property) || "end".equals(property)) {
          Date d = (Date) dateSpinner.getValue();
          LocalDate date = d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

          Date t = (Date) timeSpinner.getValue();
          LocalTime time = t.toInstant().atZone(ZoneId.systemDefault()).toLocalTime();

          LocalDateTime dt = LocalDateTime.of(date, time);
          newValue = dt.toString().substring(0, 16);

        } else if ("location".equals(property)) {
          newValue = (String) locationCombo.getSelectedItem();

        } else if ("status".equals(property)) {
          newValue = (String) statusCombo.getSelectedItem();

        } else {
          newValue = textField.getText().trim();
          if (newValue.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Value cannot be empty",
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
          }
        }

        String scope = (String) scopeCombo.getSelectedItem();

        String scopeString;
        if ("Single Event".equals(scope)) {
          scopeString = "single";
        } else if ("All Events From This".equals(scope)) {
          scopeString = "from";
        } else {
          scopeString = "series";
        }

        emitEditEvent(property, event.getSubject(), event.getStartDateTime(),
            event.getEndDateTime(), newValue, scopeString);

        dialog.dispose();

      } catch (Exception ex) {
        JOptionPane.showMessageDialog(dialog, "Failed to edit event: " + ex.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
      }
    });

    buttonPanel.add(saveButton);

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dialog.dispose());
    buttonPanel.add(cancelButton);

    panel.add(buttonPanel);

    dialog.add(panel);
    dialog.setLocationRelativeTo(parent);
    dialog.setVisible(true);
  }

  private void emitEditEvent(String property, String subject,
                             java.time.LocalDateTime startDateTime,
                             java.time.LocalDateTime endDateTime, String newValue, String scope) {
    for (ViewListener listener : listeners) {
      listener.handleEditEvent(property, subject, startDateTime, endDateTime, newValue, scope);
    }
  }

}
