package calendar.controller;

abstract class CommandFactory {
  protected String subject;
  protected String startDateTime;
  protected String endDateTime;
  protected String weekdays;
  protected int repeatTimes;
  protected String seriesEndDateTime;

  protected String description;
  protected String location;
  protected String eventStatus;

  protected abstract void parseCommand(String commandLine);

  protected abstract void execute();
}
