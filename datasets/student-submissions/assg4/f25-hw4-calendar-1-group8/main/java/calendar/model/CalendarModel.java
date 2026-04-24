package calendar.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Class that contains the implementation of the CalendarModel interface.
 * It handles the operations to be done on CalendarEvent including addition and edition of events
 * and series. It also checks for events conflicts (having same subject, start date/time and
 * end date/time.
 * It uses seriesId alongwith the event properties to identify the series and the events that are
 * part of it.
 */
public class CalendarModel implements InterfaceCalendarModel {

  private final List<CalendarEvent> events;
  private final List<SeriesMaster> seriesMasters;
  private final ZoneId timeZone;

  /**
   * Constructor to initialize the lists that store events and series. It also sets the time zone to
   * the desired time zone as mentioned in the interface.
   */
  public CalendarModel() {
    this.events = new ArrayList<>();
    this.seriesMasters = new ArrayList<>();
    this.timeZone = ZoneId.of(InterfaceCalendarModel.TIME_ZONE_ID);
  }

  @Override
  public void addEvent(CalendarEvent event) throws CalendarException, IllegalArgumentException {
    if (event.isRecurringEvent()) {
      throw new IllegalArgumentException("Use addEventSeries for recurring events.");
    }
    checkUniqueness(event, List.of());
    this.events.add(event);
  }

  @Override
  public void addEventSeries(SeriesMaster master) throws CalendarException {
    List<CalendarEvent> instances = generateSeriesInstances(master);

    for (CalendarEvent instance : instances) {
      checkUniqueness(instance, List.of());
    }

    this.events.addAll(instances);
    master.setEvents(instances);
    this.seriesMasters.add(master);
  }

  @Override
  public void editEvent(CalendarEvent original, CalendarEvent template, EditScope scope)
      throws CalendarException {

    SeriesMaster master = findMasterForEvent(original);

    if (master == null) {
      scope = EditScope.SINGLE_INSTANCE;
    }


    switch (scope) {
      case SINGLE_INSTANCE:
        editSingleInstance(original, template, master);
        break;
      case ALL_FOLLOWING:
        editFollowing(original, template, master);
        break;
      case ENTIRE_SERIES:
        editEntireSeries(original, template, master);
        break;
      default:
        throw new IllegalStateException("Unexpected edit scope: " + scope);
    }
  }

  @Override
  public CalendarEvent getUniqueEvent(String subject, ZonedDateTime start, ZonedDateTime end)
      throws CalendarException {

    List<CalendarEvent> matchingEvents = events.stream()
        .filter(e -> e.getSubject().equalsIgnoreCase(subject)
            && e.getStart().isEqual(start)
            && e.getEnd().isEqual(end))
        .collect(Collectors.toList());

    if (matchingEvents.isEmpty()) {
      throw new CalendarException(
          "No event found with specified subject, start, and end.");
    }
    if (matchingEvents.size() > 1) {
      throw new CalendarException(
          "Multiple events found. Cannot uniquely identify event.");
    }
    return matchingEvents.get(0);
  }

  @Override
  public CalendarEvent getEventBySubjectAndStart(String subject, ZonedDateTime start)
      throws CalendarException {

    List<CalendarEvent> matchingEvents = events.stream()
        .filter(e -> e.getSubject().equalsIgnoreCase(subject)
            && e.getStart().isEqual(start))
        .collect(Collectors.toList());

    if (matchingEvents.isEmpty()) {
      throw new CalendarException("No event found with specified subject and start time.");
    }
    if (matchingEvents.size() > 1) {
      throw new CalendarException(
          "Multiple events found. Cannot uniquely identify event.");
    }
    return matchingEvents.get(0);
  }

  @Override
  public List<CalendarEvent> getEventsInRange(ZonedDateTime start, ZonedDateTime end) {
    return events.stream()
        .filter(e -> !e.getStart().isAfter(end)
            && !e.getEnd().isBefore(start))
        .sorted(Comparator.comparing(CalendarEvent::getStart))
        .collect(Collectors.toList());
  }

  @Override
  public List<CalendarEvent> getEventsOnDay(LocalDate date) {
    ZonedDateTime startOfDay = date.atStartOfDay(timeZone);
    ZonedDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
    return getEventsInRange(startOfDay, endOfDay);
  }

  @Override
  public boolean isBusy(ZonedDateTime checkTime) {
    return events.stream()
        .anyMatch(e -> !checkTime.isBefore(e.getStart())
            && checkTime.isBefore(e.getEnd()));
  }

  @Override
  public List<CalendarEvent> getAllEvents() {
    return new ArrayList<>(events);
  }

  /**
   * Method to get a list of all series masters currently in the calendar.
   * This is primarily intended for testing.
   *
   * @return A List of all SeriesMaster objects.
   */
  public List<SeriesMaster> getSeriesMasters() {
    return new ArrayList<>(seriesMasters);
  }

  /**
   * Private helper method to check if a given event conflicts with any existing event
   * in the calendar.
   * A conflict is: "same subject, start date/time and end date/time."
   *
   * @param eventToCheck   The event to check for conflicts.
   * @param eventsToIgnore A list of events to *ignore* during the check
   *                       (e.g.,the event being replaced).
   * @throws EventConflictException if a conflict is found.
   */
  private void checkUniqueness(CalendarEvent eventToCheck, List<CalendarEvent> eventsToIgnore)
      throws EventConflictException {

    String subject = eventToCheck.getSubject();
    ZonedDateTime start = eventToCheck.getStart();
    ZonedDateTime end = eventToCheck.getEnd();

    for (CalendarEvent existing : this.events) {
      if (eventsToIgnore.stream().anyMatch(e -> e.getId().equals(existing.getId()))) {
        continue;
      }

      if (existing.getSubject().equalsIgnoreCase(subject)
          && existing.getStart().isEqual(start)
          && existing.getEnd().isEqual(end)) {

        throw new EventConflictException(
            "Event conflict: An event with subject '" + subject
                + "' from " + start + " to " + end + " already exists.");
      }
    }
  }

  /**
   * Generates all event instances for a given series master.
   *
   * @param master The SeriesMaster containing the template and rule.
   * @return A list of generated CalendarEvent instances.
   * @throws CalendarException if any generated event has a uniqueness conflict.
   */
  private List<CalendarEvent> generateSeriesInstances(SeriesMaster master)
      throws CalendarException {
    List<CalendarEvent> instances = new ArrayList<>();
    CalendarEvent template = master.getTemplateEvent();
    RecurrenceRule rule = master.getRule();

    if (!template.getStart().toLocalDate().isEqual(template.getEnd().toLocalDate())) {
      throw new CalendarException("Recurring events must start and end on the same day.");
    }

    long durationInMinutes = ChronoUnit.MINUTES.between(template.getStart(), template.getEnd());
    ZonedDateTime currentStart = template.getStart();

    Predicate<ZonedDateTime> terminationCondition;
    if (rule.isCountBased()) {
      terminationCondition = current -> instances.size() < rule.getOccurences();
    } else {
      terminationCondition = current -> !current.toLocalDate().isAfter(rule.getEndDate());
    }

    while (terminationCondition.test(currentStart)) {
      if (rule.getDaysOfWeek().contains(currentStart.getDayOfWeek())) {
        ZonedDateTime currentEnd = currentStart.plusMinutes(durationInMinutes);

        CalendarEvent instance =
            new CalendarEvent.CalendarEventBuilder(template.getSubject(), currentStart)
                .withEnd(currentEnd)
                .withDescription(template.getDescription())
                .withLocation(template.getLocation())
                .withStatus(template.getStatus())
                .withSeriesMasterId(master.getMasterId())
                .build();

        instances.add(instance);
      }
      currentStart = currentStart.plusDays(1);
    }
    return instances;
  }

  /**
   * Private helper method to find the SeriesMaster that a given event instance belongs to.
   *
   * @param event The event instance to check.
   * @return SeriesMaster object or null if not found.
   */
  private SeriesMaster findMasterForEvent(CalendarEvent event) {
    if (!event.isRecurringEvent()) {
      return null;
    }
    return seriesMasters.stream()
        .filter(m -> m.getMasterId().equals(event.getSeriesMasterId()))
        .findFirst()
        .orElse(null);
  }

  /**
   * Private helper to build a new event instance based on an old one (start,en and seriesId)
   * using a template event as an argument.
   *
   * @param oldEvent CalendarEvent object to be used as template
   * @param templateEvent   CalendarEvent object with properties of new event
   * @return new CalendarEvent objects with edited properties
   */
  private CalendarEvent buildPropertyChange(CalendarEvent oldEvent,
                                            CalendarEvent templateEvent) {
    return new CalendarEvent.CalendarEventBuilder(
        templateEvent.getSubject(), oldEvent.getStart())
        .withEnd(oldEvent.getEnd())
        .withDescription(templateEvent.getDescription())
        .withLocation(templateEvent.getLocation())
        .withStatus(templateEvent.getStatus())
        .withSeriesMasterId(oldEvent.getSeriesMasterId())
        .build();
  }

  /**
   * Private helper method to build a new event instance based on an old one, but with updated
   * properties AND time from a template.
   * This is used for editing when there is a split of series
   *
   * @param oldEvent CalendarEvent to be edited
   * @param template CalendarEvent template with desired properties
   * @return new CalendarEvent reflecting edited properties
   */
  private CalendarEvent buildTimeChange(CalendarEvent oldEvent, CalendarEvent template) {
    long durationInMinutes = ChronoUnit.MINUTES.between(
        template.getStart(), template.getEnd());
    ZonedDateTime newStart = oldEvent.getStart().toLocalDate()
        .atTime(template.getStart().toLocalTime())
        .atZone(this.timeZone);
    ZonedDateTime newEnd = newStart.plusMinutes(durationInMinutes);

    return new CalendarEvent.CalendarEventBuilder(
        template.getSubject(), newStart)
        .withEnd(newEnd)
        .withDescription(template.getDescription())
        .withLocation(template.getLocation())
        .withStatus(template.getStatus())
        .build();
  }

  /**
   * Handles the 'edit event' (SINGLE_INSTANCE) scope.
   * Replaces the original event with the template event.
   * If the event was part of a series, it is detached from it.
   *
   * @param original The event to be replaced.
   * @param template The new event to add.
   * @param master The series the original event belongs to, or null.
   * @throws CalendarException If the edit causes a conflict.
   */
  private void editSingleInstance(CalendarEvent original, CalendarEvent template,
                                  SeriesMaster master) throws CalendarException {

    checkUniqueness(template, List.of(original));
    this.events.remove(original);
    this.events.add(template);

    if (master != null) {
      master.removeEvent(original);
      template.setSeriesMasterId(null);
    }
  }

  /**
   * Handles the 'edit events' (ALL_FOLLOWING) scope.
   * Applies changes to the original event and all following events in its series.
   * Will split the series on a time change, or modify properties otherwise.
   *
   * @param original The event marking the start of the change.
   * @param template The template for the new/updated events.
   * @param master The series being modified.
   * @throws CalendarException If the edit causes a conflict.
   */
  private void editFollowing(CalendarEvent original, CalendarEvent template,
                             SeriesMaster master) throws CalendarException {

    List<CalendarEvent> allEvents = master.getSortedEvents();
    List<CalendarEvent> toChange = allEvents.stream()
        .filter(e -> !e.getStart().isBefore(original.getStart()))
        .collect(Collectors.toList());

    boolean isTimeChange = !original.getStart().toLocalTime()
        .equals(template.getStart().toLocalTime())
        || !original.getEnd().toLocalTime()
        .equals(template.getEnd().toLocalTime());

    if (isTimeChange) {
      splitSeries(master, template, toChange);
    } else {
      modifyProperties(master, template, toChange);
    }
  }

  /**
   * Handles the 'edit series' (ENTIRE_SERIES) scope.
   * Applies changes to all events in the entire series.
   * Will replace the series on a time change, or modify properties otherwise.
   *
   * @param original An event from the series (used to find the master).
   * @param template The template for the new/updated events.
   * @param master The series being modified.
   * @throws CalendarException If the edit causes a conflict.
   */
  private void editEntireSeries(CalendarEvent original, CalendarEvent template,
                                SeriesMaster master) throws CalendarException {

    List<CalendarEvent> toChange = master.getSortedEvents();

    boolean isTimeChange = !original.getStart().toLocalTime()
        .equals(template.getStart().toLocalTime())
        || !original.getEnd().toLocalTime()
        .equals(template.getEnd().toLocalTime());

    if (isTimeChange) {
      splitSeries(master, template, toChange);
    } else {
      modifyProperties(master, template, toChange);
    }
  }

  /**
   * Helper for editing: Modifies properties (Subject, Desc, etc.) of a list
   * of events.
   */
  private void modifyProperties(SeriesMaster master, CalendarEvent template,
                                List<CalendarEvent> eventsToChange)
      throws CalendarException {

    List<CalendarEvent> newEvents = new ArrayList<>();
    for (CalendarEvent oldEvent : eventsToChange) {
      CalendarEvent newEvent = buildPropertyChange(oldEvent, template);
      checkUniqueness(newEvent, List.of(oldEvent));
      newEvents.add(newEvent);
    }

    this.events.removeAll(eventsToChange);
    this.events.addAll(newEvents);

    master.removeEvents(eventsToChange);
    master.addEvents(newEvents);

    master.setTemplateEvent(master.getSortedEvents().get(0));
  }

  /**
   * Helper for editing: Creates a new series and (potentially) shortens
   * the old one.
   */
  private void splitSeries(SeriesMaster oldMaster, CalendarEvent newTemplate,
                           List<CalendarEvent> eventsToMove)
      throws CalendarException {

    RecurrenceRule newRule =
        new RecurrenceRule.Builder(oldMaster.getRule().getDaysOfWeek())
            .repeatsFor(eventsToMove.size())
            .build();

    SeriesMaster newMaster = new SeriesMaster(newTemplate, newRule);

    List<CalendarEvent> newInstances = new ArrayList<>();
    for (CalendarEvent oldEventToMove : eventsToMove) {
      CalendarEvent newInstance = buildTimeChange(oldEventToMove, newTemplate);
      newInstance.setSeriesMasterId(newMaster.getMasterId());

      checkUniqueness(newInstance, List.of(oldEventToMove));
      newInstances.add(newInstance);
    }

    this.events.removeAll(eventsToMove);
    this.events.addAll(newInstances);

    this.seriesMasters.add(newMaster);
    newMaster.setEvents(newInstances);

    oldMaster.removeEvents(eventsToMove);
    if (oldMaster.getEvents().isEmpty()) {
      this.seriesMasters.remove(oldMaster);
    } else {
      int remainingCount = oldMaster.getEvents().size();
      RecurrenceRule.Builder oldRuleBuilder =
          new RecurrenceRule.Builder(oldMaster.getRule().getDaysOfWeek());

      if (oldMaster.getRule().isCountBased()) {
        oldRuleBuilder.repeatsFor(remainingCount);
      } else {
        LocalDate newEndDate = oldMaster.getSortedEvents()
            .get(remainingCount - 1).getStart().toLocalDate();
        oldRuleBuilder.repeatsUntil(newEndDate);
      }
      oldMaster.setRule(oldRuleBuilder.build());
    }
  }
}