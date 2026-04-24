package calendar.view;

import calendar.model.Event;
import java.util.List;

/**
 * Represents a simple text-based calendar view that displays events or messages.
 * This abstraction allows swapping between console or test views.
 */
public interface IcalendarView {

  /** Render a list of events to the output. */
  void render(List<Event> events);

  /** Render a single message or error string to the output. */
  void renderMessage(String message);
}
