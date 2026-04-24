package calendar.view.gui;

import calendar.controller.gui.Features;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import javax.swing.border.EmptyBorder;

/**
 * Dialog for copying all events on a specific date to another calendar.
 */
class CopyEventsOnDateDialog extends JDialog {

  private final LocalDate sourceDate;
  private final Features features;
  private boolean copied = false;
  private JPanel formPanel;

  private JComboBox<String> targetCalendarCombo;
  private JSpinner targetDateSpinner;

  /**
   * Constructor for copy all events on a specific date dialogue.
   *
   * @param parent     the main view panel
   * @param sourceDate the date on which the events are copied from
   * @param features   controller with callback features
   */
  public CopyEventsOnDateDialog(JFrame parent, LocalDate sourceDate, Features features) {
    super(parent, "Copy Events on Date", true);
    this.sourceDate = sourceDate;
    this.features = features;

    setSize(400, 300);
    setLocationRelativeTo(parent);
    setLayout(new BorderLayout(10, 10));

    formPanel = new JPanel();
    formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
    formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
    initializeUi();
  }

  /**
   * Create the UI for this dialogue box.
   */
  private void initializeUi() {

    sourceDate();
    targetCalendar();
    targetDate();

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton copyButton = new JButton("Copy All");
    copyButton.addActionListener(e -> copyEvents());
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());

    buttonPanel.add(copyButton);
    buttonPanel.add(cancelButton);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  /**
   * The panel for date on which the events are copied from.
   */
  private void sourceDate() {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
    JLabel sourceLabel = new JLabel("Copying all events from: "
        + sourceDate.format(formatter));
    sourceLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
    sourceLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(sourceLabel);
    formPanel.add(Box.createRigidArea(new Dimension(0, 20)));
  }

  /**
   * The panel for the target calendar.
   */
  private void targetCalendar() {
    JLabel tarLabel = new JLabel("Target Calendar:");
    tarLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(tarLabel);

    List<String> calendars = features.getCalendarNames();
    targetCalendarCombo = new JComboBox<>(calendars.toArray(new String[0]));
    targetCalendarCombo.setMaximumSize(new Dimension(300, 25));
    targetCalendarCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(targetCalendarCombo);
    formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
  }

  /**
   * The panel for target date.
   */
  private void targetDate() {
    JLabel dateLabel = new JLabel("Target Date:");
    dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(dateLabel);

    java.util.Calendar cal = java.util.Calendar.getInstance();
    cal.setTime(java.util.Date.from(sourceDate.atStartOfDay(
        java.time.ZoneId.systemDefault()).toInstant()));

    javax.swing.SpinnerDateModel dateModel = new javax.swing.SpinnerDateModel(
        cal.getTime(), null, null, java.util.Calendar.DAY_OF_MONTH);
    targetDateSpinner = new JSpinner(dateModel);
    JSpinner.DateEditor dateEditor =
        new JSpinner.DateEditor(targetDateSpinner, "MM/dd/yyyy");
    targetDateSpinner.setEditor(dateEditor);
    targetDateSpinner.setMaximumSize(new Dimension(150, 25));
    targetDateSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(targetDateSpinner);

    add(formPanel, BorderLayout.CENTER);
  }

  /**
   * Action method for copy all the events, calls the required feature.
   */
  private void copyEvents() {
    try {
      String targetCalendar = (String) targetCalendarCombo.getSelectedItem();
      java.util.Date targetDateValue = (java.util.Date) targetDateSpinner.getValue();

      LocalDate targetDate = targetDateValue.toInstant()
          .atZone(java.time.ZoneId.systemDefault()).toLocalDate();

      Map<String, Object> parameters = new HashMap<>();
      parameters.put("target", targetCalendar);
      parameters.put("sourcedate", sourceDate);
      parameters.put("targetdate", targetDate);

      features.copyEventsOnDate(parameters);

      copied = true;
      JOptionPane.showMessageDialog(this,
          "All events copied successfully!", "Success",
          JOptionPane.INFORMATION_MESSAGE);
      dispose();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this,
          "Error copying events: " + e.getMessage(),
          "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Returns if the events were copied or not.
   *
   * @return true if the events were copied
   */
  public boolean wasCopied() {
    return copied;
  }
}