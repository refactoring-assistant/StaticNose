package view;

import java.time.Month;
import java.time.YearMonth;
import javax.swing.JComboBox;
import javax.swing.JFrame;

/**
 * An abstract class that contains overlapping code between AddEventPopUp and
 * EditDetailsPopUp.
 */
public abstract class AbstractCommandEvents extends JFrame {

  protected JComboBox<Month> startMonthBox;
  protected JComboBox<Integer> startDayBox;
  protected JComboBox<Integer> startYearBox;

  protected JComboBox<Month> endMonthBox;
  protected JComboBox<Integer> endDayBox;
  protected JComboBox<Integer> endYearBox;

  protected JComboBox<String> startHourBox;
  protected JComboBox<String> startMinuteBox;
  protected JComboBox<String> startAmPmBox;

  protected JComboBox<String> endHourBox;
  protected JComboBox<String> endMinuteBox;
  protected JComboBox<String> endAmPmBox;

  protected JComboBox<String> locationBox;
  protected JComboBox<String> statusBox;

  /**
   * A helper function that determines how many days are in a corresponding
   * month of a specific year.
   *
   * @param dayBox days that go in the combo box
   * @param year the given year
   * @param month the given month
   */
  protected void updateDay(JComboBox<Integer> dayBox, int year, Month month) {
    dayBox.removeAllItems();
    int length = YearMonth.of(year, month).lengthOfMonth();
    for (int day = 1; day <= length; day++) {
      dayBox.addItem(day);
    }
  }

  /**
   * A helper function that builds a DateTime string YYYY-MM-DDTHH:MM.
   *
   * @param year the given year
   * @param month the given month
   * @param day the given day
   * @param hour24 the given time (military)
   * @param minute the given minutes
   *
   * @return the formated DateTime string
   */
  protected String buildDateTime(int year, Month month, int day, int hour24, int minute) {
    int monthNumber = month.getValue();
    return String.format("%04d-%02d-%02dT%02d:%02d", year, monthNumber, day, hour24, minute);
  }

  /**
   * A helper function that converts the hour from 12 hour am/pm to 24 hours.
   *
   * @param hour12 in representing the hour
   * @param amPm a string representing am or pm
   *
   * @return the hour in 24 hours (military time)
   */
  protected int to24Hour(int hour12, String amPm) {
    int h = hour12 % 12;
    if ("PM".equals(amPm)) {
      h += 12;
    }
    return h;
  }

  /**
   * A helper function that returns an empty string if given a null.
   *
   * @param text a string
   *
   * @return an empty string
   */
  protected String safe(String text) {
    return text == null ? "" : text;
  }

  /**
   * Shared helper to build a labeled row panel for form layouts.
   *
   * @param label left component (usually a JLabel)
   * @param field right component (input or container)
   * @return configured panel with spacing and max width
   */
  protected javax.swing.JPanel buildRow(java.awt.Component label, java.awt.Component field) {
    javax.swing.JPanel row = new javax.swing.JPanel(new java.awt.BorderLayout(6, 0));
    row.add(label, java.awt.BorderLayout.WEST);
    row.add(field, java.awt.BorderLayout.CENTER);
    row.setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 0, 2, 0));
    row.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
    row.setMaximumSize(new java.awt.Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
    return row;
  }


}
