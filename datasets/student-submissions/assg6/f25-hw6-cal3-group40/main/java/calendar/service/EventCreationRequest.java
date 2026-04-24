package calendar.service;

/**
 * Represents a request to create an event.
 */
public class EventCreationRequest {
  private final String subject;
  private final String fromStr;
  private final String toStr;
  private final String onStr;
  private final String description;
  private final String location;
  private final boolean isPrivate;
  private final String repeats;
  private final Integer occurrences;
  private final String untilStr;

  /**
   * Creates the EventCreationRequest object.
   *
   * @param subject Subject for the event.
   * @param fromStr From date/time string for the event.
   * @param toStr To date/time string for the event.
   * @param onStr On date string for the event.
   * @param description Description of the Event.
   * @param location Location of the Event.
   * @param isPrivate If the event is private.
   * @param repeats On which days the event repeats itself.
   * @param occurrences Number of occurrences of the event.
   * @param untilStr Until date for the event.
   */
  public EventCreationRequest(String subject, String fromStr, String toStr, String onStr,
                              String description, String location, boolean isPrivate,
                              String repeats, Integer occurrences, String untilStr) {
    this.subject = subject;
    this.fromStr = fromStr;
    this.toStr = toStr;
    this.onStr = onStr;
    this.description = description;
    this.location = location;
    this.isPrivate = isPrivate;
    this.repeats = repeats;
    this.occurrences = occurrences;
    this.untilStr = untilStr;
  }

  public String getSubject() {
    return subject;
  }

  public String getFromStr() {
    return fromStr;
  }

  public String getToStr() {
    return toStr;
  }

  public String getOnStr() {
    return onStr;
  }

  public String getDescription() {
    return description;
  }

  public String getLocation() {
    return location;
  }

  public boolean isPrivate() {
    return isPrivate;
  }

  public String getRepeats() {
    return repeats;
  }

  public Integer getOccurrences() {
    return occurrences;
  }

  public String getUntilStr() {
    return untilStr;
  }
}
