package calendar.model;

import java.util.regex.Pattern;

/**
 * Represents a participant or creator of a calendar event.
 * Name: letters and spaces only.
 * Email: username@domain.ext where ext is com/org/edu/net,
 *        username cannot start/end with a period or have consecutive periods.
 */
public class Participant {

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-z][A-Za-z ]*$");

    // username@domain.ext  (ext = com|org|edu|net)
    // username rules: no leading/trailing dot, no consecutive dots
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^(?!\\.)[A-Za-z0-9._%+\\-]+(?<!\\.)@[A-Za-z0-9\\-]+(\\.[A-Za-z0-9\\-]+)*\\.(com|org|edu|net)$");

    private static final Pattern NO_CONSECUTIVE_DOTS = Pattern.compile("\\.\\.");

    private final String name;
    private final String email;

    public Participant(String name, String email) throws CalendarException {
        if (name == null || name.trim().isEmpty()) {
            throw new CalendarException("Participant name cannot be empty");
        }
        String trimmedName = name.trim();
        if (!NAME_PATTERN.matcher(trimmedName).matches()) {
            throw new CalendarException("Participant name must contain letters and spaces only: " + name);
        }
        if (email == null || email.trim().isEmpty()) {
            throw new CalendarException("Participant email cannot be empty");
        }
        String trimmedEmail = email.trim().toLowerCase();
        // check consecutive dots in local part
        String localPart = trimmedEmail.contains("@") ? trimmedEmail.substring(0, trimmedEmail.indexOf('@')) : trimmedEmail;
        if (NO_CONSECUTIVE_DOTS.matcher(localPart).find()) {
            throw new CalendarException("Email username cannot have consecutive periods: " + email);
        }
        if (!EMAIL_PATTERN.matcher(trimmedEmail).matches()) {
            throw new CalendarException("Invalid email address: " + email);
        }
        this.name = trimmedName;
        this.email = trimmedEmail;
    }

    public String getName() { return name; }

    public String getEmail() { return email; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Participant)) return false;
        Participant other = (Participant) o;
        return email.equalsIgnoreCase(other.email);
    }

    @Override
    public int hashCode() {
        return email.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return name + ":" + email;
    }

    /**
     * Parse a participant from "Name:email" format.
     */
    public static Participant parse(String token) throws CalendarException {
        int colonIdx = token.indexOf(':');
        if (colonIdx < 0) {
            throw new CalendarException("Participant must be in Name:email format: " + token);
        }
        String name = token.substring(0, colonIdx).trim();
        String email = token.substring(colonIdx + 1).trim();
        return new Participant(name, email);
    }
}
