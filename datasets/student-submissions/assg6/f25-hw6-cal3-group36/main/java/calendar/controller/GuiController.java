package calendar.controller;

import calendar.controller.features.GuiFeatures;
import calendar.model.CalendarModel;
import calendar.model.CalendarSystemModel;
import calendar.model.Event;
import calendar.model.EventBuilder;
import calendar.model.InterfaceEvent;
import calendar.model.RecurrenceRule;
import calendar.view.gui.SwingCalendarView;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * GUI controller that implements {@link GuiFeatures} and mediates between
 * the Swing view and the calendar system model.
 * The controller receives callbacks from the view, delegates work to the
 * model, and then updates the view with the resulting state.
 */
public class GuiController implements GuiFeatures {

  private static final String DEFAULT_CALENDAR_NAME = "Default";

  private final CalendarSystemModel model;
  private final SwingCalendarView view;
  private final List<String> calendarNames;

  /**
   * Creates a GUI controller.
   * A default calendar named "Default" in the system timezone is ensured
   * to exist and be active before the view is used.
   *
   * @param model model instance to delegate calendar operations to
   * @param view Swing view to be driven by this controller
   */
  public GuiController(CalendarSystemModel model, SwingCalendarView view) {
    this.model = model;
    this.view = view;
    this.calendarNames = new ArrayList<String>();
    this.view.setFeatures(this);

    CalendarModel active = ensureDefaultCalendarActive();
    calendarNames.add(active.getName());
    view.setCalendars(new ArrayList<String>(calendarNames));
    view.setActiveCalendar(active.getName(), active.getTimeZone());
  }

  /**
   * Starts the GUI by configuring the view and showing
   * the initial month and day based on the active calendar.
   */
  public void start() {
    CalendarModel active;
    try {
      active = model.getActiveCalendar();
    } catch (IllegalArgumentException e) {
      active = ensureDefaultCalendarActive();
      if (!calendarNames.contains(active.getName())) {
        calendarNames.clear();
        calendarNames.add(active.getName());
        view.setCalendars(new ArrayList<String>(calendarNames));
      }
      view.setActiveCalendar(active.getName(), active.getTimeZone());
    }

    view.showView();
    LocalDate today = LocalDate.now(active.getTimeZone());
    view.showMonth(today.withDayOfMonth(1));
    onViewDay(today);
  }

  /**
   * Ensures that a local-timezone default calendar exists and is active.
   *
   * @return the active calendar after initialization
   */
  private CalendarModel ensureDefaultCalendarActive() {
    ZoneId systemZone = ZoneId.systemDefault();
    try {
      model.createCalendar(DEFAULT_CALENDAR_NAME, systemZone.getId());
    } catch (IllegalArgumentException e) {
      // calendar may already exist
    }
    model.useCalendar(DEFAULT_CALENDAR_NAME);
    return model.getActiveCalendar();
  }

  @Override
  public void onCreateCalendar(String name, ZoneId zone) {
    try {
      model.createCalendar(name, zone.getId());
      model.useCalendar(name);
      final CalendarModel cal = model.getActiveCalendar();

      if (calendarNames.contains(name)) {
        calendarNames.remove(name);
      }
      calendarNames.add(0, name);

      view.setCalendars(new ArrayList<String>(calendarNames));
      view.setActiveCalendar(cal.getName(), cal.getTimeZone());
      view.info("Created calendar: " + cal.getName()
          + " [" + cal.getTimeZone().getId() + "]");
    } catch (Exception e) {
      view.error(e.getMessage());
    }
  }

  @Override
  public void onUseCalendar(String name) {
    try {
      model.useCalendar(name);
      final CalendarModel cal = model.getActiveCalendar();

      if (calendarNames.contains(name)) {
        calendarNames.remove(name);
      }
      calendarNames.add(0, name);

      view.setCalendars(new ArrayList<String>(calendarNames));
      view.setActiveCalendar(cal.getName(), cal.getTimeZone());
      view.refreshDay(LocalDate.now(cal.getTimeZone()));
    } catch (Exception e) {
      view.error(e.getMessage());
    }
  }

  @Override
  public void onRenameCalendar(String oldName, String newName) {
    try {
      model.renameCalendar(oldName, newName);
      model.useCalendar(newName);
      final CalendarModel cal = model.getActiveCalendar();

      if (calendarNames.contains(oldName)) {
        int idx = calendarNames.indexOf(oldName);
        calendarNames.set(idx, newName);
      } else if (!calendarNames.contains(newName)) {
        calendarNames.add(0, newName);
      }

      view.setCalendars(new ArrayList<String>(calendarNames));
      view.setActiveCalendar(cal.getName(), cal.getTimeZone());
      view.info("Calendar renamed to: " + newName);
    } catch (Exception e) {
      view.error(e.getMessage());
    }
  }

  @Override
  public void onChangeCalendarTimezone(String calName, ZoneId zone) {
    try {
      model.changeCalendarTimezone(calName, zone.getId());
      final CalendarModel cal = model.getActiveCalendar();
      view.setActiveCalendar(cal.getName(), cal.getTimeZone());
      view.refreshDay(LocalDate.now(cal.getTimeZone()));
      view.info("Calendar timezone updated to: " + zone.getId());
    } catch (Exception e) {
      view.error(e.getMessage());
    }
  }

  @Override
  public void onViewDay(LocalDate date) {
    try {
      CalendarModel cal;
      try {
        cal = model.getActiveCalendar();
      } catch (IllegalArgumentException ex) {
        cal = ensureDefaultCalendarActive();
        if (!calendarNames.contains(cal.getName())) {
          calendarNames.add(0, cal.getName());
          view.setCalendars(new ArrayList<String>(calendarNames));
        }
        view.setActiveCalendar(cal.getName(), cal.getTimeZone());
      }
      List<InterfaceEvent> events = cal.getEventsOn(date);
      view.renderDayEvents(date, events, cal.getTimeZone());
    } catch (Exception e) {
      view.error(e.getMessage());
    }
  }

  @Override
  public void onCreateSingleEvent(String subject, Instant start, Instant end,
                                  String description, String location,
                                  boolean isPublic, ZoneId zone) {
    try {
      InterfaceEvent e = new EventBuilder()
          .subject(subject)
          .start(start)
          .end(end)
          .description(description == null ? "" : description)
          .location(location == null ? "" : location)
          .isPublic(isPublic)
          .zone(zone)
          .build();
      model.createEvent(e);
      view.refreshDay(LocalDate.ofInstant(start, zone));
      view.info("Event created.");
    } catch (Exception e) {
      view.error(e.getMessage());
    }
  }

  @Override
  public void onCreateRecurringByCount(String subject, Instant start, Instant end,
                                       List<DayOfWeek> days, int count,
                                       String description, String location,
                                       boolean isPublic, ZoneId zone) {
    try {
      InterfaceEvent template = new EventBuilder()
          .subject(subject)
          .start(start)
          .end(end)
          .description(description == null ? "" : description)
          .location(location == null ? "" : location)
          .isPublic(isPublic)
          .zone(zone)
          .build();
      RecurrenceRule rule = new RecurrenceRule(days, count);
      model.createRecurringEvent(template, rule);
      view.refreshDay(LocalDate.ofInstant(start, zone));
      view.info("Recurring events created.");
    } catch (Exception e) {
      view.error(e.getMessage());
    }
  }

  @Override
  public void onCreateRecurringUntil(String subject, Instant start, Instant end,
                                     List<DayOfWeek> days, String until,
                                     String description, String location,
                                     boolean isPublic, ZoneId zone) {
    try {
      InterfaceEvent template = new EventBuilder()
          .subject(subject)
          .start(start)
          .end(end)
          .description(description == null ? "" : description)
          .location(location == null ? "" : location)
          .isPublic(isPublic)
          .zone(zone)
          .build();
      RecurrenceRule rule = new RecurrenceRule(days, until);
      model.createRecurringEvent(template, rule);
      view.refreshDay(LocalDate.ofInstant(start, zone));
      view.info("Recurring events created.");
    } catch (Exception e) {
      view.error(e.getMessage());
    }
  }

  @Override
  public void onEditEventSingle(String subject, Instant originalStart,
                                String property, String newValue) {
    try {
      CalendarModel cal = model.getActiveCalendar();
      InterfaceEvent base = cal.findEvent(subject, originalStart);
      if (base == null) {
        throw new IllegalArgumentException("Event not found to edit.");
      }
      EventBuilder b = new EventBuilder()
          .subject(base.getSubject())
          .start(base.getStart())
          .end(base.getEnd())
          .description(base.getDescription())
          .location(base.getLocation())
          .isPublic(base.isPublicEvent())
          .zone(base.getZone());
      applyEdit(property, newValue, b, base);
      Event replacement = b.build();
      model.editEvent(subject, originalStart, replacement);
      view.refreshDay(LocalDate.ofInstant(originalStart, base.getZone()));
      view.info("Event edited.");
    } catch (Exception e) {
      view.error(e.getMessage());
    }
  }

  @Override
  public void onEditEventsFrom(String subject, Instant pivotStart,
                               String property, String newValue) {
    try {
      CalendarModel cal = model.getActiveCalendar();
      InterfaceEvent base = cal.findEvent(subject, pivotStart);
      if (base == null) {
        throw new IllegalArgumentException("Base event not found for series edit.");
      }
      EventBuilder b = new EventBuilder()
          .subject(base.getSubject())
          .start(base.getStart())
          .end(base.getEnd())
          .description(base.getDescription())
          .location(base.getLocation())
          .isPublic(base.isPublicEvent())
          .zone(base.getZone());
      applyEdit(property, newValue, b, base);
      Event replacement = b.build();
      model.editSeries(subject, pivotStart, replacement, true, false);
      view.refreshDay(LocalDate.ofInstant(pivotStart, base.getZone()));
      view.info("Series edited.");
    } catch (Exception e) {
      view.error(e.getMessage());
    }
  }

  @Override
  public void onEditSeriesAll(String subject, Instant anyStart,
                              String property, String newValue) {
    try {
      CalendarModel cal = model.getActiveCalendar();
      InterfaceEvent base = cal.findEvent(subject, anyStart);
      if (base == null) {
        throw new IllegalArgumentException("Base event not found for series edit.");
      }
      EventBuilder b = new EventBuilder()
          .subject(base.getSubject())
          .start(base.getStart())
          .end(base.getEnd())
          .description(base.getDescription())
          .location(base.getLocation())
          .isPublic(base.isPublicEvent())
          .zone(base.getZone());
      applyEdit(property, newValue, b, base);
      Event replacement = b.build();
      model.editSeries(subject, anyStart, replacement, false, true);
      view.refreshDay(LocalDate.ofInstant(anyStart, base.getZone()));
      view.info("Series edited.");
    } catch (Exception e) {
      view.error(e.getMessage());
    }
  }

  @Override
  public void onMonthShown(LocalDate firstOfMonth) {
    try {
      CalendarModel cal;
      try {
        cal = model.getActiveCalendar();
      } catch (IllegalArgumentException ex) {
        cal = ensureDefaultCalendarActive();
        if (!calendarNames.contains(cal.getName())) {
          calendarNames.add(0, cal.getName());
          view.setCalendars(new ArrayList<String>(calendarNames));
        }
        view.setActiveCalendar(cal.getName(), cal.getTimeZone());
      }
      List<LocalDate> daysWith = collectDaysWithEvents(cal, firstOfMonth);
      view.markDaysWithEvents(daysWith);
    } catch (Exception e) {
      view.error(e.getMessage());
    }
  }

  private List<LocalDate> collectDaysWithEvents(CalendarModel cal,
                                                LocalDate firstOfMonth) {
    List<LocalDate> daysWith = new ArrayList<LocalDate>();
    LocalDate cursor = firstOfMonth.withDayOfMonth(1);
    LocalDate end = cursor.withDayOfMonth(cursor.lengthOfMonth());
    LocalDate d = cursor;
    while (!d.isAfter(end)) {
      if (!cal.getEventsOn(d).isEmpty()) {
        daysWith.add(d);
      }
      d = d.plusDays(1);
    }
    return daysWith;
  }

  private void applyEdit(String property, String value,
                         EventBuilder b, InterfaceEvent base) {
    String p = property == null ? "" : property.trim().toLowerCase();
    if ("subject".equals(p)) {
      b.subject(value);
      return;
    }
    if ("description".equals(p)) {
      b.description(value == null ? "" : value);
      return;
    }
    if ("location".equals(p)) {
      b.location(value == null ? "" : value);
      return;
    }
    if ("status".equals(p)) {
      boolean isPublic = "public".equalsIgnoreCase(value);
      b.isPublic(isPublic);
      return;
    }
    if ("start".equals(p)) {
      Instant newStart = Instant.parse(value);
      long dur = base.getDurationInSeconds();
      b.start(newStart).end(newStart.plusSeconds(dur));
      return;
    }
    if ("end".equals(p)) {
      Instant newEnd = Instant.parse(value);
      b.end(newEnd);
      return;
    }
    throw new IllegalArgumentException("Unsupported property: " + property);
  }
}