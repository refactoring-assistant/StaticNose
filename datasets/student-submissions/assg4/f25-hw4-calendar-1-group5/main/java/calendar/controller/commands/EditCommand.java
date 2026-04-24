package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.Event;
import calendar.model.Model;
import calendar.view.View;

/**
 * Command to edit calendar events.
 */
public class EditCommand implements Command {
  private final EditScope scope;
  private final EditProperty property;
  private final Event targetEvent;
  private final String newValue;

  /**
   * Creates an edit command.
   *
   * @param scope Scope of the edit (SINGLE, FORWARD, or SERIES)
   * @param property Property to edit
   * @param targetEvent Event to find and edit
   * @param newValue New value for the property
   */
  public EditCommand(EditScope scope, EditProperty property, Event targetEvent, String newValue) {
    this.scope = scope;
    this.property = property;
    this.targetEvent = targetEvent;
    this.newValue = newValue;
  }

  @Override
  public void execute(Model model, View view) {
    switch (scope) {
      case SINGLE:
        model.editEvent(property, targetEvent, newValue);
        break;
      case FORWARD:
        model.editEventsForward(property, targetEvent, newValue);
        break;
      case SERIES:
      default:
        model.editEventSeries(property, targetEvent, newValue);
    }
  }
}