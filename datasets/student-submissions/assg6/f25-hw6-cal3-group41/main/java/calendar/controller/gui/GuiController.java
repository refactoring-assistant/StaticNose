package calendar.controller.gui;

import calendar.model.Calendar;
import calendar.model.CalendarModel;
import calendar.model.Event;
import calendar.model.MultiCalendarModel;
import calendar.view.gui.EventData;
import calendar.view.gui.GuiView;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * GUI controller.
 */
public class GuiController implements GuiFeatures {
  private final MultiCalendarModel model;
  private final GuiView view;
  private String currentCalendarName;
  private LocalDate currentlyShownMonth;

  /**
   * Constructor.
   */
  public GuiController(MultiCalendarModel model, GuiView view) {
    this.model = model;
    this.view = view;
    this.view.setFeatures(this);

    List<String> names = model.getCalendarNames();

    if (names.isEmpty()) {
      String defaultName = "default";
      try {
        model.createCalendar(defaultName, ZoneId.systemDefault().getId());
        currentCalendarName = defaultName;
        model.useCalendar(defaultName);
      } catch (Exception ex) {
        view.showError("Failed to create default calendar: " + ex.getMessage());
        currentCalendarName = null;
      }
      names = model.getCalendarNames();
    } else {
      currentCalendarName = names.get(0);
      model.useCalendar(currentCalendarName);
    }

    view.setCalendarNames(names);
    view.setCurrentCalendarName(currentCalendarName);
    currentlyShownMonth = LocalDate.now().withDayOfMonth(1);
    view.showMonth(currentlyShownMonth);
    updateDayHighlights();
  }

  @Override
  public void createCalendar(String name, String zoneId) {
    try {
      ZoneId zid = ZoneId.of(zoneId);
      model.createCalendar(name, zid);
      view.setCalendarNames(model.getCalendarNames());
    } catch (Exception ex) {
      view.showError("Failed to create calendar: " + ex.getMessage());
    }
  }

  @Override
  public void switchCalendar(String calendarName) {
    try {
      this.currentCalendarName = calendarName;
      model.useCalendar(calendarName);
      currentlyShownMonth = LocalDate.now().withDayOfMonth(1);
      view.showMonth(currentlyShownMonth);
      view.setCurrentCalendarName(calendarName);
      updateDayHighlights();
      view.refresh();
    } catch (Exception ex) {
      view.showError("Failed to switch calendar: " + ex.getMessage());
    }
  }

  @Override
  public void previousMonth() {
    currentlyShownMonth = currentlyShownMonth.minusMonths(1);
    view.showMonth(currentlyShownMonth);
    updateDayHighlights();
  }

  @Override
  public void nextMonth() {
    currentlyShownMonth = currentlyShownMonth.plusMonths(1);
    view.showMonth(currentlyShownMonth);
    updateDayHighlights();
  }

  private void updateDayHighlights() {
    if (currentCalendarName == null) {
      return;
    }
    try {
      Set<Integer> daysWithEvents = new HashSet<>();
      YearMonth yearMonth = YearMonth.from(currentlyShownMonth);
      LocalDate firstDay = yearMonth.atDay(1);
      LocalDate lastDay = yearMonth.atEndOfMonth();

      for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
        List<Event> events = model.getEventsForCalendarOnDate(currentCalendarName, date);
        if (!events.isEmpty()) {
          daysWithEvents.add(date.getDayOfMonth());
        }
      }
      view.highlightDaysWithEvents(daysWithEvents);
    } catch (Exception ex) {
      // Silently fail - highlights are not critical
    }
  }

  @Override
  public void requestViewDay(LocalDate date) {
    try {
      List<Event> events = model.getEventsForCalendarOnDate(currentCalendarName, date);
      List<String> summaries = new ArrayList<>();
      Map<String, Boolean> isSeriesMap = new HashMap<>();
      for (Event e : events) {
        String summary = formatEventSummary(e);
        summaries.add(summary);
        isSeriesMap.put(summary, e.getSeriesId() != null);
      }
      view.showEventsForDay(date, summaries, isSeriesMap);
    } catch (Exception ex) {
      view.showError("Failed to load events: " + ex.getMessage());
    }
  }

  @Override
  public void requestCreateEvent(LocalDate date) {
    Optional<EventData> maybe = view.showCreateEventDialog(date);
    if (!maybe.isPresent()) {
      return;
    }
    EventData d = maybe.get();

    try {
      LocalDateTime startLocal = d.startDateTime;
      LocalDateTime endLocal = d.endDateTime;

      Event e = new Event(d.title, startLocal, endLocal);
      e.setDescription(d.description);
      if (d.location != null && !d.location.isEmpty()) {
        e.setLocation(d.location);
      }
      if (d.status != null && !d.status.isEmpty()) {
        e.setStatus(d.status);
      }

      if (d.isRecurring && d.repeatDays != null && !d.repeatDays.isEmpty()) {
        Calendar calendar = model.getCalendar(currentCalendarName);
        calendar.getModel().createEventSeries(
            d.title, startLocal, endLocal,
            d.repeatDays,
            d.occurrences,
            d.untilDate
        );
      } else {
        model.addEventToCalendar(currentCalendarName, e);
      }

      updateDayHighlights();
      view.refresh();
    } catch (Exception ex) {
      view.showError("Failed to create event: " + ex.getMessage());
    }
  }

  @Override
  public void requestEditEvent(LocalDate day, String eventSummary) {
    try {
      Event existing = findEventBySummary(day, eventSummary);
      if (existing == null) {
        view.showError("Could not find the selected event (it may have been deleted).");
        return;
      }
      Optional<EventData> maybe = view.showEditEventDialog(existing);
      if (!maybe.isPresent()) {
        return;
      }
      EventData changed = maybe.get();
      CalendarModel calModel = getCalendarModel();
      applyEventEdits(calModel, existing, changed);
      updateDayHighlights();
      view.refresh();
    } catch (Exception ex) {
      view.showError("Failed to edit event: " + ex.getMessage());
    }
  }

  @Override
  public void requestBulkEditEvents(LocalDate day, String eventName,
                                     List<String> selectedSummaries) {
    try {
      List<Event> eventsToEdit = findEventsBySummaries(day, selectedSummaries);
      if (eventsToEdit.isEmpty()) {
        view.showError("Could not find any of the selected events (they may have been deleted).");
        return;
      }

      Event template = eventsToEdit.get(0);
      Optional<EventData> maybe = view.showEditEventDialog(template);
      if (!maybe.isPresent()) {
        return;
      }
      EventData changed = maybe.get();
      CalendarModel calModel = getCalendarModel();

      int successCount = 0;
      for (Event event : eventsToEdit) {
        try {
          applyEventEdits(calModel, event, changed);
          successCount++;
        } catch (Exception ex) {
          // skip if fails
        }
      }

      if (successCount == 0) {
        view.showError("Failed to edit any events. Please check for conflicts or duplicates.");
      } else if (successCount < eventsToEdit.size()) {
        view.showError("Successfully edited " + successCount + " of "
            + eventsToEdit.size() + " events.");
      }

      updateDayHighlights();
      view.refresh();
      requestViewDay(day);
    } catch (Exception ex) {
      view.showError("Failed to bulk edit events: " + ex.getMessage());
    }
  }

  @Override
  public void requestEditSeries(LocalDate day, String eventSummary) {
    try {
      Event anchor = findEventBySummary(day, eventSummary);
      if (anchor == null) {
        view.showError("Could not find the selected event (it may have been deleted).");
        return;
      }
      if (anchor.getSeriesId() == null) {
        view.showError("This event is not part of a series.");
        return;
      }
      Optional<EventData> maybe = view.showEditEventDialog(anchor);
      if (!maybe.isPresent()) {
        return;
      }
      EventData changed = maybe.get();
      CalendarModel calModel = getCalendarModel();
      applySeriesEdits(calModel, anchor, changed);
      updateDayHighlights();
      view.refresh();
      requestViewDay(day);
    } catch (Exception ex) {
      view.showError("Failed to edit series: " + ex.getMessage());
    }
  }

  private Event findEventBySummary(LocalDate day, String eventSummary) {
    List<Event> events = model.getEventsForCalendarOnDate(currentCalendarName, day);
    for (Event e : events) {
      if (formatEventSummary(e).equals(eventSummary)) {
        return e;
      }
    }
    return null;
  }

  private List<Event> findEventsBySummaries(LocalDate day, List<String> summaries) {
    List<Event> allDayEvents = model.getEventsForCalendarOnDate(currentCalendarName, day);
    List<Event> found = new ArrayList<>();
    for (String summary : summaries) {
      for (Event e : allDayEvents) {
        if (formatEventSummary(e).equals(summary)) {
          found.add(e);
          break;
        }
      }
    }
    return found;
  }

  private CalendarModel getCalendarModel() {
    Calendar calendar = model.getCalendar(currentCalendarName);
    return calendar.getModel();
  }

  private void applyEventEdits(CalendarModel calModel, Event existing, EventData changed) {
    String currentSubject = existing.subject();
    LocalDateTime currentStart = existing.startDate();
    LocalDateTime currentEnd = existing.endDate();

    if (!existing.subject().equals(changed.title)) {
      calModel.editEvent(currentSubject, currentStart, currentEnd, "subject", changed.title);
      currentSubject = changed.title;
    }
    if (!existing.startDate().equals(changed.startDateTime)) {
      calModel.editEvent(currentSubject, currentStart, currentEnd,
          "start", changed.startDateTime.toString());
      currentStart = changed.startDateTime;
    }
    if (!existing.endDate().equals(changed.endDateTime)) {
      calModel.editEvent(currentSubject, currentStart, currentEnd,
          "end", changed.endDateTime.toString());
      currentEnd = changed.endDateTime;
    }
    if (!existing.description().equals(changed.description)) {
      calModel.editEvent(currentSubject, currentStart, currentEnd,
          "description", changed.description);
    }
    if (changed.location != null && !changed.location.equals(existing.location())) {
      calModel.editEvent(currentSubject, currentStart, currentEnd, "location", changed.location);
    }
  }

  private void applySeriesEdits(CalendarModel calModel, Event anchor, EventData changed) {
    String currentSubject = anchor.subject();
    LocalDateTime currentStart = anchor.startDate();

    if (!anchor.subject().equals(changed.title)) {
      calModel.editSeries(currentSubject, currentStart, "subject", changed.title);
      currentSubject = changed.title;
    }
    if (!anchor.startDate().equals(changed.startDateTime)) {
      calModel.editSeries(currentSubject, currentStart,
          "start", changed.startDateTime.toString());
      currentStart = changed.startDateTime;
    }
    if (!anchor.endDate().equals(changed.endDateTime)) {
      calModel.editSeries(currentSubject, currentStart, "end", changed.endDateTime.toString());
    }
    if (!anchor.description().equals(changed.description)) {
      calModel.editSeries(currentSubject, currentStart, "description", changed.description);
    }
    if (changed.location != null && !changed.location.equals(anchor.location())) {
      calModel.editSeries(currentSubject, currentStart, "location", changed.location);
    }
  }

  private String formatEventSummary(Event e) {
    try {
      return String.format("%s (%s - %s)", e.subject(),
          e.startDate().toString(),
          e.endDate().toString());
    } catch (Exception ex) {
      return e.toString();
    }
  }
}
