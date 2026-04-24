package calendar.view.gui;

import calendar.controller.gui.Features;
import calendar.model.event.CalendarEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.border.EmptyBorder;

/**
 * Dialog for copying an event to another calendar.
 */
class CopyEventDialog extends JDialog {

  private final JPanel formPanel;
  private final CalendarEvent event;
  private final Features features;
  private boolean copied = false;

  private JComboBox<String> targetCalendarCombo;
  private JSpinner targetDateSpinner;
  private JSpinner targetTimeSpinner;

  /**
   * Constructor for copy event dialogue.
   *
   * @param parent   the main view panel
   * @param event    the event to be copied
   * @param features controller with callback features
   */
  public CopyEventDialog(JFrame parent, CalendarEvent event, Features features) {
    super(parent, "Copy Event", true);
    this.event = event;
    this.features = features;
    setLayout(new BorderLayout(10, 10));
    this.formPanel = new JPanel();

    setSize(400, 300);
    setLocationRelativeTo(parent);
    initializeUi();
  }

  /**
   * Create the UI for this dialogue box.
   */
  private void initializeUi() {
    formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
    formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

    JLabel eventLabel = new JLabel("Copying: " + event.getSubject());
    eventLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
    eventLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(eventLabel);
    formPanel.add(Box.createRigidArea(new Dimension(0, 20)));

    targetCalendar();
    targetDate();
    targetTime();

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton copyButton = new JButton("Copy");
    copyButton.addActionListener(e -> copyEvent());
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());

    buttonPanel.add(copyButton);
    buttonPanel.add(cancelButton);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  /**
   * Create target calendar pane.
   */
  private void targetCalendar() {
    JLabel calLabel = new JLabel("Target Calendar:");
    calLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(calLabel);

    List<String> calendars = features.getCalendarNames();
    targetCalendarCombo = new JComboBox<>(calendars.toArray(new String[0]));
    targetCalendarCombo.setMaximumSize(new Dimension(300, 25));
    targetCalendarCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(targetCalendarCombo);
    formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
  }

  /**
   * Create target date pane.
   */
  private void targetDate() {
    JLabel dateLabel = new JLabel("Target Date:");
    dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(dateLabel);

    targetDateSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(
        targetDateSpinner, "MM/dd/yyyy");
    targetDateSpinner.setEditor(dateEditor);
    targetDateSpinner.setMaximumSize(new Dimension(150, 25));
    targetDateSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(targetDateSpinner);
    formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
  }

  /**
   * Create target time pane.
   */
  private void targetTime() {
    JLabel timeLabel = new JLabel("Target Time:");
    timeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(timeLabel);

    targetTimeSpinner = new JSpinner(new SpinnerDateModel());
    JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(
        targetTimeSpinner, "HH:mm");
    targetTimeSpinner.setEditor(timeEditor);
    targetTimeSpinner.setMaximumSize(new Dimension(100, 25));
    targetTimeSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(targetTimeSpinner);

    add(formPanel, BorderLayout.CENTER);
  }

  /**
   * Action method for copy this event, calls the required feature.
   */
  private void copyEvent() {
    try {
      String targetCalendar = (String) targetCalendarCombo.getSelectedItem();
      Date targetDate = (Date) targetDateSpinner.getValue();
      Date targetTime = (Date) targetTimeSpinner.getValue();

      LocalDate date = targetDate.toInstant()
          .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
      java.time.LocalTime time = targetTime.toInstant()
          .atZone(java.time.ZoneId.systemDefault()).toLocalTime();

      ZonedDateTime targetDateTime = ZonedDateTime.of(date, time,
          java.time.ZoneId.of(features.getCurrentCalendarTimezone()));

      Map<String, Object> parameters = new HashMap<>();
      parameters.put("subject", event.getSubject());
      parameters.put("start", event.getStartDateTime());
      parameters.put("target", targetCalendar);
      parameters.put("targetstart", targetDateTime);
      features.copyEvent(parameters);

      copied = true;
      JOptionPane.showMessageDialog(this,
          "Event copied successfully!", "Success",
          JOptionPane.INFORMATION_MESSAGE);
      dispose();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this,
          "Error copying event: " + e.getMessage(),
          "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Returns if the event was copied or not.
   *
   * @return true if event was copied
   */
  public boolean wasCopied() {
    return copied;
  }
}
