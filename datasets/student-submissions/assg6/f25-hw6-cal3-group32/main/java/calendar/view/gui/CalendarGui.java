package calendar.view.gui;

import calendar.controller.GuiController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * Main Gui frame for calendar application.
 * Contains header, month view, and action panels.
 */
public class CalendarGui extends JFrame {
  private GuiController controller;
  private HeaderPanel headerPanel;
  private MonthViewPanel monthViewPanel;
  private JPanel actionPanel;

  /**..*/
  public CalendarGui(GuiController controller) {
    this.controller = controller;

    setLookAndFeel();
    setTitle("Calendar Application");
    setSize(1100, 850);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setLocationRelativeTo(null);

    initComponents();
  }

  private void setLookAndFeel() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      //yes
    }
  }

  private void initComponents() {
    setLayout(new BorderLayout(0, 0));
    getContentPane().setBackground(new Color(248, 249, 250));

    headerPanel = new HeaderPanel(controller);
    add(headerPanel, BorderLayout.NORTH);

    monthViewPanel = new MonthViewPanel(controller);
    add(monthViewPanel, BorderLayout.CENTER);

    actionPanel = createActionPanel();
    add(actionPanel, BorderLayout.SOUTH);
  }

  private JPanel createActionPanel() {
    JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
    panel.setBackground(new Color(248, 249, 250));
    panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

    JButton createEventBtn = createStyledButton("Create Event", new Color(16, 185, 129));
    createEventBtn.addActionListener(e -> onCreateEventClick());

    JButton exportcsvBtn = createStyledButton("Export CSV", new Color(139, 92, 246)); 
    exportcsvBtn.addActionListener(e -> onExportCsvClick());

    JButton exportIcalBtn = createStyledButton("Export iCal", new Color(236, 72, 153));  
    exportIcalBtn.addActionListener(e -> onExportIcalClick());

    panel.add(createEventBtn);
    panel.add(exportcsvBtn);
    panel.add(exportIcalBtn);

    return panel;
  }

  private JButton createStyledButton(String text, Color bgColor) {
    JButton btn = new JButton(text) {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        if (getModel().isPressed()) {
          g2.setColor(bgColor.darker().darker());
        } else if (getModel().isRollover()) {
          g2.setColor(bgColor.darker());
        } else {
          g2.setColor(bgColor);
        }

        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

        g2.setColor(getForeground());
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(getText())) / 2;
        int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(getText(), x, y);
        g2.dispose();
      }
    };
    btn.setForeground(Color.WHITE);
    btn.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
    btn.setFocusPainted(false);
    btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
    btn.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    btn.setOpaque(false);
    btn.setContentAreaFilled(false);
    btn.setBorderPainted(false);
    return btn;
  }

  private void onCreateEventClick() {
    controller.onDayClicked(java.time.LocalDate.now());
  }

  private void onExportCsvClick() {
    String filename = JOptionPane.showInputDialog(this,
        "Enter filename:", "Export CSV", JOptionPane.QUESTION_MESSAGE);
    if (filename != null && !filename.trim().isEmpty()) {
      if (!filename.endsWith(".csv")) {
        filename += ".csv";
      }
      controller.onExportCsv(filename);
    }
  }


  private void onExportIcalClick() {
    String filename = JOptionPane.showInputDialog(this,
        "Enter filename:", "Export iCal", JOptionPane.QUESTION_MESSAGE);
    if (filename != null && !filename.trim().isEmpty()) {
      if (!filename.endsWith(".ical")) {
        filename += ".ical";
      }
      controller.onExportIcal(filename);
    }
  }

  /**This is Lorem Ipsum javadoc for the for in sunrise to have it inside me so it.*/
  public void refreshMonth() {
    monthViewPanel.refresh();
    headerPanel.updateTitle();
  }

  /**..*/
  public void updateCalendarList() {
    headerPanel.updateCalendarList();
  }

  /**..*/
  public void showErrorDialog(String message) {
    JOptionPane.showMessageDialog(this, message, "Error",
        JOptionPane.ERROR_MESSAGE);
  }

  /**..*/
  public void showSuccessDialog(String message) {
    JOptionPane.showMessageDialog(this, message, "Success",
        JOptionPane.INFORMATION_MESSAGE);
  }
}