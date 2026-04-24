package calendar.controllers;

import calendar.models.Calendar;
import calendar.models.Event;
import calendar.models.EventImpl;
import calendar.views.ObservableView;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Handle the command to create a single event.
 */
public class CreateEventCommand implements Command {

  private final Calendar model;
  private final ObservableView view;
  private final String subject;
  private final LocalDate startDate;
  private final LocalTime startTime;
  private final LocalDate endDate;
  private final LocalTime endTime;

  /**
   * Initialize the command class with the details required to create an event.
   *
   * @param subject   title of the event
   * @param startDate start date of the event
   * @param startTime start time of the event
   * @param endDate   end date of the event
   * @param endTime   end time of the event
   */
  public CreateEventCommand(
      Calendar model,
      ObservableView view,
      String subject,
      LocalDate startDate,
      LocalTime startTime,
      LocalDate endDate,
      LocalTime endTime) {
    this.model = Objects.requireNonNull(model);
    this.view = Objects.requireNonNull(view);
    this.subject = Objects.requireNonNull(subject);
    this.startDate = Objects.requireNonNull(startDate);
    this.startTime = Objects.requireNonNull(startTime);
    this.endDate = Objects.requireNonNull(endDate);
    this.endTime = Objects.requireNonNull(endTime);
  }

  @Override
  public void execute() {
    try {
      Event event = EventImpl.getBuilder()
          .subject(this.subject)
          .from(startDate, startTime)
          .to(endDate, endTime)
          .build();
      boolean created = model.addEvent(event);
      if (created) {
        view.displaySuccess("Event created: " + event.getSubject());
      } else {
        view.displayError("Event already exists: " + event.getSubject());
      }
    } catch (IllegalArgumentException e) {
      view.displayError("Failed to create event: " + e.getMessage());
    }
  }
}
