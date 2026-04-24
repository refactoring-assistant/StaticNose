package calendar.command;

import calendar.service.CalendarService;
import calendar.view.textbased.CalendarView;
import java.util.Map;

/**
 * Command to edit calendar event(s).
 */
public class EditEventCommand implements CalendarCommand {
  private final String property;
  private final String subject;
  private final Map<String, String> params;
  private final boolean singleEventUpdate;
  private final boolean updateAll;

  /**
   * Constructs an EditEventCommand.
   *
   * @param property          The property to edit (subject, start, end, etc.).
   * @param subject           The event subject to find.
   * @param params            The keyword arguments (from, to, with).
   * @param singleEventUpdate true for "edit event", false otherwise.
   * @param updateAll         true for "edit series", false otherwise.
   */
  public EditEventCommand(String property, String subject, Map<String, String> params,
                          boolean singleEventUpdate, boolean updateAll) {
    this.property = property;
    this.subject = subject;
    this.params = params;
    this.singleEventUpdate = singleEventUpdate;
    this.updateAll = updateAll;
  }

  @Override
  public void execute(CalendarService service, CalendarView view) throws IllegalArgumentException {
    String from = params.get("from");
    String to = params.get("to");
    String newValue = params.get("with");

    if (from == null || newValue == null) {
      throw new IllegalArgumentException("Edit commands require 'from <start>' and 'with "
          + "<newValue>'.");
    }
    if (singleEventUpdate && to == null) {
      throw new IllegalArgumentException("'edit event' command requires 'to <end>'.");
    }

    service.editEvent(subject, from, to, property, newValue, singleEventUpdate, updateAll);
    view.showMessage("Event(s) updated successfully.");
  }
}