package calendar.view.gui;

import calendar.controller.GuiController;
import calendar.model.Calendar;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;



/**
 * Header panel with month navigation and calendar selector.
 */
public class HeaderPanel extends JPanel {
  private GuiController controller;
  private JLabel monthLabel;
  private JComboBox<String> calendarSelector;

  /**.*/
  public HeaderPanel(GuiController controller) {
    this.controller = controller;

    setLayout(new BorderLayout(15, 15));
    setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(229, 231, 235)),
        BorderFactory.createEmptyBorder(15, 20, 15, 20)
    ));

    initComponents();
  }

  private void initComponents() {
    JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    navPanel.setBackground(Color.WHITE);

    JButton prevBtn = createNavButton("◀");
    prevBtn.addActionListener(e -> previousMonth());

    JPanel monthBox = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    monthBox.setBackground(new Color(249, 250, 251));
    monthBox.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(229, 231, 235), 1),
        BorderFactory.createEmptyBorder(8, 20, 8, 20)
    ));
    monthBox.setPreferredSize(new Dimension(220, 42));
    monthBox.setMinimumSize(new Dimension(220, 42));
    monthBox.setMaximumSize(new Dimension(220, 42));

    monthLabel = new JLabel(formatMonth(controller.getCurrentMonth()), SwingConstants.CENTER);
    monthLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
    monthLabel.setForeground(new Color(31, 41, 55));
    monthBox.add(monthLabel);

    JButton nextBtn = createNavButton("▶");
    nextBtn.addActionListener(e -> nextMonth());

    navPanel.add(prevBtn);
    navPanel.add(monthBox);
    navPanel.add(nextBtn);

    add(navPanel, BorderLayout.WEST);

    JPanel calPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
    calPanel.setBackground(Color.WHITE);

    JLabel calLabel = new JLabel("Calendar:");
    calLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
    calLabel.setForeground(new Color(75, 85, 99));

    calendarSelector = new JComboBox<>();
    calendarSelector.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
    calendarSelector.setPreferredSize(new Dimension(150, 32));
    updateCalendarList();
    calendarSelector.addActionListener(e -> onCalendarChanged());

    JButton createCalBtn = createStyledButton("+ New Calendar");
    createCalBtn.addActionListener(e -> onCreateCalendar());

    calPanel.add(calLabel);
    calPanel.add(calendarSelector);
    calPanel.add(createCalBtn);

    add(calPanel, BorderLayout.EAST);
  }

  private JButton createNavButton(String text) {
    JButton btn = new JButton(text) {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        if (getModel().isPressed()) {
          g2.setColor(new Color(219, 234, 254));
        } else if (getModel().isRollover()) {
          g2.setColor(new Color(239, 246, 255));
        } else {
          g2.setColor(Color.WHITE);
        }

        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

        g2.setColor(new Color(229, 231, 235));
        g2.setStroke(new java.awt.BasicStroke(1));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

        g2.setColor(new Color(59, 130, 246));
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), x, y);

        g2.dispose();
      }
    };

    btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
    btn.setPreferredSize(new Dimension(50, 42));
    btn.setFocusPainted(false);
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btn.setOpaque(false);
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);

    return btn;
  }

  private JButton createStyledButton(String text) {
    JButton btn = new JButton(text);
    btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
    btn.setForeground(new Color(59, 130, 246));
    btn.setBackground(new Color(239, 246, 255));
    btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    btn.setFocusPainted(false);
    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btn.setOpaque(true);
    btn.setBorderPainted(false);

    btn.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseEntered(java.awt.event.MouseEvent e) {
        btn.setBackground(new Color(219, 234, 254));
      }

      public void mouseExited(java.awt.event.MouseEvent e) {
        btn.setBackground(new Color(239, 246, 255));
      }
    });

    return btn;
  }

  private void previousMonth() {
    LocalDate prev = controller.getCurrentMonth().minusMonths(1);
    controller.onMonthChanged(prev);
  }

  private void nextMonth() {
    LocalDate next = controller.getCurrentMonth().plusMonths(1);
    controller.onMonthChanged(next);
  }

  private void onCalendarChanged() {
    String selected = (String) calendarSelector.getSelectedItem();
    if (selected != null) {
      controller.onCalendarChanged(selected);
    }
  }

  private void onCreateCalendar() {
    String name = JOptionPane.showInputDialog(this,
        "Enter calendar name:", "New Calendar", JOptionPane.QUESTION_MESSAGE);
    if (name != null && !name.trim().isEmpty()) {
      ZoneId tz = ZoneId.systemDefault();
      controller.onCreateCalendar(name, tz);
    }
  }

  /**.*/
  public void updateTitle() {
    monthLabel.setText(formatMonth(controller.getCurrentMonth()));
  }

  /**.*/
  public void updateCalendarList() {
    calendarSelector.removeAllItems();
    for (Calendar cal : controller.getAllCalendars()) {
      calendarSelector.addItem(cal.getName());
    }
    calendarSelector.setSelectedItem(controller.getCurrentCalendar().getName());
  }

  private String formatMonth(LocalDate date) {
    return date.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
  }
}