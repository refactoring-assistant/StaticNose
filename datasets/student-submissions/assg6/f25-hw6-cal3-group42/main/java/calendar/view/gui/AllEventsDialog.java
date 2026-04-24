package calendar.view.gui;

import calendar.controller.gui.Features;
import calendar.model.event.CalendarEvent;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

/**
 * Dialog showing all events in the current calendar.
 */
class AllEventsDialog extends JDialog {

  private final Features features;
  private boolean modified = false;

  /**
   * Constructor for all events dialogue.
   *
   * @param parent   the main view panel
   * @param events   all events on this calendar
   * @param features controller with callback features
   */
  public AllEventsDialog(JFrame parent, List<CalendarEvent> events,
                         Features features) {
    super(parent, "All Events", true);
    this.features = features;

    setSize(700, 500);
    setLocationRelativeTo(parent);
    initializeUi(events);
  }

  /**
   * Create the UI for this dialogue panel.
   *
   * @param events all events to be displayed
   */
  private void initializeUi(List<CalendarEvent> events) {
    setLayout(new BorderLayout(10, 10));

    String[] columnNames = {"Date", "Time", "Subject", "Description", "Location", "Status"};
    Object[][] data = new Object[events.size()][6];

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");

    for (int i = 0; i < events.size(); i++) {
      CalendarEvent event = events.get(i);
      data[i][0] = event.getStartDateTime().format(dateFormatter);
      data[i][1] = event.getStartDateTime().format(timeFormatter)
          + " - " + event.getEndDateTime().format(timeFormatter);
      data[i][2] = event.getSubject()
          + (event.isPartOfSeries() ? " (R)" : "");
      data[i][3] = event.getDescription();
      data[i][4] = event.getLocation() != null ? event.getLocation() : "";
      data[i][5] = event.getStatus() != null ? event.getStatus() : "";
    }

    JTable table = new JTable(data, columnNames);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.getColumnModel().getColumn(1).setPreferredWidth(150);
    table.getColumnModel().getColumn(2).setPreferredWidth(100);
    table.getColumnModel().getColumn(3).setPreferredWidth(200);

    JScrollPane scrollPane = new JScrollPane(table);
    scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
    add(scrollPane, BorderLayout.CENTER);

    JPanel buttonPanel = editButtonPanel(events, table);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  /**
   * All the buttons for edit events on this panel.
   *
   * @param events all events to be displayed
   * @param table  the table containing all events
   * @return the panel with edit buttons
   */
  private JPanel editButtonPanel(List<CalendarEvent> events, JTable table) {

    JButton editButton = new JButton("Edit Selected");
    editButton.setEnabled(!events.isEmpty());
    editButton.addActionListener(e -> {
      int selectedRow = table.getSelectedRow();
      if (selectedRow >= 0) {
        CalendarEvent selectedEvent = events.get(selectedRow);
        EditEventDialog dialog = new EditEventDialog(
            (JFrame) getOwner(), selectedEvent, features);
        dialog.setVisible(true);
        if (dialog.wasModified()) {
          modified = true;
          dispose();
        }
      }
    });

    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(e -> dispose());

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.add(editButton);
    buttonPanel.add(closeButton);
    return buttonPanel;
  }

  /**
   * Check if this panel was modified.
   *
   * @return true if panel is modified, false if not
   */
  public boolean wasModified() {
    return modified;
  }
}
