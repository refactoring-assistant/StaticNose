package calendar.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

/**
 * A custom dialog for displaying error messages to the user.
 * This dialog features a standard error icon, a scrollable text area for detailed error messages,
 * and an "OK" button to close the dialog.
 */
public class ErrorDialog extends JDialog {

  /**
   * Constructs a new ErrorDialog.
   *
   * @param owner        the parent frame of this dialog
   * @param errorMessage the error message string to display
   */
  public ErrorDialog(Frame owner, String errorMessage) {
    super(owner, "Error", true);
    this.setSize(400, 200);
    this.setLayout(new BorderLayout(10, 10));

    JPanel messagePanel = new JPanel(new BorderLayout(10, 10));
    messagePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 0, 15));

    JLabel iconLabel = new JLabel(UIManager.getIcon("OptionPane.errorIcon"));
    messagePanel.add(iconLabel, BorderLayout.WEST);

    JTextArea errorText = new JTextArea(errorMessage);
    errorText.setWrapStyleWord(true);
    errorText.setLineWrap(true);
    errorText.setEditable(false);
    errorText.setOpaque(false);
    errorText.setFont(new Font("Arial", Font.PLAIN, 13));

    messagePanel.add(new JScrollPane(errorText), BorderLayout.CENTER);

    this.add(messagePanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    JButton okButton = new JButton("OK");

    buttonPanel.add(okButton);
    this.add(buttonPanel, BorderLayout.SOUTH);

    okButton.addActionListener(e -> this.setVisible(false));
    this.setLocationRelativeTo(owner);
  }
}