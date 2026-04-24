package calendar.controller;

/**
 * Interface for the calendar controller. Parses commands and delegates to model/view.
 */
public interface ICalendarController {

    /**
     * Process a single command string and execute it.
     *
     * @param command the raw command string
     * @return true if the application should continue, false if "exit" was given
     */
    boolean processCommand(String command);

    /**
     * Run the application in interactive mode, reading commands from stdin.
     */
    void runInteractive();

    /**
     * Run the application in headless mode, reading commands from a file.
     *
     * @param filename the path to the commands file
     */
    void runHeadless(String filename);
}
