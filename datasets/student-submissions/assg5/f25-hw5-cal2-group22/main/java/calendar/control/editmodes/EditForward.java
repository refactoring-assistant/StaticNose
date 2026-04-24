package calendar.control.editmodes;

import calendar.model.Imodel;
import java.time.LocalDateTime;

/**
 * Edit mode that applies changes to the identified event instance
 * and all future instances within the same recurring series.
 */
public class EditForward implements IeditModes {

  /**
   * Applies a property change to the specified event and all
   * subsequent events in its series.
   *
   * @param model    calendar model
   * @param subject  event subject identifying the target series
   * @param start    start time identifying the target instance
   * @param property property name to change (e.g., subject, start, end)
   * @param newValue new value for the given property
   * @return true if the edit was applied successfully, otherwise false
   */
  @Override
  public boolean edit(Imodel model, String subject, LocalDateTime start,
                      LocalDateTime end, String property, String newValue) {
    return model.editEvent(subject, start, end, property, newValue, EditMode.FORWARD);
  }
}
