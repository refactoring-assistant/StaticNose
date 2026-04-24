package calendar.control.editmodes;

import calendar.model.Imodel;
import java.time.LocalDateTime;

/**
 * Edit mode that updates only the identified event instance.
 */
public class EditSingleInstance implements IeditModes {
  @Override
  public boolean edit(Imodel model, String subject, LocalDateTime start,
                      LocalDateTime end, String property, String newValue) {
    return model.editEvent(subject, start, end, property, newValue, EditMode.SINGLE);
  }
}
