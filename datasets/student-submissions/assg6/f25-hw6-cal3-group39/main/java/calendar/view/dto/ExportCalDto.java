package calendar.view.dto;

/**
 * The class below is the data transfer object of the export functionality.
 */
public class ExportCalDto implements ExportCalDtoI {
  private final String filename;
  private final String format;

  /**
   * Public constructor which initializes the object.
   *
   * @param filename name of the file to be exported in.
   * @param format   either csv or ical for now
   */
  public ExportCalDto(String filename, String format) {
    this.filename = filename;
    this.format = format;
  }

  /**
   * Get the filename.
   *
   * @return name of file
   */
  public String getFilename() {

    return filename;
  }

  /**
   * get the format.
   *
   * @return either csv or ical
   */
  public String getFormat() {

    return format;
  }

  /**
   * Helper to get the full filename (e.g., "mycal.csv").
   */
  public String getFullFilename() {
    if (filename.toLowerCase().endsWith(format)) {
      return filename;
    }
    return filename + format;
  }
}