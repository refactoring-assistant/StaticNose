package calendar.model;

/**
 * Represents a class to render an event for a calendar csv file .
 */
public class RenderCsv implements EventRenderer {

  @Override
  public String render(EventObject event) {
    return event.toString();
  }
}
