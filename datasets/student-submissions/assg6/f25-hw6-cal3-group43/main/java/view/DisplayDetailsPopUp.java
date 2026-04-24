package view;

import controller.CalendarController;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import model.Event;

/**
 * A popup that displays the corresponding details for a single event. The fields cannot
 * be changed unless the user presses the edit button, which will open EditDetailsPopUp.
 * To close the window, press the cancel button.
 */
public final class DisplayDetailsPopUp extends JFrame {

  private final CalendarController controller;
  private final Event event;
  private final EventsListPanel eventsListPanel;

  /**
   *  Constructs a DisplayDetailPopUp that shows the user the details about the
   *  event they have selected from the event list.
   *
   * @param controller the controller
   * @param event the event that has been selected
   * @param eventsListPanel panel to refresh after edits (may be null)
   */
  public DisplayDetailsPopUp(CalendarController controller, Event event,
                             EventsListPanel eventsListPanel) {
    super("Event Details");
    this.controller = controller;
    this.event = event;
    this.eventsListPanel = eventsListPanel;

    buildUi();
    setPreferredSize(new Dimension(480, 360));
    pack();
    setLocationRelativeTo(null);
  }

  private void buildUi() {
    JPanel content = new JPanel(new BorderLayout(10, 10));
    content.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JLabel titleLabel = new JLabel(event.getSubject());
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
    content.add(titleLabel, BorderLayout.NORTH);

    JPanel fieldsPanel = new JPanel(new GridLayout(0, 1, 4, 4));

    LocalDateTime start = event.getStartTime();
    LocalDateTime end = event.getEndTime();
    boolean isAllDay = event.getIsAllDay();

    DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm a");

    fieldsPanel.add(makeRow("Start date", start.toLocalDate().format(dateFmt)));
    fieldsPanel.add(makeRow("Start time", start.toLocalTime().format(timeFmt)));

    fieldsPanel.add(makeRow("End date", end.toLocalDate().format(dateFmt)));
    fieldsPanel.add(makeRow("End time", end.toLocalTime().format(timeFmt)));
    fieldsPanel.add(makeRow("All day", isAllDay ? "Yes" : "No"));

    fieldsPanel.add(makeRow("Location", safe(event.getLocation())));
    fieldsPanel.add(makeRow("Status", safe(event.getStatus())));

    JTextArea descArea = new JTextArea(safe(event.getDescription()));
    descArea.setEditable(false);
    descArea.setLineWrap(true);
    descArea.setWrapStyleWord(true);
    JScrollPane descScroll = new JScrollPane(descArea);
    descScroll.setBorder(BorderFactory.createTitledBorder("Description"));

    JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
    centerPanel.add(fieldsPanel, BorderLayout.NORTH);
    centerPanel.add(descScroll, BorderLayout.CENTER);

    content.add(centerPanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel();
    JButton editButton = new JButton("Edit");
    JButton cancelButton = new JButton("Cancel");

    editButton.addActionListener(e -> {
      EditDetailsPopUp editPopUp = new EditDetailsPopUp(controller, event, eventsListPanel);
      editPopUp.setVisible(true);
      dispose();
    });

    cancelButton.addActionListener(e -> dispose());

    buttonPanel.add(editButton);
    buttonPanel.add(cancelButton);

    content.add(buttonPanel, BorderLayout.SOUTH);

    setContentPane(content);
  }

  private JPanel makeRow(String label, String value) {
    JPanel row = new JPanel(new BorderLayout(4, 0));
    JLabel nameLabel = new JLabel(label + ": ");
    JLabel valueLabel = new JLabel(value);
    row.add(nameLabel, BorderLayout.WEST);
    row.add(valueLabel, BorderLayout.CENTER);
    return row;
  }

  private String safe(String text) {
    if (text == null) {
      return "";
    }
    return text;
  }
}
