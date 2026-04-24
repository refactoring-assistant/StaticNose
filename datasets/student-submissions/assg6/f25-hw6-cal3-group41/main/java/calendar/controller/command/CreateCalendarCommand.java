package calendar.controller.command;

import calendar.model.MultiCalendarModel;

/**
 * Command to create a calendar.
 */
class CreateCalendarCommand implements Command {
  private final MultiCalendarModel multiModel;

  /**
   * Constructor.
   *
   * @param multiModel the model
   */
  public CreateCalendarCommand(MultiCalendarModel multiModel) {
    this.multiModel = multiModel;
  }

  @Override
  public String execute(String command) {
    ArgumentParser parser = new ArgumentParser("create calendar");
    parser.parse(command);
    
    String name = parser.getRequired("name");
    String timezone = parser.getRequired("timezone");
    
    multiModel.createCalendar(name, timezone);
    return "OK: calendar '" + name + "' created with timezone " + timezone + ".";
  }
}

