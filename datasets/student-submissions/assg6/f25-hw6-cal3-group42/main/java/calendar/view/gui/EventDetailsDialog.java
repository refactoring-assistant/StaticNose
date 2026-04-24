package calendar.view.gui;

import calendar.model.event.CalendarEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;

/**
 * Dialog displaying detailed information about a single event.
 */
class EventDetailsDialog extends JDialog {

  private final CalendarEvent event;
  private final JPanel contentPanel;

  /**
   * Constructor to initialize the event details display dialogue box.
   *
   * @param parent the caller JFrame who called this dialogue box
   * @param event  the event to display details about
   */
  public EventDetailsDialog(JFrame parent, CalendarEvent event) {
    super(parent, "Event Details", true);
    this.event = event;
    setSize(500, 400);
    setLocationRelativeTo(parent);

    setLayout(new BorderLayout(10, 10));
    contentPanel = new JPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

    initializeUi();
  }

  /**
   * Create the UI for event display.
   */
  private void initializeUi() {
    dateAndTime();
    optionalDetails();
    recurringDetails();

    JScrollPane scrollPane = new JScrollPane(contentPanel);
    scrollPane.setBorder(null);
    add(scrollPane, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton closeButton = new JButton("Close");
    closeButton.addActionListener(e -> dispose());
    buttonPanel.add(closeButton);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  /**
   * Date, time and timezone details if the event.
   */
  private void dateAndTime() {

    JLabel titleLabel = new JLabel(event.getSubject());
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
    titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    contentPanel.add(titleLabel);
    contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));

    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
    addDetailRow(contentPanel, "Date:",
        event.getStartDateTime().format(dateFormatter));

    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");
    String timeStr = event.getStartDateTime().format(timeFormatter)
        + " - " + event.getEndDateTime().format(timeFormatter);
    addDetailRow(contentPanel, "Time:", timeStr);

    String timezone = event.getStartDateTime().getZone().getId();
    addDetailRow(contentPanel, "Timezone:", timezone);
  }

  /**
   * Optional details information for this event.
   */
  private void optionalDetails() {
    if (event.getDescription() != null && !event.getDescription().trim().isEmpty()) {
      addDetailRow(contentPanel, "Description:", event.getDescription());
    }

    if (event.getLocation() != null && !event.getLocation().trim().isEmpty()) {
      addDetailRow(contentPanel, "Location:", event.getLocation());
    }

    if (event.getStatus() != null && !event.getStatus().trim().isEmpty()) {
      addDetailRow(contentPanel, "Status:", event.getStatus());
    }
  }

  /**
   * Details if event is recurring.
   */
  private void recurringDetails() {
    if (event.isPartOfSeries()) {
      addDetailRow(contentPanel, "Event Type:", "Recurring Event");
      if (event.getSeriesUid() != null) {
        addDetailRow(contentPanel, "Series ID:", event.getSeriesUid());
      }
    } else {
      addDetailRow(contentPanel, "Event Type:", "Single Event");
    }
  }

  /**
   * The label and value table type of display.
   *
   * @param panel the main parent panel
   * @param label the label for each detail
   * @param value the value of each detail
   */
  private void addDetailRow(JPanel panel, String label, String value) {
    JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
    rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel labelComponent = new JLabel(label);
    labelComponent.setFont(new Font("SansSerif", Font.BOLD, 13));
    labelComponent.setPreferredSize(new Dimension(120, 20));
    rowPanel.add(labelComponent);

    JLabel valueComponent = new JLabel(value);
    valueComponent.setFont(new Font("SansSerif", Font.PLAIN, 13));
    rowPanel.add(valueComponent);

    panel.add(rowPanel);
  }
}