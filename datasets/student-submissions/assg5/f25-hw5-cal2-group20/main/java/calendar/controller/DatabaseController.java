package calendar.controller;

import calendar.model.CalendarDatabaseModel;
import calendar.model.CalendarModel;
import calendar.view.CalendarView;
import java.util.Objects;

/**
 * Represents a second controller for database model that extends the single calendar model's
 * controller.
 */
public class DatabaseController extends AbstractController {
  private final CalendarDatabaseModel database;

  /**
   * Constructs a Controller given the calendar view, the readable input stream, and the database
   * of multiple calendars. Keeps track of commands.
   *
   * @param view        the calendar view.
   * @param inputStream the readable.
   * @param database    multiple models of calendar.
   * @throws NullPointerException if model/view/input stream is null.
   */
  public DatabaseController(CalendarView view, Readable inputStream,
                              CalendarDatabaseModel database) {
    super(view, inputStream);
    this.database = Objects.requireNonNull(database);
    this.commands.put("create", (CalendarModel mod) ->
        new CreateWithDatabaseCommand(this.database));
    this.commands.put("edit", (CalendarModel mod) -> new EditWithDatabaseCommand(this.database));
    this.commands.put("export", (CalendarModel mod) -> new ExportCommand(mod, super.view));
    this.commands.put("print", (CalendarModel mod) -> new PrintEventsCommand(mod, super.view));
    this.commands.put("show", (CalendarModel mod) -> new ShowUserStatus(mod, super.view));
    this.commands.put("copy", (CalendarModel mod) -> new CopyCommand(this.database));
    this.commands.put("use", (CalendarModel mod) -> new UseCommand(super.view, this.database));
  }

  @Override
  protected CalendarModel getModelToRun() {
    return this.database.getCurrCalendarModel();
  }
}
