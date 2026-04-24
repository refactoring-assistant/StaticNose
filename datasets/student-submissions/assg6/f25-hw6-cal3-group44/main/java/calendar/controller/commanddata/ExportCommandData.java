package calendar.controller.commanddata;

/**
 * Data transfer object for ExportCommand parsed data.
 */
public class ExportCommandData {
  private final String fileName;

  /**
   * Constructor for ExportCommandData.
   *
   * @param fileName the file name to export to
   */
  public ExportCommandData(String fileName) {
    this.fileName = fileName;
  }

  public String getFileName() {
    return fileName;
  }
}

