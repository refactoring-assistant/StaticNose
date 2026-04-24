package calendar.view.guibased;

import calendar.controller.UiFeatures;
import calendar.view.EventViewData;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * This class represents the Event Dialog.
 */
public class EventDialog extends JDialog {

  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
  private final UiFeatures features;
  private final EventViewData eventToEdit;
  private final LocalDate defaultDate;
  private final ZoneId zone;
  private final JTextField subjectField;
  private final JTextField fromField;
  private final JTextField toField;
  private final JTextField descriptionField;
  private final JTextField locationField;
  private final JCheckBox isPrivateBox;
  private final JTextField occurrencesField;
  private JCheckBox[] dayCheckBoxes;
  private final JTextField untilField;
  private final JLabel errorLabel;

  /**
   * Initializes the EventDialog object with create event view.
   *
   * @param owner The owner window of this dialog.
   * @param features The features of this dialog.
   * @param defaultDate The date selected for adding the event.
   * @param zone Time zone of the current calendar.
   */
  public EventDialog(CalendarSwingViewImpl owner, UiFeatures features, LocalDate defaultDate,
                     ZoneId zone) {
    this(owner, features, null, defaultDate, zone);
  }

  /**
   * Initializes the EventDialog object with edit event view.
   *
   * @param owner The owner window of this dialog.
   * @param features The features of this dialog.
   * @param eventToEdit The event to be edited.
   * @param zone Time zone of the current calendar.
   */
  public EventDialog(CalendarSwingViewImpl owner, UiFeatures features, EventViewData eventToEdit,
                     ZoneId zone) {
    this(owner, features, eventToEdit, null, zone);
  }

  private EventDialog(CalendarSwingViewImpl owner, UiFeatures features, EventViewData eventToEdit,
                      LocalDate defaultDate,
                      ZoneId zone) {
    super(owner, eventToEdit == null ? "Create Event" : "Edit Event", true);
    this.features = features;
    this.eventToEdit = eventToEdit;
    this.defaultDate = defaultDate;
    this.zone = zone != null ? zone : ZoneId.systemDefault();

    setLayout(new BorderLayout());
    setSize(500, 500);
    setLocationRelativeTo(owner);

    subjectField = new JTextField();
    fromField = new JTextField();
    toField = new JTextField();
    descriptionField = new JTextField();
    locationField = new JTextField();
    isPrivateBox = new JCheckBox("Private");
    occurrencesField = new JTextField();
    untilField = new JTextField();
    errorLabel = new JLabel(" ");
    errorLabel.setForeground(java.awt.Color.RED);
    errorLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10));

    add(createFormPanel(), BorderLayout.CENTER);
    add(createButtonPanel(), BorderLayout.SOUTH);

    prefillData();
  }

  /**
   * Creates the form panel with all input fields.
   *
   * @return the form panel
   */
  private JPanel createFormPanel() {
    JPanel formPanel = new JPanel(new GridLayout(0, 2));

    JPanel daysPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));
    String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    dayCheckBoxes = new JCheckBox[7];
    for (int i = 0; i < 7; i++) {
      dayCheckBoxes[i] = new JCheckBox(days[i]);
      daysPanel.add(dayCheckBoxes[i]);
    }

    formPanel.add(new JLabel("Subject:"));
    formPanel.add(subjectField);
    formPanel.add(new JLabel("From (yyyy-MM-dd HH:mm):"));
    formPanel.add(fromField);
    formPanel.add(new JLabel("To (yyyy-MM-dd HH:mm):"));
    formPanel.add(toField);
    formPanel.add(new JLabel("Description:"));
    formPanel.add(descriptionField);
    formPanel.add(new JLabel("Location:"));
    formPanel.add(locationField);
    formPanel.add(new JLabel("Is Private:"));
    formPanel.add(isPrivateBox);

    if (eventToEdit == null) {
      formPanel.add(new JLabel("Repeats On:"));
      formPanel.add(daysPanel);
      formPanel.add(new JLabel("Occurrences:"));
      formPanel.add(occurrencesField);
      formPanel.add(new JLabel("Until (yyyy-MM-dd):"));
      formPanel.add(untilField);
    }

    return formPanel;
  }

  /**
   * Creates the button panel with save button and error label.
   *
   * @return the button panel
   */
  private JPanel createButtonPanel() {
    JPanel bottomPanel = new JPanel(new BorderLayout());
    bottomPanel.add(errorLabel, BorderLayout.NORTH);

    JButton saveButton = new JButton("Save");
    saveButton.addActionListener(e -> onSave());
    bottomPanel.add(saveButton, BorderLayout.SOUTH);

    return bottomPanel;
  }

  private void prefillData() {
    if (eventToEdit != null) {
      subjectField.setText(eventToEdit.getSubject());
      LocalDateTime start = LocalDateTime.ofInstant(eventToEdit.getStart(), zone);
      LocalDateTime end = LocalDateTime.ofInstant(eventToEdit.getEnd(), zone);

      fromField.setText(start.format(FORMATTER));
      toField.setText(end.format(FORMATTER));
      descriptionField.setText(eventToEdit.getDescription());
      locationField.setText(eventToEdit.getLocation());
      isPrivateBox.setSelected(eventToEdit.isPrivate());
    } else if (defaultDate != null) {
      fromField.setText(defaultDate.atTime(9, 0).format(FORMATTER));
      toField.setText(defaultDate.atTime(10, 0).format(FORMATTER));
    }
  }

  private void onSave() {
    errorLabel.setText(" ");

    try {
      if (eventToEdit == null) {
        handleCreate();
      } else {
        handleEdit();
      }
      dispose();
    } catch (RuntimeException e) {
      errorLabel.setText("Error: " + e.getMessage());
    }
  }

  /**
   * Handles event creation logic.
   */
  private void handleCreate() {
    String subject = subjectField.getText();
    String fromStr = fromField.getText();
    String toStr = toField.getText();
    String description = descriptionField.getText();
    String location = locationField.getText();
    boolean isPrivate = isPrivateBox.isSelected();

    StringBuilder repeatsBuilder = new StringBuilder();
    char[] codes = {'U', 'M', 'T', 'W', 'R', 'F', 'S'};
    for (int i = 0; i < 7; i++) {
      if (dayCheckBoxes[i].isSelected()) {
        repeatsBuilder.append(codes[i]);
      }
    }
    String repeats = repeatsBuilder.toString();

    String occurrencesStr = occurrencesField.getText();
    Integer occurrences = null;
    if (!occurrencesStr.isEmpty()) {
      try {
        occurrences = Integer.parseInt(occurrencesStr);
      } catch (NumberFormatException e) {
        throw new IllegalArgumentException("Occurrences must be a number.");
      }
    }
    String untilStr = untilField.getText();

    features.createEvent(subject, fromStr, toStr, null, description, location, isPrivate, repeats,
        occurrences, untilStr);
  }

  /**
   * Handles event editing logic.
   */
  private void handleEdit() {
    String subject = subjectField.getText();
    String fromStr = fromField.getText();
    String toStr = toField.getText();

    LocalDateTime originalStart = LocalDateTime.ofInstant(eventToEdit.getStart(), zone);
    LocalDateTime originalEnd = LocalDateTime.ofInstant(eventToEdit.getEnd(), zone);

    String currentFromStr = originalStart.format(FORMATTER);
    String currentToStr = originalEnd.format(FORMATTER);
    String currentSubject = eventToEdit.getSubject();

    boolean singleEventUpdate = true;
    boolean updateAll = false;

    if (eventToEdit.isSeries()) {
      Object[] options = {"Only this event", "This and future events", "All events in series"};
      int choice = JOptionPane.showOptionDialog(this,
          "This is a repeating event. How would you like to apply changes?",
          "Edit Repeating Event",
          JOptionPane.YES_NO_CANCEL_OPTION,
          JOptionPane.QUESTION_MESSAGE,
          null,
          options,
          options[0]);

      if (choice == JOptionPane.CLOSED_OPTION) {
        return;
      }

      if (choice == 0) {
        singleEventUpdate = true;
        updateAll = false;
      } else if (choice == 1) {
        singleEventUpdate = false;
        updateAll = false;
      } else if (choice == 2) {
        singleEventUpdate = false;
        updateAll = true;
      }
    }

    if (!subject.equals(currentSubject)) {
      features.editEvent(currentSubject, currentFromStr, currentToStr, "subject", subject,
          singleEventUpdate, updateAll);
      currentSubject = subject;
    }

    if (!fromStr.equals(currentFromStr) || !toStr.equals(currentToStr)) {
      features.updateEventTime(currentSubject, currentFromStr, fromStr, toStr, singleEventUpdate,
          updateAll);
      currentFromStr = fromStr;
      currentToStr = toStr;
    }

    String description = descriptionField.getText();
    if (!description.equals(eventToEdit.getDescription())) {
      features.editEvent(currentSubject, currentFromStr, currentToStr, "description", description,
          singleEventUpdate,
          updateAll);
    }

    String location = locationField.getText();
    if (!location.equals(eventToEdit.getLocation())) {
      features.editEvent(currentSubject, currentFromStr, currentToStr, "location", location,
          singleEventUpdate, updateAll);
    }

    boolean isPrivate = isPrivateBox.isSelected();
    if (isPrivate != eventToEdit.isPrivate()) {
      features.editEvent(currentSubject, currentFromStr, currentToStr, "isPrivate",
          String.valueOf(isPrivate), singleEventUpdate, updateAll);
    }
  }
}
