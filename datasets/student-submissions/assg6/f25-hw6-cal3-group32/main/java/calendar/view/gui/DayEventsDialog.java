package calendar.view.gui;

import calendar.controller.GuiController;
import calendar.model.Event;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 * Dialog showing all events on a specific day.
 * Allows creating new events or editing existing ones.
 */
public class DayEventsDialog extends JDialog {
  private static final DateTimeFormatter TIME_FMT =
      DateTimeFormatter.ofPattern("h:mm a");
  private GuiController controller;
  private LocalDate date;

  /**.*/
  public DayEventsDialog(JFrame parent, GuiController controller, LocalDate date) {
    super(parent, "Events on " + date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")), true);
    this.controller = controller;
    this.date = date;

    setSize(520, 580);
    setLocationRelativeTo(parent);

    initComponents();
  }

  private void initComponents() {
    setLayout(new BorderLayout(0, 0));
    getContentPane().setBackground(new Color(248, 249, 250));

    JPanel contentPanel = new JPanel();
    contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
    contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    contentPanel.setBackground(new Color(248, 249, 250));

    List<Event> events = controller.getEventsForDate(date);

    if (events.isEmpty()) {
      JPanel emptyPanel = new JPanel();
      emptyPanel.setBackground(new Color(248, 249, 250));
      emptyPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));

      JLabel noEvents = new JLabel("No events scheduled");
      noEvents.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
      noEvents.setForeground(new Color(107, 114, 128));
      emptyPanel.add(noEvents);

      contentPanel.add(Box.createVerticalStrut(40));
      contentPanel.add(emptyPanel);
    } else {
      for (Event e : events) {
        contentPanel.add(createEventRow(e));
        contentPanel.add(Box.createVerticalStrut(10));
      }
    }

    JScrollPane scrollPane = new JScrollPane(contentPanel);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    add(scrollPane, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20));
    buttonPanel.setBackground(Color.WHITE);
    buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));

    JButton createBtn = new JButton("+ Create Event");
    createBtn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
    createBtn.setForeground(Color.WHITE);
    createBtn.setBackground(new Color(59, 130, 246));
    createBtn.setPreferredSize(new Dimension(180, 42));
    createBtn.setFocusPainted(false);
    createBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    createBtn.setOpaque(true);
    createBtn.setBorderPainted(false);
    createBtn.addActionListener(e -> onCreateEvent());

    createBtn.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseEntered(java.awt.event.MouseEvent e) {
        createBtn.setBackground(new Color(37, 99, 235));
      }

      public void mouseExited(java.awt.event.MouseEvent e) {
        createBtn.setBackground(new Color(59, 130, 246));
      }
    });

    buttonPanel.add(createBtn);

    add(buttonPanel, BorderLayout.SOUTH);
  }

  private JPanel createEventRow(Event e) {
    JPanel row = new JPanel(new BorderLayout(15, 0));
    row.setBackground(Color.WHITE);
    row.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
        BorderFactory.createEmptyBorder(15, 18, 15, 18)
    ));
    row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));

    JPanel infoPanel = new JPanel();
    infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
    infoPanel.setBackground(Color.WHITE);

    JLabel subjectLabel = new JLabel(e.getSubject());
    subjectLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
    subjectLabel.setForeground(new Color(31, 41, 55));

    JLabel timeLabel = new JLabel(e.getStart().format(TIME_FMT) + " - "
        + e.getEnd().format(TIME_FMT));
    timeLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
    timeLabel.setForeground(new Color(107, 114, 128));
    timeLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

    infoPanel.add(subjectLabel);
    infoPanel.add(timeLabel);

    if (e.getLocation() != null && !e.getLocation().isEmpty()) {
      JLabel locLabel = new JLabel("📍 " + e.getLocation());
      locLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
      locLabel.setForeground(new Color(107, 114, 128));
      locLabel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
      infoPanel.add(locLabel);
    }

    JPanel actionPanel = new JPanel();
    actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
    actionPanel.setBackground(Color.WHITE);

    JButton editBtn = createActionButton("Edit", new Color(59, 130, 246),
        new Color(239, 246, 255));
    editBtn.addActionListener(ev -> onEditEvent(e));

    JButton deleteBtn = createActionButton("Delete", new Color(239, 68, 68),
        new Color(254, 242, 242));
    deleteBtn.addActionListener(ev -> onDeleteEvent(e));

    actionPanel.add(editBtn);
    actionPanel.add(Box.createVerticalStrut(5));
    actionPanel.add(deleteBtn);

    row.add(infoPanel, BorderLayout.CENTER);
    row.add(actionPanel, BorderLayout.EAST);

    return row;
  }

  private JButton createActionButton(String text, Color fg, Color bg) {
    JButton btn = new JButton(text);
    btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
    btn.setForeground(fg);
    btn.setBackground(bg);
    btn.setPreferredSize(new Dimension(80, 28));
    btn.setMaximumSize(new Dimension(80, 28));
    btn.setFocusPainted(false);
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btn.setOpaque(true);
    btn.setBorderPainted(false);

    Color hoverBg = new Color(
        Math.max(0, bg.getRed() - 20),
        Math.max(0, bg.getGreen() - 20),
        Math.max(0, bg.getBlue() - 20)
    );

    btn.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseEntered(java.awt.event.MouseEvent e) {
        btn.setBackground(hoverBg);
      }

      public void mouseExited(java.awt.event.MouseEvent e) {
        btn.setBackground(bg);
      }
    });

    return btn;
  }

  private void onEditEvent(Event e) {
    dispose();
    controller.onEventClicked(e);
  }

  private void onDeleteEvent(Event e) {
    dispose();
    controller.onEventDeleted(e);
  }

  private void onCreateEvent() {
    dispose();
    controller.onDayClicked(date);
  }
}