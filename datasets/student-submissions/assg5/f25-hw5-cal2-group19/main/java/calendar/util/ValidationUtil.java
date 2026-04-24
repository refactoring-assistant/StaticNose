package calendar.util;

import calendar.exception.DuplicateEventException;
import calendar.model.InEvent;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Centralized validation logic to avoid duplication across the application.
 * Validates event constraints and command parameters.
 * Follows the DRY (Don't Repeat Yourself) principle by consolidating
 * common validation rules in one place.
 */
public class ValidationUtil {

  /**
   * Private constructor to prevent instantiation.
   * This is a utility class with only static methods.
   */
  private ValidationUtil() {
    throw new AssertionError("Utility class should not be instantiated");
  }

  /**
   * Validates that an object is not null.
   * Throws IllegalArgumentException with descriptive message if null.
   *
   * @param obj       the object to check
   * @param fieldName the name of the field (for error messages)
   * @throws IllegalArgumentException if object is null
   */
  public static void validateNotNull(Object obj, String fieldName) {
    if (obj == null) {
      throw new IllegalArgumentException(fieldName + " cannot be null");
    }
  }

  /**
   * Validates that a string is not null or empty (after trimming).
   * Throws IllegalArgumentException with descriptive message if invalid.
   *
   * @param str       the string to check
   * @param fieldName the name of the field (for error messages)
   * @throws IllegalArgumentException if string is null or empty
   */
  public static void validateNotEmpty(String str, String fieldName) {
    if (str == null || str.trim().isEmpty()) {
      throw new IllegalArgumentException(fieldName + " cannot be null or empty");
    }
  }

  /**
   * Validates that a list is not null or empty.
   *
   * @param list      the list to validate
   * @param fieldName the name of the field (for error messages)
   * @throws IllegalArgumentException if list is null or empty
   */
  public static void validateNotEmpty(List<?> list, String fieldName) {
    if (list == null || list.isEmpty()) {
      throw new IllegalArgumentException(fieldName + " cannot be null or empty");
    }
  }

  /**
   * Validates that an event does not conflict with existing events.
   * Two events conflict if they have the same subject, start date/time,
   * and end date/time (this constitutes a duplicate per requirements).
   *
   * @param event          the event to validate
   * @param existingEvents list of existing events to check against
   * @throws DuplicateEventException if a duplicate is found
   */
  public static void validateEventDoesNotConflict(InEvent event,
                                                  List<InEvent> existingEvents)
      throws DuplicateEventException {
    if (event == null) {
      throw new IllegalArgumentException("Event cannot be null");
    }

    if (existingEvents == null) {
      return;
    }

    for (InEvent existing : existingEvents) {
      if (existing.getSubject().equals(event.getSubject())
          && existing.getStartDateTime().equals(event.getStartDateTime())
          && existing.getEndDateTime().equals(event.getEndDateTime())) {
        throw new DuplicateEventException(
            "Event with same subject, start, and end already exists: "
                + event.getSubject() + " at " + event.getStartDateTime());
      }
    }
  }

  /**
   * Validates that start date/time is before or equal to end date/time.
   * Throws IllegalArgumentException if start is after end.
   *
   * @param start the start date/time
   * @param end   the end date/time
   * @throws IllegalArgumentException if start is after end
   */
  public static void validateDateTimeRange(LocalDateTime start, LocalDateTime end) {
    if (start == null || end == null) {
      throw new IllegalArgumentException("Start and end date/time cannot be null");
    }

    if (start.isAfter(end)) {
      throw new IllegalArgumentException(
          "Start date/time cannot be after end date/time. Start: "
              + start + ", End: " + end);
    }
  }

  /**
   * Validates that start and end are on the same day (for recurring events).
   * Per assignment requirements, recurring events must start and end on same day.
   *
   * @param start the start date/time
   * @param end   the end date/time
   * @throws IllegalArgumentException if not on same day
   */
  public static void validateRecurringSameDay(LocalDateTime start, LocalDateTime end) {
    if (start == null || end == null) {
      throw new IllegalArgumentException("Start and end date/time cannot be null");
    }

    if (!start.toLocalDate().equals(end.toLocalDate())) {
      throw new IllegalArgumentException(
          "Recurring events must start and end on the same day. "
              + "Start date: " + start.toLocalDate() + ", End date: " + end.toLocalDate());
    }
  }

  /**
   * Validates that a count/occurrence value is positive.
   * Used for validating recurring event occurrence counts.
   *
   * @param count     the count to validate
   * @param fieldName the name of the field (for error messages)
   * @throws IllegalArgumentException if count is not positive
   */
  public static void validatePositive(int count, String fieldName) {
    if (count <= 0) {
      throw new IllegalArgumentException(fieldName + " must be positive, got: " + count);
    }
  }

  /**
   * Validates that a subject string meets requirements.
   * Subject cannot be null, empty, or just whitespace.
   *
   * @param subject the subject to validate
   * @throws IllegalArgumentException if subject is invalid
   */
  public static void validateSubject(String subject) {
    validateNotEmpty(subject, "Subject");

  }

  /**
   * Validates that a property name is one of the allowed values.
   * Used when editing events to ensure only valid properties are modified.
   *
   * @param property the property name to validate
   * @throws IllegalArgumentException if property is not allowed
   */
  public static void validatePropertyName(String property) {
    validateNotEmpty(property, "Property name");

    String prop = property.toLowerCase();
    if (!prop.equals("subject")
        && !prop.equals("start")
        && !prop.equals("end")
        && !prop.equals("description")
        && !prop.equals("location")
        && !prop.equals("status")) {
      throw new IllegalArgumentException(
          "Invalid property name: " + property
              + ". Allowed: subject, start, end, description, location, status");
    }
  }

  /**
   * Validates that an edit type is one of the allowed values.
   * Used when editing events to ensure valid edit scope.
   *
   * @param editType the edit type to validate (single, from, entire)
   * @throws IllegalArgumentException if edit type is not allowed
   */
  public static void validateEditType(String editType) {
    validateNotEmpty(editType, "Edit type");

    String type = editType.toLowerCase();
    if (!type.equals("single") && !type.equals("from") && !type.equals("entire")) {
      throw new IllegalArgumentException(
          "Invalid edit type: " + editType
              + ". Allowed: single, from, entire");
    }
  }

  /**
   * Validates an index is within valid range for a list.
   * Useful for array/list access validation.
   *
   * @param index     the index to validate
   * @param listSize  the size of the list
   * @param fieldName the name of the field (for error messages)
   * @throws IndexOutOfBoundsException if index is out of range
   */
  public static void validateIndex(int index, int listSize, String fieldName) {
    if (index < 0 || index >= listSize) {
      throw new IndexOutOfBoundsException(
          fieldName + " index out of bounds: " + index
              + " (list size: " + listSize + ")");
    }
  }

  /**
   * Validates that two events don't have overlapping time ranges.
   * Used to check for scheduling conflicts.
   *
   * @param event1 the first event
   * @param event2 the second event
   * @return true if events overlap in time, false otherwise
   */
  public static boolean eventsOverlap(InEvent event1, InEvent event2) {
    if (event1 == null || event2 == null) {
      return false;
    }

    return !event1.getEndDateTime().isBefore(event2.getStartDateTime())
        && !event1.getStartDateTime().isAfter(event2.getEndDateTime());
  }

  /**
   * Validates that a file path is not null and is properly formatted.
   * Ensures path doesn't contain invalid characters.
   *
   * @param filePath the file path to validate
   * @throws IllegalArgumentException if path is invalid
   */
  public static void validateFilePath(String filePath) {
    validateNotEmpty(filePath, "File path");

    if (filePath.contains("\0")) {
      throw new IllegalArgumentException("File path contains invalid characters");
    }
  }
}
