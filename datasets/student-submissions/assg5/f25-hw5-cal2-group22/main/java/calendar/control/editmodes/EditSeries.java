package calendar.control.editmodes;

import calendar.model.Imodel;
import java.time.LocalDateTime;

/**
 * Editing mode that applies a change to all instances
 * within a recurring event series.
 * Uses the EditMode SERIES mode to update
 * every occurrence of the event, both past and future.
 */
public class EditSeries implements IeditModes {

  /**
   * Applies a property change to every instance in the specified series.
   *
   * @param model    calendar model
   * @param subject  subject identifying the target series
   * @param start    start time identifying one event in the series
   * @param property property name to modify (e.g., subject, start, end)
   * @param newValue new value for the specified property
   * @return true if the edit was successfully applied, otherwise false
   *
   */
  @Override
  public boolean edit(Imodel model, String subject, LocalDateTime start,
                      LocalDateTime end, String property, String newValue) {
    return model.editEvent(subject, start, end, property, newValue, EditMode.SERIES);
  }
}
