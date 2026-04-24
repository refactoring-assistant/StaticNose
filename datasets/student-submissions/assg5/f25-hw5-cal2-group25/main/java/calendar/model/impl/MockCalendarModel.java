package calendar.model.impl;

import calendar.model.CalendarModel;
import calendar.model.EditScope;
import calendar.model.EventSelector;
import calendar.model.EventSpec;
import calendar.model.Exporter;
import calendar.model.PropertyChange;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mock implementation of CalendarModel for testing controllers in isolation.
 * Logs all method calls and their parameters instead of executing them.
 */
public class MockCalendarModel implements CalendarModel {
  private final StringBuilder log;
  private final int uniqueCode;
  private String timezone = "UTC";  // Add default timezone

  /**
   * Creates a mock model.
   *
   * @param log        StringBuilder to record all method calls
   * @param uniqueCode Unique identifier to embed in mock return values
   */
  public MockCalendarModel(StringBuilder log, int uniqueCode) {
    this.log = log;
    this.uniqueCode = uniqueCode;
  }

  /**
   * Constructor with timezone parameter for testing.
   *
   * @param log        StringBuilder to record all method calls
   * @param uniqueCode Unique identifier to embed in mock return values
   * @param timezone   Timezone of Mock.
   */
  public MockCalendarModel(StringBuilder log, int uniqueCode, String timezone) {
    this.log = log;
    this.uniqueCode = uniqueCode;
    this.timezone = timezone;
  }


  @Override
  public EventId createSingle(EventSpec spec) {
    log.append("createSingle called\n");
    log.append("  subject: ").append(spec.subject()).append("\n");
    log.append("  start: ").append(spec.start()).append("\n");
    log.append("  end: ").append(spec.end().orElse(null)).append("\n");
    log.append("  description: ").append(spec.description()).append("\n");
    log.append("  location: ").append(spec.location()).append("\n");
    log.append("  status: ").append(spec.status()).append("\n");
    log.append("  allDay: ").append(spec.allDay()).append("\n");
    return new EventId();
  }

  @Override
  public List<EventId> createSeries(EventSpec base, SeriesRule rule) {
    log.append("createSeries called\n");
    log.append("  subject: ").append(base.subject()).append("\n");
    log.append("  days: ").append(rule.days).append("\n");
    log.append("  occurrences: ").append(rule.occurrences).append("\n");
    log.append("  until: ").append(rule.until).append("\n");
    List<EventId> mockIds = new ArrayList<>();
    int count = rule.occurrences != null ? rule.occurrences : 5;
    for (int i = 0; i < count; i++) {
      mockIds.add(new EventId());
    }
    return mockIds;
  }

  @Override
  public void edit(EventSelector selector, EditScope scope, PropertyChange change) {
    log.append("edit called\n");
    log.append("  subject: ").append(selector.subject()).append("\n");
    log.append("  start: ").append(selector.start()).append("\n");
    log.append("  scope: ").append(scope).append("\n");
    log.append("  change kind: ").append(change.kind()).append("\n");
    if (change.stringValue() != null) {
      log.append("  new value: ").append(change.stringValue()).append("\n");
    }
    if (change.dateTimeValue() != null) {
      log.append("  new dateTime: ").append(change.dateTimeValue()).append("\n");
    }
    if (throwOnEdit) {
      throw new RuntimeException("Simulated edit failure");
    }
    log.append("edit called ...\n");
  }

  @Override
  public List<Event> eventsOn(LocalDate date) {
    log.append("eventsOn called\n");
    log.append("  date: ").append(date).append("\n");
    if (shouldReturnEmpty) {
      return Collections.emptyList();
    }
    Event mockEvent = new Event(
        new EventId(),
        null,
        "MockEvent-" + uniqueCode,
        date.atTime(10, 0),
        date.atTime(11, 0),
        "",
        mockEventLocation,
        EventSpec.Status.PUBLIC,
        false
    );
    return Collections.singletonList(mockEvent);
  }

  public String mockEventLocation = "";

  @Override
  public List<Event> eventsBetween(LocalDateTime start, LocalDateTime end) {
    log.append("eventsBetween called\n");
    log.append("  start: ").append(start).append("\n");
    log.append("  end: ").append(end).append("\n");
    if (shouldReturnEmpty) {
      return Collections.emptyList();
    }
    Event mockEvent = new Event(
        new EventId(),
        null,
        "MockEvent-" + uniqueCode,
        start,
        end,
        "",
        mockEventLocation,
        EventSpec.Status.PUBLIC,
        false
    );
    return Collections.singletonList(mockEvent);
  }

  @Override
  public boolean isBusy(LocalDateTime at) {
    log.append("isBusy called\n");
    log.append("  at: ").append(at).append("\n");
    return uniqueCode % 2 == 0;
  }

  public boolean throwOnEdit = false;

  public boolean throwExportException = false;

  @Override
  public void export(Exporter exporter, Path file) throws IOException {
    if (throwExportException) {
      throw new IOException("Simulated export failure");
    }
    log.append("export called ").append("file: ").append(file).append("\n");
    log.append("  timezone: ").append(timezone).append("\n");
    exporter.export(Collections.emptyList(), this.timezone, file);
  }

  @Override
  public String getTimezone() {
    log.append("getTimezone called\n");
    return timezone;
  }

  @Override
  public void setTimezone(String newTimezone) {
    log.append("setTimezone called\n");
    log.append("  timezone: ").append(newTimezone).append("\n");
    this.timezone = newTimezone;
  }

  public boolean shouldReturnEmpty = false;

}