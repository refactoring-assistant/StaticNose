package calendar.model;

import calendar.Location;
import calendar.Property;
import calendar.Status;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * interface outlining the required methods of Event.
 */
public interface Event {

  /**
   * Get method to get the next event.
   *
   * @return next event
   */
  Event getNextEvent();

  /**
   * Set method to set next event.
   *
   * @param newEvent the event to set as next
   */
  void setNextEvent(Event newEvent);

  /**
   * Set method to set previous event.
   *
   * @param currentEvent event to set as previous event
   */
  void setPreviousEvent(Event currentEvent);

  /**
   * Create copy of event with specified field edited only if specified field is one that needs to
   * be unique. Used for testing edit validity.
   *
   * @param property    property to edit
   * @param newProperty new value of property
   * @return the edited copy
   * @throws IllegalArgumentException if property is not of correct type or if required field is
   *                                  null
   */
  Event createEditedCopy(Property property, Object newProperty) throws IllegalArgumentException;

  /**
   * Get method to get start field of event.
   *
   * @return start date/time of event
   */
  Date getStart();

  /**
   * Get method to get the subject field of event.
   *
   * @return subject of event
   */
  String getSubject();

  /**
   * Get method to get start field of event.
   *
   * @return end date/time of event
   */
  Date getEnd();

  /**
   * Set method to set previous event field.
   *
   * @return event to set
   */
  Event getPreviousEvent();

  /**
   * Gets description field of event.
   *
   * @return description of event
   */
  String getDescription();

  /**
   * Gets location field of event.
   *
   * @return location of event
   */
  Location getLocation();

  /**
   * Gets status field of event.
   *
   * @return status of event
   */
  Status getStatus();

  /**
   * Method to edit specified property to specified new value.
   *
   * @param property    property to edit
   * @param newProperty new value of property
   * @throws IllegalArgumentException if new value is not of correct type
   */
  void editProperty(Property property, Object newProperty) throws IllegalArgumentException;

  /**
   * Method to edit the specified property to new value only if it is start date/time, end date/time
   * or subject.
   *
   * @param property    the property to edit
   * @param newProperty the new value of the property
   * @throws IllegalArgumentException if new value is not of correct type
   */
  void editPropertyOfCopy(Property property, Object newProperty) throws IllegalArgumentException;

  /**
   * Method to add event and all events after in series to given list.
   *
   * @param allAfterInSeries the list to add to
   * @return the updated list
   */
  List<Event> addThisAndAllAfter(List<Event> allAfterInSeries);

  /**
   * Get first event in series of this event.
   *
   * @return first event in series
   */
  Event getFirstEventInSeries();

  /**
   * Creates start date for use in iterating change methods.
   *
   * @param newProperty the start date parameter
   * @return a clone of the start date with the correct date/time
   */
  Object getCorrectStartProperty(Object newProperty, Date targetStartDate);

  /**
   * Creates end date for use in iterating change methods.
   *
   * @param newProperty the end date parameter
   * @return a clone of the end date with the correct date/time
   */
  Object getCorrectEndProperty(Object newProperty, Date targetEndDate);
}
