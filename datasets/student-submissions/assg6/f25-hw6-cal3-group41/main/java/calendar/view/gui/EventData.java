package calendar.view.gui;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Data transfer object for event creation and editing.
 * Used to pass event data between view dialogs and controller.
 */
public class EventData {
  /** Event title/subject. */
  public String title;
  
  /** Event start date and time. */
  public LocalDateTime startDateTime;
  
  /** Event end date and time. */
  public LocalDateTime endDateTime;
  
  /** Event description. */
  public String description;
  
  /** Event location (optional). */
  public String location;
  
  /** Event status (optional, e.g., "public", "private"). */
  public String status;
  
  /** Whether this is a recurring event. */
  public boolean isRecurring;
  
  /** Days of week for recurring events. */
  public Set<DayOfWeek> repeatDays;
  
  /** Number of occurrences for recurring events (optional). */
  public Integer occurrences;
  
  /** Until date for recurring events (optional). */
  public LocalDate untilDate;
}

