package calendar.model.tree;

import static calendar.model.CalendarEventUtils.createDuplicateEvent;

import calendar.model.event.CalendarEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * An AVL-based interval tree for efficient storage and querying of calendar events.
 * The tree is ordered by event start times and augmented with maximum end times
 * for efficient interval overlap queries.
 *
 * <p>This data structure provides O(log n) insertion, deletion, and search operations,
 * and O(log n + k) for finding overlapping intervals where k is the number of results.
 *
 * <p>The tree maintains the following invariants:
 * <ul>
 *   <li>BST property: left subtree events start before current, right after or equal.</li>
 *   <li>AVL property: height difference between subtrees is at most 1.</li>
 *   <li>Interval property: each node stores the maximum end time in its subtree.</li>
 * </ul>
 */
public class IntervalTree {
  /**
   * Root node of the interval tree.
   */
  IntervalTreeNode root;

  /**
   * Constructs an empty interval tree.
   */
  public IntervalTree() {
    this.root = null;
  }

  /**
   * Gets the root node of the tree.
   *
   * @return the root node, or null if tree is empty
   */
  public IntervalTreeNode getRoot() {
    return root;
  }

  /**
   * Gets the maximum end time in the entire tree.
   *
   * @return the maximum end time, or throws if tree is empty.
   */
  public ZonedDateTime getMaxEnd() {
    return root.getMaxEndTime();
  }

  /**
   * Gets the height of a node.
   *
   * @param node the node to query.
   * @return the height of the node, or 0 if null.
   */
  public int height(IntervalTreeNode node) {
    return node != null ? node.height : 0;
  }

  /**
   * Updates the height of a node based on its children's heights.
   *
   * @param node the node to update.
   */
  public void updateHeight(IntervalTreeNode node) {
    if (node != null) {
      node.height = 1 + Math.max(height(node.left), height(node.right));
    }
  }

  /**
   * Calculates the balance factor of a node.
   *
   * @param node the node to query.
   * @return the balance factor (left height - right height).
   */
  public int getBalance(IntervalTreeNode node) {
    return node != null ? height(node.left) - height(node.right) : 0;
  }

  /**
   * Updates the maximum end time for a node based on its subtrees.
   *
   * @param node the node to update.
   */
  public void updateMaxEnd(IntervalTreeNode node) {
    if (node == null) {
      return;
    }

    ZonedDateTime maxEnd = node.event.getEndDateTime();
    if (node.left != null
        && node.left.maxEndTime.isAfter(maxEnd)) {
      maxEnd = node.left.maxEndTime;
    }
    if (node.right != null
        && node.right.maxEndTime.isAfter(maxEnd)) {
      maxEnd = node.right.maxEndTime;
    }

    node.maxEndTime = maxEnd;
  }

  /**
   * Performs a left rotation on the given node.
   *
   * @param x the node to rotate.
   * @return the new root of the rotated subtree.
   */
  public IntervalTreeNode rotateLeft(IntervalTreeNode x) {
    IntervalTreeNode y = x.right;
    IntervalTreeNode z = y.left;

    y.left = x;
    x.right = z;

    updateHeight(x);
    updateHeight(y);

    updateMaxEnd(x);
    updateMaxEnd(y);

    return y;
  }

  /**
   * Performs a right rotation on the given node.
   *
   * @param y the node to rotate.
   * @return the new root of the rotated subtree.
   */
  public IntervalTreeNode rotateRight(IntervalTreeNode y) {
    IntervalTreeNode x = y.left;
    IntervalTreeNode z = x.right;

    x.right = y;
    y.left = z;

    updateHeight(y);
    updateHeight(x);

    updateMaxEnd(y);
    updateMaxEnd(x);

    return x;
  }

  /**
   * Balances a node by performing necessary rotations.
   * Maintains the AVL tree property after insertions or deletions.
   *
   * @param node the node to balance.
   * @return the new root of the balanced subtree.
   */
  public IntervalTreeNode balance(IntervalTreeNode node) {
    if (node == null) {
      return null;
    }

    updateHeight(node);
    int balance = getBalance(node);

    if (balance > 1) {
      if (getBalance(node.left) >= 0) {
        return rotateRight(node);
      } else {
        node.left = rotateLeft(node.left);
        return rotateRight(node);
      }
    }
    if (balance < -1) {
      if (getBalance(node.right) <= 0) {
        return rotateLeft(node);
      } else {
        node.right = rotateRight(node.right);
        return rotateLeft(node);
      }
    }
    updateMaxEnd(node);
    return node;
  }

  /**
   * Inserts a single event into the tree.
   *
   * @param event the event to insert.
   * @throws IllegalArgumentException if event is null or already exists.
   */
  public void insertSingleEvent(CalendarEvent event) {
    if (event == null) {
      throw new IllegalArgumentException("Event can not be null");
    }
    if (alreadyExists(event)) {
      throw new IllegalArgumentException("Duplicate event detected.");
    }
    root = insertIntoSubtree(root, event);
  }

  /**
   * Inserts multiple events into the tree.
   *
   * @param events list of events to insert.
   * @throws IllegalArgumentException if events list is null or contains duplicates.
   */
  public void insertAll(List<CalendarEvent> events) {
    if (events == null) {
      throw new IllegalArgumentException("Events can not be null");
    }
    if (alreadyExists(events)) {
      throw new IllegalArgumentException("Duplicate event detected.");
    }
    for (CalendarEvent event : events) {
      root = insertIntoSubtree(root, event);
    }
  }

  /**
   * Inserts an event into a subtree and maintains tree properties.
   *
   * @param node  the root of the subtree.
   * @param event the event to insert.
   * @return the new root of the balanced subtree.
   */
  public IntervalTreeNode insertIntoSubtree(IntervalTreeNode node, CalendarEvent event) {
    if (node == null) {
      return new IntervalTreeNode(event);
    }
    if (event.getStartDateTime().isBefore(node.event.getStartDateTime())) {
      node.left = insertIntoSubtree(node.left, event);
    } else {
      node.right = insertIntoSubtree(node.right, event);
    }
    return balance(node);
  }

  /**
   * Removes a single event from the tree.
   *
   * @param event the event to remove.
   * @throws IllegalArgumentException if event is null.
   */
  public void remove(CalendarEvent event) {
    if (event == null) {
      throw new IllegalArgumentException("Event can not be null");
    }
    root = removeFromSubtree(root, event);
  }

  /**
   * Removes multiple events from the tree.
   *
   * @param events list of events to remove.
   * @throws IllegalArgumentException if events list is null.
   */
  public void removeAll(List<CalendarEvent> events) {
    if (events == null) {
      throw new IllegalArgumentException("Event can not be null");
    }
    for (CalendarEvent event : events) {
      root = removeFromSubtree(root, event);
    }
  }

  /**
   * Removes an event from a subtree.
   *
   * @param node  the root of the subtree.
   * @param event the event to remove.
   * @return the new root of the balanced subtree.
   */
  private IntervalTreeNode removeFromSubtree(IntervalTreeNode node, CalendarEvent event) {
    if (node == null) {
      return null;
    }
    if (node.event.equals(event)) {
      return removeNode(node);
    }
    if (event.getStartDateTime().isBefore(node.event.getStartDateTime())) {
      node.left = removeFromSubtree(node.left, event);
    } else {
      node.right = removeFromSubtree(node.right, event);
    }
    return balance(node);
  }

  /**
   * Removes a node from the tree using in-order successor replacement.
   *
   * @param node the node to remove.
   * @return the new root of the subtree.
   */
  private IntervalTreeNode removeNode(IntervalTreeNode node) {
    if (node.left == null) {
      return node.right;
    }
    if (node.right == null) {
      return node.left;
    }
    IntervalTreeNode successor = findSuccessor(node.right);
    node.event = successor.event;
    node.right = removeFromSubtree(node.right, successor.event);

    return balance(node);
  }

  /**
   * Finds the in-order successor (leftmost node in right subtree).
   *
   * @param node the root of the subtree to search.
   * @return the successor node.
   */
  private IntervalTreeNode findSuccessor(IntervalTreeNode node) {
    while (node.left != null) {
      node = node.left;
    }
    return node;
  }

  /**
   * Retrieves all events in chronological order.
   *
   * @return list of all events sorted by start time.
   */
  public List<CalendarEvent> getAllEvents() {
    List<CalendarEvent> events = new ArrayList<>();
    inOrderTraversal(root, events);
    return events;
  }

  /**
   * Performs in-order traversal of the tree.
   *
   * @param node   the current node.
   * @param events list to accumulate events.
   */
  private void inOrderTraversal(IntervalTreeNode node, List<CalendarEvent> events) {
    if (node == null) {
      return;
    }
    inOrderTraversal(node.left, events);
    events.add(createDuplicateEvent(node.event));
    inOrderTraversal(node.right, events);
  }

  /**
   * Finds all events that overlap with a given time range.
   *
   * @param startTime the start of the range.
   * @param endTime   the end of the range.
   * @return list of events overlapping with the range.
   */
  public List<CalendarEvent> findEventsInRange(ZonedDateTime startTime,
                                               ZonedDateTime endTime) {
    List<CalendarEvent> events = new ArrayList<>();
    findSubtreeEventsInRange(root, startTime, endTime, events);
    return events;
  }

  /**
   * Recursively finds events in a subtree that overlap with a time range.
   * Uses the maxEndTime optimization to prune branches that cannot contain results.
   *
   * @param node      the current node.
   * @param startTime the start of the range.
   * @param endTime   the end of the range.
   * @param events    list to accumulate matching events.
   */
  private void findSubtreeEventsInRange(IntervalTreeNode node, ZonedDateTime startTime,
                                        ZonedDateTime endTime, List<CalendarEvent> events) {
    if (node == null) {
      return;
    }

    if (node.left != null
        && node.left.getMaxEndTime().isAfter(startTime)) {
      findSubtreeEventsInRange(node.left, startTime, endTime, events);
    }

    if (node.event.getStartDateTime().isBefore(endTime)
        && node.event.getEndDateTime().isAfter(startTime)) {
      events.add(createDuplicateEvent(node.event));
    }

    if (node.right != null
        && node.event.getStartDateTime().isBefore(endTime)) {
      findSubtreeEventsInRange(node.right, startTime, endTime, events);
    }
  }

  /**
   * Checks if an event already exists in the tree.
   *
   * @param event the event to check.
   * @return true if the event exists, false otherwise.
   */
  public boolean alreadyExists(CalendarEvent event) {
    return findEvent(event) != null;
  }

  /**
   * Checks if any event in a list already exists in the tree.
   *
   * @param events list of events to check.
   * @return true if any event exists, false otherwise.
   */
  public boolean alreadyExists(List<CalendarEvent> events) {
    for (CalendarEvent event : events) {
      if (alreadyExists(event)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Finds all events with matching subject and start time.
   *
   * @param subject   the event subject to match.
   * @param startTime the start time to match.
   * @return list of matching events.
   * @throws IllegalArgumentException if subject or startTime is null.
   */
  public List<CalendarEvent> findEventsByFields(String subject, ZonedDateTime startTime) {
    if (subject == null) {
      throw new IllegalArgumentException("Subject can not be null");
    }
    if (startTime == null) {
      throw new IllegalArgumentException("Start time can not be null");
    }
    List<CalendarEvent> events = new ArrayList<>();
    findEventsByFieldsHelper(root, events, subject, startTime);
    return events;
  }

  /**
   * Helper method to recursively find events by subject and start time.
   * Uses BST property to navigate efficiently.
   *
   * @param node      the current node.
   * @param list      list to accumulate matching events.
   * @param subject   the subject to match.
   * @param startTime the start time to match.
   */
  public void findEventsByFieldsHelper(IntervalTreeNode node, List<CalendarEvent> list,
                                       String subject, ZonedDateTime startTime) {
    if (node == null) {
      return;
    }
    if (node.event.getSubject().equals(subject)
        && node.event.getStartDateTime().isEqual(startTime)) {
      list.add(node.event);
    }
    if (startTime.isBefore(node.event.getStartDateTime())) {
      findEventsByFieldsHelper(node.left, list, subject, startTime);
    } else {
      findEventsByFieldsHelper(node.right, list, subject, startTime);
    }
  }

  /**
   * Finds an event in the tree by equality comparison.
   *
   * @param event the event to find.
   * @return the matching event if found, null otherwise.
   * @throws IllegalArgumentException if event is null.
   */
  public CalendarEvent findEvent(CalendarEvent event) {
    if (event == null) {
      throw new IllegalArgumentException("Event can not be null");
    }
    IntervalTreeNode eventNode = findNodeInSubtree(root, event);
    if (eventNode == null) {
      return null;
    }
    return eventNode.event;
  }

  /**
   * Finds a node in a subtree that matches the given event.
   *
   * @param node  the root of the subtree.
   * @param event the event to find.
   * @return the matching node, or null if not found.
   */
  private IntervalTreeNode findNodeInSubtree(IntervalTreeNode node, CalendarEvent event) {
    if (node == null) {
      return null;
    }
    if (node.event.equals(event)) {
      return node;
    }
    if (event.getStartDateTime().isBefore(node.event.getStartDateTime())) {
      return findNodeInSubtree(node.left, event);
    } else {
      return findNodeInSubtree(node.right, event);
    }
  }

  /**
   * Checks if the user is busy at a specific date and time.
   *
   * @param dateTime the date-time to check.
   * @return true if an event is scheduled at that time, false otherwise.
   */
  public boolean isBusy(ZonedDateTime dateTime) {
    return isBusyHelper(root, dateTime);
  }

  /**
   * Helper method to recursively check if a time falls within any event.
   * An event is considered busy if dateTime is at or after start and before end.
   *
   * @param node     the current node.
   * @param dateTime the date-time to check.
   * @return true if busy, false if available.
   */
  private boolean isBusyHelper(IntervalTreeNode node, ZonedDateTime dateTime) {
    if (node == null) {
      return false;
    }

    if (!dateTime.isBefore(node.event.getStartDateTime())
        && dateTime.isBefore(node.event.getEndDateTime())) {
      return true;
    }

    if (node.left != null
        && node.left.maxEndTime.isAfter(dateTime)) {
      return isBusyHelper(node.left, dateTime);
    }

    if (node.right != null
        && node.event.getStartDateTime().isBefore(dateTime)) {
      return isBusyHelper(node.right, dateTime);
    }

    return false;
  }

  /**
   * Replaces an event's reference in the tree without restructuring.
   * Used for non-time property edits where tree ordering is unaffected.
   *
   * @param oldEvent the event to replace.
   * @param newEvent the new event.
   * @throws IllegalArgumentException if old event is not found.
   */
  public void replaceEventInPlace(CalendarEvent oldEvent, CalendarEvent newEvent) {
    IntervalTreeNode node = findNodeInSubtree(root, oldEvent);
    if (node == null) {
      throw new IllegalArgumentException("Event not found");
    }
    node.event = newEvent;
  }
}