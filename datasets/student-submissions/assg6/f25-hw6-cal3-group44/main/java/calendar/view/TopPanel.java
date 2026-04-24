package calendar.view;

import calendar.controller.guicontroller.ViewListener;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Top panel component for calendar selection, navigation, and timezone display.
 */
public class TopPanel extends JPanel implements ActionListener {
  private static final DateTimeFormatter MONTH_YEAR_FORMAT =
      DateTimeFormatter.ofPattern("MMMM yyyy");
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private final List<ViewListener> listeners;
  private final JFrame parent;
  private final CalendarManageDialog createDialog;
  private JComboBox<String> calendarComboBox;
  private JLabel monthYearLabel;
  private JLabel timezoneLabel;
  private LocalDate currentMonth;
  private JButton createCalendarButton;
  private JButton editCalendarButton;
  private JButton prevButton;
  private JButton nextButton;
  private boolean suppressComboEvents = false;

  /**
   * Top panel constructor.
   *
   * @param parent    view.
   * @param listeners list of controllers.
   */
  public TopPanel(JFrame parent, List<ViewListener> listeners) {
    super(new BorderLayout());
    setBorder(new EmptyBorder(10, 10, 10, 10));
    this.currentMonth = LocalDate.now();
    this.parent = parent;
    this.listeners = listeners;
    createDialog = new CalendarManageDialog(listeners);
    initializeComponents();
  }

  private void initializeComponents() {
    JPanel calendarManagerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    calendarManagerPanel.add(new JLabel("Use Calendar:"));
    calendarComboBox = new JComboBox<>();
    calendarComboBox.addActionListener(this);
    calendarComboBox.setActionCommand("switch");
    calendarManagerPanel.add(calendarComboBox);

    createCalendarButton = new JButton("Create New Calendar");
    createCalendarButton.addActionListener(this);
    createCalendarButton.setActionCommand("create");
    calendarManagerPanel.add(createCalendarButton);

    editCalendarButton = new JButton("Edit Current Calendar");
    editCalendarButton.addActionListener(this);
    editCalendarButton.setActionCommand("edit");
    calendarManagerPanel.add(editCalendarButton);

    this.add(calendarManagerPanel, BorderLayout.WEST);


    prevButton = new JButton("◀ Previous Month");
    prevButton.addActionListener(this);
    prevButton.setActionCommand("prev");
    JPanel navPanel = new JPanel(new FlowLayout());
    navPanel.add(prevButton);

    monthYearLabel = new JLabel("", SwingConstants.CENTER);
    monthYearLabel.setFont(monthYearLabel.getFont().deriveFont(18f));
    monthYearLabel.setText(currentMonth.format(MONTH_YEAR_FORMAT));
    navPanel.add(monthYearLabel);

    nextButton = new JButton("Next Month ▶");
    nextButton.addActionListener(this);
    nextButton.setActionCommand("next");
    navPanel.add(nextButton);

    add(navPanel, BorderLayout.CENTER);

    JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

    timezoneLabel = new JLabel("Timezone: -");
    rightPanel.add(timezoneLabel);

    add(rightPanel, BorderLayout.EAST);
  }

  /**
   * Gets the calendar combo box.
   *
   * @return the calendar combo box
   */
  public JComboBox<String> getCalendarComboBox() {
    return calendarComboBox;
  }

  /**
   * Gets the create calendar button.
   *
   * @return the create calendar button
   */
  public JButton getCreateCalendarButton() {
    return createCalendarButton;
  }

  /**
   * Gets the edit calendar button.
   *
   * @return the edit calendar button
   */
  public JButton getEditCalendarButton() {
    return editCalendarButton;
  }

  /**
   * Gets the previous month button.
   *
   * @return the previous month button
   */
  public JButton getPrevButton() {
    return prevButton;
  }

  /**
   * Gets the next month button.
   *
   * @return the next month button
   */
  public JButton getNextButton() {
    return nextButton;
  }

  /**
   * Updates the calendar combo box.
   *
   * @param calendarNames      the list of calendar names
   * @param activeCalendarName the active calendar name
   */
  public void updateCalendarComboBox(List<String> calendarNames, String activeCalendarName) {
    if (calendarComboBox == null) {
      return;
    }
    suppressComboEvents = true;
    calendarComboBox.removeAllItems();
    for (String name : calendarNames) {
      calendarComboBox.addItem(name);
    }
    if (activeCalendarName != null && calendarNames.contains(activeCalendarName)) {
      calendarComboBox.setSelectedItem(activeCalendarName);
    }
    suppressComboEvents = false;
  }

  /**
   * Updates the month year label.
   *
   * @param month the current month
   */
  public void updateMonthYear(LocalDate month) {
    this.currentMonth = month;
    if (monthYearLabel != null) {
      monthYearLabel.setText(month.format(MONTH_YEAR_FORMAT));
    }
  }

  /**
   * Updates the timezone label.
   *
   * @param timezone the timezone string
   */
  public void updateTimezone(String timezone) {
    if (timezoneLabel != null) {
      timezoneLabel.setText("Timezone: " + timezone);
    }
  }

  /**
   * Returns the current month shown in the top panel. Added so the view can expose
   * the up-to-date month to the controller instead of a stale stored value.
   *
   * @return the current month
   */
  public LocalDate getCurrentMonth() {
    return this.currentMonth;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    switch (e.getActionCommand()) {
      case "switch":
        if (suppressComboEvents) {
          return;
        }
        Object selObj = calendarComboBox.getSelectedItem();
        if (selObj == null) {
          return;
        }
        String selected = selObj.toString();
        emitSwitchCalendar(selected);
        emitRefresh();
        break;
      case "create":
        createDialog.showCreateDialog(parent);
        break;
      case "edit":
        createDialog.showEditDialog(parent);
        break;
      case "prev":
        currentMonth = currentMonth.minusMonths(1);
        updateMonthYear(currentMonth);
        emitPreviousMonth();
        emitRefresh();
        break;
      case "next":
        currentMonth = currentMonth.plusMonths(1);
        updateMonthYear(currentMonth);
        emitNextMonth();
        emitRefresh();
        break;
      default:
        break;
    }
  }

  private void emitSwitchCalendar(String selected) {
    for (ViewListener listener : listeners) {
      listener.handleSwitchCalendar(selected);
    }
  }

  private void emitPreviousMonth() {
    for (ViewListener listener : listeners) {
      listener.handlePreviousMonth();
    }
  }

  private void emitNextMonth() {
    for (ViewListener listener : listeners) {
      listener.handleNextMonth();
    }
  }

  private void emitRefresh() {
    for (ViewListener listener : listeners) {
      listener.handleRefresh();
    }
  }
}
