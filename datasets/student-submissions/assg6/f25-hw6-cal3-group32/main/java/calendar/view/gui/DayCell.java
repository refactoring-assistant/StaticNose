package calendar.view.gui;

import calendar.controller.GuiController;
import calendar.model.Event;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Individual day cell displaying date and events.
 */
public class DayCell extends JPanel {
  private static final int MAX_EVENTS_SHOWN = 3;
  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a");

  private GuiController controller;
  private LocalDate date;
  private JLabel dayLabel;
  private JPanel eventsPanel;
  private boolean isHovered = false;
  private boolean isToday = false;

  /**.*/
  public DayCell(GuiController controller) {
    this.controller = controller;

    setLayout(new BorderLayout(5, 5));
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));
    setPreferredSize(new Dimension(130, 110));

    dayLabel = new JLabel();
    dayLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
    dayLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 0));
    add(dayLabel, BorderLayout.NORTH);

    eventsPanel = new JPanel();
    eventsPanel.setLayout(new BoxLayout(eventsPanel, BoxLayout.Y_AXIS));
    eventsPanel.setBackground(Color.WHITE);
    eventsPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 5, 5));
    add(eventsPanel, BorderLayout.CENTER);

    addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if (date != null) {
          showDayEventsDialog();
        }
      }

      public void mouseEntered(MouseEvent e) {
        isHovered = true;
        setBackground(new Color(249, 250, 251));
        eventsPanel.setBackground(new Color(249, 250, 251));
        repaint();
      }

      public void mouseExited(MouseEvent e) {
        isHovered = false;
        setBackground(Color.WHITE);
        eventsPanel.setBackground(Color.WHITE);
        repaint();
      }
    });

    setCursor(new Cursor(Cursor.HAND_CURSOR));
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    if (isToday) {
      Graphics2D g2 = (Graphics2D) g;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(new Color(59, 130, 246, 40));
      g2.fillRect(0, 0, getWidth(), getHeight());
      g2.setColor(new Color(37, 99, 235));
      g2.setStroke(new java.awt.BasicStroke(3));
      g2.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
    }
  }

  private void showDayEventsDialog() {
    DayEventsDialog dialog = new DayEventsDialog(
        (javax.swing.JFrame) javax.swing.SwingUtilities.getWindowAncestor(this),
        controller,
        date
    );
    dialog.setVisible(true);
  }

  /**.*/
  public void setDate(LocalDate date, List<Event> events, boolean isCurrentMonth, boolean isToday) {
    this.date = date;
    this.isToday = isToday;

    dayLabel.setText(String.valueOf(date.getDayOfMonth()));

    if (isToday) {
      dayLabel.setForeground(new Color(59, 130, 246));
    } else if (isCurrentMonth) {
      dayLabel.setForeground(new Color(31, 41, 55));
    } else {
      dayLabel.setForeground(new Color(156, 163, 175));
    }

    eventsPanel.removeAll();

    if (events != null && !events.isEmpty()) {
      int shown = Math.min(events.size(), MAX_EVENTS_SHOWN);
      for (int i = 0; i < shown; i++) {
        eventsPanel.add(createEventLabel(events.get(i)));
      }

      if (events.size() > MAX_EVENTS_SHOWN) {
        int more = events.size() - MAX_EVENTS_SHOWN;
        JLabel moreLabel = new JLabel("+" + more + " more");
        moreLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 9));
        moreLabel.setForeground(new Color(107, 114, 128));
        moreLabel.setBorder(BorderFactory.createEmptyBorder(3, 5, 0, 0));
        eventsPanel.add(moreLabel);
      }
    }

    revalidate();
    repaint();
  }

  private JLabel createEventLabel(Event e) {
    String time = e.getStart().format(TIME_FMT);
    String subj = e.getSubject();
    if (subj.length() > 12) {
      subj = subj.substring(0, 10) + "...";
    }

    JLabel label = new JLabel("● " + time + " " + subj) {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        if (getCursor().getType() == Cursor.HAND_CURSOR) {
          g2.setColor(new Color(219, 234, 254));
          g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
        }

        super.paintComponent(g);
        g2.dispose();
      }
    };

    label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
    label.setForeground(new Color(59, 130, 246));
    label.setBorder(BorderFactory.createEmptyBorder(3, 6, 3, 6));
    label.setCursor(new Cursor(Cursor.HAND_CURSOR));

    label.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent evt) {
        evt.consume();
        controller.onEventClicked(e);
      }

      public void mouseEntered(MouseEvent evt) {
        label.setForeground(new Color(37, 99, 235));
        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        label.repaint();
      }

      public void mouseExited(MouseEvent evt) {
        label.setForeground(new Color(59, 130, 246));
        label.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
        label.repaint();
      }
    });

    return label;
  }
}