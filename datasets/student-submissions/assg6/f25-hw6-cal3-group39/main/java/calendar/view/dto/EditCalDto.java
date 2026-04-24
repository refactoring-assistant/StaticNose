package calendar.view.dto;

/**
 * The class below is the data transfer object for the edit calendar functionality.
 */
public class EditCalDto implements EditCalDtoI {
  private final String newName;
  private final String newTimezone;

  /**
   * Public constructor which initializes the object.
   *
   * @param newName     the new name, null if timezone is to be edited.
   * @param newTimezone the new timezone, null if name is to be edited.
   */
  public EditCalDto(String newName, String newTimezone) {
    this.newName = newName;
    this.newTimezone = newTimezone;
  }

  /**
   * Return the new name.
   *
   * @return new name.
   */
  public String getNewName() {
    return newName;
  }

  /**
   * Return the new time zone.
   *
   * @return new time zone.
   */
  public String getNewTimezone() {
    return newTimezone;
  }
}