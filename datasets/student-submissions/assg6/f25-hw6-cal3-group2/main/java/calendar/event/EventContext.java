package calendar.event;

/**
 * Parameter object for operating events, e.g. create single event, event series, or edit event.
 */
public class EventContext {
  private String subject;
  private String startDateTime;
  private String endDateTime;
  private String description;
  private String location;
  private String eventStatus;

  /**
   * Constructor for class EventContext.
   *
   * @param subject       the theme of the event on calendar (must specify)
   * @param startDateTime the start date and/or time of the event (must specify)
   * @param endDateTime   the end date and/or time of the event (must specify) if input is null,
   *                      note this event as an all-day event with default time from 8am to 5pm
   * @param description   a longer description of the event (optional)
   * @param location      the location of the event (optional)
   * @param eventStatus   the eventStatus (public/private) (optional)
   */
  public EventContext(String subject, String startDateTime, String endDateTime,
                      String description, String location, String eventStatus) {
    this.subject = subject;
    this.startDateTime = startDateTime;
    this.endDateTime = endDateTime;
    this.description = description;
    this.location = location;
    this.eventStatus = eventStatus;
  }

  public String getSubject() {
    return this.subject;
  }

  public String getStartDateTime() {
    return this.startDateTime;
  }

  public String getEndDateTime() {
    return this.endDateTime;
  }

  public String getDescription() {
    return this.description;
  }

  public String getLocation() {
    return this.location;
  }

  public String getEventStatus() {
    return this.eventStatus;
  }

}
