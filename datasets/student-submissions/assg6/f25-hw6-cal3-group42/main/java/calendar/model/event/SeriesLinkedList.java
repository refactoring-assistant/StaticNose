package calendar.model.event;

import static calendar.model.CalendarEventUtils.withUpdatedProperty;
import static calendar.model.CalendarEventUtils.withUpdatedTime;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A doubly-linked list maintaining events in a series in chronological order.
 * Provides efficient operations for adding, removing, updating, and splitting
 * series events while preserving temporal ordering.
 *
 * <p>This data structure is optimized for series operations where events
 * need to be accessed sequentially from a specific point onwards.
 */
public class SeriesLinkedList {
  /**
   * Head of the doubly-linked list.
   */
  private SeriesNode head;

  /**
   * The unique identifier for this series.
   */
  private final String seriesUid;

  /**
   * Number of events in this series.
   */
  private int size;

  /**
   * Node in the doubly-linked list storing a calendar event.
   */
  private static class SeriesNode {
    /**
     * The calendar event stored in this node.
     */
    CalendarEvent event;

    /**
     * Reference to the next node (later event).
     */
    SeriesNode next;

    /**
     * Reference to the previous node (earlier event).
     */
    SeriesNode prev;

    /**
     * Constructs a new node with the given event.
     *
     * @param event the calendar event to store
     */
    SeriesNode(CalendarEvent event) {
      this.event = event;
      this.next = null;
      this.prev = null;
    }
  }

  /**
   * Constructs an empty series linked list.
   *
   * @param seriesUid the unique identifier for this series
   */
  public SeriesLinkedList(String seriesUid) {
    this.seriesUid = seriesUid;
    this.head = null;
    this.size = 0;
  }

  /**
   * Adds a single event to the list in chronological order.
   * The event is inserted at the appropriate position based on its start time.
   *
   * @param event the calendar event to add
   */
  public void addSingleEvent(CalendarEvent event) {
    SeriesNode newNode = new SeriesNode(event);

    if (head == null) {
      head = newNode;
      size++;
      return;
    }

    SeriesNode temp = findPrevNode(event.getStartDateTime());
    if (temp == null) {
      newNode.next = head;
      head.prev = newNode;
      head = newNode;
      size++;
      return;
    }
    newNode.next = temp.next;
    newNode.prev = temp;

    if (temp.next != null) {
      temp.next.prev = newNode;
    }

    temp.next = newNode;
    size++;
  }

  /**
   * Adds all events from a list to the series.
   * Assumes the events are already in chronological order.
   * More efficient than adding individually when events are pre-sorted.
   *
   * @param events list of calendar events in chronological order
   */
  public void addAll(List<CalendarEvent> events) {
    if (events == null || events.isEmpty()) {
      return;
    }
    SeriesNode currentNode = null;
    for (CalendarEvent event : events) {
      SeriesNode newNode = new SeriesNode(event);
      if (head == null) {
        head = newNode;
      } else {
        currentNode.next = newNode;
        newNode.prev = currentNode;
      }
      currentNode = newNode;
      size++;
    }
  }

  /**
   * Retrieves all events in the series in chronological order.
   *
   * @return list of all calendar events in this series
   */
  public List<CalendarEvent> getAllEvents() {
    List<CalendarEvent> events = new ArrayList<>();
    SeriesNode current = head;

    while (current != null) {
      events.add(current.event);
      current = current.next;
    }

    return events;
  }

  /**
   * Finds a node by its event reference.
   *
   * @param event the event to find
   * @return the node containing the event, or null if not found
   */
  private SeriesNode findNodeByEvent(CalendarEvent event) {
    return findNodeByValues(event.getSubject(), event.getStartDateTime());
  }

  /**
   * Finds a node by subject and start time.
   *
   * @param subject   the event subject
   * @param startTime the event start time
   * @return the node with matching subject and start time, or null if not found
   */
  private SeriesNode findNodeByValues(String subject, ZonedDateTime startTime) {
    SeriesNode current = head;

    while (current != null) {
      if (current.event.getSubject().equals(subject)
          && current.event.getStartDateTime().equals(startTime)) {
        return current;
      }
      current = current.next;
    }
    return null;
  }

  /**
   * Finds the node that should precede a new event with the given start time.
   * Used for maintaining chronological order when inserting.
   *
   * @param startTime the start time of the event to be inserted
   * @return the node that should come before the new event
   */
  private SeriesNode findPrevNode(ZonedDateTime startTime) {
    SeriesNode current = head;
    if (head.event.getStartDateTime().isAfter(startTime)) {
      return null;
    }
    while (current.next != null && current.next.event.getStartDateTime().isBefore(startTime)) {
      current = current.next;
    }
    return current;
  }

  /**
   * Removes a node from the list.
   * Updates head if necessary and maintains prev/next pointers.
   *
   * @param node the node to remove
   */
  private void removeNode(SeriesNode node) {
    if (node.prev != null) {
      node.prev.next = node.next;
    } else {
      head = node.next;
    }
    if (node.next != null) {
      node.next.prev = node.prev;
    }
    size--;
  }

  /**
   * Removes a single event from the series by subject and start time.
   *
   * @param subject   the subject of the event to remove
   * @param startTime the start time of the event to remove
   */
  public void removeSingleEvent(String subject, ZonedDateTime startTime) {
    SeriesNode node = findNodeByValues(subject, startTime);
    if (node == null) {
      return;
    }
    removeNode(node);
  }

  /**
   * Gets all events from a starting event onwards (inclusive).
   *
   * @param startEvent the event to start from
   * @return list of events from startEvent to the end of the series
   */
  public List<CalendarEvent> getEventsFrom(CalendarEvent startEvent) {
    return getEventsFrom(startEvent, null);
  }

  /**
   * Gets events between two events (inclusive).
   *
   * @param startEvent the first event in the range
   * @param endEvent   the last event in the range, or null for all events after start
   * @return list of events from startEvent to endEvent (or end of series if endEvent is null)
   */
  public List<CalendarEvent> getEventsFrom(CalendarEvent startEvent, CalendarEvent endEvent) {
    SeriesNode startNode = findNodeByEvent(startEvent);
    SeriesNode endNode = endEvent != null ? findNodeByEvent(endEvent) : null;

    if (startNode == null) {
      return new ArrayList<>();
    }

    List<CalendarEvent> events = new ArrayList<>();
    SeriesNode current = startNode;

    while (current != null) {
      events.add(current.event);
      if (endNode != null && current == endNode) {
        break;
      }
      current = current.next;
    }

    return events;
  }

  /**
   * Updates a property for all events from a starting event onwards.
   * Modifies events in-place within the linked list.
   *
   * @param start    the event to start updating from
   * @param property the property name to update
   * @param newValue the new value for the property
   */
  public void updateSeries(CalendarEvent start, String property, Object newValue) {
    SeriesNode startNode = findNodeByEvent(start);
    if (startNode == null) {
      return;
    }

    SeriesNode current = startNode;
    while (current != null) {
      current.event = withUpdatedProperty(current.event, property, newValue,
          start.getSeriesUid());
      current = current.next;
    }
  }

  /**
   * Splits the series from a starting event onwards and updates start times.
   * Creates a new series with a new UID containing events from the split point onwards.
   * Updates the start times of events in the new series to match the new time pattern.
   *
   * @param splitStart   the event to start the split from (inclusive)
   * @param newSeriesUid the UID for the new series
   * @param newStartTime the new start time pattern (time component will be extracted)
   * @return a new SeriesLinkedList containing the split events with updated times
   */
  public SeriesLinkedList splitUpdateFrom(CalendarEvent splitStart,
                                          String newSeriesUid, ZonedDateTime newStartTime) {
    SeriesNode splitStartNode = findNodeByEvent(splitStart);

    if (splitStartNode == null) {
      return null;
    }

    SeriesLinkedList newSeries = new SeriesLinkedList(newSeriesUid);

    if (splitStartNode.prev != null) {
      splitStartNode.prev.next = null;
      splitStartNode.prev = null;
    } else {
      head = null;
    }

    SeriesNode current = splitStartNode;

    while (current != null) {
      current.event = withUpdatedTime(current.event, "start", newStartTime, newSeriesUid);
      newSeries.addSingleEvent(current.event);
      current = current.next;
    }

    size -= newSeries.size();
    return newSeries;
  }

  /**
   * Replaces an existing event with a new event in the list.
   * The new event must have the same position (matched by subject and start time).
   *
   * @param oldEvent the event to replace
   * @param newEvent the new event to put in its place
   * @throws IllegalArgumentException if the old event is not found in the series
   */
  public void replaceEvent(CalendarEvent oldEvent, CalendarEvent newEvent) {
    SeriesNode node = findNodeByEvent(oldEvent);

    if (node == null) {
      throw new IllegalArgumentException("Event not found in series");
    }

    node.event = newEvent;
  }

  /**
   * Gets the number of events in this series.
   *
   * @return the size of the series
   */
  public int size() {
    return size;
  }

  /**
   * Checks if this series is empty.
   *
   * @return true if the series contains no events
   */
  public boolean isEmpty() {
    return size == 0;
  }

  /**
   * Gets the unique identifier for this series.
   *
   * @return the series UID
   */
  public String getSeriesUid() {
    return seriesUid;
  }

  /**
   * Gets the first event in the series chronologically.
   *
   * @return the first event, or null if the series is empty
   */
  public CalendarEvent getFirstEvent() {
    if (head == null) {
      return null;
    }
    return head.event;
  }
}