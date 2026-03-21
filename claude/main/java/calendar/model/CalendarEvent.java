package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single calendar event (standalone or part of a series).
 */
public class CalendarEvent {

    private static final int MAX_SUBJECT_LENGTH = 150;
    static final LocalTime ALL_DAY_START = LocalTime.of(8, 0);
    static final LocalTime ALL_DAY_END   = LocalTime.of(17, 0);

    // Required fields
    private String subject;
    private LocalDateTime startDateTime;

    // Optional fields
    private LocalDateTime endDateTime;
    private String location;
    private EventStatus status;
    private List<Participant> participants;
    private Participant creator;
    private LocalDateTime createdAt;
    private String description;
    private boolean allDay;

    // Series tracking (null for standalone events)
    private String seriesId;

    /**
     * Constructs a timed event with explicit start and end.
     */
    public CalendarEvent(String subject, LocalDateTime startDateTime, LocalDateTime endDateTime)
            throws CalendarException {
        setSubject(subject);
        setStartDateTime(startDateTime);
        setEndDateTime(endDateTime);
        this.location    = "Online";
        this.status      = EventStatus.CONFIRMED;
        this.participants = new ArrayList<>();
        this.createdAt   = LocalDateTime.now();
        this.allDay      = false;
    }

    /**
     * Constructs an all-day event on the given date (08:00 - 17:00).
     */
    public CalendarEvent(String subject, LocalDate date) throws CalendarException {
        setSubject(subject);
        this.startDateTime = date.atTime(ALL_DAY_START);
        this.endDateTime   = date.atTime(ALL_DAY_END);
        this.location      = "Online";
        this.status        = EventStatus.CONFIRMED;
        this.participants  = new ArrayList<>();
        this.createdAt     = LocalDateTime.now();
        this.allDay        = true;
    }

    // ---- Validation helpers ----

    private void validateSubject(String s) throws CalendarException {
        if (s == null || s.trim().isEmpty()) {
            throw new CalendarException("Subject cannot be empty");
        }
        if (s.trim().length() > MAX_SUBJECT_LENGTH) {
            throw new CalendarException("Subject exceeds maximum length of " + MAX_SUBJECT_LENGTH);
        }
    }

    // ---- Setters ----

    public void setSubject(String subject) throws CalendarException {
        validateSubject(subject);
        this.subject = subject.trim();
    }

    public void setStartDateTime(LocalDateTime startDateTime) throws CalendarException {
        if (startDateTime == null) throw new CalendarException("Start date/time cannot be null");
        this.startDateTime = startDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) throws CalendarException {
        if (endDateTime != null && this.startDateTime != null
                && endDateTime.isBefore(this.startDateTime)) {
            throw new CalendarException("End date/time cannot be before start date/time");
        }
        this.endDateTime = endDateTime;
    }

    public void setLocation(String location) {
        this.location = (location == null || location.trim().isEmpty()) ? "Online" : location.trim();
    }

    public void setStatus(EventStatus status) {
        this.status = (status == null) ? EventStatus.CONFIRMED : status;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = (participants == null) ? new ArrayList<>() : new ArrayList<>(participants);
    }

    public void setCreator(Participant creator) {
        this.creator = creator;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAllDay(boolean allDay) {
        this.allDay = allDay;
    }

    public void setSeriesId(String seriesId) {
        this.seriesId = seriesId;
    }

    // ---- Getters ----

    public String getSubject()              { return subject; }
    public LocalDateTime getStartDateTime() { return startDateTime; }
    public LocalDateTime getEndDateTime()   { return endDateTime; }
    public String getLocation()             { return location; }
    public EventStatus getStatus()          { return status; }
    public List<Participant> getParticipants() {
        return Collections.unmodifiableList(participants);
    }
    public Participant getCreator()         { return creator; }
    public LocalDateTime getCreatedAt()     { return createdAt; }
    public String getDescription()          { return description; }
    public boolean isAllDay()               { return allDay; }
    public String getSeriesId()             { return seriesId; }

    /**
     * Deep-copy this event (for editing series members independently).
     */
    public CalendarEvent copy() throws CalendarException {
        CalendarEvent copy;
        if (allDay) {
            copy = new CalendarEvent(subject, startDateTime.toLocalDate());
        } else {
            copy = new CalendarEvent(subject, startDateTime, endDateTime);
        }
        copy.location     = this.location;
        copy.status       = this.status;
        copy.participants = new ArrayList<>(this.participants);
        copy.creator      = this.creator;
        copy.createdAt    = this.createdAt;
        copy.description  = this.description;
        copy.allDay       = this.allDay;
        copy.seriesId     = this.seriesId;
        return copy;
    }

    /**
     * Returns true if this event overlaps the given date (any part of the event falls on it).
     */
    public boolean overlapsDate(LocalDate date) {
        LocalDate startDate = startDateTime.toLocalDate();
        LocalDate endDate   = (endDateTime != null) ? endDateTime.toLocalDate() : startDate;
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Returns true if this event overlaps the range [from, to] (inclusive boundary check on datetimes).
     */
    public boolean overlapsRange(LocalDateTime from, LocalDateTime to) {
        LocalDateTime evEnd = (endDateTime != null) ? endDateTime : startDateTime;
        // event overlaps range if event starts before range ends AND event ends after range starts
        return !startDateTime.isAfter(to) && !evEnd.isBefore(from);
    }

    /**
     * Returns true if any event is active at the given datetime.
     */
    public boolean isActiveAt(LocalDateTime dt) {
        LocalDateTime evEnd = (endDateTime != null) ? endDateTime : startDateTime;
        return !startDateTime.isAfter(dt) && !evEnd.isBefore(dt);
    }

    /**
     * Two events are duplicates if they share subject + startDateTime + endDateTime.
     */
    public boolean isDuplicateOf(CalendarEvent other) {
        return Objects.equals(this.subject, other.subject)
                && Objects.equals(this.startDateTime, other.startDateTime)
                && Objects.equals(this.endDateTime, other.endDateTime);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("NAME: ").append(subject);
        sb.append(" starting on ").append(startDateTime.toLocalDate());
        sb.append(" at ").append(String.format("%02d:%02d", startDateTime.getHour(), startDateTime.getMinute()));
        LocalDateTime end = (endDateTime != null) ? endDateTime : startDateTime;
        sb.append("; ending on ").append(end.toLocalDate());
        sb.append(" at ").append(String.format("%02d:%02d", end.getHour(), end.getMinute()));
        sb.append(" STATUS: ").append(status);
        sb.append(" PARTICIPANTS:");
        if (!participants.isEmpty()) {
            StringBuilder pEmails = new StringBuilder();
            for (Participant p : participants) {
                if (pEmails.length() > 0) pEmails.append(",");
                pEmails.append(p.getEmail());
            }
            sb.append(pEmails);
        }
        return sb.toString();
    }
}
