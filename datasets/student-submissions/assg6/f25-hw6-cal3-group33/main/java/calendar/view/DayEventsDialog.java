package calendar.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

/**
 * Dialog displaying all events for a specific day.
 * Allows creating new events and editing existing events.
 */
public class DayEventsDialog extends JDialog {

  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");

  private final LocalDate selectedDate;
  private JPanel eventsContainer;
  private JButton createEventBtn;
  private JButton closeBtn;

  private Runnable onCreateEvent;
  private Consumer<EventDisplayInfo> onEditEvent;

  /**
   * Constructs a DayEventsDialog.
   *
   * @param parent the parent frame
   * @param date   the date to display events for
   */
  public DayEventsDialog(JFrame parent, LocalDate date) {
    super(parent, "Events", true);
    this.selectedDate = date;

    setSize(600, 500);
    setMinimumSize(new Dimension(500, 400));
    setLocationRelativeTo(parent);
    setLayout(new BorderLayout());

    initializeComponents();
  }

  /**
   * Initializes all dialog components.
   */
  private void initializeComponents() {
    JPanel headerPanel = createHeaderPanel();
    add(headerPanel, BorderLayout.NORTH);

    eventsContainer = new JPanel();
    eventsContainer.setLayout(new BoxLayout(eventsContainer, BoxLayout.Y_AXIS));
    eventsContainer.setBackground(Color.WHITE);
    eventsContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JScrollPane scrollPane = new JScrollPane(eventsContainer);
    scrollPane.setBorder(null);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    add(scrollPane, BorderLayout.CENTER);

    JPanel footerPanel = createFooterPanel();
    add(footerPanel, BorderLayout.SOUTH);
  }

  /**
   * Creates the header panel with date label.
   */
  private JPanel createHeaderPanel() {
    JPanel headerPanel = new JPanel(new BorderLayout());
    headerPanel.setBackground(Color.WHITE);
    headerPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY),
        BorderFactory.createEmptyBorder(15, 15, 15, 15)
    ));

    JLabel dateLabel = new JLabel("Events for " + selectedDate.format(DATE_FORMATTER));
    dateLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
    headerPanel.add(dateLabel, BorderLayout.WEST);

    return headerPanel;
  }

  /**
   * Creates the footer panel with action buttons.
   */
  private JPanel createFooterPanel() {
    JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
    footerPanel.setBackground(Color.WHITE);
    footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

    createEventBtn = new JButton("+ Create Event");
    createEventBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
    createEventBtn.setBackground(new Color(66, 133, 244));
    createEventBtn.setForeground(Color.WHITE);
    createEventBtn.setOpaque(true);
    createEventBtn.setFocusPainted(false);
    createEventBtn.setBorderPainted(false);
    createEventBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    createEventBtn.setPreferredSize(new Dimension(140, 35));
    createEventBtn.addActionListener(e -> handleCreateEvent());

    closeBtn = new JButton("Close");
    closeBtn.setFont(new Font("SansSerif", Font.PLAIN, 13));
    closeBtn.setFocusPainted(false);
    closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    closeBtn.setPreferredSize(new Dimension(80, 35));
    closeBtn.addActionListener(e -> dispose());

    footerPanel.add(createEventBtn);
    footerPanel.add(closeBtn);

    return footerPanel;
  }

  /**
   * Displays events in the dialog.
   * Clears existing content and shows all events or empty state.
   *
   * @param events list of event information to display
   */
  public void displayEvents(List<EventDisplayInfo> events) {
    eventsContainer.removeAll();

    if (events == null || events.isEmpty()) {
      showEmptyState();
    } else {
      for (EventDisplayInfo event : events) {
        EventItemPanel eventPanel = new EventItemPanel(event);
        eventsContainer.add(eventPanel);
        eventsContainer.add(Box.createVerticalStrut(10));
      }
    }

    eventsContainer.revalidate();
    eventsContainer.repaint();
  }

  /**
   * Shows empty state when no events exist for this day.
   */
  private void showEmptyState() {
    JPanel emptyPanel = new JPanel(new BorderLayout());
    emptyPanel.setBackground(Color.WHITE);
    emptyPanel.setPreferredSize(new Dimension(0, 150));
    emptyPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

    JLabel emptyLabel = new JLabel("No events scheduled", SwingConstants.CENTER);
    emptyLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
    emptyLabel.setForeground(Color.GRAY);

    emptyPanel.add(emptyLabel, BorderLayout.CENTER);
    eventsContainer.add(emptyPanel);
  }

  /**
   * Handles create event button click.
   */
  private void handleCreateEvent() {
    if (onCreateEvent != null) {
      onCreateEvent.run();
    }
  }

  /**
   * Sets the callback for create event action.
   *
   * @param callback the callback to execute when creating event
   */
  public void setOnCreateEvent(Runnable callback) {
    this.onCreateEvent = callback;
  }

  /**
   * Sets the callback for edit event action.
   * Passes the entire EventDisplayInfo object for identification.
   *
   * @param callback the callback to execute with event data
   */
  public void setOnEditEvent(Consumer<EventDisplayInfo> callback) {
    this.onEditEvent = callback;
  }

  /**
   * Gets the selected date for this dialog.
   *
   * @return the LocalDate
   */
  public LocalDate getSelectedDate() {
    return selectedDate;
  }

  /**
   * Panel displaying a single event item with details and edit button.
   */
  private class EventItemPanel extends JPanel {

    public EventItemPanel(EventDisplayInfo event) {
      setLayout(new BorderLayout(10, 5));
      setBackground(Color.WHITE);
      setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
          BorderFactory.createEmptyBorder(10, 10, 10, 10)
      ));
      setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

      JPanel detailsPanel = createDetailsPanel(event);

      JPanel buttonPanel = createButtonPanel(event);

      add(detailsPanel, BorderLayout.CENTER);
      add(buttonPanel, BorderLayout.EAST);
    }

    /**
     * Creates the event details panel.
     */
    private JPanel createDetailsPanel(EventDisplayInfo event) {
      JPanel detailsPanel = new JPanel();
      detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
      detailsPanel.setBackground(Color.WHITE);

      JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
      namePanel.setBackground(Color.WHITE);

      JLabel colorDot = new JLabel("●");
      colorDot.setForeground(event.getCalendarColor());
      colorDot.setFont(new Font("SansSerif", Font.PLAIN, 16));

      JLabel nameLabel = new JLabel(event.getEventName());
      nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

      namePanel.add(colorDot);
      namePanel.add(nameLabel);

      detailsPanel.add(namePanel);
      detailsPanel.add(Box.createVerticalStrut(3));

      LocalDate startDate = event.getStartDateTime().toLocalDate();
      LocalDate endDate = event.getEventEndDate();

      if (endDate != null && !endDate.equals(startDate)) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("MMM d");
        String dateRange = "  📅 " + startDate.format(dateFormat)
            + " - " + endDate.format(dateFormat);

        JLabel dateRangeLabel = new JLabel(dateRange);
        dateRangeLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        dateRangeLabel.setForeground(new Color(100, 100, 100));
        detailsPanel.add(dateRangeLabel);
        detailsPanel.add(Box.createVerticalStrut(2));
      }

      JLabel timeLabel = new JLabel("  " + event.getTimeRange());
      timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
      timeLabel.setForeground(new Color(100, 100, 100));
      detailsPanel.add(timeLabel);

      if (event.getLocation() != null && !event.getLocation().isEmpty()) {
        JLabel locationLabel = new JLabel("  📍Location : " + event.getLocation());
        locationLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        locationLabel.setForeground(new Color(100, 100, 100));
        detailsPanel.add(Box.createVerticalStrut(2));
        detailsPanel.add(locationLabel);
      }

      if (event.getDescription() != null && !event.getDescription().isEmpty()) {
        JLabel descLabel = new JLabel("  📋Description : " + event.getDescription());
        descLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        descLabel.setForeground(new Color(100, 100, 100));
        detailsPanel.add(Box.createVerticalStrut(2));
        detailsPanel.add(descLabel);
      }

      if (event.getStatus() != null && event.getStatus().equals("PRIVATE")) {
        JLabel statusLabel = new JLabel("  🔒Status : Private");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(100, 100, 100));
        detailsPanel.add(Box.createVerticalStrut(2));
        detailsPanel.add(statusLabel);
      }

      if (event.isRecurring()) {
        JLabel recurringLabel = new JLabel("  🔄 " + event.getRecurrenceInfo());
        recurringLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        recurringLabel.setForeground(new Color(100, 100, 100));
        detailsPanel.add(Box.createVerticalStrut(2));
        detailsPanel.add(recurringLabel);
      }

      return detailsPanel;
    }

    /**
     * Creates the button panel with edit button.
     */
    private JPanel createButtonPanel(EventDisplayInfo event) {
      JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
      buttonPanel.setBackground(Color.WHITE);

      JButton editBtn = new JButton("Edit");
      editBtn.setFont(new Font("SansSerif", Font.PLAIN, 12));
      editBtn.setFocusPainted(false);
      editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
      editBtn.setPreferredSize(new Dimension(70, 28));
      editBtn.addActionListener(e -> {
        if (onEditEvent != null) {
          onEditEvent.accept(event);
        }
      });

      buttonPanel.add(editBtn);
      return buttonPanel;
    }
  }
}