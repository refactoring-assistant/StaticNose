package calendar.view.gui;

import calendar.controller.features.GuiFeatures;
import calendar.model.InterfaceEvent;
import java.awt.GridLayout;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Dialog for viewing and editing event details.
 */
public class EventDetailsDialog {

  private enum EditScope {
    SINGLE,
    FROM_THIS,
    SERIES_ALL
  }

  private final JFrame parent;
  private GuiFeatures features;

  /**
   * Constructs a dialog attached to the given parent frame.
   *
   * @param parent parent frame
   */
  public EventDetailsDialog(JFrame parent) {
    this.parent = parent;
  }

  /**
   * Sets the controller features used to perform edits.
   *
   * @param features gui features
   */
  public void setFeatures(GuiFeatures features) {
    this.features = features;
  }

  /**
   * Shows details for the given event, with edit options.
   *
   * @param event event to show
   */
  public void show(InterfaceEvent event) {
    if (event == null) {
      return;
    }

    String details = buildDetailsString(event);
    Object[] options = new Object[] {"Close", "Edit One", "Edit From This", "Edit Series"};
    int choice = JOptionPane.showOptionDialog(
        parent,
        details,
        "Event Details",
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.INFORMATION_MESSAGE,
        null,
        options,
        options[0]);

    if (choice == 1) {
      promptEditForEvent(event, EditScope.SINGLE);
    } else if (choice == 2) {
      promptEditForEvent(event, EditScope.FROM_THIS);
    } else if (choice == 3) {
      promptEditForEvent(event, EditScope.SERIES_ALL);
    }
  }

  /**
   * Builds a textual description of the event including subject, times,
   * location, visibility status, and description.
   *
   * @param e event to describe
   * @return multi-line details string
   */
  private String buildDetailsString(InterfaceEvent e) {
    ZoneId zone = e.getZone();
    LocalDateTime start = LocalDateTime.ofInstant(e.getStart(), zone);
    LocalDateTime end = LocalDateTime.ofInstant(e.getEnd(), zone);
    DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    StringBuilder sb = new StringBuilder();
    sb.append("Subject: ").append(e.getSubject()).append('\n');
    sb.append("Start: ").append(df.format(start)).append(" (")
        .append(zone.getId()).append(")").append('\n');
    sb.append("End: ").append(df.format(end)).append(" (")
        .append(zone.getId()).append(")").append('\n');
    if (e.getLocation() != null && !e.getLocation().isEmpty()) {
      sb.append("Location: ").append(e.getLocation()).append('\n');
    }
    sb.append("Status: ").append(e.isPublicEvent() ? "public" : "private").append('\n');
    if (e.getDescription() != null && !e.getDescription().isEmpty()) {
      sb.append('\n');
      sb.append("Description:\n");
      sb.append(e.getDescription());
    }
    return sb.toString();
  }

  /**
   * Prompts the user for an edit operation on the given event under a
   * specific scope and dispatches the change via the controller.
   *
   * @param e     event to edit
   * @param scope edit scope
   */
  private void promptEditForEvent(InterfaceEvent e, EditScope scope) {
    if (features == null) {
      return;
    }

    Object[] props = new Object[] {"subject", "start", "end", "description", "location", "status"};
    String property = (String) JOptionPane.showInputDialog(
        parent,
        "Property to edit:",
        "Edit Event",
        JOptionPane.PLAIN_MESSAGE,
        null,
        props,
        "subject");
    if (property == null || property.isBlank()) {
      return;
    }

    String newValue;
    if ("status".equals(property)) {
      newValue = promptForStatus();
      if (newValue == null) {
        return;
      }
    } else if ("start".equals(property)) {
      newValue = promptForInstantIso("Edit start time", e.getStart(), e.getZone());
      if (newValue == null) {
        return;
      }
    } else if ("end".equals(property)) {
      newValue = promptForInstantIso("Edit end time", e.getEnd(), e.getZone());
      if (newValue == null) {
        return;
      }
    } else {
      String prompt = "New value for " + property + ":";
      String entered = JOptionPane.showInputDialog(parent, prompt);
      if (entered == null) {
        return;
      }
      newValue = entered;
    }

    try {
      if (scope == EditScope.SINGLE) {
        features.onEditEventSingle(e.getSubject(), e.getStart(), property, newValue);
      } else if (scope == EditScope.FROM_THIS) {
        features.onEditEventsFrom(e.getSubject(), e.getStart(), property, newValue);
      } else if (scope == EditScope.SERIES_ALL) {
        features.onEditSeriesAll(e.getSubject(), e.getStart(), property, newValue);
      }
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(parent, ex.getMessage(),
          "Error", JOptionPane.ERROR_MESSAGE);
    }
  }

  /**
   * Prompts the user to select a new public or private status.
   *
   * @return chosen status, or {@code null} if cancelled
   */
  private String promptForStatus() {
    Object[] statuses = new Object[] {"public", "private"};
    String selected = (String) JOptionPane.showInputDialog(
        parent,
        "New status:",
        "Edit Event",
        JOptionPane.PLAIN_MESSAGE,
        null,
        statuses,
        "public");
    if (selected == null || selected.isBlank()) {
      return null;
    }
    return selected;
  }

  /**
   * Prompts the user for a new instant value using separate date and time
   * text fields, validates the input, and returns an ISO-8601 UTC string.
   *
   * @param title   dialog title
   * @param current current instant value
   * @param zone    zone used to present and interpret the local date and time
   * @return ISO-8601 UTC string, or {@code null} if the dialog is cancelled
   */
  private String promptForInstantIso(String title, Instant current, ZoneId zone) {
    LocalDateTime ldt = LocalDateTime.ofInstant(current, zone);
    LocalDate date = ldt.toLocalDate();
    LocalTime time = ldt.toLocalTime().withSecond(0).withNano(0);

    DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
    DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    JTextField dateField = new JTextField(dateFormatter.format(date), 10);
    JTextField timeField = new JTextField(timeFormatter.format(time), 5);

    JPanel panel = new JPanel(new GridLayout(0, 2, 4, 4));
    panel.add(new JLabel("Date (yyyy-MM-dd):"));
    panel.add(dateField);
    panel.add(new JLabel("Time (HH:mm):"));
    panel.add(timeField);

    while (true) {
      int res = JOptionPane.showConfirmDialog(
          parent,
          panel,
          title,
          JOptionPane.OK_CANCEL_OPTION,
          JOptionPane.PLAIN_MESSAGE);
      if (res != JOptionPane.OK_OPTION) {
        return null;
      }

      String dateText = dateField.getText();
      String timeText = timeField.getText();
      try {
        LocalDate newDate = LocalDate.parse(dateText, dateFormatter);
        LocalTime newTime = LocalTime.parse(timeText, timeFormatter);
        LocalDateTime newLdt = LocalDateTime.of(newDate, newTime);
        Instant newInstant = newLdt.atZone(zone).toInstant();
        return DateTimeFormatter.ISO_INSTANT.format(newInstant);
      } catch (Exception ex) {
        JOptionPane.showMessageDialog(
            parent,
            "Invalid date or time. Please use yyyy-MM-dd for the date and HH:mm for the time.",
            "Invalid Input",
            JOptionPane.ERROR_MESSAGE);
      }
    }
  }
}
