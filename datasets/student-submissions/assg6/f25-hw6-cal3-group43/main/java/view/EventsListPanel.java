package view;

import controller.CalendarController;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import model.Calendar;
import model.Event;

/**
 * Panel that shows events for the selected day. There is a button next
 * to the header that allows a new event to be created. Double-clicking on
 * an event allows the person to see the event details for that event.
 */
public final class EventsListPanel extends JPanel {

  private final CalendarController controller;

  private LocalDate selectedDate;
  private final JLabel headerLabel;
  private DefaultListModel<Event> listModel;
  private JList<Event> list;

  /**
   * Constructs a EventsListPanel that will display a list of events
   * on a selected day from the calendar grid.
   *
   * @param controller the controller
   */
  public EventsListPanel(CalendarController controller) {
    super(new BorderLayout(5, 5));
    this.controller = controller;
    this.selectedDate = LocalDate.now();

    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setPreferredSize(new Dimension(260, 0));

    headerLabel = new JLabel();
    headerLabel.setFont(headerLabel.getFont().deriveFont(Font.BOLD, 14f));
    updateHeaderText();

    JButton addButton = new JButton("+");
    addButton.addActionListener(e -> {
      AddEventPopUp popUp = new AddEventPopUp(controller, selectedDate, EventsListPanel.this);
      popUp.setVisible(true);
    });

    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.add(headerLabel, BorderLayout.WEST);
    headerPanel.add(addButton, BorderLayout.EAST);

    listModel = new DefaultListModel<>();
    list = new JList<>(listModel);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    list.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2) {
          Event selected = list.getSelectedValue();
          if (selected != null) {
            DisplayDetailsPopUp details =
                new DisplayDetailsPopUp(controller, selected, EventsListPanel.this);
            details.setVisible(true);
          }
        }
      }
    });


    JScrollPane scrollPane = new JScrollPane(list);

    add(headerPanel, BorderLayout.NORTH);
    add(scrollPane, BorderLayout.CENTER);

    refreshEvents();
  }

  private void updateHeaderText() {
    headerLabel.setText("Events on " + selectedDate.toString());
  }

  /**
   * Shows the events on the day of the clicked date on the calendar.
   */
  public void showEventsForDate(LocalDate date) {
    this.selectedDate = date;
    updateHeaderText();
    refreshEvents();
  }

  /**
   * Refresh the events for the current selected date from the active calendar.
   */
  public void refreshEvents() {
    listModel.clear();

    Calendar active = controller.getActiveCalendar();
    if (active == null) {
      return;
    }

    List<Event> allEvents = active.getEvents();
    for (Event event : allEvents) {
      if (event.onDate(selectedDate)) {
        listModel.addElement(event);
      }
    }
    revalidate();
    repaint();
  }

  public Event getSelectedEvent() {
    return list.getSelectedValue();
  }

  public JList<Event> getList() {
    return list;
  }
}
