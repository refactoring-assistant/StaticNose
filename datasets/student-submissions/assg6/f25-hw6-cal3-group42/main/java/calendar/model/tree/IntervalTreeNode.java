package calendar.model.tree;

import calendar.model.event.CalendarEvent;
import java.time.ZonedDateTime;

/**
 * Represents a node in an AVL-based interval tree.
 * Each node stores a calendar event and maintains tree balancing information
 * along with the maximum end time in its subtree for efficient interval queries.
 *
 * <p>The tree is ordered by event start times, and the maxEndTime field allows
 * for efficient searching of overlapping intervals.
 */
public class IntervalTreeNode {

  /** The calendar event stored in this node. */
  CalendarEvent event;

  /** Reference to the left child (events with earlier start times). */
  IntervalTreeNode left;

  /** Reference to the right child (events with later start times). */
  IntervalTreeNode right;

  /** The maximum end time among this node and all its descendants. */
  ZonedDateTime maxEndTime;

  /** The height of this node in the AVL tree (for balancing). */
  int height;

  /**
   * Constructs a new interval tree node with the given event.
   * Initializes the node as a leaf with height 1 and maxEndTime
   * equal to the event's end time.
   *
   * @param event the calendar event to store in this node
   */
  public IntervalTreeNode(CalendarEvent event) {
    this.event = event;
    this.maxEndTime = event.getEndDateTime();
    this.height = 1;
    this.left = null;
    this.right = null;
  }

  /**
   * Gets the maximum end time in this subtree.
   *
   * @return the maximum end time among this node and all descendants
   */
  public ZonedDateTime getMaxEndTime() {
    return maxEndTime;
  }
}