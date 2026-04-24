package calendar.control.editmodes;

import calendar.model.Imodel;
import java.time.LocalDateTime;

/**
 * Interface for applying different edit modes to events.
 */
public interface IeditModes {

  /**
   * Applies an edit to events according to a specific mode.
   *
   * @param model    calendar model
   * @param subject  subject identifying the target event/series
   * @param start    start time identifying the target instance
   * @param property property name to change
   * @param newValue new value for the property
   * @return true if the edit was applied successfully
   */
  boolean edit(Imodel model, String subject, LocalDateTime start,
               LocalDateTime end, String property, String newValue);
}
