package calendar.model;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Represents a predicate class that filters events based on the provided event subject.
 */
public class EventSubjectFilter implements Predicate<EventObject> {
  private final String subject;

  /**
   * Constructs an Event Subject Filter, given the event subject.
   *
   * @param subject event subject.
   */
  public EventSubjectFilter(String subject) {
    this.subject = Objects.requireNonNull(subject, "Subject cannot be null");
  }

  @Override
  public boolean test(EventObject event) {
    return event.getSubject().equals(this.subject);
  }
}
