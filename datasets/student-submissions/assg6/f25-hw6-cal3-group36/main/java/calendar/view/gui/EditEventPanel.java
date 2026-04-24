package calendar.view.gui;

import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Panel to collect edit parameters.
 */
public class EditEventPanel extends JPanel {

  private final JTextField subjectField;
  private final JTextField startIsoField;
  private final JComboBox<String> propertyBox;
  private final JTextField newValueField;
  private final JComboBox<String> scopeBox;

  /**
   * Constructs the panel for editing.
   */
  public EditEventPanel() {
    setLayout(new GridLayout(0, 2, 6, 6));
    setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    subjectField = new JTextField();
    startIsoField = new JTextField("2025-01-06T09:00:00Z");
    propertyBox = new JComboBox<>(new String[] {"subject", "start",
        "end", "description", "location", "status"});
    newValueField = new JTextField();
    scopeBox = new JComboBox<>(new String[] {"single", "from", "series"});

    add(new JLabel("Subject"));
    add(subjectField);
    add(new JLabel("Original Start (ISO-8601 UTC)"));
    add(startIsoField);
    add(new JLabel("Property"));
    add(propertyBox);
    add(new JLabel("New Value"));
    add(newValueField);
    add(new JLabel("Scope"));
    add(scopeBox);
  }

  /**
   * Subject accessor.
   *
   * @return subject
   */
  public String getSubject() {
    return subjectField.getText();
  }

  /**
   * Start accessor.
   *
   * @return start text
   */
  public String getOriginalStartIso() {
    return startIsoField.getText();
  }

  /**
   * Property accessor.
   *
   * @return property name
   */
  public String getProperty() {
    return propertyBox.getSelectedItem().toString();
  }

  /**
   * New value accessor.
   *
   * @return new value
   */
  public String getNewValue() {
    return newValueField.getText();
  }

  /**
   * Scope accessor.
   *
   * @return scope
   */
  public String getScope() {
    return scopeBox.getSelectedItem().toString();
  }
}