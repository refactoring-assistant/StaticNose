package calendar.model.debug;

import calendar.model.Calendar;
import calendar.model.Date;
import calendar.model.Day;
import calendar.model.IntEvent;
import calendar.model.Location;
import calendar.model.Status;
import calendar.model.Time;
import java.io.IOException;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

/**
 * Debug implementation of a calendar that allows for testing of the input given
 * to a calendar by a Controller.
 */
public class DebugCalendar extends Calendar {
  private final String name;
  public final Appendable log;

  /**
   * DebugCalendar constructor.
   *
   * @param name the name of the calendar
   * @param log  the appendable where the logging for input commands from
   *             the Controller will be stored.
   */
  public DebugCalendar(String name, Appendable log) {
    super(name);
    this.name = Objects.requireNonNull(name);
    this.log = Objects.requireNonNull(log);
  }

  @Override
  public void createEvent(String subject, Date startDate, Time startTime, Date endDate,
                          Time endTime, String description, Location location,
                          Status status) {
    try {
      log.append(subject + startDate + startTime + endDate + endTime
          + description + location + status);
    } catch (IOException e) {
      throw new RuntimeException("Could not write to log");
    }
  }

  @Override
  public void createEvent(String subject, Date startDate, Time startTime, Date endDate,
                          Time endTime) {
    try {
      log.append(subject + startDate + startTime + endDate + endTime);
    } catch (IOException e) {
      throw new RuntimeException("Could not write to log");
    }
  }

  @Override
  public void createAllDayEvent(String subject, Date date, String description,
                                Location location, Status status) {
    try {
      log.append(subject + date + description + location + status);
    } catch (IOException e) {
      throw new RuntimeException("Could not write to log");
    }
  }

  @Override
  public void createAllDayEvent(String subject, Date date) {
    try {
      log.append(subject + date);
    } catch (IOException e) {
      throw new RuntimeException("Could not write to log");
    }
  }

  @Override
  public void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                                Set<Day> repeatDays, int occurrences, String description,
                                Location location, Status status) {

    try {
      log.append(subject + startDate + startTime + endTime + new TreeSet<>(repeatDays)
          + occurrences + description + location + status);
    } catch (IOException e) {
      throw new RuntimeException("Could not write to log");
    }
  }

  @Override
  public void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                                Set<Day> repeatDays, int occurrences) {
    try {
      log.append(subject + startDate + startTime + endTime
          + new TreeSet<>(repeatDays) + occurrences);
    } catch (IOException e) {
      throw new RuntimeException("Could not write to log");
    }
  }

  @Override
  public void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                                Set<Day> repeatDays, Date endDate, String description,
                                Location location, Status status) {
    try {
      log.append(subject + startDate + startTime + endTime + new TreeSet<>(repeatDays)
          + endDate + description + location + status);
    } catch (IOException e) {
      throw new RuntimeException("Could not write to log");
    }
  }

  @Override
  public void createEventSeries(String subject, Date startDate, Time startTime, Time endTime,
                                Set<Day> repeatDays, Date endDate) {
    try {
      log.append(subject + startDate + startTime + endTime + new TreeSet<>(repeatDays) + endDate);
    } catch (IOException e) {
      throw new RuntimeException("Could not write to log");
    }
  }

  @Override
  public void editEvent(String subject, Date startDate, Time startTime, Date endDate, Time endTime,
                        String property, String newValue) {
    try {
      log.append(subject + startDate + startTime + endDate + endTime + property + newValue);
    } catch (IOException e) {
      throw new RuntimeException("Could not write to log");
    }
  }

  @Override
  public void editEventsFromDate(String subject, Date startDate, Time startTime, String property,
                                 String newValue) {
    try {
      log.append(subject + startDate + startTime + property + newValue);
    } catch (IOException e) {
      throw new RuntimeException("Could not write to log");
    }
  }

  @Override
  public void editSeries(String subject, Date startDate, Time startTime, String property,
                         String newValue) {
    try {
      log.append(subject + startDate + startTime + property + newValue);
    } catch (IOException e) {
      throw new RuntimeException("Could not write to log");
    }
  }

  @Override
  public List<IntEvent> getEventsOnDate(Date date) {
    try {
      log.append(date.toString());
    } catch (IOException e) {
      throw new RuntimeException("Could not write to log");
    }
    return Collections.emptyList();
  }

  @Override
  public boolean isBusy(Date date, Time time) {
    try {
      log.append(date.toString() + time.toString());
    } catch (IOException e) {
      throw new RuntimeException("Could not write to log");
    }
    return false;
  }

  @Override
  public String export(String fileName) {
    try {
      log.append(fileName);
    } catch (IOException e) {
      throw new RuntimeException("Could not write to log");
    }
    return "export";
  }

  @Override
  public DebugCalendar withName(String newName) {
    if (newName == null || newName.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    return new DebugCalendar(newName, this.log);
  }

  @Override
  public DebugCalendar withTimezone(ZoneId newTimezone) {
    if (newTimezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }
    return new DebugCalendar(this.name, this.log);
  }
}
