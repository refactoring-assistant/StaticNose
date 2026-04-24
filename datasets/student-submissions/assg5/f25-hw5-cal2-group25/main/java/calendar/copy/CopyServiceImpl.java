package calendar.copy;

import calendar.model.CalendarModel;
import calendar.model.EventSpec;
import calendar.model.impl.Event;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Implements {@link CopyService} by providing utilities to copy single events,
 * all events on a date, or events across a date range between calendar models.
 * Preserves event properties while adjusting dates and times as needed.
 */
public class CopyServiceImpl implements CopyService {

  @Override
  public void copyEvent(CalendarModel src, String subject, LocalDateTime srcStart,
                        CalendarModel dst, LocalDateTime dstStart) {
    Event srcEvt = findBySubjectAndStart(src, subject, srcStart);

    Duration dur = Duration.between(srcEvt.start(), srcEvt.end());
    LocalDateTime targetStart = dstStart;
    LocalDateTime targetEnd = targetStart.plus(dur);

    EventSpec spec = new EventSpec(
        srcEvt.subject(), targetStart, targetEnd,
        srcEvt.description(), srcEvt.location(),
        srcEvt.status(), srcEvt.allDay()
    );

    dst.createSingle(spec);
  }

  @Override
  public void copyEventsOnDate(CalendarModel src, LocalDate srcDate,
                               CalendarModel dst, LocalDate dstStartDate) {
    List<Event> dayEvents = src.eventsOn(srcDate);

    ZoneId srcZone = ZoneId.of(src.getTimezone());
    ZoneId dstZone = ZoneId.of(dst.getTimezone());

    for (Event e : dayEvents) {
      ZonedDateTime srcStartZ = e.start().atZone(srcZone);
      ZonedDateTime srcEndZ = e.end().atZone(srcZone);

      LocalDateTime targetStart = srcStartZ.withZoneSameInstant(dstZone).toLocalDateTime();
      LocalDateTime targetEnd = srcEndZ.withZoneSameInstant(dstZone).toLocalDateTime();

      EventSpec spec = new EventSpec(
          e.subject(), targetStart, targetEnd,
          e.description(), e.location(), e.status(), e.allDay());
      dst.createSingle(spec);
    }
  }

  @Override
  public void copyEventsBetween(CalendarModel src, LocalDate fromDate, LocalDate toDate,
                                CalendarModel dst, LocalDate dstStartDate) {
    LocalDateTime winStart = fromDate.atStartOfDay();
    LocalDateTime winEnd = toDate.plusDays(1).atStartOfDay().minusSeconds(1);

    List<Event> srcEvents = src.eventsBetween(winStart, winEnd);
    if (srcEvents.isEmpty()) {
      return;
    }

    LocalDateTime dstAnchor = dstStartDate.atStartOfDay();

    for (Event e : srcEvents) {
      Duration offset = Duration.between(winStart, e.start());
      Duration dur = Duration.between(e.start(), e.end());

      LocalDateTime targetStart = dstAnchor.plus(offset);
      LocalDateTime targetEnd = targetStart.plus(dur);

      EventSpec spec = new EventSpec(
          e.subject(), targetStart, targetEnd,
          e.description(), e.location(), e.status(), e.allDay());
      dst.createSingle(spec);
    }
  }

  private Event findBySubjectAndStart(CalendarModel src, String subject, LocalDateTime start) {
    List<Event> day = src.eventsOn(start.toLocalDate());
    return day.stream()
        .filter(e -> e.subject().equals(subject) && e.start().equals(start))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
            "Event not found: \"" + subject + "\" at " + start));
  }
}