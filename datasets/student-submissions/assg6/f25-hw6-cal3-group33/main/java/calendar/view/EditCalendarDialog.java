package calendar.view;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * Dialog for editing calendar properties.
 * Pre-fills form with current calendar data.
 */
public class EditCalendarDialog extends JDialog {

  /**
   * Predefined calendar colors (same as CreateCalendarDialog).
   */
  private static final Color[] CALENDAR_COLORS = {
      new Color(66, 133, 244),   // Blue
      new Color(15, 157, 88),    // Green
      new Color(251, 188, 4),    // Orange
      new Color(234, 67, 53),    // Red
      new Color(171, 71, 188),   // Purple
      new Color(244, 81, 30),    // Deep Orange
      new Color(142, 36, 170),   // Deep Purple
      new Color(97, 97, 97)      // Gray
  };

  /**
   * Color names for tooltips.
   */
  private static final String[] COLOR_NAMES = {
      "Blue", "Green", "Orange", "Red",
      "Purple", "Deep Orange", "Deep Purple", "Gray"
  };

  private JTextField calendarNameField;
  private JComboBox<String> timezoneCombo;
  private JRadioButton[] colorRadioButtons;
  private ButtonGroup colorButtonGroup;

  private JButton saveButton;
  private JButton cancelButton;

  private String calendarName;

  private boolean confirmed = false;
  private CalendarEditData editData;

  /**
   * Constructs an EditCalendarDialog.
   *
   * @param parent          the parent frame
   * @param calendarName    the name of the calendar being edited
   * @param currentTimezone current timezone
   * @param currentColor    current color
   */
  public EditCalendarDialog(JFrame parent, String calendarName,
                            String currentTimezone, Color currentColor) {
    super(parent, "Edit Calendar Properties", true);

    this.calendarName = calendarName;

    setSize(450, 400);
    setMinimumSize(new Dimension(400, 350));
    setLocationRelativeTo(parent);
    setLayout(new BorderLayout());

    initializeComponents(calendarName, currentTimezone, currentColor);
  }

  /**
   * Initializes all dialog components.
   */
  private void initializeComponents(String currentName, String currentTimezone,
                                    Color currentColor) {
    JPanel formPanel = createFormPanel(currentName, currentTimezone, currentColor);
    add(formPanel, BorderLayout.CENTER);

    JPanel footerPanel = createFooterPanel();
    add(footerPanel, BorderLayout.SOUTH);
  }

  /**
   * Creates the main form panel with pre-filled input fields.
   */
  private JPanel createFormPanel(String currentName, String currentTimezone,
                                 Color currentColor) {
    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBackground(Color.WHITE);
    formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.insets = new Insets(8, 5, 8, 5);
    gbc.weightx = 1.0;

    int row = 0;

    addFieldLabel(formPanel, gbc, row++, "Calendar Name: *");
    calendarNameField = addTextField(formPanel, gbc, row++, currentName);

    addFieldLabel(formPanel, gbc, row++, "Timezone: *");
    gbc.gridy = row++;
    gbc.gridwidth = 2;
    timezoneCombo = new JComboBox<>(TimezoneUtils.getPopularTimezones());
    timezoneCombo.setFont(new Font("SansSerif", Font.PLAIN, 13));
    timezoneCombo.setPreferredSize(new Dimension(0, 30));
    timezoneCombo.setSelectedItem(currentTimezone);
    formPanel.add(timezoneCombo, gbc);

    addFieldLabel(formPanel, gbc, row++, "Color: *");
    gbc.gridy = row++;
    gbc.gridwidth = 2;
    JPanel colorPanel = createColorSelectionPanel(currentColor);
    formPanel.add(colorPanel, gbc);

    return formPanel;
  }

  /**
   * Adds a field label to the form.
   */
  private void addFieldLabel(JPanel panel, GridBagConstraints gbc, int row, String text) {
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.gridwidth = 1;
    JLabel label = new JLabel(text);
    label.setFont(new Font("SansSerif", Font.BOLD, 13));
    panel.add(label, gbc);
  }

  /**
   * Adds a text field to the form with initial value.
   */
  private JTextField addTextField(JPanel panel, GridBagConstraints gbc, int row,
                                  String initialValue) {
    gbc.gridy = row;
    gbc.gridwidth = 2;
    JTextField field = new JTextField(initialValue);
    field.setFont(new Font("SansSerif", Font.PLAIN, 13));
    field.setPreferredSize(new Dimension(0, 30));
    panel.add(field, gbc);
    return field;
  }

  /**
   * Creates the color selection panel with pre-selected color.
   */
  private JPanel createColorSelectionPanel(Color currentColor) {
    JPanel colorPanel = new JPanel(new GridBagLayout());
    colorPanel.setBackground(Color.WHITE);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);

    colorButtonGroup = new ButtonGroup();
    colorRadioButtons = new JRadioButton[CALENDAR_COLORS.length];

    int selectedIndex = 0;
    for (int i = 0; i < CALENDAR_COLORS.length; i++) {
      if (CALENDAR_COLORS[i].equals(currentColor)) {
        selectedIndex = i;
        break;
      }
    }

    for (int i = 0; i < CALENDAR_COLORS.length; i++) {
      JRadioButton radioButton = new JRadioButton();
      radioButton.setBackground(Color.WHITE);
      radioButton.setFocusPainted(false);
      radioButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

      radioButton.setIcon(createColorIcon(CALENDAR_COLORS[i], false));
      radioButton.setSelectedIcon(createColorIcon(CALENDAR_COLORS[i], true));
      radioButton.setToolTipText(COLOR_NAMES[i]);

      colorButtonGroup.add(radioButton);
      colorRadioButtons[i] = radioButton;

      gbc.gridx = i % 4;
      gbc.gridy = i / 4;
      colorPanel.add(radioButton, gbc);
    }

    colorRadioButtons[selectedIndex].setSelected(true);

    return colorPanel;
  }

  /**
   * Creates a colored circle icon for radio button.
   */
  private Icon createColorIcon(Color color, boolean selected) {
    return new Icon() {
      @Override
      public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(color);
        g2.fillOval(x, y, 30, 30);

        if (selected) {
          g2.setColor(Color.BLACK);
          g2.setStroke(new BasicStroke(3.0f));
        } else {
          g2.setColor(Color.GRAY);
          g2.setStroke(new BasicStroke(1.0f));
        }
        g2.drawOval(x, y, 30, 30);

        g2.dispose();
      }

      @Override
      public int getIconWidth() {
        return 32;
      }

      @Override
      public int getIconHeight() {
        return 32;
      }
    };
  }

  /**
   * Creates the footer panel with action buttons.
   */
  private JPanel createFooterPanel() {
    JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
    footerPanel.setBackground(Color.WHITE);
    footerPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

    cancelButton = new JButton("Cancel");
    cancelButton.setFont(new Font("SansSerif", Font.PLAIN, 13));
    cancelButton.setFocusPainted(false);
    cancelButton.setOpaque(true);
    cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    cancelButton.setPreferredSize(new Dimension(90, 35));
    cancelButton.addActionListener(e -> {
      confirmed = false;
      dispose();
    });

    saveButton = new JButton("Save Changes");
    saveButton.setFont(new Font("SansSerif", Font.BOLD, 13));
    saveButton.setBackground(new Color(66, 133, 244));
    saveButton.setForeground(Color.WHITE);
    saveButton.setOpaque(true);
    saveButton.setFocusPainted(false);
    saveButton.setBorderPainted(false);
    saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    saveButton.setPreferredSize(new Dimension(130, 35));
    saveButton.addActionListener(e -> handleSave());

    footerPanel.add(cancelButton);
    footerPanel.add(saveButton);

    return footerPanel;
  }

  /**
   * Handles the save button click.
   * Validates all inputs and creates CalendarEditData if valid.
   */
  private void handleSave() {
    try {
      String name = calendarNameField.getText().trim();
      if (name.isEmpty()) {
        showError("Calendar name is required.");
        calendarNameField.requestFocus();
        return;
      }

      String timezone = (String) timezoneCombo.getSelectedItem();

      if (getSelectedColor() == null) {
        showError("Please select a color.");
        return;
      }

      editData = new CalendarEditData(calendarName, name, timezone);

      confirmed = true;
      dispose();

    } catch (Exception ex) {
      showError("Error saving calendar: " + ex.getMessage());
      ex.printStackTrace();
    }
  }

  /**
   * Shows an error message dialog.
   */
  private void showError(String message) {
    JOptionPane.showMessageDialog(this, message, "Validation Error",
        JOptionPane.ERROR_MESSAGE);
  }

  /**
   * Checks if the dialog was confirmed.
   *
   * @return true if Save button clicked, false if cancelled
   */
  public boolean wasConfirmed() {
    return confirmed;
  }

  /**
   * Gets the edited calendar data.
   *
   * @return CalendarEditData if confirmed, null otherwise
   */
  public CalendarEditData getEditData() {
    return editData;
  }

  /**
   * Gets the selected color from the dialog.
   * Should be called after wasConfirmed() returns true.
   *
   * @return the selected Color, or null if none selected
   */
  public Color getSelectedColor() {
    for (int i = 0; i < colorRadioButtons.length; i++) {
      if (colorRadioButtons[i].isSelected()) {
        return CALENDAR_COLORS[i];
      }
    }
    return null;
  }

  /**
   * Data class holding calendar edit data for Model update.
   * Color is NOT included - it's managed by the View layer.
   */
  public static class CalendarEditData {
    private final String originalName;
    private final String name;
    private final String timezone;

    /**
     * Initializes name and timezone.
     */
    public CalendarEditData(String originalName, String name, String timezone) {
      this.originalName = originalName;
      this.name = name;
      this.timezone = timezone;
    }

    /**
     * Returns original name before edit.
     */
    public String getOriginalName() {
      return originalName;
    }

    /**
     * Returns name.
     */
    public String getName() {
      return name;
    }

    /**
     * Returns time zone.
     */
    public String getTimezone() {
      return timezone;
    }
  }
}