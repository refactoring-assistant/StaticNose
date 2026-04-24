package calendar.view;

import calendar.controller.guicontroller.ViewListener;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * Bottom panel component for action buttons.
 */
public class BottomPanel extends JPanel {
  private final JButton createEventButton;
  private final JButton editEventButton;
  private final JButton deleteEventButton;
  private final JButton exportButton;
  private final List<ViewListener> listeners;
  private final JFrame parent;
  private LocalDate selectedDate;
  private List<ViewEvent> dayEvents;

  /**
   * Constructor for BottomPanel.
   */
  public BottomPanel(JFrame parent, List<ViewListener> listeners) {
    super(new FlowLayout());
    this.parent = parent;
    this.listeners = listeners;
    createEventButton = new JButton("Create Event");
    editEventButton = new JButton("Edit Event");
    deleteEventButton = new JButton("Delete Event");
    exportButton = new JButton("Export Calendar");
    dayEvents = new ArrayList<>();
    initializeComponents();
  }

  private void initializeComponents() {
    this.add(createEventButton);
    createEventButton.setActionCommand("createEvent");

    this.add(editEventButton);
    editEventButton.setActionCommand("editEvent");

    this.add(deleteEventButton);
    deleteEventButton.setActionCommand("deleteEvent");

    this.add(exportButton);
    exportButton.setActionCommand("exportCalendar");
    setBottomPanelListeners();
  }

  /**
   * Gets the create event button.
   *
   * @return the create event button
   */
  public JButton getCreateEventButton() {
    return createEventButton;
  }

  /**
   * Gets the edit event button.
   *
   * @return the edit event button
   */
  public JButton getEditEventButton() {
    return editEventButton;
  }

  /**
   * Gets the delete event button.
   *
   * @return the delete event button
   */
  public JButton getDeleteEventButton() {
    return deleteEventButton;
  }

  /**
   * Gets the export button.
   *
   * @return the export button
   */
  public JButton getExportButton() {
    return exportButton;
  }

  /**
   * Sets current selected date.
   */
  public void setSelectedDate(LocalDate selectedDate) {
    this.selectedDate = selectedDate;
  }

  public void setDayEvents(List<ViewEvent> dayEvents) {
    this.dayEvents = dayEvents;
  }

  private void setBottomPanelListeners() {
    createEventButton.addActionListener(e -> {
      if (selectedDate == null) {
        JOptionPane.showMessageDialog(parent, "Please select a date first.", "Error",
            JOptionPane.ERROR_MESSAGE);
        return;
      }
      CreateEventDialog createEventDialog = new CreateEventDialog(listeners);
      createEventDialog.showCreateDialog(parent, selectedDate);
      emitRefresh();
    });

    editEventButton.addActionListener(e -> {
      if (selectedDate == null) {
        JOptionPane.showMessageDialog(parent, "Please select a date first.", "Error",
            JOptionPane.ERROR_MESSAGE);
        return;
      }
      EditEventDialog editEventDialog = new EditEventDialog(listeners);
      List<ViewEvent> events = getEventsForDay(selectedDate);
      editEventDialog.showEditEventSelectionDialog(parent, selectedDate, events);
      emitRefresh();
    });

    deleteEventButton.addActionListener(e -> {
      if (selectedDate == null) {
        JOptionPane.showMessageDialog(parent, "Please select a date first.", "Error",
            JOptionPane.ERROR_MESSAGE);
        return;
      }
      DeleteEventDialog deleteEventDialog = new DeleteEventDialog(listeners);
      List<ViewEvent> events = getEventsForDay(selectedDate);
      deleteEventDialog.showDeleteEventSelectionDialog(parent, selectedDate, events);
      emitRefresh();
    });

    exportButton.addActionListener(e -> {
      ExportCalendarDialog exportCalendarDialog = new ExportCalendarDialog(listeners);
      exportCalendarDialog.showExportDialog(parent);
    });
  }

  private List<ViewEvent> getEventsForDay(LocalDate date) {
    List<ViewEvent> events = new ArrayList<>();
    for (ViewListener listener : listeners) {
      try {
        List<ViewEvent> res = dayEvents;
        if (res != null) {
          events.addAll(res);
        }
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(parent, ex.getMessage(), "Error",
            JOptionPane.ERROR_MESSAGE);
      }
    }
    return events;
  }

  private void emitRefresh() {
    for (ViewListener listener : listeners) {
      listener.handleRefresh();
    }
  }
}
