package calendar.model;

import calendar.CalendarProperty;
import calendar.Property;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TimeZone;

/**
 * Implementation of model wrapper.
 */
public class ModelImpl implements Model {

  private CalendarModel currentCalendarModel;
  private HashMap<String, CalendarModel> calendars;

  /**
   * Public constructor.
   */
  public ModelImpl() {
    this.currentCalendarModel = null;
    this.calendars = new HashMap<>();
  }

  @Override
  public void createEvent(String subject, Date startDateTime, Date endDateTime)
      throws IllegalArgumentException {
    if (this.currentCalendarModel == null) {
      throw new IllegalArgumentException("No calendar currently selected to perform command.");
    }
    this.currentCalendarModel.createEvent(subject, startDateTime, endDateTime);

  }

  @Override
  public void createEvent(String subject, Date startDateTime, Date endDateTime, String name)
      throws IllegalArgumentException {
    if (this.calendars.get(name) == null) {
      throw new IllegalArgumentException("No calendar exists with given name.");
    }
    this.calendars.get(name).createEvent(subject, startDateTime, endDateTime);

  }

  @Override
  public void createReoccurringEvent(String subject, Date startDateTime, Date endDateTime,
      int[] weekdays, int repeat) throws IllegalArgumentException {
    if (this.currentCalendarModel == null) {
      throw new IllegalArgumentException("No calendar currently selected to perform command.");
    }
    this.currentCalendarModel.createReoccurringEvent(subject, startDateTime, endDateTime,
        weekdays, repeat);
  }

  @Override
  public void createReoccurringEvent(String subject, Date startDateTime, Date endDateTime,
      int[] weekdays, int repeat, String name)
      throws IllegalArgumentException {
    if (this.calendars.get(name) == null) {
      throw new IllegalArgumentException("No calendar exists with given name.");
    }
    this.calendars.get(name).createReoccurringEvent(subject, startDateTime, endDateTime,
        weekdays, repeat);

  }

  @Override
  public void createEventUntil(String subject, Date startDateTime, Date endDateTime, int[] weekdays,
      Date untilDate) throws IllegalArgumentException {
    if (this.currentCalendarModel == null) {
      throw new IllegalArgumentException("No calendar currently selected to perform command.");
    }
    this.currentCalendarModel.createEventUntil(subject, startDateTime, endDateTime,
        weekdays, untilDate);

  }

  @Override
  public void createEventUntil(String subject, Date startDateTime, Date endDateTime, int[] weekdays,
      Date untilDate, String name) throws IllegalArgumentException {
    if (this.calendars.get(name) == null) {
      throw new IllegalArgumentException("No calendar exists with given name.");
    }
    this.calendars.get(name).createEventUntil(subject, startDateTime, endDateTime,
        weekdays, untilDate);

  }

  @Override
  public void createAllDayEvent(String subject, Date startDate) throws IllegalArgumentException {
    if (this.currentCalendarModel == null) {
      throw new IllegalArgumentException("No calendar currently selected to perform command.");
    }
    this.currentCalendarModel.createAllDayEvent(subject, startDate);

  }

  @Override
  public void createAllDayEvent(String subject, Date startDate, String name)
      throws IllegalArgumentException {
    if (this.calendars.get(name) == null) {
      throw new IllegalArgumentException("No calendar exists with given name.");
    }
    this.calendars.get(name).createAllDayEvent(subject, startDate);

  }

  @Override
  public void createAllDayEventSeries(String subject, Date startDate, int[] weekdays, int repeat)
      throws IllegalArgumentException {
    if (this.currentCalendarModel == null) {
      throw new IllegalArgumentException("No calendar currently selected to perform command.");
    }
    this.currentCalendarModel.createAllDayEventSeries(subject, startDate, weekdays, repeat);

  }

  @Override
  public void createAllDayEventSeries(String subject, Date startDate, int[] weekdays, int repeat,
      String name) throws IllegalArgumentException {
    if (this.calendars.get(name) == null) {
      throw new IllegalArgumentException("No calendar exists with given name.");
    }
    this.calendars.get(name).createAllDayEventSeries(subject, startDate, weekdays, repeat);

  }

  @Override
  public void createAllDayEventUntil(String subject, Date startDate, int[] weekdays, Date untilDate)
      throws IllegalArgumentException {
    if (this.currentCalendarModel == null) {
      throw new IllegalArgumentException("No calendar currently selected to perform command.");
    }
    this.currentCalendarModel.createAllDayEventUntil(subject, startDate, weekdays, untilDate);

  }

  @Override
  public void createAllDayEventUntil(String subject, Date startDate, int[] weekdays, Date untilDate,
      String name) throws IllegalArgumentException {
    if (this.calendars.get(name) == null) {
      throw new IllegalArgumentException("No calendar exists with given name.");
    }
    this.calendars.get(name).createAllDayEventUntil(subject, startDate, weekdays, untilDate);

  }

  @Override
  public void editEvent(Property property, String subject, Date startDateTime, Date endDateTime,
      Object newProperty) throws IllegalArgumentException {
    if (this.currentCalendarModel == null) {
      throw new IllegalArgumentException("No calendar currently selected to perform command.");
    }
    this.currentCalendarModel.editEvent(property, subject, startDateTime,
        endDateTime, newProperty);

  }

  @Override
  public void editEvent(Property property, String subject, Date startDateTime, Date endDateTime,
      Object newProperty, String name) throws IllegalArgumentException {
    if (this.calendars.get(name) == null) {
      throw new IllegalArgumentException("No calendar exists with given name.");
    }
    this.calendars.get(name).editEvent(property, subject, startDateTime, endDateTime,
        newProperty);

  }

  @Override
  public void editEventsStartingOn(Property property, String subject, Date startDateTime,
      Object newProperty) throws IllegalArgumentException {
    if (this.currentCalendarModel == null) {
      throw new IllegalArgumentException("No calendar currently selected to perform command.");
    }
    this.currentCalendarModel.editEventsStartingOn(property, subject, startDateTime, newProperty);

  }

  @Override
  public void editEventsStartingOn(Property property, String subject, Date startDateTime,
      Object newProperty, String name)
      throws IllegalArgumentException {
    if (this.calendars.get(name) == null) {
      throw new IllegalArgumentException("No calendar exists with given name.");
    }
    this.calendars.get(name).editEventsStartingOn(property, subject, startDateTime, newProperty);

  }

  @Override
  public void editSeries(Property property, String subject, Date startDateTime, Object newProperty)
      throws IllegalArgumentException {
    if (this.currentCalendarModel == null) {
      throw new IllegalArgumentException("No calendar currently selected to perform command.");
    }
    this.currentCalendarModel.editSeries(property, subject, startDateTime, newProperty);

  }

  @Override
  public void editSeries(Property property, String subject, Date startDateTime, Object newProperty,
      String name) throws IllegalArgumentException {
    if (this.calendars.get(name) == null) {
      throw new IllegalArgumentException("No calendar exists with given name.");
    }
    this.calendars.get(name).editSeries(property, subject, startDateTime, newProperty);

  }

  @Override
  public Set<Event> getEventsOnDate(Date date) {
    if (this.currentCalendarModel == null) {
      throw new IllegalArgumentException("No calendar currently selected to perform command.");
    }
    return this.currentCalendarModel.getEventsOnDate(date);
  }

  @Override
  public Set<Event> getEventsOnDate(Date date, String name) throws IllegalArgumentException {
    if (this.calendars.get(name) == null) {
      throw new IllegalArgumentException("No calendar exists with given name.");
    }
    return this.calendars.get(name).getEventsOnDate(date);
  }

  @Override
  public Set<Event> getEventsInRange(Date startDateTime, Date endDateTime) {
    if (this.currentCalendarModel == null) {
      throw new IllegalArgumentException("No calendar currently selected to perform command.");
    }
    return this.currentCalendarModel.getEventsInRange(startDateTime, endDateTime);
  }

  @Override
  public Set<Event> getEventsInRange(Date startDateTime, Date endDateTime, String name)
      throws IllegalArgumentException {
    if (this.calendars.get(name) == null) {
      throw new IllegalArgumentException("No calendar exists with given name.");
    }
    return this.calendars.get(name).getEventsInRange(startDateTime, endDateTime);
  }

  @Override
  public boolean eventAt(Date dateTime) {
    if (this.currentCalendarModel == null) {
      throw new IllegalArgumentException("No calendar currently selected to perform command.");
    }
    return this.currentCalendarModel.eventAt(dateTime);
  }

  @Override
  public boolean eventAt(Date dateTime, String name) throws IllegalArgumentException {
    if (this.calendars.get(name) == null) {
      throw new IllegalArgumentException("No calendar exists with given name.");
    }
    return this.calendars.get(name).eventAt(dateTime);
  }

  @Override
  public void createCalendar(String name, TimeZone timeZone) throws IllegalArgumentException {
    if (name == null || timeZone == null) {
      throw new IllegalArgumentException("Calendar must have name and timezone");
    }
    if (this.calendars.get(name) != null) {
      throw new IllegalArgumentException("Calendar already exists with name.");
    }
    CalendarModel newCalendar = new CalendarModel(timeZone);
    this.calendars.put(name, newCalendar);

  }

  @Override
  public void useCalendar(String name) throws IllegalArgumentException {
    if (this.calendars.get(name) == null) {
      throw new IllegalArgumentException("No calendar exists with given name.");
    }
    this.currentCalendarModel = this.calendars.get(name);

  }

  @Override
  public void editCalendar(CalendarProperty calendarProperty, String name, Object newProperty)
      throws IllegalArgumentException {
    if (calendarProperty == null || name == null || newProperty == null) {
      throw new IllegalArgumentException("Can not have null argument with call to edit calendar.");
    }
    if (this.calendars.get(name) == null) {
      throw new IllegalArgumentException("No calendar exists with given name.");
    }
    if (calendarProperty == CalendarProperty.NAME) {
      if (!(newProperty instanceof String)) {
        throw new IllegalArgumentException("New name must be a string.");
      }
      if (this.calendars.get(String.valueOf(newProperty)) != null) {
        throw new IllegalArgumentException("Can not change calendar name to existing"
            + " calendar name.");
      }
      CalendarModel calendarModelUnderSearch = this.calendars.get(name);
      this.calendars.remove(name);
      this.calendars.put((String) newProperty, calendarModelUnderSearch);
    } else {
      if (!(newProperty instanceof TimeZone)) {
        throw new IllegalArgumentException("New name must be a string.");
      }
      this.calendars.get(name).setTimeZone((TimeZone) newProperty);
    }

  }

  @Override
  public TimeZone getTimeZone() throws IllegalStateException {
    if (this.currentCalendarModel == null) {
      throw new IllegalStateException("No active calendar.");
    }
    return this.currentCalendarModel.getTimeZone();
  }

  @Override
  public TimeZone getTimeZone(String name) throws IllegalArgumentException {
    if (this.calendars.get(name) == null) {
      throw new IllegalArgumentException("No calendar with given name.");
    }
    return this.calendars.get(name).getTimeZone();
  }


}
