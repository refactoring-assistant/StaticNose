package calendar.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A dialog for checking user availability at a specific date and time.
 * This dialog provides input fields for the date and time, along with a "Check" button to trigger
 * the availability check. It also displays a status label to inform the user whether they are busy
 * or available at the specified time.
 */
public class AvailabilityDialog extends JDialog {

  private final JTextField dateField;
  private final JTextField timeField;
  private final JLabel statusLabel;
  private final JButton checkButton;
  private final JButton closeButton;

  /**
   * Constructs a new AvailabilityDialog.
   *
   * @param owner the parent frame of this dialog
   */
  public AvailabilityDialog(Frame owner) {
    super(owner, "Check Availability", false);
    this.setSize(350, 250);
    this.setLayout(new BorderLayout(10, 10));

    JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
    formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    formPanel.add(new JLabel("Date (YYYY-MM-DD):"));
    dateField = new JTextField(LocalDate.now().toString());
    formPanel.add(dateField);

    formPanel.add(new JLabel("Time (HH:MM):"));
    timeField = new JTextField("12:00");
    formPanel.add(timeField);

    formPanel.add(new JLabel("Status:"));
    statusLabel = new JLabel("Enter time and click Check");
    statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
    statusLabel.setForeground(Color.GRAY);
    formPanel.add(statusLabel);

    this.add(formPanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    checkButton = new JButton("Check");
    closeButton = new JButton("Close");

    buttonPanel.add(checkButton);
    buttonPanel.add(closeButton);
    this.add(buttonPanel, BorderLayout.SOUTH);

    closeButton.addActionListener(e -> this.setVisible(false));
    this.setLocationRelativeTo(owner);
  }

  /**
   * Adds an ActionListener to the "Check" button.
   *
   * @param listener the ActionListener to be added
   */
  public void addCheckListener(ActionListener listener) {
    checkButton.addActionListener(listener);
  }

  /**
   * Retrieves the text entered in the date field.
   *
   * @return the date string
   */
  public String getDateText() {
    return dateField.getText();
  }

  /**
   * Retrieves the text entered in the time field.
   *
   * @return the time string
   */
  public String getTimeText() {
    return timeField.getText();
  }

  /**
   * Updates the status label to reflect the availability result.
   *
   * @param isBusy true if the user is busy at the specified time, false otherwise
   */
  public void setStatusResult(boolean isBusy) {
    if (isBusy) {
      statusLabel.setText("BUSY");
      statusLabel.setForeground(Color.RED);
    } else {
      statusLabel.setText("AVAILABLE");
      statusLabel.setForeground(new Color(0, 150, 0));
    }
  }

  /**
   * Updates the status label to display an error message.
   *
   * @param message the error message to display
   */
  public void setStatusError(String message) {
    statusLabel.setText(message);
    statusLabel.setForeground(Color.ORANGE);
  }
}