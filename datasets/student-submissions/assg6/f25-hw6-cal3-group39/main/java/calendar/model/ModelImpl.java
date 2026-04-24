package calendar.model;

import calendar.controller.CopySpec;
import calendar.controller.CreateSpec;
import calendar.controller.EditSpec;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Below is the model implementation of the MVC architecture. The model supports the
 * functionalities of a working virtual calendar, these functionalities include, creating an event
 * or a series of events, editing an event or events, along with querying events on a specific date
 * or time and also between an interval of dates.
 * The model uses a TreeMap to represent the calendar. Additionally, it has two static fields
 * weekdayMap and internalMap to map weekdays to numbers.
 */
public class ModelImpl implements Model {

  HashMap<String, InterfaceCalendar> calendars = new HashMap<>();
  static Map<Character, Integer> weekdayMap = new HashMap<>();
  static Map<String, Character> internalMap = new HashMap<>();
  static Random rand = new Random();

  /**
   * The modelImpl constructor.
   */
  public ModelImpl() {
    weekdayMap.put('M', 0);
    weekdayMap.put('T', 1);
    weekdayMap.put('W', 2);
    weekdayMap.put('R', 3);
    weekdayMap.put('F', 4);
    weekdayMap.put('S', 5);
    weekdayMap.put('U', 6);

    internalMap.put("MONDAY", 'M');
    internalMap.put("TUESDAY", 'T');
    internalMap.put("WEDNESDAY", 'W');
    internalMap.put("THURSDAY", 'R');
    internalMap.put("FRIDAY", 'F');
    internalMap.put("SATURDAY", 'S');
    internalMap.put("SUNDAY", 'U');
  }

  /**
   * The function below is used to create a calendar.
   *
   * @param name     name of calendar.
   * @param timezone timezone of calendar
   * @throws IllegalArgumentException if input is wrong or if the calendar which the user is trying
   *                                  to create already exists.
   */
  @Override
  public void createCalendar(String name, String timezone) {

    if (calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar Already Exists " + name);
    }

    Calendar cal = new Calendar(timezone);
    calendars.put(name, cal);
  }


  /**
   * The method below is used to edit a calendar.
   *
   * @param name         the name of calendar.
   * @param property     the property of the calendar to be edited.
   * @param newPropValue the new property value
   * @throws IllegalArgumentException if input is wrong, edited calendar already exists or unknow
   *                                  property entered.
   */
  @Override
  public void editCalendar(String name, String property, String newPropValue) {

    if (!calendars.containsKey(name)) {
      throw new IllegalArgumentException("Calendar Does Not Exists " + name);
    }
    InterfaceCalendar cal = calendars.get(name);
    if (property.equals("name")) {
      if (calendars.containsKey(newPropValue)) {
        throw new IllegalArgumentException("Calendar Already Exists " + newPropValue);
      } else {
        calendars.remove(name);
        calendars.put(newPropValue, cal);
      }
    } else if (property.equals("timezone")) {
      ZoneId zone = ZoneId.of(newPropValue);
      cal.setZone(zone);
    } else {
      throw new IllegalArgumentException("Property can be either Name or TimeZone");
    }
  }

  /**
   * The create function of the model, it is used to create a single event or events
   * starting and ending at specific times or creating an all day event or events (8am to 5pm).
   * It first manipulates the input string, so that it is in the right format to use
   * LocalTime.Parse().
   * It then creates the first event, then checks if this is a series/ recurrent event and calls
   * the createRecurrentEvents function. If not then it adds the first event to the calendar.
   *
   * @param createDto The Create Data Transfer object.
   * @throws IllegalArgumentException if start date or time is after end date or time or if
   *                                  recurrent event spans multiple days.
   */
  public void create(String calName, CreateSpec createDto) {

    TreeMap<LocalDate, HashSet<Event>> calendar = getCalendar(calName);
    String subject = createDto.getSubject();
    LocalDate startDate = createDto.getStartDate();
    LocalDate endDate = createDto.getEndDate();
    LocalTime startTime = createDto.getStartTime();
    LocalTime endTime = createDto.getEndTime();
    Event first =
        buildEventFromCommand(createDto, subject, startDate, startTime, endDate, endTime, -1);
    if (createDto.getWeekdays() != null) {
      createRecurrentEvents(calendar, createDto, first);
    } else {
      checkedAdd(calendar, first);
    }
  }

  /**
   * Builds an calendar.Model.Event object from the user command array.
   *
   * @param subject   the subject of the event
   * @param startDate the start date of the event
   * @param startTime the start time of the event
   * @param endDate   the end date of the event
   * @param endTime   the end time of the event
   * @param seriesId  the series ID, -1 for single events
   * @param createDto the create data transfer object
   * @return the calendar.Model.Event object
   */
  private Event buildEventFromCommand(CreateSpec createDto, String subject, LocalDate startDate,
                                      LocalTime startTime,
                                      LocalDate endDate, LocalTime endTime, int seriesId) {
    Event.Builder builder =
        new Event.Builder(subject, startDate, startTime, endDate, endTime).seriesId(seriesId);

    if (createDto != null) {
      builder.description(createDto.getDescription());
      builder.location(createDto.getLocation());
      builder.status(createDto.getStatus());
    }

    return builder.build();
  }

  /**
   * The below method is used to create a series of recurrent events. It is passed an allDayFlag
   * from Create() method to indicate if all-day events have to be created. Based on this it
   * extracts the weekdays substring. It then checks if an event has to be repeated 'n times' or
   * 'until' a specific date, by checking if 'times' is present in the string or not.
   * It then accordingly sets the value of 'ntimes' and 'until'.
   * The create recurrent dates method is then called to give us the list of all dates on which
   * this event will happen.
   * Finally, when we have the dates, we create an event for each date and add it to the calendar.
   *
   * @param first     The first event of this series of events.
   * @param createDto The data transfer object
   * @param calendar  The calendar in use
   * @throws IllegalArgumentException if any of the events of the series already exists.
   */
  private void createRecurrentEvents(TreeMap<LocalDate, HashSet<Event>> calendar,
                                     CreateSpec createDto, Event first) {
    String weekdays = createDto.getWeekdays();
    LocalDate until = createDto.getUntil();
    int ntimes = createDto.getTimes();

    List<LocalDate> recurrentDates = this.createRecurrentDates(first, weekdays, ntimes, until);
    int seriesId = rand.nextInt(10000);
    List<Event> recurrentEvents = new ArrayList<>();
    for (LocalDate date : recurrentDates) {
      Event newEvent =
          buildEventFromCommand(createDto, first.getSubject(), date, first.getStartTime(), date,
              first.getEndTime(), seriesId);
      if (calendar.get(newEvent.getStartDate()) != null
          && calendar.get(newEvent.getStartDate()).contains(newEvent)) {
        throw new IllegalArgumentException("Event already exists");
      } else {
        recurrentEvents.add(newEvent);
      }
    }
    for (Event e : recurrentEvents) {
      this.addEvent(calendar, e);
    }
  }


  /**
   * The below method checks if a newly created event already exists, it not then it adds it to
   * the calendar. If it does then it throws an IllegalArgumentException.
   *
   * @param newEvent The new event which is to be added.
   * @throws IllegalArgumentException if the event to be added already exists.
   */
  private void checkedAdd(Map<LocalDate, HashSet<Event>> calendar, Event newEvent) {
    LocalDate startDate = newEvent.getStartDate();
    if (calendar.containsKey(startDate) && calendar.get(startDate).contains(newEvent)) {
      throw new IllegalArgumentException("Event already exists!");
    }
    this.addEvent(calendar, newEvent);
  }

  /**
   * The method below is used to create the list of all dates on which an event series happens.
   * It calculates this from the start date of the series, either by the event repeating 'ntimes'
   * or repeating 'until' a specific date.
   * It uses the 'should continue' method to decide if 'ntimes' or 'until' is supposed to
   * be considered.
   */
  private List<LocalDate> createRecurrentDates(Event first, String weekdays, int num,
                                               LocalDate end) {
    char[] days = orderedWeekdays(first, weekdays);
    List<LocalDate> list = new ArrayList<>();
    list.add(first.getStartDate());
    LocalDate temp = first.getStartDate();

    while (shouldContinue(temp, num, end)) {
      for (int i = 0; i < days.length; i++) {
        int next = (i + 1) % days.length;
        int diff = (weekdayMap.get(days[next]) - weekdayMap.get(days[i]) + 7) % 7;
        diff = diff == 0 ? 7 : diff;
        LocalDate nextDate = temp.plusDays(diff);
        list.add(nextDate);
        temp = nextDate;
        if (num != -1) {
          num--;
          if (num < 2) {
            return list;
          }
        } else if (!temp.isBefore(end)) {
          return list;
        }
      }
    }
    return list;
  }

  /**
   * The method below decides if a while loop in createRecurrentDates should continue or stop.
   *
   * @param temp The previous event of the series, null if we are using until.
   * @param num  The number of times an event should be repeated, -1 if we are using until
   * @param end  The date until when an event should be repeated, null if are using num.
   * @return true if we should continue else false.
   */
  private boolean shouldContinue(LocalDate temp, int num, LocalDate end) {
    if (num != -1) {
      return num > 1;
    }
    return temp.isBefore(end);
  }

  /**
   * The below method reorders the weekdays given the start day of the series. For example if a
   * series happens on MWF and starts on F, then the new order becomes FMW.
   * Additionally, if the command is to create a recurrent event starting on 2025-11-01 (Sat) which
   * repeats on MRU then the start date becomes 2025-11-02 with the weekdays as UMR.
   *
   * @param first    The first event of the series.
   * @param weekdays The days on which this series occurs.
   * @return The reordered weekdays.
   */
  private char[] orderedWeekdays(Event first, String weekdays) {
    char startDay = internalMap.get(first.getStartDate().getDayOfWeek().toString());

    if (weekdays.indexOf(startDay) == -1) {
      int currentDayIndex = weekdayMap.get(startDay);
      int minDiff = 7;
      char nextDay = startDay;

      for (char day : weekdays.toCharArray()) {
        int diff = (weekdayMap.get(day) - currentDayIndex + 7) % 7;
        if (diff > 0 && diff < minDiff) {
          minDiff = diff;
          nextDay = day;
        }
      }
      if (nextDay != startDay) {
        first.setStartDate(first.getStartDate().plusDays(minDiff));
        first.setEndDate(first.getEndDate().plusDays(minDiff));
        startDay = nextDay;
      }
    }
    int index = weekdays.indexOf(startDay);
    if (index > 0) {
      weekdays = weekdays.substring(index) + weekdays.substring(0, index);
    }
    return weekdays.toCharArray();
  }

  /**
   * The below method adds an event to the calendar.
   *
   * @param e The event to be added.
   */
  private void addEvent(Map<LocalDate, HashSet<Event>> calendar, Event e) {
    calendar.computeIfAbsent(e.getStartDate(), k -> new HashSet<>()).add(e);
  }


  /**
   * Below is the edit function of the model, it supports the functionality of editing both a
   * single event and a series.
   * It first manipulates the input string, so that it is in the right format to use
   * LocalTime.Parse().
   * The method first creates an event object given the input String by the user. Using this object
   * we find other events of the series.
   * The DaysDiff and minutesDiff values are computed here to avoid calculating multiple times in
   * following helper functions.
   * This method based on if a single event or a series of events have to be edited calls
   * 'editEvent' or 'editEvents'.
   * Please note, editing the start time of an event or subset of a series makes the event/subset
   * break out of that series.
   *
   * @param editDto The edit data transfer object.
   * @throws IllegalArgumentException if edited end date or time is before start date or time.
   */
  public void edit(String calName, EditSpec editDto) {

    TreeMap<LocalDate, HashSet<Event>> calendar = getCalendar(calName);
    String subject = editDto.getSubject();
    String property = editDto.getProperty();
    String newPropValue = editDto.getNewPropValue();
    LocalDate startDate = editDto.getStartDate();
    LocalTime startTime = editDto.getStartTime();
    LocalDate endDate = editDto.getEndDate();
    LocalTime endTime = editDto.getEndTime();
    long daysDiff = editDto.getDaysDiff();
    long minutesDiff = editDto.getMinsDiff();

    Event temp = buildEventFromCommand(null, subject, startDate, startTime, endDate,
        endTime, -1);

    if (editDto.getType().equals("event")) {
      editEvent(calendar, temp, property, newPropValue, daysDiff, minutesDiff);
    } else if (editDto.getType().equals("events")) {
      editEvents(calendar, temp, property, newPropValue, daysDiff, minutesDiff, 0);
    } else {
      editEvents(calendar, temp, property, newPropValue, daysDiff, minutesDiff, 1);
    }
  }

  @Override
  public String allCals() {
    return String.join(",", calendars.keySet());
  }

  @Override
  public String calTimezone(String calName) {
    if (calendars.containsKey(calName)) {
      return calendars.get(calName).getZone().toString();
    } else {
      throw new IllegalArgumentException("Calendar does not exits");
    }
  }

  @Override
  public boolean exists(String calName) {
    return calendars.containsKey(calName);
  }

  /**
   * The below method edits a single event in the calendar. It gets the submap of the TreeMap which
   * corresponds to only the row/ entry in which this event is stored.
   * It then calls the makeEdits() method to execute the changed.
   *
   * @param temp         A copy of the event to be edited.
   * @param property     The property of the event which has to be edited.
   * @param newPropValue The new value of the property to be edited.
   * @param daysDiff     The number of days to add or subtract if property is start or end.
   * @param minsDiff     The number if mins to add or subtract if the property is start or end.
   * @throws IllegalArgumentException if no events are found for the start date.
   *
   */
  private void editEvent(TreeMap<LocalDate, HashSet<Event>> calendar, Event temp, String property,
                         String newPropValue, long daysDiff, long minsDiff) {

    HashSet<Event> firstEntry = calendar.get(temp.getStartDate());
    if (firstEntry == null || !firstEntry.contains(temp)) {
      throw new IllegalArgumentException("No event found");
    }

    List<Event> events = new ArrayList<>();
    for (Event e : firstEntry) {
      if (temp.equals(e)) {
        events.add(e);
      }
    }
    if (events.isEmpty()) {
      throw new IllegalArgumentException("No events found");
    } else if (events.size() > 1) {
      throw new IllegalArgumentException("Multiple events found");
    }
    temp.setSeriesId(events.get(0).getSeriesId());
    SortedMap<LocalDate, HashSet<Event>> subMap =
        calendar.subMap(temp.getStartDate(), true, temp.getStartDate(), true);
    makeEdits(calendar, subMap, temp, property, newPropValue, daysDiff, minsDiff);
  }

  /**
   * The edit events method which is used to edit a full or part of series using the field
   * 'series_id' of the events in the series.
   * It uses only the entries given below the start date if part of the series has to be modified.
   * Else is uses the entire calendar.
   *
   * @param temp         A copy of an event in the series to be edited.
   * @param property     The property of the event which has to be edited.
   * @param newPropValue The new value of the property to be edited.
   * @param daysDiff     The number of days to add or subtract if property is start or end.
   * @param minsDiff     The number if mins to add or subtract if the property is start or end.
   * @param flag         A flag to indicate if the full series or a part of it has to be edited.
   * @throws IllegalArgumentException if no events are found for the start date or if multiple
   *                                  events are found or if edited end date or time is before
   *                                  start date or time
   */
  private void editEvents(TreeMap<LocalDate, HashSet<Event>> calendar, Event temp, String property,
                          String newPropValue, long daysDiff, long minsDiff, int flag) {

    HashSet<Event> firstEntry = calendar.get(temp.getStartDate());
    if (firstEntry == null) {
      throw new IllegalArgumentException("No events found");
    }

    List<Event> events = new ArrayList<>();
    for (Event e : firstEntry) {
      if (temp.getSubject().equals(e.getSubject())
          && temp.getStartTime().equals(e.getStartTime())) {
        events.add(e);
      }
    }
    if (events.isEmpty()) {
      throw new IllegalArgumentException("No events found");
    } else if (events.size() > 1) {
      throw new IllegalArgumentException("Multiple events found");
    }
    if (property.equals("end")) {
      LocalDate endDate = events.get(0).getEndDate();
      LocalTime endTime = events.get(0).getEndTime();
      daysDiff = ChronoUnit.DAYS.between(endDate, returnDate(newPropValue));
      minsDiff = ChronoUnit.MINUTES.between(endTime, returnTime(newPropValue));
      if ((endDate.plusDays(daysDiff).isBefore(temp.getStartDate()))
          || (endTime.plusMinutes(minsDiff).isBefore(temp.getStartTime()))) {
        throw new IllegalArgumentException("End date/time cannot be before Start date/time");
      }
    }

    temp.setSeriesId(events.get(0).getSeriesId());
    SortedMap<LocalDate, HashSet<Event>> subMap;
    if (flag == 0) {
      subMap = calendar.tailMap(temp.getStartDate(), true);
    } else {
      subMap = calendar;
    }
    makeEdits(calendar, subMap, temp, property, newPropValue, daysDiff, minsDiff);
  }

  /**
   * The below method is responsible for executing the edits in the calendar TreeMap.
   * It is passed a submap (subset) of the TreeMap on which it iterates row by row, it checks if the
   * event/ series to be edited is present for that key, if yes then it edits it.
   * After editing an event, it assigns the same series ID to it which was assigned to the first
   * edited event of this series.
   * It throws an exception if multiple events have the same properties that were specified.
   *
   * @param subMap       The submap/subset of the calendar TreeMao
   * @param temp         A copy of an event in the series to be edited.
   * @param property     The property of the event which has to be edited.
   * @param newPropValue The new value of the property to be edited.
   * @param daysDiff     The number of days to add or subtract if property is start or end.
   * @param minsDiff     The number if mins to add or subtract if the property is start or end.
   */
  private void makeEdits(Map<LocalDate, HashSet<Event>> calendar,
                         Map<LocalDate, HashSet<Event>> subMap, Event temp, String property,
                         String newPropValue, long daysDiff, long minsDiff) {
    List<Event> toMove = new ArrayList<>();
    for (Map.Entry<LocalDate, HashSet<Event>> entry : subMap.entrySet()) {
      HashSet<Event> events = entry.getValue();
      Iterator<Event> it = events.iterator();
      List<Event> toAdd = new ArrayList<>();
      while (it.hasNext()) {
        Event e = it.next();
        if (checkEquals(temp, e)) {
          it.remove();
          e.setSeriesId(temp.getSeriesId());
          if (property.equals("start")) {
            toMove.add(e);
          } else {
            changeProp(calendar, e, property, newPropValue, daysDiff, minsDiff);
            toAdd.add(e);
          }
        }
      }
      for (Event e : toAdd) {
        checkedAdd(calendar, e);
      }
    }
    int newId = rand.nextInt();
    if (property.equals("start")) {
      for (Event e : toMove) {
        changeProp(calendar, e, property, newPropValue, daysDiff, minsDiff);
        try {
          checkedAdd(calendar, e);
          if (minsDiff != 0) {
            e.setSeriesId(newId);
          }
        } catch (IllegalArgumentException ignored) {
          //ignored
        }
      }
    }
  }

  /**
   * A method which checks quality using .equals() method for editEvent() function and series_id
   * for editEvents() function.
   *
   * @param temp A copy of the event to be edited.
   * @param e    An event object which has to be checked for equality.
   * @return true if the two events are equal or part of the same series else false.
   */
  private boolean checkEquals(Event temp, Event e) {
    if (temp.getEndDate() == null) {
      if (e.getSeriesId() != -1) {
        return temp.getSeriesId() == e.getSeriesId();
      } else {
        return temp.getSubject().equals(e.getSubject())
            && temp.getStartDate().equals(e.getStartDate())
            && temp.getStartTime().equals(e.getStartTime());
      }
    } else {
      return temp.equals(e);
    }
  }

  /**
   * The changeProp method below uses a switch case to change the property value inputted by the
   * user. Start Date is the key of our calendar TreeMap hence changing it results in us to re-add
   * this event back to the TreeMap under a new key.
   *
   * @param e            The event e which has to be edited.
   * @param property     The property of the event which has to be edited.
   * @param newPropValue The new value of the property to be edited.
   * @param daysDiff     The number of days to add or subtract if property is start or end.
   * @param minsDiff     The number if mins to add or subtract if the property is start or end.
   * @throws IllegalArgumentException if unknown property is entered or the new event created after
   *                                  editing already exits.
   */
  private void changeProp(Map<LocalDate, HashSet<Event>> calendar, Event e, String property,
                          String newPropValue, long daysDiff, long minsDiff) {

    if (property.equals("start")) {
      changeStartEnd(e, daysDiff, minsDiff, property);
    } else if (property.equals("end")) {
      changeStartEnd(e, daysDiff, minsDiff, property);
    } else if (property.equals("subject")) {
      e.setSubject(newPropValue);
    } else if (property.equals("description")) {
      e.setDescription(newPropValue);
    } else if (property.equals("location")) {
      e.setLocation(newPropValue);
    } else if (property.equals("status")) {
      e.setStatus(newPropValue);
    } else {
      throw new IllegalArgumentException("Unknown Property");
    }
  }

  /**
   * The method below is used to modify the start and end dates of the calendar.Model.Event
   * object which is to be
   * edited.
   *
   * @param e        The event object to be edited.
   * @param daysDiff The number of days to be added or subtracted.
   * @param minsDiff The number of minutes to be added or subtracted.
   * @param property The property which has to be modified.
   */
  private void changeStartEnd(Event e, long daysDiff, long minsDiff, String property) {

    if (property.equals("start")) {
      e.setStartDate(e.getStartDate().plusDays(daysDiff));
      e.setStartTime(e.getStartTime().plusMinutes(minsDiff));
    }
    e.setEndDate(e.getEndDate().plusDays(daysDiff));
    e.setEndTime(e.getEndTime().plusMinutes(minsDiff));
  }

  /**
   * The method below parses a date in the form of a String to a LocalDate object.
   *
   * @param input DateTime user input String.
   * @return A localDate object.
   */
  private LocalDate returnDate(String input) {
    return LocalDate.parse(input.substring(0, input.indexOf("T")));
  }

  /**
   * The method below parses time in the form of a String to a LocalTime object.
   *
   * @param input DateTime user input String.
   * @return A localTime object.
   */
  private LocalTime returnTime(String input) {
    return LocalTime.parse(input.substring(input.indexOf("T") + 1));
  }

  /**
   * The below method returns the specified calendar.
   *
   * @param calName the name of calendar.
   * @return The TreeMap calendar.
   * @throws IllegalArgumentException if calendar does not exist.
   */
  private TreeMap<LocalDate, HashSet<Event>> getCalendar(String calName) {
    InterfaceCalendar calendarObj = calendars.get(calName);
    if (calendarObj == null) {
      throw new IllegalArgumentException("Calendar Does not exist " + calName);
    } else {
      return calendarObj.getMap();
    }
  }

  /**
   * This is the copy method, acts as the entrance for the copy commands logic.
   *
   * @param calName Name of context calendar.
   * @param copyDto the copy spec data transfer object.
   */
  public void copy(String calName, CopySpec copyDto) {

    LocalDate endDate = copyDto.getEndDate();
    LocalTime startTime = copyDto.getStartTime();

    if (endDate != null) {
      copyEvents(calName, copyDto);
    } else if (startTime == null) {
      copyEvents(calName, copyDto);
    } else {
      copyEvent(calName, copyDto);
    }
  }

  /**
   * Copy event method.
   *
   * @param calName is cal name.
   * @param copyDto is the copy data transfer object.
   * @throws IllegalArgumentException if no events are found for the start date or if multiple
   *                                  events are found or target cal does not exist
   */
  private void copyEvent(String calName, CopySpec copyDto) {
    if (!exists(copyDto.getTargetCalName())) {
      throw new IllegalArgumentException("Target calendar does not exist "
          + copyDto.getTargetCalName());
    }

    TreeMap<LocalDate, HashSet<Event>> calendar = getCalendar(calName);
    LocalDate startDate = copyDto.getStartDate();
    LocalTime startTime = copyDto.getStartTime();

    if (!calendar.containsKey(startDate)) {
      throw new IllegalArgumentException("Event does not exist");
    }
    HashSet<Event> events = calendar.get(startDate);
    int count = 0;
    Event toCopy = null;
    for (Event e : events) {
      if (e.getSubject().equals(copyDto.getSubject()) && e.getStartTime().equals(startTime)) {
        count++;
        toCopy = e.copy();
      }
    }
    if (count > 1 || count == 0) {
      throw new IllegalArgumentException("Multiple or no events found");
    }

    LocalDate targetDate = copyDto.getTargetDate();
    LocalTime targetTime = copyDto.getTargetTime();
    setDateTime(toCopy, targetDate, targetTime, ZoneId.of(calTimezone(calName)),
        ZoneId.of(calTimezone(copyDto.getTargetCalName())));
    checkedAdd(getCalendar(copyDto.getTargetCalName()), toCopy);
  }

  /**
   * the copy Events method.
   *
   * @param calName is cal name.
   * @param copyDto is the copy data transfer object.
   * @throws IllegalArgumentException if no events are found for the start date or if multiple
   *                                  events are found or target cal does not exist
   */
  private void copyEvents(String calName, CopySpec copyDto) {

    if (!exists(copyDto.getTargetCalName())) {
      throw new IllegalArgumentException("Target calendar does not exist "
          + copyDto.getTargetCalName());
    }
    TreeMap<LocalDate, HashSet<Event>> calendar = getCalendar(calName);
    SortedMap<LocalDate, HashSet<Event>> subCal;
    if (copyDto.getEndDate() != null) {
      subCal = calendar.subMap(copyDto.getStartDate(), true, copyDto.getEndDate(),
          true);
    } else {
      subCal = calendar.subMap(copyDto.getStartDate(), true, copyDto.getStartDate(),
          true);
    }
    LocalDate targetDate = copyDto.getTargetDate();
    LocalDate curPrev = null;
    LocalDate targetPrev = targetDate;
    long days;
    List<Event> toAdd = new ArrayList<>();
    for (Map.Entry<LocalDate, HashSet<Event>> entry : subCal.entrySet()) {
      HashSet<Event> events = entry.getValue();
      for (Event e : events) {
        Event toCopy = e.copy();
        if (curPrev != null) {
          days = ChronoUnit.DAYS.between(curPrev, toCopy.getStartDate());
          targetDate = targetPrev.plusDays(days);
        }
        curPrev = toCopy.getStartDate();
        setDateTime(toCopy, targetDate, null, ZoneId.of(calTimezone(calName)),
            ZoneId.of(calTimezone(copyDto.getTargetCalName())));
        targetPrev = toCopy.getStartDate();
        toAdd.add(toCopy);
      }
    }
    for (Event e : toAdd) {
      try {
        checkedAdd(getCalendar(copyDto.getTargetCalName()), e);
      } catch (IllegalArgumentException ignored) {
        //ignored
      }
    }
  }

  /**
   * The below method sets the Date and Time when converted when events are copied to a different
   * timezone.
   *
   * @param toCopy     The event to be copied.
   * @param targetDate the date to be copied on
   * @param targetTime the time to be copied on
   * @param cur        the current timezone
   * @param target     the next timezone
   * @throws IllegalArgumentException if on copying a recurring event spans two or more days.
   */
  void setDateTime(Event toCopy, LocalDate targetDate, LocalTime targetTime, ZoneId cur,
                   ZoneId target) {

    ZonedDateTime srcStart = ZonedDateTime.of(toCopy.getStartDate(), toCopy.getStartTime(), cur);
    ZonedDateTime srcEnd = ZonedDateTime.of(toCopy.getEndDate(), toCopy.getEndTime(), cur);
    ZonedDateTime targetStart = srcStart.withZoneSameInstant(target);
    ZonedDateTime targetEnd = srcEnd.withZoneSameInstant(target);
    long durationMinutes = Duration.between(srcStart, srcEnd).toMinutes();

    if (targetTime != null) {
      targetStart = ZonedDateTime.of(targetDate, targetTime, target);
      targetEnd = targetStart.plusMinutes(durationMinutes);
      toCopy.setSeriesId(-1);
    } else {
      targetStart = ZonedDateTime.of(targetDate, targetStart.toLocalTime(), target);
      targetEnd = targetStart.plusMinutes(durationMinutes);
      if (toCopy.getSeriesId() != -1
          && !targetStart.toLocalDate().equals(targetEnd.toLocalDate())) {
        throw new IllegalArgumentException("Recurrent Event on conversions spans two days");
      }
    }

    toCopy.setStartDate(targetStart.toLocalDate());
    toCopy.setStartTime(targetStart.toLocalTime());
    toCopy.setEndDate(targetEnd.toLocalDate());
    toCopy.setEndTime(targetEnd.toLocalTime());
  }

  /**
   * The below function is used to query events.
   *
   * @param calName   the name of calendar in use
   * @param startDate the start date to query from
   * @param startTime the start time to query from
   * @param endDate   the end date to query till, inclusive
   * @param endTime   the end time to query till
   * @param export    if true then the command is being used to export the calendar
   * @return the list of events queried
   * @throws IllegalArgumentException if start date/time is after end date/time
   */
  @Override
  public List<InterfaceEvent> queryEvents(String calName, LocalDate startDate, LocalTime startTime,
                                          LocalDate endDate, LocalTime endTime, boolean export) {

    TreeMap<LocalDate, HashSet<Event>> calendar = getCalendar(calName);
    if (export) {
      return getCalendarEvents(calName);
    } else if (endDate == null) {
      return queryEventOnDate(calendar, startDate);
    } else {
      if (startDate.isAfter(endDate)) {
        throw new IllegalArgumentException("Start date is after end date");
      }
      return queryEventsInRange(calendar, startDate, endDate, startTime, endTime);
    }
  }

  /**
   * Returns a list of events occurring on a specific date.
   */
  private List<InterfaceEvent> queryEventOnDate(Map<LocalDate, HashSet<Event>> calendar,
                                                LocalDate startDate) {
    HashSet<Event> events = calendar.get(startDate);
    if (events == null) {
      return new ArrayList<>();
    }
    List<InterfaceEvent> result = new ArrayList<>(events.size());
    for (Event e : events) {
      result.add(e.copy());
    }
    result.sort(Comparator.comparing(InterfaceEvent::getStartTime));
    return result;
  }

  /**
   * Returns a list of events occurring within a specified date and time range.
   */
  private List<InterfaceEvent> queryEventsInRange(TreeMap<LocalDate, HashSet<Event>> calendar,
                                                  LocalDate startDate, LocalDate endDate,
                                                  LocalTime startTime, LocalTime endTime) {
    List<InterfaceEvent> result = new ArrayList<>();
    SortedMap<LocalDate, HashSet<Event>> sub = calendar.subMap(startDate, true, endDate, true);

    for (Map.Entry<LocalDate, HashSet<Event>> entry : sub.entrySet()) {
      LocalDate date = entry.getKey();
      for (Event e : entry.getValue()) {
        if (startDate.equals(endDate)) {
          if (e.getStartTime().isBefore(endTime) && e.getEndTime().isAfter(startTime)) {
            result.add(e.copy());
          }
        } else {
          boolean validEvent = (date.equals(startDate) && !e.getEndTime().isBefore(startTime))
              || (date.equals(endDate) && !e.getStartTime().isAfter(endTime))
              || (date.isAfter(startDate) && date.isBefore(endDate));
          if (validEvent) {
            result.add(e.copy());
          }
        }
      }
    }

    result.sort(Comparator.comparing(InterfaceEvent::getStartDate)
        .thenComparing(InterfaceEvent::getStartTime));
    return result;
  }

  /**
   * Checks whether there is any event scheduled at a specific date and time.
   *
   * @param input the date-time string to check (can include "on " prefix or be just the datetime)
   * @return true if an event exists at the specified date and time; false otherwise
   */
  @Override
  public boolean isBusy(String calName, String input) {

    TreeMap<LocalDate, HashSet<Event>> calendar = getCalendar(calName);
    String dateTimeStr =
        input.contains("on ") ? input.substring(input.indexOf("on ") + 3).trim() : input.trim();
    LocalDate date = returnDate(dateTimeStr);
    LocalTime time = returnTime(dateTimeStr);
    List<InterfaceEvent> events = queryEventOnDate(calendar, date);
    for (InterfaceEvent e : events) {
      if (!time.isBefore(e.getStartTime()) && !time.isAfter(e.getEndTime())) {
        return true;
      }
    }
    return false;
  }

  private List<InterfaceEvent> getCalendarEvents(String calName) {

    TreeMap<LocalDate, HashSet<Event>> calendar = getCalendar(calName);
    List<InterfaceEvent> allEvents = new ArrayList<>();

    for (HashSet<Event> dailyEvents : calendar.values()) {
      for (Event e : dailyEvents) {
        allEvents.add(e.copy());
      }
    }
    allEvents.sort(Comparator.comparing(InterfaceEvent::getStartDate)
        .thenComparing(InterfaceEvent::getStartTime));
    return allEvents;
  }

}

