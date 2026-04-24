package view;

import controller.CalendarController;
import java.awt.BorderLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import messaging.MessageSink;
import messaging.Messages;

/**
 * A lightweight Swing demo that showcases how a graphical view could integrate with the existing
 * controllers. It is intentionally simple: it draws a month grid and lets the user fire commands
 * into the controller so the backend logic can stay the same across GUI / interactive / headless
 * modes, exactly as described in the README.
 */
public final class CalendarView {

  private final CalendarController controller;
  private EventsListPanel eventsListPanel;
  private JLabel titleLabel;
  private JLabel timezoneLabel;
  private CalendarListPanel calendarListPanel;

  /**
   * Constructs a CalendarView object and initializes the controller to it.
   *
   * @param controller the controller
   */
  private CalendarView(CalendarController controller) {
    this.controller = controller;
  }

  /**
   * Launches the demo view on the Swing event dispatch thread.
   *
   * @param controller backing controller shared with the rest of the app
   */
  public static void launch(CalendarController controller) {
    Objects.requireNonNull(controller, "controller");
    SwingUtilities.invokeLater(() -> new CalendarView(controller).showFrame());
  }

  private void showFrame() {
    JFrame frame = new JFrame("Calendar");
    MessageSink previousSink = Messages.getSink();
    Messages.setSink(new SwingMessageSink(frame, previousSink));
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLayout(new BorderLayout(10, 10));

    frame.add(buildHeader(), BorderLayout.NORTH);
    calendarListPanel = new CalendarListPanel(controller,
        this::refreshActiveCalendarLabel);
    frame.add(calendarListPanel, BorderLayout.WEST);
    frame.add(buildSelectMonthPanel(), BorderLayout.CENTER);

    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  private JPanel buildHeader() {
    final JPanel header = new JPanel(new BorderLayout());
    JPanel labels = new JPanel();
    labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
    labels.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    titleLabel = new JLabel(controller.getActiveCalendarName());
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
    timezoneLabel = new JLabel(controller.getActiveCalendarTimezoneText());
    timezoneLabel.setFont(timezoneLabel.getFont().deriveFont(Font.PLAIN, 12f));

    labels.add(titleLabel);
    labels.add(timezoneLabel);

    header.add(labels, BorderLayout.WEST);
    return header;
  }

  private JPanel buildSelectMonthPanel() {
    JPanel wrapper = new JPanel(new BorderLayout());
    wrapper.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

    CalendarPanel calendarPanel =
        new CalendarPanel(YearMonth.now(), this::handleDayClick);
    wrapper.add(calendarPanel, BorderLayout.CENTER);

    eventsListPanel = new EventsListPanel(controller);
    wrapper.add(eventsListPanel, BorderLayout.EAST);

    return wrapper;
  }

  private void refreshActiveCalendarLabel(boolean refreshEventsDate) {
    if (titleLabel != null) {
      titleLabel.setText(controller.getActiveCalendarName());
    }
    if (timezoneLabel != null) {
      timezoneLabel.setText(buildTimezoneText());
    }
    if (calendarListPanel != null) {
      calendarListPanel.refreshCalendarList();
    }
    if (refreshEventsDate && eventsListPanel != null) {
      eventsListPanel.showEventsForDate(LocalDate.now());
    }
  }

  private void handleDayClick(LocalDate date) {
    controller.interpret("print events on " + date);
    if (eventsListPanel != null) {
      eventsListPanel.showEventsForDate(date);
    }
  }

  private String buildTimezoneText() {
    return controller.getActiveCalendarTimezoneText();
  }

}
