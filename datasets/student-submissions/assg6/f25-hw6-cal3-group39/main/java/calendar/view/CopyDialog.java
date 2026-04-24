package calendar.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * The CopyDialog class opens up a dialog to facilitate the functionality of copying events.
 */
public class CopyDialog extends JDialog {

  // --- Mode Selection ---
  private final JRadioButton selectedEventRadio;
  private final JRadioButton rangeRadio;
  private final JRadioButton allEventsOnDateRadio;

  // --- Common Inputs ---
  private final JComboBox<String> targetCalendarDropdown;
  private final JTextField targetDateField; // YYYY-MM-DD
  private final JTextField targetTimeField; // HH:MM (NEW)

  // --- Range Inputs (Only visible in Range mode) ---
  private final JTextField startDateField;
  private final JTextField endDateField;
  private final JTextField sourceDateField;

  // --- Buttons ---
  private final JButton copyButton;
  private final JButton cancelButton;

  // --- Layout Components ---
  private final JPanel cardsPanel;
  private final CardLayout cardLayout;

  // State tracking
  private boolean confirmed = false;

  /**
   * Constructor for the Copy Dialog.
   *
   * @param owner The main window frame.
   */
  public CopyDialog(Frame owner, List<String> calendarNames) {
    super(owner, "Copy Events", true); // Modal
    this.setSize(450, 450);
    this.setLayout(new BorderLayout(10, 10));

    // --- 1. NORTH: Mode & Target Settings ---
    JPanel topPanel = new JPanel(new GridLayout(3, 1, 5, 5));
    topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

    // A. Target Calendar Selection
    JPanel targetPanel = new JPanel(new BorderLayout(5, 5));
    targetPanel.add(new JLabel("Copy To Calendar:"), BorderLayout.WEST);

    if (calendarNames == null || calendarNames.isEmpty()) {
      targetCalendarDropdown = new JComboBox<>(new String[] {"Default"});
    } else {
      targetCalendarDropdown = new JComboBox<>(calendarNames.toArray(new String[0]));
    }

    targetPanel.add(targetCalendarDropdown, BorderLayout.CENTER);
    topPanel.add(targetPanel);

    // Date Part
    JPanel datePart = new JPanel(new BorderLayout());
    datePart.add(new JLabel("To Date (YYYY-MM-DD):"), BorderLayout.NORTH);
    targetDateField = new JTextField(LocalDate.now().toString());
    datePart.add(targetDateField, BorderLayout.CENTER);

    // Time Part (NEW)
    JPanel timePart = new JPanel(new BorderLayout());
    timePart.add(new JLabel("To Time (HH:MM):"), BorderLayout.NORTH);
    targetTimeField = new JTextField("09:00");
    timePart.add(targetTimeField, BorderLayout.CENTER);

    JPanel dateTimePanel = new JPanel(new GridLayout(1, 2, 10, 0)); // Split row
    dateTimePanel.add(datePart);
    dateTimePanel.add(timePart);
    topPanel.add(dateTimePanel);

    // C. Mode Radio Buttons
    selectedEventRadio = new JRadioButton("Copy Selected Event", true);
    rangeRadio = new JRadioButton("Copy Date Range");
    allEventsOnDateRadio = new JRadioButton("All on Date");

    ButtonGroup group = new ButtonGroup();
    group.add(selectedEventRadio);
    group.add(rangeRadio);
    group.add(allEventsOnDateRadio);

    JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    radioPanel.add(selectedEventRadio);
    radioPanel.add(rangeRadio);
    radioPanel.add(allEventsOnDateRadio);
    topPanel.add(radioPanel);

    this.add(topPanel, BorderLayout.NORTH);

    // --- 2. CENTER: Variable Inputs (CardLayout) ---
    cardLayout = new CardLayout();
    cardsPanel = new JPanel(cardLayout);
    cardsPanel.setBorder(BorderFactory.createTitledBorder("Source Details"));

    // -- Card A: Selected Event (Simple text explanation) --
    JPanel simpleCard = new JPanel(new BorderLayout());
    JLabel infoLabel = new JLabel(
        "<html><div style='text-align: center;'>Will copy the currently selected "
            + "event<br/>to the target calendar/date.</div></html>");
    infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
    infoLabel.setForeground(Color.GRAY);
    simpleCard.add(infoLabel, BorderLayout.CENTER);

    // -- Card B: Range Inputs --
    JPanel rangeCard = new JPanel(new GridLayout(2, 2, 10, 10));
    rangeCard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    rangeCard.add(new JLabel("From Date:"));
    startDateField = new JTextField(LocalDate.now().toString());
    rangeCard.add(startDateField);

    rangeCard.add(new JLabel("To Date:"));
    endDateField = new JTextField(LocalDate.now().toString());
    rangeCard.add(endDateField);

    // -- Card C: All On Date (NEW) --
    JPanel dateCard = new JPanel(new GridLayout(2, 1, 10, 10));
    dateCard.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
    dateCard.add(new JLabel("Copy all events on this date:"));
    sourceDateField = new JTextField(LocalDate.now().toString());
    dateCard.add(sourceDateField);

    // Add cards to the layout
    cardsPanel.add(simpleCard, "Simple");
    cardsPanel.add(rangeCard, "Range");
    cardsPanel.add(dateCard, "AllOnDate");


    this.add(cardsPanel, BorderLayout.CENTER);

    // --- 3. SOUTH: Action Buttons ---
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    copyButton = new JButton("Copy");
    cancelButton = new JButton("Cancel");

    buttonPanel.add(copyButton);
    buttonPanel.add(cancelButton);
    this.add(buttonPanel, BorderLayout.SOUTH);

    // --- Visual Logic (Switching Cards) ---

    // Mode 1: Selected Event (Needs specific Time)
    selectedEventRadio.addActionListener(e -> {
      cardLayout.show(cardsPanel, "Simple");
      targetTimeField.setEnabled(true);
      targetTimeField.setBackground(Color.WHITE);
    });

    // Mode 2: All on Date (Time is irrelevant, usually preserves original times)
    allEventsOnDateRadio.addActionListener(e -> {
      cardLayout.show(cardsPanel, "AllOnDate");
      targetTimeField.setEnabled(false);
      targetTimeField.setBackground(Color.LIGHT_GRAY);
    });

    // Mode 3: Range (Time is irrelevant)
    rangeRadio.addActionListener(e -> {
      cardLayout.show(cardsPanel, "Range");
      targetTimeField.setEnabled(false);
      targetTimeField.setBackground(Color.LIGHT_GRAY);
    });

    // Confirm Action
    copyButton.addActionListener(e -> {
      confirmed = true;
      this.setVisible(false);
    });

    // Cancel Action
    cancelButton.addActionListener(e -> {
      confirmed = false;
      this.setVisible(false);
    });
    this.setLocationRelativeTo(owner);
  }

  // --- Accessors (For extracting data later) ---

  public boolean isConfirmed() {
    return confirmed;
  }

  public boolean isRangeMode() {
    return rangeRadio.isSelected();
  }

  public boolean isSelectedEventMode() {
    return selectedEventRadio.isSelected();
  }

  public boolean isAllOnDateMode() {
    return allEventsOnDateRadio.isSelected();
  }

  public String getTargetCalendar() {
    return (String) targetCalendarDropdown.getSelectedItem();
  }

  public String getTargetDate() {
    return targetDateField.getText();
  }

  public String getTargetTime() {
    return targetTimeField.getText();
  }

  public String getSourceDate() {
    return sourceDateField.getText();
  }

  public String getRangeStart() {
    return startDateField.getText();
  }

  public String getRangeEnd() {
    return endDateField.getText();
  }

  /**
   * Helper to pre-fill the range fields (e.g. if user had dates selected).
   */
  public void setInitialRange(String start, String end) {
    startDateField.setText(start);
    endDateField.setText(end);
  }
}