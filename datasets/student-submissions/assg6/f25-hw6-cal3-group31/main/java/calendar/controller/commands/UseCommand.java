package calendar.controller.commands;

import calendar.controller.Parser;
import calendar.model.IntCalendar;
import calendar.model.IntCalendarManager;
import calendar.view.IntView;
import java.util.Objects;

/**
 * Handles the "use" command for choosing which calendar the controller is currently using.
 */
public class UseCommand implements IntCommand {
  protected final IntView out;
  protected final IntCalendarManager calendarManager;

  /**
   * UseCommand constructor.
   *
   * @param out             the output view
   * @param calendarManager the calendarManager to use
   */
  public UseCommand(IntView out, IntCalendarManager calendarManager) {
    this.out = Objects.requireNonNull(out);
    this.calendarManager = Objects.requireNonNull(calendarManager);
  }

  /**
   * Processes the use command.
   *
   * @param input    the remaining input from the user for the given command.
   * @param calendar the calendar to operate on
   */
  @Override
  public void go(String input, IntCalendar calendar) {
    int firstSpace = Parser.getFirstSpaceIndex(input);
    String currentWord = input.substring(0, firstSpace).toLowerCase();
    switch (currentWord) {
      case "calendar":
        useCalendar(input.substring(firstSpace + 1));
        break;
      default:
        throw new UnsupportedOperationException("use " + currentWord + " is not supported");
    }
  }

  private void useCalendar(String input) {
    int firstSpace = Parser.getFirstSpaceIndex(input);
    String currentWord = input.substring(0, firstSpace);
    if (!currentWord.equalsIgnoreCase("--name")) {
      throw new IllegalArgumentException("Expected token \"--name\" after tokens \"use calendar\""
          + " but was: " + currentWord);
    }
    input = input.substring(firstSpace + 1);

    Parser.TokenPair namePair = Parser.extractToken(input);

    try {
      calendarManager.setActiveCalendar(namePair.token);
      out.writeln("Using calendar: " + namePair.token);
    } catch (IllegalArgumentException e) {
      out.writeln(e.getMessage());
    }
  }
}
