package calendar.view.gui;

import calendar.controller.gui.Features;
import calendar.model.event.CalendarEvent;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

/**
 * Dialog showing all events for a specific day.
 */
class DayEventsDialog extends JDialog {

  private final Features features;
  private final LocalDate date;
  private boolean modified = false;

  /**
   * Constructor to initialize the create event dialogue box.
   *
   * @param parent   the caller JFrame who called this dialogue box
   * @param date     the date on which all events should be displayed
   * @param events   all the events on this day
   * @param features the interface defining all callbacks
   */
  public DayEventsDialog(JFrame parent, LocalDate date,
                         List<CalendarEvent> events, Features features) {
    super(parent, "Events on " + date.format(
        DateTimeFormatter.ofPattern("MMMM d, yyyy")), true);
    this.features = features;
    this.date = date;

    setSize(600, 400);
    setLocationRelativeTo(parent);
    initializeUi(events);
  }

  /**
   * Create the UI for this dialogue box.
   *
   * @param events all the events on this date
   */
  private void initializeUi(List<CalendarEvent> events) {
    setLayout(new BorderLayout(10, 10));

    DefaultListModel<String> listModel = new DefaultListModel<>();
    JList<String> eventList = new JList<>(listModel);

    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    for (CalendarEvent event : events) {
      String timeStr = event.getStartDateTime().format(timeFormatter)
          + " - " + event.getEndDateTime().format(timeFormatter);
      String display = timeStr + " | " + event.getSubject();
      if (event.isPartOfSeries()) {
        display += " (Recurring)";
      }
      listModel.addElement(display);
    }

    if (events.isEmpty()) {
      listModel.addElement("No events scheduled for this day");
    }

    eventList.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent e) {
        if (e.getClickCount() == 2) {
          int selectedIndex = eventList.getSelectedIndex();
          if (selectedIndex >= 0 && !events.isEmpty()) {
            CalendarEvent selectedEvent = events.get(selectedIndex);
            showEventDetails(selectedEvent);
          }
        }
      }
    });

    JScrollPane scrollPane = new JScrollPane(eventList);
    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
    add(scrollPane, BorderLayout.CENTER);

    JPanel buttonPanel = getButtonPanel(events, eventList);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  /**
   * Create the button panel for creating, editing and copying the events.
   *
   * @param events    all the events on this date
   * @param eventList the JList model for all events on this date
   * @return the panel which has all the buttons
   */
  private JPanel getButtonPanel(List<CalendarEvent> events, JList<String> eventList) {
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    JButton createButton = getCreateButton();
    buttonPanel.add(createButton);
    JButton editButton = getEditButton(events, eventList);
    buttonPanel.add(editButton);
    JButton copyButton = getCopyButton(events, eventList);
    buttonPanel.add(copyButton);
    JButton copyAllButton = new JButton("Copy Shown Events");
    copyAllButton.setEnabled(!events.isEmpty());
    copyAllButton.addActionListener(e -> showCopyEventsOnDateDialog());
    buttonPanel.add(copyAllButton);
    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(e -> dispose());
    buttonPanel.add(closeButton);

    return buttonPanel;
  }

  /**
   * Create button to create a new create event dialogue.
   *
   * @return the create button
   */
  private JButton getCreateButton() {
    JButton createButton = new JButton("Create Event");
    createButton.addActionListener(e -> {
      CreateEventDialog dialog = new CreateEventDialog(
          (JFrame) getOwner(), features, date);
      dialog.setVisible(true);
      if (dialog.wasCreated()) {
        modified = true;
        dispose();
      }
    });
    return createButton;
  }

  /**
   * Edit button to edit a selected event.
   *
   * @param events    all the events on this date
   * @param eventList JList of all events on this date
   * @return the edit button
   */
  private JButton getEditButton(List<CalendarEvent> events, JList<String> eventList) {
    JButton editButton = new JButton("Edit Selected");
    editButton.setEnabled(!events.isEmpty());
    editButton.addActionListener(e -> {
      int selectedIndex = eventList.getSelectedIndex();
      if (selectedIndex >= 0) {
        CalendarEvent selectedEvent = events.get(selectedIndex);
        showEditEventDialog(selectedEvent);
      } else {
        JOptionPane.showMessageDialog(this,
            "Please select an event to edit",
            "No Selection",
            JOptionPane.WARNING_MESSAGE);
      }
    });
    return editButton;
  }

  /**
   * Copy button to copy the selected event.
   *
   * @param events    all the events on this date
   * @param eventList JList of all events on this date
   * @return the copy button
   */
  private JButton getCopyButton(List<CalendarEvent> events, JList<String> eventList) {
    JButton copyButton = new JButton("Copy Selected");
    copyButton.setEnabled(!events.isEmpty());
    copyButton.addActionListener(e -> {
      int selectedIndex = eventList.getSelectedIndex();
      if (selectedIndex >= 0) {
        CalendarEvent selectedEvent = events.get(selectedIndex);
        showCopyEventDialog(selectedEvent);
      } else {
        JOptionPane.showMessageDialog(this,
            "Please select an event to Copy",
            "No Selection",
            JOptionPane.WARNING_MESSAGE);
      }
    });
    return copyButton;
  }

  /**
   * Action method to call the edit dialogue box.
   *
   * @param event the event to edit
   */
  private void showEditEventDialog(CalendarEvent event) {
    EditEventDialog dialog = new EditEventDialog(
        (JFrame) getOwner(), event, features);
    dialog.setVisible(true);
    if (dialog.wasModified()) {
      modified = true;
      dispose();
    }
  }

  /**
   * Action method to call the copy dialogue box.
   *
   * @param event the event to copy
   */
  private void showCopyEventDialog(CalendarEvent event) {
    CopyEventDialog dialog = new CopyEventDialog(
        (JFrame) getOwner(), event, features);
    dialog.setVisible(true);
    if (dialog.wasCopied()) {
      modified = true;
      dispose();
    }
  }

  /**
   * Show the details of the selected event.
   *
   * @param event the event which is selected
   */
  private void showEventDetails(CalendarEvent event) {
    EventDetailsDialog dialog = new EventDetailsDialog(
        (JFrame) getOwner(), event);
    dialog.setVisible(true);
  }

  /**
   * Copy all the events shown on this date.
   */
  private void showCopyEventsOnDateDialog() {
    CopyEventsOnDateDialog dialog = new CopyEventsOnDateDialog(
        (JFrame) getOwner(), date, features);
    dialog.setVisible(true);
    if (dialog.wasCopied()) {
      modified = true;
      dispose();
    }
  }

  /**
   * Returns if the panel was modified.
   *
   * @return true if panel was modified
   */
  public boolean wasModified() {
    return modified;
  }
}
