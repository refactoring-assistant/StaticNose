package calendar.view;

import calendar.controller.guicontroller.ViewListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Dialog for selecting and deleting calendar events.
 */
public class DeleteEventDialog {

  private static final java.time.format.DateTimeFormatter TIME_FORMAT =
      java.time.format.DateTimeFormatter.ofPattern("HH:mm");
  private final List<ViewListener> listeners;
  private JComboBox<String> scopeCombo;
  private JDialog dialog;

  /**
   * Creates a delete dialog with the given listeners.
   *
   * @param listeners listeners to notify when an event is deleted
   */
  public DeleteEventDialog(List<ViewListener> listeners) {
    this.listeners = listeners;
  }

  /**
   * Displays a dialog allowing the user to pick an event to delete.
   *
   * @param parent       the parent frame
   * @param selectedDate the selected date
   * @param events       the events available for deletion
   */
  public void showDeleteEventSelectionDialog(JFrame parent, LocalDate selectedDate,
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
    centerPanel.add(new JLabel("Select event to Delete:"), BorderLayout.NORTH);

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
        showDeleteEventDialog(parent, selectedEvent);
        dialog.dispose();

      } else {
        JOptionPane.showMessageDialog(parent, "No event selected", "Error",
            JOptionPane.ERROR_MESSAGE);
      }
    });


    cancelButton.addActionListener(e -> dialog.dispose());

    dialog.setVisible(true);
  }


  /**
   * Renders a delete event dialog box with the event details and.
   * a combo box with event scope to delete.
   */
  public void showDeleteEventDialog(JFrame parent, ViewEvent event) {
    dialog = new JDialog(parent, "Delete Event", true);
    dialog.setSize(400, 250);
    dialog.setLayout(new BorderLayout());

    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setBorder(new EmptyBorder(20, 20, 20, 20));

    panel.add(new JLabel("Event: " + event.getSubject()));
    panel.add(new JLabel("Date: " + event.getStartDateTime().toLocalDate().toString()));
    panel.add(new JLabel("From: " + event.getStartDateTime().toLocalTime().toString()));
    panel.add(new JLabel("To: " + event.getEndDateTime().toLocalTime().toString()));

    if (event.isSeries()) {
      panel.add(new JLabel("Choose Delete Scope:"));
      scopeCombo = new JComboBox<>(new String[] {
          "Only this event",
          "All events from this event",
          "All events in series"
      });
      panel.add(scopeCombo);
    }

    JPanel buttonPanel = new JPanel(new FlowLayout());
    JButton deleteButton = new JButton("Delete");

    deleteButton.addActionListener(e -> {
      String scope;

      if (event.isSeries()) {
        Object sel = scopeCombo.getSelectedItem();
        if (sel == null) {
          JOptionPane.showMessageDialog(dialog, "Please select delete scope.",
              "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }
        scope = sel.toString();
      } else {
        scope = "Only this event";
      }

      emitDeleteEvent(event.getSubject(), event.getStartDateTime(),
          event.getEndDateTime(), scope);

      dialog.dispose();
    });

    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dialog.dispose());

    buttonPanel.add(deleteButton);
    buttonPanel.add(cancelButton);
    panel.add(buttonPanel);

    dialog.add(panel);
    dialog.setLocationRelativeTo(parent);
    dialog.setVisible(true);
  }


  private void emitDeleteEvent(String subject, LocalDateTime startDateTime,
                               LocalDateTime endDateTime, String scope) {
    for (ViewListener listener : listeners) {
      listener.handleDeleteEvent(subject, startDateTime, endDateTime, scope);
    }
  }
}
