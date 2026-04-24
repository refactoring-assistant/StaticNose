package calendar.model;

/**
 * Represents an Event renderer needed to get an event in a string form for exporting into a file.
 */
public interface EventRenderer {

  /**
   * Renders the event object in an appropriate format based on the file format of calendar.
   *
   * @param event event to render.
   * @return rendered event in required format.
   */
  String render(EventObject event);
}
