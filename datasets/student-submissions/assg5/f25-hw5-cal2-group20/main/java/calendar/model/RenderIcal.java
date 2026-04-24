package calendar.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Represents a class for rendering an event for ical/ics file format of calendar.
 */
public class RenderIcal implements EventRenderer {

  @Override
  public String render(EventObject event) {
    StringBuilder sb = new StringBuilder();
    sb.append("DTSTART:").append(this.convertDateTimeForIcal(event.getStartDateTime()))
        .append(System.lineSeparator());
    sb.append("DTEND:").append(this.convertDateTimeForIcal(event.getEndDateTime()))
        .append(System.lineSeparator());
    sb.append("DTSTAMP:").append(this.convertDateTimeForIcal(LocalDateTime.now()))
        .append(System.lineSeparator());
    sb.append("UID:").append(event.hashCode()).append(System.lineSeparator());
    sb.append("CLASS:").append(event.getStatus() == null ? "" : event.getStatus().toString())
        .append(System.lineSeparator());
    sb.append("LOCATION:").append(this.locationToString(event).trim())
        .append(System.lineSeparator());
    sb.append("STATUS:CONFIRMED").append(System.lineSeparator());
    sb.append("SUMMARY:").append(event.getSubject()).append(System.lineSeparator());
    sb.append("DESCRIPTION:").append(event.getDescription() == null ? "" : event.getDescription());
    return sb.toString();
  }

  private String convertDateTimeForIcal(LocalDateTime dt) {
    DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss",
        Locale.ENGLISH);
    return dt.format(timeFormat);
  }

  private String locationToString(EventObject event) {
    if (event.getLocation() == null) {
      return "";
    } else if (event.getLocation().equals(EventLocation.PHYSICAL)) {
      return "in person";
    } else {
      return "online";
    }
  }
}

