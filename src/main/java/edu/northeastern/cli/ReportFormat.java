package edu.northeastern.cli;

/**
 * Enum to store types of report formats.
 */
public enum ReportFormat {
    CSV("csv"),
    JSON("json");

    private final String format;

    ReportFormat(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }

    @Override
    public String toString() {
        return format;
    }
}
