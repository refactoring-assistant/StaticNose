package calendar.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * A dialog window that allows the user to specify criteria for querying events.
 * This class extends JDialog and provides two distinct modes for searching:
 * querying for a specific single date or querying within a specified start
 * and end date range. It utilizes a CardLayout to dynamically switch input forms
 * based on the user's selection.
 */
public class QueryEventsDialog extends JDialog {

  private final JRadioButton singleDateRadio;
  private final JRadioButton rangeRadio;

  private final JTextField singleDateField;

  private final JTextField startDateField;
  private final JTextField startTimeField;
  private final JTextField endDateField;
  private final JTextField endTimeField;

  private final JButton searchButton;
  private final JButton cancelButton;

  private final JPanel cardsPanel;
  private final CardLayout cardLayout;

  private boolean confirmed = false;

  /**
   * Constructs a new QueryEventsDialog.
   * Initializes the modal dialog with a BorderLayout. It sets up the radio buttons
   * for mode selection in the northern region, a card-based panel in the center
   * containing the input fields for both single-date and range-based queries,
   * and a button panel in the southern region for confirming or canceling the action.
   *
   * @param owner the parent Frame to which this dialog is attached.
   */
  public QueryEventsDialog(Frame owner) {
    super(owner, "Query Events", true);
    this.setSize(450, 350);
    this.setLayout(new BorderLayout(10, 10));

    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    topPanel.setBorder(BorderFactory.createTitledBorder("Search Type"));

    singleDateRadio = new JRadioButton("On a specific date", true);
    rangeRadio = new JRadioButton("Between dates/times");

    ButtonGroup group = new ButtonGroup();
    group.add(singleDateRadio);
    group.add(rangeRadio);

    topPanel.add(singleDateRadio);
    topPanel.add(rangeRadio);

    this.add(topPanel, BorderLayout.NORTH);

    cardLayout = new CardLayout();
    cardsPanel = new JPanel(cardLayout);
    cardsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

    JPanel dateCard = new JPanel(new GridLayout(4, 1, 5, 5));
    dateCard.add(new JLabel("Date (YYYY-MM-DD):"));
    singleDateField = new JTextField(LocalDate.now().toString());
    dateCard.add(singleDateField);
    dateCard.add(new JLabel(""));
    dateCard.add(new JLabel(""));

    JPanel rangeCard = new JPanel(new GridLayout(4, 2, 10, 10));

    rangeCard.add(new JLabel("Start Date:"));
    startDateField = new JTextField(LocalDate.now().toString());
    rangeCard.add(startDateField);

    rangeCard.add(new JLabel("Start Time:"));
    startTimeField = new JTextField("00:00");
    rangeCard.add(startTimeField);

    rangeCard.add(new JLabel("End Date:"));
    endDateField = new JTextField(LocalDate.now().toString());
    rangeCard.add(endDateField);

    rangeCard.add(new JLabel("End Time:"));
    endTimeField = new JTextField("23:59");
    rangeCard.add(endTimeField);

    cardsPanel.add(dateCard, "Single");
    cardsPanel.add(rangeCard, "Range");

    this.add(cardsPanel, BorderLayout.CENTER);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    searchButton = new JButton("Query");
    cancelButton = new JButton("Cancel");

    buttonPanel.add(searchButton);
    buttonPanel.add(cancelButton);
    this.add(buttonPanel, BorderLayout.SOUTH);

    singleDateRadio.addActionListener(e -> cardLayout.show(cardsPanel, "Single"));
    rangeRadio.addActionListener(e -> cardLayout.show(cardsPanel, "Range"));

    searchButton.addActionListener(e -> {
      confirmed = true;
      this.setVisible(false);
    });

    cancelButton.addActionListener(e -> {
      confirmed = false;
      this.setVisible(false);
    });
    this.setLocationRelativeTo(owner);
  }

  public boolean isRangeQuery() {
    return rangeRadio.isSelected();
  }

  public boolean isConfirmed() {
    return confirmed;
  }

  public String getSingleDate() {
    return singleDateField.getText();
  }

  public String getStartDate() {
    return startDateField.getText();
  }

  public String getStartTime() {
    return startTimeField.getText();
  }

  public String getEndDate() {
    return endDateField.getText();
  }

  public String getEndTime() {
    return endTimeField.getText();
  }

}