package calendar.view.dto;

import calendar.model.InterfaceEvent;

/**
 * Defines the contract for a data object that represents a request to modify an existing event.
 *
 * <p>This interface provides read-only access to the necessary information required
 * to process an update, including the target event, the specific field being changed,
 * the new value, and the scope of recurrence updates.</p>
 */
public interface EditEventDtoI {

  /**
   * Enum to represent the scope of the edit operation for recurring events.
   */
  enum Scope {
    /**
     * Indicates the change should apply only to the specific event instance selected.
     */
    SINGLE_EVENT,

    /**
     * Indicates the change should apply to the selected event and all subsequent occurrences.
     */
    FUTURE_EVENTS,

    /**
     * Indicates the change should apply to every event in the recurring series.
     */
    ENTIRE_SERIES
  }

  /**
   * Retrieves the original event object that is being targeted for modification.
   *
   * @return the original {@link InterfaceEvent} instance.
   */
  InterfaceEvent getOriginalEvent();

  /**
   * Retrieves the identifier of the property that is being modified.
   *
   * @return a String representing the property key (e.g., "title", "location", "start-time").
   */
  String getPropertyToEdit();

  /**
   * Retrieves the new value intended for the specified property.
   *
   * @return a String representation of the new value.
   */
  String getNewValue();

  /**
   * Retrieves the scope of the modification.
   *
   * <p>This determines if the update logic should branch to handle a single instance,
   * a split in the series (future events), or the whole series definition.</p>
   *
   * @return the {@link Scope} of this edit request.
   */
  Scope getScope();
}