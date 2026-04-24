package view;

import controller.CalendarController;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * Sidebar panel showing available calendars with an add button.
 */
public class CalendarListPanel extends JPanel {

  private final CalendarController controller;
  private final Consumer<Boolean> onActiveChanged;
  private final DefaultListModel<String> model = new DefaultListModel<>();
  private final JList<String> list = new JList<>(model);

  /**
   * Creates the panel.
   *
   * @param controller      calendar controller
   * @param onActiveChanged callback invoked when active calendar changes; receives true when
   *                        switching to an existing calendar and false when a calendar was added
   */
  public CalendarListPanel(CalendarController controller, Consumer<Boolean> onActiveChanged) {
    this.controller = Objects.requireNonNull(controller, "controller");
    this.onActiveChanged = onActiveChanged;
    buildUi();
    refreshCalendarList();
  }

  private void buildUi() {
    setLayout(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    JLabel label = new JLabel("Calendar List");
    label.setFont(label.getFont().deriveFont(label.getFont().getStyle() | java.awt.Font.BOLD, 14f));

    JButton addButton = new JButton("+");
    addButton.addActionListener(e -> promptCreateCalendar());

    JPanel header = new JPanel(new BorderLayout());
    header.add(label, BorderLayout.WEST);
    header.add(addButton, BorderLayout.EAST);

    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    list.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        String selected = list.getSelectedValue();
        if (selected != null && !selected.equals(controller.getActiveCalendarName())) {
          controller.selectCalendar(selected);
          if (onActiveChanged != null) {
            onActiveChanged.accept(Boolean.TRUE);
          }
        }
      }
    });

    add(header, BorderLayout.NORTH);
    add(new JScrollPane(list), BorderLayout.CENTER);
  }

  private void promptCreateCalendar() {
    final JPanel form = new JPanel(new BorderLayout(5, 5));
    final javax.swing.JTextField nameField = new javax.swing.JTextField();

    JComboBox<String> tzCombo = new JComboBox<>(ZoneId.getAvailableZoneIds().stream()
        .sorted().toArray(String[]::new));
    tzCombo.setSelectedItem(ZoneId.systemDefault().getId());

    JPanel labels = new JPanel(new GridLayout(0, 1, 4, 4));
    labels.add(new JLabel("Name:"));
    labels.add(new JLabel("Timezone:"));
    JPanel fields = new JPanel(new GridLayout(0, 1, 4, 4));
    fields.add(nameField);
    fields.add(tzCombo);
    form.add(labels, BorderLayout.WEST);
    form.add(fields, BorderLayout.CENTER);

    int result = JOptionPane.showConfirmDialog(
        this,
        form,
        "Create Calendar",
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.PLAIN_MESSAGE);
    if (result == JOptionPane.OK_OPTION) {
      String name = nameField.getText().trim();
      Object selectedTz = tzCombo.getSelectedItem();
      if (name.isEmpty() || selectedTz == null) {
        JOptionPane.showMessageDialog(this, "Name and timezone are required.");
        return;
      }
      try {
        controller.createCalendar(name, selectedTz.toString());
        refreshCalendarList();
        if (onActiveChanged != null) {
          onActiveChanged.accept(Boolean.FALSE);
        }
      } catch (IllegalArgumentException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage());
      }
    }
  }

  /**
   * Reloads the list of calendars and highlights the active one.
   */
  public void refreshCalendarList() {
    List<model.Calendar> calendars = controller.getCalendars();
    model.clear();
    for (model.Calendar cal : calendars) {
      model.addElement(cal.getName());
    }
    if (!calendars.isEmpty()) {
      list.setSelectedValue(controller.getActiveCalendarName(), true);
    }
  }
}
