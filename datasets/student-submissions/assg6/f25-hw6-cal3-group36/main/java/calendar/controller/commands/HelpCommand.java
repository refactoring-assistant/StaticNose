package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.CalendarSystemModel;

/**
 * Lists all the commands available.
 */
public class HelpCommand implements Command {

  @Override
  public String execute(CalendarSystemModel model) {
    return String.join(System.lineSeparator(),
        "Available commands:",
        "create event subject <s> from <t1> to <t2>",
        "create events subject <s> from <t1> to <t2> repeats <days> <count> times",
        "edit event subject <s> from <t1> to <t2> with <new>",
        "print events on <date>",
        "print events from <d1> to <d2>",
        "show status on <dateTtime>",
        "copy event <subj> from <start> to <newStart> calendar <target>",
        "copy events on <date> to <target> date <targetDate>",
        "export <filename>.csv|.ical",
        "exit");
  }
}