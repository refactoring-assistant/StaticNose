package calendar.controller;

import calendar.model.CalendarModel;
import calendar.view.CalendarView;
import java.util.Objects;


/**
 * Represents a Controller implementation of the interface. It works with the model and view and
 * serves as a connecting part between them.
 */
public class Controller extends AbstractController {
  private final CalendarModel model;

  /**
   * Constructs a Controller given the single calendar model, view, the readable input stream,
   * and the database of multiple calendars. Keeps track of commands.
   *
   * @param model       the calendar model.
   * @param view        the calendar view.
   * @param inputStream the readable.
   * @throws NullPointerException if model/view/input stream is null.
   */
  public Controller(CalendarModel model, CalendarView view, Readable inputStream) {
    super(view, inputStream);
    this.model = Objects.requireNonNull(model);
    this.commands.put("create", (CalendarModel mod) -> new CreateCommand(this.model));
    this.commands.put("edit", (CalendarModel mod) -> new EditCommand(this.model));
    this.commands.put("export", (CalendarModel mod) -> new ExportCommand(this.model, super.view));
    this.commands.put("print", (CalendarModel mod) -> new PrintEventsCommand(mod, super.view));
    this.commands.put("show", (CalendarModel mod) -> new ShowUserStatus(mod, super.view));
  }

  @Override
  protected CalendarModel getModelToRun() {
    return this.model;
  }
}
