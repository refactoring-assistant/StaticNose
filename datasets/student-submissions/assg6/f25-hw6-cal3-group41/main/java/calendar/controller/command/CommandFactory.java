package calendar.controller.command;

import calendar.model.MultiCalendarModel;
import java.util.Locale;

/**
 * Creates command objects.
 */
public class CommandFactory {

  private final MultiCalendarModel multiModel;
  private final CreateCommand createCommand;
  private final EditCommand editCommand;
  private final PrintCommand printCommand;
  private final ExportCommand exportCommand;
  private final StatusCommand statusCommand;
  private final CreateCalendarCommand createCalendarCommand;
  private final EditCalendarCommand editCalendarCommand;
  private final UseCalendarCommand useCalendarCommand;
  private final CopyEventCommand copyEventCommand;

  /**
   * Constructor.
   *
   * @param multiModel the model
   */
  public CommandFactory(MultiCalendarModel multiModel) {
    this.multiModel = multiModel;
    this.createCommand = new CreateCommand(multiModel);
    this.editCommand = new EditCommand(multiModel);
    this.printCommand = new PrintCommand(multiModel);
    this.exportCommand = new ExportCommand(multiModel);
    this.statusCommand = new StatusCommand(multiModel);
    this.createCalendarCommand = new CreateCalendarCommand(multiModel);
    this.editCalendarCommand = new EditCalendarCommand(multiModel);
    this.useCalendarCommand = new UseCalendarCommand(multiModel);
    this.copyEventCommand = new CopyEventCommand(multiModel);
  }

  /**
   * Creates a command from string.
   *
   * @param commandString the command string
   * @return the command or null
   */
  public Command createCommand(String commandString) {
    String lower = commandString.toLowerCase(Locale.ROOT);

    if (lower.startsWith("create calendar")) {
      return createCalendarCommand;
    } else if (lower.startsWith("edit calendar")) {
      return editCalendarCommand;
    } else if (lower.startsWith("use calendar")) {
      return useCalendarCommand;
    } else if (lower.startsWith("copy event") || lower.startsWith("copy events")) {
      return copyEventCommand;
    } else if (lower.startsWith("create event")) {
      return createCommand;
    } else if (lower.startsWith("edit event ")
        || lower.startsWith("edit events ")
        || lower.startsWith("edit series ")) {
      return editCommand;
    } else if (lower.startsWith("print events")) {
      return printCommand;
    } else if (lower.startsWith("export cal")) {
      return exportCommand;
    } else if (lower.startsWith("show status")) {
      return statusCommand;
    }

    return null;
  }
}

