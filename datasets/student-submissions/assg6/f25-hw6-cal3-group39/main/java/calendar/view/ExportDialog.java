package calendar.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A dialog for exporting calendar data to a file.
 * This modal dialog prompts the user to enter a filename and select an export format (CSV or iCal).
 * It provides "Export" and "Cancel" buttons to confirm or discard the action.
 */
public class ExportDialog extends JDialog {

  private final JTextField filenameField;
  private final JComboBox<String> formatDropdown;
  private final JButton exportButton;
  private final JButton cancelButton;

  /**
   * Constructs a new ExportDialog.
   * Initializes the GUI components, including input fields for the filename and format selection,
   * and sets up the layout and event listeners for the buttons.
   *
   * @param owner the Frame from which the dialog is displayed
   */
  public ExportDialog(Frame owner) {
    super(owner, "Export Calendar", true);
    this.setSize(350, 200);
    this.setLayout(new BorderLayout(10, 10));

    JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
    formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    formPanel.add(new JLabel("File Name:"));
    filenameField = new JTextField("calendar_export");
    formPanel.add(filenameField);

    formPanel.add(new JLabel("Format:"));
    formatDropdown = new JComboBox<>(new String[] {".csv", ".ical"});
    formPanel.add(formatDropdown);

    this.add(formPanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    exportButton = new JButton("Export");
    cancelButton = new JButton("Cancel");

    buttonPanel.add(exportButton);
    buttonPanel.add(cancelButton);
    this.add(buttonPanel, BorderLayout.SOUTH);

    cancelButton.addActionListener(e -> this.setVisible(false));
    this.setLocationRelativeTo(owner);
  }

  /**
   * Retrieves the filename entered by the user.
   *
   * @return the filename string, trimmed of whitespace
   */
  public String getFilename() {
    return filenameField.getText().trim();
  }

  /**
   * Retrieves the selected export format.
   *
   * @return the selected format string (e.g., ".csv", ".ical")
   */
  public String getFormat() {
    return (String) formatDropdown.getSelectedItem();
  }

  /**
   * Adds an ActionListener to the "Export" button.
   *
   * @param listener the ActionListener to be added
   */
  public void addExportListener(ActionListener listener) {
    exportButton.addActionListener(listener);
  }
}