package calendar.controller.commands;

import calendar.controller.utils.CommandParserUtils;
import calendar.model.CalendarEvent;
import calendar.model.InterfaceCalendarModel;
import calendar.model.InterfaceCalendarModel.EditScope;
import calendar.view.InterfaceCalendarView;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Command to handle editing events.
 * Parses arguments for "edit event", "edit events", and "edit series"
 * to modify events in the model.
 */
public class EditCommand implements CommandInterface {

  private final CommandParserUtils utils;

  /**
   * Creates an Edit command with the given command parser utils.
   *
   * @param utils The command parser utils to use
   */
  public EditCommand(CommandParserUtils utils) {
    this.utils = utils;
  }

  @Override
  public void execute(InterfaceCalendarModel model, InterfaceCalendarView view, List<String> args)
      throws Exception {

    if (args.size() < 1) {
      throw new Exception(
          "Invalid 'edit' command. Must be 'event', 'events', or 'series'.");
    }

    String editType = args.get(0).toLowerCase();
    String property;
    String subject;
    String newValue;
    ZonedDateTime start;
    ZonedDateTime end;
    CalendarEvent originalEvent;
    CalendarEvent updatedTemplate;
    EditScope scope;

    switch (editType) {
      case "event":
        scope = EditScope.SINGLE_INSTANCE;
        break;
      case "events":
        scope = EditScope.ALL_FOLLOWING;
        break;
      case "series":
        scope = EditScope.ENTIRE_SERIES;
        break;
      default:
        throw new Exception("Invalid edit type. Must be 'event', 'events', or 'series'.");
    }

    if (scope == EditScope.SINGLE_INSTANCE) {
      if (args.size() != 9) {
        throw new Exception(
            "Invalid 'edit event' command. "
                + "Usage: edit event <prop> <subj> from <start> to <end> with <val>");
      }
      property = args.get(1).toLowerCase();
      subject = utils.stripQuotes(args.get(2));
      start = utils.parseDateTimeToZonedDateTime(args.get(4));
      end = utils.parseDateTimeToZonedDateTime(args.get(6));
      if (!args.get(7).equals("with")) {
        throw new Exception("Expected 'with'");
      }
      newValue = utils.stripQuotes(args.get(8));

      try {
        originalEvent = model.getUniqueEvent(subject, start, end);
      } catch (Exception e) {
        throw new Exception("Could not find a unique event to edit. "
            + "Error: " + e.getMessage());
      }

    } else {
      if (args.size() != 7) {
        throw new Exception(
            "Invalid 'edit " + editType + "' command. Usage: edit "
                + editType + " <prop> <subj> from <start> with <val>");
      }
      property = args.get(1).toLowerCase();
      subject = utils.stripQuotes(args.get(2));
      start = utils.parseDateTimeToZonedDateTime(args.get(4));
      if (!args.get(5).equals("with")) {
        throw new Exception("Expected 'with'");
      }
      newValue = utils.stripQuotes(args.get(6));

      try {
        originalEvent = model.getEventBySubjectAndStart(subject, start);
      } catch (Exception e) {
        throw new Exception(
            "Could not find a unique event to edit. "
                + "Error: " + e.getMessage());
      }
    }

    updatedTemplate = utils.buildUpdatedEvent(originalEvent, property, newValue);
    model.editEvent(originalEvent, updatedTemplate, scope);
    view.displayMessage("Event edited successfully.");
  }
}