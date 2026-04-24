package calendar.model;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates the context information for editing calendar events.
 * This class acts as a parameter object to avoid long parameter lists
 * when passing edit operation details between methods.
 *
 * <p>EditContext is immutable - use {@link #withNewSeriesUid(String)} to create
 * modified copies for operations that require a different series UID.
 */
class EditContext {
  /**
   * The subject of the event to be edited.
   */
  final String subject;

  /**
   * The start date-time used to identify the event.
   */
  final ZonedDateTime startDateTime;

  /**
   * The end date-time used to identify the event.
   */
  final ZonedDateTime endDateTime;

  /**
   * The property name to be edited (e.g., "subject", "start", "end").
   */
  final String property;

  /**
   * The new value to set for the property.
   */
  final Object newValue;

  /**
   * The series UID for the edited events (null for non-split operations).
   */
  final String seriesUid;

  /**
   * Constructs an EditContext from a parameter map.
   *
   * @param parameters map containing edit parameters with keys:
   *                   "subject", "start", "end", "property", "value", and optionally "seriesUid"
   */
  EditContext(Map<String, Object> parameters) {
    this.subject = parameters.get("subject").toString();
    this.startDateTime = (ZonedDateTime) parameters.get("start");
    this.endDateTime = (ZonedDateTime) parameters.getOrDefault("end", null);
    this.property = (String) parameters.getOrDefault("property", null);
    this.newValue = parameters.getOrDefault("value", null);
    this.seriesUid = (String) parameters.getOrDefault("seriesUid", null);
  }

  /**
   * Creates a new EditContext with a different series UID.
   * Used when splitting a series to create a new series with a new UID.
   *
   * @param newSeriesUid the new series UID to use
   * @return a new EditContext with the specified series UID
   */
  EditContext withNewSeriesUid(String newSeriesUid) {
    Map<String, Object> params = new HashMap<>();
    params.put("subject", subject);
    params.put("start", startDateTime);
    params.put("end", endDateTime);
    params.put("property", property);
    params.put("value", newValue);
    params.put("seriesUid", newSeriesUid);
    return new EditContext(params);
  }

  /**
   * Checks if the property being edited is the start time.
   *
   * @return true if editing the start property, false otherwise
   */
  boolean isStartProperty() {
    return "start".equals(property);
  }
}