package controller.command;

import static model.CalendarConstants.ALL_DAY_END;
import static model.CalendarConstants.ALL_DAY_START;

import controller.CommandResult;
import java.time.LocalDateTime;
import model.Icalendar;
import model.SingleEvent;



/**
 * Command implementation for creating single (non-recurring) events in the calendar.
 * This command supports creating regular timed events and all-day events.
 */
public class CreateSingleEventCommand implements Command {
  private final String subject;
  private final LocalDateTime startDateTime;
  private final LocalDateTime endDateTime;
  private final String description;
  private final String location;
  private final boolean isPublic;

  /**
   * Private constructor - instances are created through the Builder pattern.
   *
   * @param builder the builder containing event properties
   */
  private CreateSingleEventCommand(Builder builder) {
    this.subject = builder.subject;
    this.startDateTime = builder.startDateTime;
    this.endDateTime = builder.endDateTime;
    this.description = builder.description;
    this.location = builder.location;
    this.isPublic = builder.isPublic;
  }

  /**
   * Creates a new Builder instance for constructing CreateSingleEventCommand objects.
   *
   * @return a new Builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  @Override
  public CommandResult execute(Icalendar calendar) {
    try {
      SingleEvent event = SingleEvent.getBuilder()
          .subject(subject)
          .startDateTime(startDateTime)
          .endDateTime(endDateTime)
          .description(description != null ? description : "")
          .location(location != null ? location : "")
          .isPublic(isPublic)
          .build();

      calendar.addSingleEvent(event);

      boolean isAllDay = startDateTime.toLocalTime().equals(ALL_DAY_START)
          && endDateTime.toLocalTime().equals(ALL_DAY_END)
          && startDateTime.toLocalDate().equals(endDateTime.toLocalDate());

      if (isAllDay) {
        return new CommandResult(true, "All-day event created: " + subject);
      } else {
        return new CommandResult(true, "Event created: " + subject);
      }
    } catch (Exception e) {
      return new CommandResult(false, "Error creating event: " + e.getMessage());
    }
  }


  /**
   * Builder class for constructing CreateSingleEventCommand instances.
   * Provides a fluent interface for setting event properties and validates
   * them before building the command.
   */
  public static class Builder {
    private String subject;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private String description = null;
    private String location = null;
    private boolean isPublic = true;

    private Builder() {
    }

    /**
     * Sets the event subject.
     *
     * @param subject the event subject (required)
     * @return this builder for method chaining
     */
    public Builder subject(String subject) {
      this.subject = subject;
      return this;
    }

    /**
     * Sets the event start date and time.
     *
     * @param startDateTime the start date/time (required)
     * @return this builder for method chaining
     */
    public Builder startDateTime(LocalDateTime startDateTime) {
      this.startDateTime = startDateTime;
      return this;
    }

    /**
     * Sets the event end date and time.
     *
     * @param endDateTime the end date/time (required)
     * @return this builder for method chaining
     */
    public Builder endDateTime(LocalDateTime endDateTime) {
      this.endDateTime = endDateTime;
      return this;
    }


    /**
     * Sets the event description.
     *
     * @param description the event description (optional)
     * @return this builder for method chaining
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the event location.
     *
     * @param location the event location (optional)
     * @return this builder for method chaining
     */
    public Builder location(String location) {
      this.location = location;
      return this;
    }

    /**
     * Sets the event visibility.
     *
     * @param isPublic true for public, false for private
     * @return this builder for method chaining
     */
    public Builder isPublic(boolean isPublic) {
      this.isPublic = isPublic;
      return this;
    }

    /**
     * Builds the CreateSingleEventCommand with validation.
     *
     * @return a new command instance
     * @throws IllegalArgumentException if required fields are missing or invalid
     */
    public CreateSingleEventCommand build() {
      if (subject == null || subject.trim().isEmpty()) {
        throw new IllegalArgumentException("Subject is required");
      }
      if (startDateTime == null) {
        throw new IllegalArgumentException("Start date/time is required");
      }
      if (endDateTime == null) {
        throw new IllegalArgumentException("End date/time is required");
      }
      if (endDateTime.isBefore(startDateTime) || endDateTime.equals(startDateTime)) {
        throw new IllegalArgumentException("End date/time must be after start date/time");
      }

      return new CreateSingleEventCommand(this);
    }
  }
}