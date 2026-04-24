package calendar.model;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a calendar with timezone support and event management.
 * Calendar objects are immutable once created and use Builder pattern.
 */
public class Calendar {

  private final String name;
  private final ZoneId timezone;
  private final String description;
  private final String color;
  private final boolean isDefault;
  private final List<Event> events;

  /**
   * Private constructor that takes a Builder.
   */
  private Calendar(Builder builder) {
    this.name = builder.name;
    this.timezone = builder.timezone;
    this.description = builder.description != null ? builder.description : "";
    this.color = builder.color != null ? builder.color : "#0066CC";
    this.isDefault = builder.isDefault;
    this.events = new ArrayList<>();

    validateCalendar();
  }

  /**
   * Validates the calendar properties.
   */
  private void validateCalendar() {
    if (name == null || name.trim().isEmpty()) {
      throw new IllegalArgumentException("Calendar name cannot be null or empty");
    }
    if (timezone == null) {
      throw new IllegalArgumentException("Timezone cannot be null");
    }
  }

  /**
   * Builder class for constructing Calendar objects.
   */
  public static class Builder {
    private final String name;
    private ZoneId timezone = ZoneId.systemDefault();
    private String description;
    private String color;
    private boolean isDefault = false;

    /**
     * Creates a new Calendar builder with required name.
     *
     * @param name the calendar name (required, must be unique)
     */
    public Builder(String name) {
      this.name = name;
    }

    /**
     * Sets the timezone for the calendar.
     *
     * @param timezone the timezone as ZoneId
     * @return this builder for method chaining
     */
    public Builder timezone(ZoneId timezone) {
      this.timezone = timezone;
      return this;
    }

    /**
     * Sets the timezone for the calendar using string format.
     *
     * @param timezoneString the timezone string (e.g., "America/New_York")
     * @return this builder for method chaining
     * @throws IllegalArgumentException if timezone string is invalid
     */
    public Builder timezone(String timezoneString) {
      try {
        this.timezone = ZoneId.of(timezoneString);
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid timezone: " + timezoneString, e);
      }
      return this;
    }

    /**
     * Sets the description for the calendar.
     *
     * @param description the calendar description
     * @return this builder for method chaining
     */
    public Builder description(String description) {
      this.description = description;
      return this;
    }

    /**
     * Sets the color for the calendar (for UI purposes).
     *
     * @param color the color code (e.g., "#FF0000" for red)
     * @return this builder for method chaining
     */
    public Builder color(String color) {
      this.color = color;
      return this;
    }

    /**
     * Marks this calendar as the default calendar.
     *
     * @return this builder for method chaining
     */
    public Builder asDefault() {
      this.isDefault = true;
      return this;
    }

    /**
     * Sets whether this calendar is the default.
     *
     * @param isDefault true if this should be the default calendar
     * @return this builder for method chaining
     */
    public Builder defaultCalendar(boolean isDefault) {
      this.isDefault = isDefault;
      return this;
    }

    /**
     * Creates a builder from an existing calendar (for modification).
     *
     * @param existingCalendar the calendar to copy from
     * @return a new builder with the existing calendar's properties
     */
    public static Builder from(Calendar existingCalendar) {
      return new Builder(existingCalendar.name)
          .timezone(existingCalendar.timezone)
          .description(existingCalendar.description)
          .color(existingCalendar.color)
          .defaultCalendar(existingCalendar.isDefault);
    }

    /**
     * Builds and returns the Calendar with all specified parameters.
     *
     * @return a new immutable Calendar object
     * @throws IllegalArgumentException if any validation fails
     */
    public Calendar build() {
      return new Calendar(this);
    }
  }


  /**
   * Adds an event to this calendar.
   *
   * @param event the event to add
   * @throws IllegalArgumentException if event already exists
   */
  public void addEvent(Event event) {
    if (events.contains(event)) {
      throw new IllegalArgumentException("Event already exists in calendar");
    }
    events.add(event);
  }

  /**
   * Removes an event from this calendar.
   *
   * @param event the event to remove
   * @return true if the event was removed, false if not found
   */
  public boolean removeEvent(Event event) {
    return events.remove(event);
  }

  /**
   * Gets all events in this calendar.
   *
   * @return a copy of the events list
   */
  public List<Event> getEvents() {
    return new ArrayList<>(events);
  }

  /**
   * Gets the number of events in this calendar.
   *
   * @return the event count
   */
  public int getEventCount() {
    return events.size();
  }

  /**
   * Checks if this calendar contains the specified event.
   *
   * @param event the event to check for
   * @return true if the event exists in this calendar
   */
  public boolean containsEvent(Event event) {
    return events.contains(event);
  }

  /**
   * Clears all events from this calendar.
   */
  public void clearEvents() {
    events.clear();
  }

  /**
   * Creates a new Calendar with modified properties using a builder.
   *
   * @return a builder initialized with this calendar's properties
   */
  public Builder toBuilder() {
    return Builder.from(this);
  }

  /**
   * Creates a copy of this calendar with a new name.
   *
   * @param newName the new calendar name
   * @return a new Calendar with the new name
   */
  public Calendar copyWithName(String newName) {
    return this.toBuilder()
        .build();
  }

  /**
   * Creates a copy of this calendar with a new timezone.
   *
   * @param newTimezone the new timezone
   * @return a new Calendar with the new timezone
   */
  public Calendar copyWithTimezone(ZoneId newTimezone) {
    return this.toBuilder()
        .timezone(newTimezone)
        .build();
  }

  public String getName() {
    return name;
  }

  public ZoneId getTimezone() {
    return timezone;
  }

  public String getDescription() {
    return description;
  }

  public String getColor() {
    return color;
  }

  public boolean isDefault() {
    return isDefault;
  }

  /**
   * Gets the timezone display name.
   *
   * @return the timezone display name
   */
  public String getTimezoneDisplayName() {
    return timezone.getDisplayName(
        java.time.format.TextStyle.FULL,
        java.util.Locale.getDefault()
    );
  }

  /**
   * Checks if this calendar has the same timezone as another calendar.
   *
   * @param other the other calendar
   * @return true if timezones are the same
   */
  public boolean hasSameTimezone(Calendar other) {
    return this.timezone.equals(other.timezone);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Calendar calendar = (Calendar) o;
    return Objects.equals(name, calendar.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name);
  }

  @Override
  public String toString() {
    return "Calendar{"
        + "name='" + name + '\''
        + ", timezone=" + timezone
        + ", eventCount=" + events.size()
        + ", isDefault=" + isDefault
        + '}';
  }
}