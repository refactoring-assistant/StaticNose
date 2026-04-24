package calendar.model;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to handle multiple calenders.
 * */
public class CalenderManager {

  private final Map<String, Calendar> calenders;
  private Calendar currCal;

  /**
   * Constructor to initialise calendar class objects.
   * */
  public CalenderManager() {
    this.calenders = new HashMap<>();
    this.currCal = null;
  }

  /**
   * Function to create calendar object in the map.
   *
   * @param calName the unique identifier for each calendar
   * @param timezone the specified timezone
   * @throws IllegalArgumentException if the calendar name is incorrect
   * */
  public void createCalender(String calName, ZoneId timezone) {
    if (calenders.containsKey(calName)) {
      throw new IllegalArgumentException("Calender with name " + calName + " already exists");
    }
    calenders.put(calName, new Calendar(calName, timezone));
  }

  /**
   * Function to fetch the calendar by name.
   *
   * @param calName the name of the calendar
   * @throws IllegalArgumentException if the calendar name is incorrect
   * */
  public Calendar fetchCal(String calName) {
    if (!calenders.containsKey(calName)) {
      throw new IllegalArgumentException("Calender with name " + calName + " does not exist");
    }
    currCal = calenders.get(calName);
    return currCal;
  }

  /**
   * Function to return the instance of the current calendar.
   *
   * @return the instance of the current calendar
   * */
  public Calendar getCurrentCalender() {
    return currCal;
  }
}
