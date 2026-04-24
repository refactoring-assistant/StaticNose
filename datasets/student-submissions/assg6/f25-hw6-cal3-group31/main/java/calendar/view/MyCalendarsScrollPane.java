package calendar.view;

import java.util.Objects;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A scroll pane which contains the list of calendars in My Calendars.
 */
public class MyCalendarsScrollPane extends JScrollPane implements ListSelectionListener {
  private final DefaultListModel<String> calendarsListModel = new DefaultListModel<>();
  private final JList<String> calendarsList = new JList<>(calendarsListModel);
  private final IntGuiView view;
  private String currentSelectionName;

  /**
   * MyCalendarsScrollPane constructor.
   *
   * @param view the parent view to use
   */
  public MyCalendarsScrollPane(IntGuiView view) {
    this.view = Objects.requireNonNull(view);
    this.setViewportView(calendarsList);
    calendarsList.addListSelectionListener(this);
  }

  /**
   * Add a calendar to the calendars list model.
   *
   * @param calendarName the name of the calendar to add
   */
  public void addCalendar(String calendarName) {
    calendarsListModel.addElement(calendarName);
    if (currentSelectionName == null) {
      currentSelectionName = calendarName;
    }
  }

  @Override
  public void valueChanged(ListSelectionEvent e) {
    String selectedValue = calendarsList.getSelectedValue();
    if (selectedValue != null && !selectedValue.equals(currentSelectionName)) {
      currentSelectionName = selectedValue;
      view.changeCalendar(selectedValue);
      view.getRequestHandler().handleSwitchCalendarRequest(selectedValue);
    }
  }
}
