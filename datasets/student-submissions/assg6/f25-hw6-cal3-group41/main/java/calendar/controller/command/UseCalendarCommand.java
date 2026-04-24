package calendar.controller.command;

import calendar.model.MultiCalendarModel;

/**
 * Command to use a calendar.
 */
class UseCalendarCommand implements Command {
  private final MultiCalendarModel multiModel;

  /**
   * Constructor.
   *
   * @param multiModel the model
   */
  public UseCalendarCommand(MultiCalendarModel multiModel) {
    this.multiModel = multiModel;
  }

  @Override
  public String execute(String command) {
    ArgumentParser parser = new ArgumentParser("use calendar");
    parser.parse(command);
    
    String name = parser.getRequired("name");
    multiModel.useCalendar(name);
    return "OK: using calendar '" + name + "'.";
  }
}

