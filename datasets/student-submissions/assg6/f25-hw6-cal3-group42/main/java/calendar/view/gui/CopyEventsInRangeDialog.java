package calendar.view.gui;

import calendar.controller.gui.Features;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
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
 * Dialog for copying all events between two dates to another calendar.
 */
class CopyEventsInRangeDialog extends JDialog {

  private final Features features;
  private boolean copied = false;
  private JPanel formPanel;

  private JSpinner startDateSpinner;
  private JSpinner endDateSpinner;
  private JComboBox<String> targetCalendarCombo;
  private JSpinner targetDateSpinner;

  /**
   * Constructor for copy all events on a specific date dialogue.
   *
   * @param parent   the main view panel
   * @param features controller with callback features
   */
  public CopyEventsInRangeDialog(JFrame parent, Features features) {
    super(parent, "Copy Events Between Dates", true);
    this.features = features;

    setSize(450, 400);
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
    JLabel titleLabel = new JLabel("Copy events from date range");
    titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
    titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(titleLabel);
    formPanel.add(Box.createRigidArea(new Dimension(0, 15)));

    startDate();
    endDate();
    targetCalendar();
    targetStart();

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton copyButton = new JButton("Copy Events");
    copyButton.addActionListener(e -> copyEvents());
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(e -> dispose());

    buttonPanel.add(copyButton);
    buttonPanel.add(cancelButton);
    add(buttonPanel, BorderLayout.SOUTH);
  }

  /**
   * The panel for the date from where event list starts.
   */
  private void startDate() {
    JLabel startLabel = new JLabel("Start Date:");
    startLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(startLabel);

    javax.swing.SpinnerDateModel startDateModel = new javax.swing.SpinnerDateModel(
        new java.util.Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
    startDateSpinner = new JSpinner(startDateModel);
    JSpinner.DateEditor startDateEditor =
        new JSpinner.DateEditor(startDateSpinner, "MM/dd/yyyy");
    startDateSpinner.setEditor(startDateEditor);
    startDateSpinner.setMaximumSize(new Dimension(150, 25));
    startDateSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(startDateSpinner);
    formPanel.add(Box.createRigidArea(new Dimension(0, 10)));
  }

  /**
   * The panel for the date from where event list ends.
   */
  private void endDate() {
    JLabel endLabel = new JLabel("End Date:");
    endLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(endLabel);

    javax.swing.SpinnerDateModel endDateModel = new javax.swing.SpinnerDateModel(
        new java.util.Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
    endDateSpinner = new JSpinner(endDateModel);
    JSpinner.DateEditor endDateEditor =
        new JSpinner.DateEditor(endDateSpinner, "MM/dd/yyyy");
    endDateSpinner.setEditor(endDateEditor);
    endDateSpinner.setMaximumSize(new Dimension(150, 25));
    endDateSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(endDateSpinner);
    formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
  }

  /**
   * The panel for the target calendar.
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
   * The panel for the target start date.
   */
  private void targetStart() {
    JLabel targetLabel = new JLabel("Target Start Date:");
    targetLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(targetLabel);

    javax.swing.SpinnerDateModel targetDateModel = new javax.swing.SpinnerDateModel(
        new java.util.Date(), null, null, java.util.Calendar.DAY_OF_MONTH);
    targetDateSpinner = new JSpinner(targetDateModel);
    JSpinner.DateEditor targetDateEditor =
        new JSpinner.DateEditor(targetDateSpinner, "MM/dd/yyyy");
    targetDateSpinner.setEditor(targetDateEditor);
    targetDateSpinner.setMaximumSize(new Dimension(150, 25));
    targetDateSpinner.setAlignmentX(Component.LEFT_ALIGNMENT);
    formPanel.add(targetDateSpinner);

    add(formPanel, BorderLayout.CENTER);
  }

  /**
   * Action method for copy all the events between two dates, calls the required feature.
   */
  private void copyEvents() {
    try {
      java.util.Date startDateValue = (java.util.Date) startDateSpinner.getValue();
      java.util.Date endDateValue = (java.util.Date) endDateSpinner.getValue();

      LocalDate startDate = startDateValue.toInstant()
          .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
      LocalDate endDate = endDateValue.toInstant()
          .atZone(java.time.ZoneId.systemDefault()).toLocalDate();

      if (endDate.isBefore(startDate)) {
        JOptionPane.showMessageDialog(this,
            "End date must be after start date", "Validation Error",
            JOptionPane.ERROR_MESSAGE);
        return;
      }

      String targetCalendar = (String) targetCalendarCombo.getSelectedItem();
      Map<String, Object> parameters = new HashMap<>();
      parameters.put("startdate", startDate);
      parameters.put("enddate", endDate);
      parameters.put("target", targetCalendar);
      java.util.Date targetDateValue = (java.util.Date) targetDateSpinner.getValue();
      LocalDate targetDate = targetDateValue.toInstant()
          .atZone(java.time.ZoneId.systemDefault()).toLocalDate();
      parameters.put("targetdate", targetDate);
      features.copyEventsInRange(parameters);

      copied = true;
      JOptionPane.showMessageDialog(this,
          "Events copied successfully!", "Success",
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