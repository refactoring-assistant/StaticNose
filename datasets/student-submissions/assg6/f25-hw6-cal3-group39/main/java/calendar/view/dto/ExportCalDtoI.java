package calendar.view.dto;

/**
 * Interface representing a Data Transfer Object (DTO) for exporting calendar data.
 * Implementations of this interface provide the necessary information
 * for the controller and model layers to process export requests,
 * including filename and desired export format.
 */
public interface ExportCalDtoI {

  /**
   * Returns the raw filename provided by the user (without guaranteed extension).
   *
   * @return the filename as entered
   */
  String getFilename();

  /**
   * Returns the export format (e.g., ".csv" or ".ical").
   *
   * @return the file format including its extension
   */
  String getFormat();

  /**
   * Returns the full filename including extension.
   * If the provided filename already ends with the desired format
   * (case-insensitive), the filename is returned unchanged.
   * Otherwise, the format extension is appended.
   *
   * @return the normalized filename including its extension
   */
  String getFullFilename();
}
