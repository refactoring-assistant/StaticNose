package calendar.model;

/**
 * Enumeration representing the privacy status of a calendar event.
 * Events can be either PUBLIC (visible to others) or PRIVATE (visible only to owner).
 */
public enum EventStatus {

  /**
   * Event is public and visible to others.
   */
  PUBLIC,

  /**
   * Event is private and visible only to the owner.
   */
  PRIVATE
}