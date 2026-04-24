package calendar.model;

import calendar.CalendarProperty;
import calendar.Property;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.TimeZone;

/**
 * Mock of model to test controller in isolation.
 */
public class MockCalendarModel implements Model {

  StringBuilder log;

  /**
   * Public constructor.
   */
  public MockCalendarModel() {
    log = new StringBuilder();
  }

  @Override
  public void createEvent(String subject, Date startDateTime, Date endDateTime)
      throws IllegalArgumentException {
    log.append("Create Event: \n")
        .append("Subject: ").append(subject).append("\n")
        .append("Start Date: ").append(startDateTime.toString()).append("\n")
        .append("End Date: ").append(endDateTime.toString()).append("\n");
  }

  @Override
  public void createEvent(String subject, Date startDateTime, Date endDateTime, String name)
      throws IllegalArgumentException {
    log.append("Create Event In Name: \n")
        .append("Subject: ").append(subject).append("\n")
        .append("Start Date: ").append(startDateTime.toString()).append("\n")
        .append("End Date: ").append(endDateTime.toString()).append("\n")
        .append("Name: ").append(name).append("\n");
  }

  @Override
  public void createReoccurringEvent(String subject, Date startDateTime, Date endDateTime,
      int[] weekdays, int repeat) throws IllegalArgumentException {
    log.append("Create Reoccurring Event: \n")
        .append("Subject: ").append(subject).append("\n")
        .append("Start Date: ").append(startDateTime.toString()).append("\n")
        .append("End Date: ").append(endDateTime.toString()).append("\n")
        .append("Weekdays: ").append(Arrays.toString(weekdays)).append("\n")
        .append("Repeat: ").append(repeat).append("\n");

  }

  @Override
  public void createReoccurringEvent(String subject, Date startDateTime, Date endDateTime,
      int[] weekdays, int repeat, String name)
      throws IllegalArgumentException {
    log.append("Create Reoccurring Event In Name: \n")
        .append("Subject: ").append(subject).append("\n")
        .append("Start Date: ").append(startDateTime.toString()).append("\n")
        .append("End Date: ").append(endDateTime.toString()).append("\n")
        .append("Weekdays: ").append(Arrays.toString(weekdays)).append("\n")
        .append("Repeat: ").append(repeat).append("\n")
        .append("Name: ").append(name).append("\n");
  }

  @Override
  public void createEventUntil(String subject, Date startDateTime, Date endDateTime, int[] weekdays,
      Date untilDate) throws IllegalArgumentException {
    log.append("Create Event Until: \n")
        .append("Subject: ").append(subject).append("\n")
        .append("Start Date: ").append(startDateTime.toString()).append("\n")
        .append("End Date: ").append(endDateTime.toString()).append("\n")
        .append("Weekdays: ").append(Arrays.toString(weekdays)).append("\n")
        .append("Until Date: ").append(untilDate.toString()).append("\n");

  }

  @Override
  public void createEventUntil(String subject, Date startDateTime, Date endDateTime, int[] weekdays,
      Date untilDate, String name) throws IllegalArgumentException {
    log.append("Create Event Until In Name: \n")
        .append("Subject: ").append(subject).append("\n")
        .append("Start Date: ").append(startDateTime.toString()).append("\n")
        .append("End Date: ").append(endDateTime.toString()).append("\n")
        .append("Weekdays: ").append(Arrays.toString(weekdays)).append("\n")
        .append("Until Date: ").append(untilDate.toString()).append("\n")
        .append("Name: ").append(name).append("\n");
  }

  @Override
  public void createAllDayEvent(String subject, Date startDate) throws IllegalArgumentException {
    log.append("Create All Day Event: \n")
        .append("Subject: ").append(subject).append("\n")
        .append("Start Date: ").append(startDate.toString()).append("\n");
  }

  @Override
  public void createAllDayEvent(String subject, Date startDate, String name)
      throws IllegalArgumentException {
    log.append("Create All Day Event In Name: \n")
        .append("Subject: ").append(subject).append("\n")
        .append("Start Date: ").append(startDate.toString()).append("\n")
        .append("Name: ").append(name).append("\n");
  }

  @Override
  public void createAllDayEventSeries(String subject, Date startDate, int[] weekdays, int repeat)
      throws IllegalArgumentException {
    log.append("Create Reoccurring All Day Event: \n")
        .append("Subject: ").append(subject).append("\n")
        .append("Start Date: ").append(startDate.toString()).append("\n")
        .append("Weekdays: ").append(Arrays.toString(weekdays)).append("\n")
        .append("Repeat: ").append(repeat).append("\n");

  }

  @Override
  public void createAllDayEventSeries(String subject, Date startDate, int[] weekdays, int repeat,
      String name) throws IllegalArgumentException {
    log.append("Create Reoccurring All Day Event In name: \n")
        .append("Subject: ").append(subject).append("\n")
        .append("Start Date: ").append(startDate.toString()).append("\n")
        .append("Weekdays: ").append(Arrays.toString(weekdays)).append("\n")
        .append("Repeat: ").append(repeat).append("\n")
        .append("Name: ").append(name).append("\n");
  }

  @Override
  public void createAllDayEventUntil(String subject, Date startDate, int[] weekdays, Date untilDate)
      throws IllegalArgumentException {
    log.append("Create All Day Event Until: \n")
        .append("Subject: ").append(subject).append("\n")
        .append("Start Date: ").append(startDate.toString()).append("\n")
        .append("Weekdays: ").append(Arrays.toString(weekdays)).append("\n")
        .append("Until Date: ").append(untilDate.toString()).append("\n");

  }

  @Override
  public void createAllDayEventUntil(String subject, Date startDate, int[] weekdays, Date untilDate,
      String name) throws IllegalArgumentException {
    log.append("Create All Day Event Until In Name: \n")
        .append("Subject: ").append(subject).append("\n")
        .append("Start Date: ").append(startDate.toString()).append("\n")
        .append("Weekdays: ").append(Arrays.toString(weekdays)).append("\n")
        .append("Until Date: ").append(untilDate.toString()).append("\n")
        .append("Name: ").append(name).append("\n");

  }

  @Override
  public void editEvent(Property property, String subject, Date startDateTime, Date endDateTime,
      Object newProperty) throws IllegalArgumentException {
    log.append("Edit Event: \n")
        .append("Property: ").append(property).append("\n")
        .append("Subject: ").append(subject).append("\n")
        .append("Start Date: ").append(startDateTime.toString()).append("\n")
        .append("End Date: ").append(endDateTime.toString()).append("\n")
        .append("New Property: ").append(newProperty).append("\n");
  }

  @Override
  public void editEvent(Property property, String subject, Date startDateTime, Date endDateTime,
      Object newProperty, String name) throws IllegalArgumentException {
    log.append("Edit Event In Name: \n")
        .append("Property: ").append(property).append("\n")
        .append("Subject: ").append(subject).append("\n")
        .append("Start Date: ").append(startDateTime.toString()).append("\n")
        .append("End Date: ").append(endDateTime.toString()).append("\n")
        .append("New Property: ").append(newProperty).append("\n")
        .append("Name: ").append(name).append("\n");

  }

  @Override
  public void editEventsStartingOn(Property property, String subject, Date startDateTime,
      Object newProperty) throws IllegalArgumentException {
    log.append("Edit Event Starting On: \n")
        .append("Property: ").append(property).append("\n")
        .append("Subject: ").append(subject).append("\n")
        .append("Start Date: ").append(startDateTime.toString()).append("\n")
        .append("New Property: ").append(newProperty).append("\n");
  }

  @Override
  public void editEventsStartingOn(Property property, String subject, Date startDateTime,
      Object newProperty, String name)
      throws IllegalArgumentException {
    log.append("Edit Event Starting On In Name: \n")
        .append("Property: ").append(property).append("\n")
        .append("Subject: ").append(subject).append("\n")
        .append("Start Date: ").append(startDateTime.toString()).append("\n")
        .append("New Property: ").append(newProperty).append("\n")
        .append("Name: ").append(name).append("\n")
    ;

  }

  @Override
  public void editSeries(Property property, String subject, Date startDateTime, Object newProperty)
      throws IllegalArgumentException {
    log.append("Edit Series: \n")
        .append("Property: ").append(property).append("\n")
        .append("Subject: ").append(subject).append("\n")
        .append("Start Date: ").append(startDateTime.toString()).append("\n")
        .append("New Property: ").append(newProperty).append("\n");

  }

  @Override
  public void editSeries(Property property, String subject, Date startDateTime, Object newProperty,
      String name) throws IllegalArgumentException {
    log.append("Edit Series In Name: \n")
        .append("Property: ").append(property).append("\n")
        .append("Subject: ").append(subject).append("\n")
        .append("Start Date: ").append(startDateTime.toString()).append("\n")
        .append("New Property: ").append(newProperty).append("\n")
        .append("Name: ").append(name).append("\n");
  }

  @Override
  public Set<Event> getEventsOnDate(Date date) {
    log.append("Get Events On Date: \n")
        .append("Date: ").append(date.toString()).append("\n");
    return null;
  }

  @Override
  public Set<Event> getEventsOnDate(Date date, String name) throws IllegalArgumentException {
    log.append("Get Events On Date In Name: \n")
        .append("Date: ").append(date.toString()).append("\n")
        .append("Name: ").append(name).append("\n");
    ;
    return null;
  }

  @Override
  public Set<Event> getEventsInRange(Date startDateTime, Date endDateTime) {
    log.append("Get Events In Range: \n")
        .append("Start Date: ").append(startDateTime.toString()).append("\n")
        .append("End Date: ").append(endDateTime).append("\n");
    return null;
  }

  @Override
  public Set<Event> getEventsInRange(Date startDateTime, Date endDateTime, String name)
      throws IllegalArgumentException {
    log.append("Get Events In Range In Name: \n")
        .append("Start Date: ").append(startDateTime.toString()).append("\n")
        .append("End Date: ").append(endDateTime).append("\n")
        .append("Name: ").append(name).append("\n");
    return null;
  }

  @Override
  public boolean eventAt(Date dateTime) {
    log.append("Event At: \n")
        .append("Date: ").append(dateTime.toString()).append("\n");
    return false;
  }

  @Override
  public boolean eventAt(Date dateTime, String name) throws IllegalArgumentException {
    log.append("Event At In Name: \n")
        .append("Date: ").append(dateTime.toString()).append("\n")
        .append("Name: ").append(name).append("\n");
    ;
    return false;
  }

  @Override
  public void createCalendar(String name, TimeZone timeZone) throws IllegalArgumentException {
    log.append("Create Calendar: \n")
        .append("Name: ").append(name).append("\n")
        .append("Timezone: ").append(timeZone.toString()).append("\n");
  }

  @Override
  public void useCalendar(String name) throws IllegalArgumentException {
    log.append("Use Calendar: \n")
        .append("Name: ").append(name).append("\n");
  }

  @Override
  public void editCalendar(CalendarProperty calendarProperty, String name, Object newProperty)
      throws IllegalArgumentException {
    log.append("Edit Calendar: \n")
        .append("Calendar Property: ").append(calendarProperty).append("\n")
        .append("Name: ").append(name).append("\n")
        .append("New Property: ").append(newProperty).append("\n");
  }

  @Override
  public TimeZone getTimeZone() throws IllegalStateException {
    log.append("Get Time Zone: \n");
    return null;
  }

  @Override
  public TimeZone getTimeZone(String name) throws IllegalArgumentException {
    log.append("Get Time Zone: \n")
        .append("Name: ").append(name).append("\n");
    return null;
  }

  /**
   * Gets the log for verification in tests.
   *
   * @return the log as a string
   */
  public String getLog() {
    return log.toString();
  }
}
