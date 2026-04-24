package calendar.view;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

/**
 * A panel that displays event details with an edit button.
 * Shows all event information in a read-only format.
 */
public class EventDetailsPanel extends JPanel {
  private final ViewEvent event;
  private final Runnable onEditCallback;

  /**
   * EventDetailsPanel constructor.
   *
   * @param event the event to display
   * @param onEditCallback callback to invoke when edit button is clicked
   */
  public EventDetailsPanel(ViewEvent event, Runnable onEditCallback) {
    this.event = event;
    this.onEditCallback = onEditCallback;
    setupPanel();
  }

  private void setupPanel() {
    this.setLayout(new BorderLayout());
    this.setBorder(new EmptyBorder(10, 10, 10, 10));

    JPanel contentPanel = createContentPanel();
    this.add(contentPanel, BorderLayout.CENTER);

    // Add Edit button at the bottom
    JPanel buttonPanel = new JPanel();
    JButton editButton = new JButton("Edit");
    editButton.addActionListener(e -> onEditCallback.run());
    buttonPanel.add(editButton);
    this.add(buttonPanel, BorderLayout.SOUTH);
  }

  private JPanel createContentPanel() {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.WEST;

    int row = 0;

    // Subject
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.0;

    JPanel contentPanel = new JPanel(new GridBagLayout());

    contentPanel.add(new JLabel("Subject:"), gbc);
    gbc.gridx = 1;
    gbc.weightx = 1.0;
    contentPanel.add(new JLabel(event.getSubject()), gbc);
    row++;

    // Start Date/Time
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.0;
    contentPanel.add(new JLabel("Start:"), gbc);
    gbc.gridx = 1;
    gbc.weightx = 1.0;
    String startDateTime = String.format("%04d-%02d-%02d %02d:%02d",
        event.getStartYear(), event.getStartMonth(), event.getStartDay(),
        event.getStartHour(), event.getStartMinute());
    contentPanel.add(new JLabel(startDateTime), gbc);
    row++;

    // End Date/Time
    gbc.gridx = 0;
    gbc.gridy = row;
    gbc.weightx = 0.0;
    contentPanel.add(new JLabel("End:"), gbc);
    gbc.gridx = 1;
    gbc.weightx = 1.0;
    String endDateTime = String.format("%04d-%02d-%02d %02d:%02d",
        event.getEndYear(), event.getEndMonth(), event.getEndDay(),
        event.getEndHour(), event.getEndMinute());
    contentPanel.add(new JLabel(endDateTime), gbc);
    row++;

    // Location
    if (event.getLocation() != null) {
      gbc.gridx = 0;
      gbc.gridy = row;
      gbc.weightx = 0.0;
      contentPanel.add(new JLabel("Location:"), gbc);
      gbc.gridx = 1;
      gbc.weightx = 1.0;
      contentPanel.add(new JLabel(event.getLocation()), gbc);
      row++;
    }

    // Status
    if (event.getStatus() != null) {
      gbc.gridx = 0;
      gbc.gridy = row;
      gbc.weightx = 0.0;
      contentPanel.add(new JLabel("Status:"), gbc);
      gbc.gridx = 1;
      gbc.weightx = 1.0;
      contentPanel.add(new JLabel(event.getStatus()), gbc);
      row++;
    }

    // Description
    if (event.getDescription() != null && !event.getDescription().isEmpty()) {
      gbc.gridx = 0;
      gbc.gridy = row;
      gbc.weightx = 0.0;
      gbc.anchor = GridBagConstraints.NORTHWEST;
      contentPanel.add(new JLabel("Description:"), gbc);
      gbc.gridx = 1;
      gbc.weightx = 1.0;
      gbc.weighty = 1.0;
      gbc.fill = GridBagConstraints.BOTH;
      JTextArea descriptionArea = new JTextArea(event.getDescription());
      descriptionArea.setEditable(false);
      descriptionArea.setLineWrap(true);
      descriptionArea.setWrapStyleWord(true);
      JScrollPane scrollPane = new JScrollPane(descriptionArea);
      contentPanel.add(scrollPane, gbc);
    }

    return contentPanel;
  }

  @Override
  public String toString() {
    return event.toString();
  }
}

