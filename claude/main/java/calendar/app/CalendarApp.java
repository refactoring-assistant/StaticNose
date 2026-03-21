package calendar.app;

import calendar.controller.CalendarController;
import calendar.controller.ICalendarController;
import calendar.model.CalendarModel;
import calendar.model.ICalendar;
import calendar.view.ConsoleView;
import calendar.view.ICalendarView;

/**
 * Entry point for the Calendar application.
 *
 * Usage:
 *   java -jar CalendarApp.jar --mode interactive
 *   java -jar CalendarApp.jar --mode headless commands.txt
 *
 * Mode argument is case-insensitive.
 */
public class CalendarApp {

    public static void main(String[] args) {
        ICalendar model         = new CalendarModel();
        ICalendarView view      = new ConsoleView();
        ICalendarController ctrl = new CalendarController(model, view);

        if (args.length < 2) {
            view.displayError("Usage: --mode interactive | --mode headless <file>");
            System.exit(1);
        }

        if (!args[0].equalsIgnoreCase("--mode")) {
            view.displayError("First argument must be --mode");
            System.exit(1);
        }

        String mode = args[1].toLowerCase();
        switch (mode) {
            case "interactive":
                ctrl.runInteractive();
                break;

            case "headless":
                if (args.length < 3) {
                    view.displayError("Headless mode requires a commands file path");
                    System.exit(1);
                }
                ctrl.runHeadless(args[2]);
                break;

            default:
                view.displayError("Unknown mode: " + args[1] + ". Use 'interactive' or 'headless'");
                System.exit(1);
        }
    }
}
