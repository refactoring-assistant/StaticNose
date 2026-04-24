package calendar.view;

/**
 * A view specific version of the events in the calendar application.
 */
public class ViewEvent {
  private final String subject;
  private final int startYear;
  private final int startMonth;
  private final int startDay;
  private final int startHour;
  private final int startMinute;
  private final int endYear;
  private final int endMonth;
  private final int endDay;
  private final int endHour;
  private final int endMinute;
  private final String description;
  private final String location;
  private final String status;

  /**
   * ViewEvent constructor.
   *
   * @param subject the subject of the event
   * @param startYear the startYear of the event
   * @param startMonth the startMonth of the event
   * @param startDay the startDay of the event
   * @param startHour the startHour of the event
   * @param startMinute the startMinute of the event
   * @param endYear the endYear of the event
   * @param endMonth the endMonth of the event
   * @param endDay the endDay of the event
   * @param endHour the endHour of the event
   * @param endMinute the endMinute of the event
   * @param description the description of the event
   * @param location the location of the event
   * @param status the status of the event
   */
  public ViewEvent(String subject,
                   int startYear, int startMonth, int startDay,
                   int startHour, int startMinute,
                   int endYear, int endMonth, int endDay,
                   int endHour, int endMinute,
                   String description, String location, String status) {
    this.subject = subject;
    this.startYear = startYear;
    this.startMonth = startMonth;
    this.startDay = startDay;
    this.startHour = startHour;
    this.startMinute = startMinute;
    this.endYear = endYear;
    this.endMonth = endMonth;
    this.endDay = endDay;
    this.endHour = endHour;
    this.endMinute = endMinute;
    this.description = description;
    this.location = location;
    this.status = status;
  }

  public String getSubject() {
    return subject;
  }

  public int getStartYear() {
    return startYear;
  }

  public int getStartMonth() {
    return startMonth;
  }

  public int getStartDay() {
    return startDay;
  }

  public int getStartHour() {
    return startHour;
  }

  public int getStartMinute() {
    return startMinute;
  }

  public int getEndYear() {
    return endYear;
  }

  public int getEndMonth() {
    return endMonth;
  }

  public int getEndDay() {
    return endDay;
  }

  public int getEndHour() {
    return endHour;
  }

  public int getEndMinute() {
    return endMinute;
  }

  public String getDescription() {
    return description;
  }

  public String getLocation() {
    return location;
  }

  public String getStatus() {
    return status;
  }

  @Override
  public String toString() {
    return String.format("%s starting on %d-%d-%d at %02d:%02d, ending on %d-%d-%d at %02d:%02d",
        subject,
        startYear, startMonth, startDay, startHour, startMinute,
        endYear, endMonth, endDay, endHour, endMinute);
  }

}
