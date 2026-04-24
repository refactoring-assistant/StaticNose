package calendar.controller.command;

import calendar.model.MultiCalendarModel;

/**
 * Command to edit a calendar.
 */
class EditCalendarCommand implements Command {
  private final MultiCalendarModel multiModel;

  /**
   * Constructor.
   *
   * @param multiModel the model
   */
  public EditCalendarCommand(MultiCalendarModel multiModel) {
    this.multiModel = multiModel;
  }

  @Override
  public String execute(String command) {
    ArgumentParser parser = new ArgumentParser("edit calendar");
    parser.parse(command);
    
    String name = parser.getRequired("name");
    String property = parser.getRequired("property");
    String value = parser.getRequired("value");
    
    multiModel.editCalendar(name, property, value);
    return "OK: calendar '" + name + "' property '" + property + "' updated to '" + value + "'.";
  }
}

