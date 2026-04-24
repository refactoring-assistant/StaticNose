package calendar.controller;

import calendar.export.CsvExporter;
import calendar.export.Exporter;
import calendar.export.IcalExporter;
import calendar.model.InterfaceEvent;
import calendar.model.Model;
import calendar.view.GuiView;
import calendar.view.dto.AvailabilityDto;
import calendar.view.dto.CopyEventDto;
import calendar.view.dto.CreateCalDto;
import calendar.view.dto.CreateEventDto;
import calendar.view.dto.EditCalDto;
import calendar.view.dto.EditEventDto;
import calendar.view.dto.ExportCalDto;
import calendar.view.dto.QueryEventDto;
import calendar.view.dto.SelectCalDto;
import calendar.view.dto.SelectDayDto;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The class below is the controller for the GUI based program.
 */
public class GuiControllerImpl implements Features {
  private final Model model;
  private final GuiView view;
  private String currentCalendarName = "Default Calendar";
  private LocalDate currentDisplayDate = LocalDate.now();

  /**
   * Below is the constructor, it takes in a Model model and GuiView view.
   *
   * @param model the model to be hooked with this controller.
   * @param view  the gui view to be hooked with this controller.
   */
  public GuiControllerImpl(Model model, GuiView view) {
    this.model = model;
    this.view = view;
    this.currentDisplayDate = LocalDate.now(); // Start on Today's month
  }

  /**
   * The method below is used to refresh the list of calendars created.
   */
  private void refreshCalendarList() {
    String raw = model.allCals();
    List<String> names = new ArrayList<>();
    for (String s : raw.split(",")) {
      names.add(getDisplayString(s.trim()));
    }
    view.updateCalendarList(names);
  }

  /**
   * The method below is used to refresh the displayed month on the view.
   */
  private void refreshMonthDisplay() {

    String monthName = currentDisplayDate.getMonth()
        .getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    int year = currentDisplayDate.getYear();
    String header = monthName + " " + year;

    String[] days = new String[42];
    YearMonth yearMonth = YearMonth.from(currentDisplayDate);
    int daysInMonth = yearMonth.lengthOfMonth();

    LocalDate firstOfMonth = currentDisplayDate.withDayOfMonth(1);
    int javaDay = firstOfMonth.getDayOfWeek().getValue();
    int startOffset = (javaDay == 7) ? 0 : javaDay;

    int currentDayNum = 1;
    for (int i = 0; i < 42; i++) {
      if (i < startOffset || currentDayNum > daysInMonth) {
        days[i] = "";
      } else {
        days[i] = String.valueOf(currentDayNum);
        currentDayNum++;
      }
    }

    view.updateMonthDisplay(header, days);
  }

  /**
   * The method below is used to create the display name of the calendar in the view.
   * It calls for the timezone of the calendar and appends that to the name.
   * if name MyCal is passed then it returns MyCal (timezone)
   *
   * @param name the unparsed name passed.
   * @return the parsed name.
   */
  private String getDisplayString(String name) {
    try {
      String tz = model.calTimezone(name);
      return name + " (" + tz + ")";
    } catch (Exception e) {
      return name;
    }
  }

  /**
   * The method below is used to extract the name of the calendar, ex name is MyCal (UTC) then it
   * returns MyCal.
   *
   * @param displayString the unparse name passed.
   * @return the parsed name.
   */
  private String extractName(String displayString) {
    if (displayString != null && displayString.contains(" (")) {
      return displayString.substring(0, displayString.lastIndexOf(" ("));
    }
    return displayString;
  }

  /**
   * The method below is used to initialize a default calendar.
   * it also then refreshes the select calendar list available on the gui view.
   */
  @Override
  public void initialize() {
    String defaultName = "Default Calendar";
    String systemTimezone = ZoneId.systemDefault().toString();
    if (!model.exists(defaultName)) {
      model.createCalendar(defaultName, systemTimezone);
    }
    refreshCalendarList();
    view.highlightActiveCalendar(getDisplayString(currentCalendarName));
    refreshMonthDisplay();
    selectDay(new SelectDayDto(LocalDate.now()));
  }

  /**
   * The method below is used to return the next month of the current month being displayed.
   */
  @Override
  public void nextMonth() {
    this.currentDisplayDate = this.currentDisplayDate.plusMonths(1);
    refreshMonthDisplay();
  }

  /**
   * The method below is used to return the previous month of the current month being displayed.
   */
  @Override
  public void prevMonth() {
    this.currentDisplayDate = this.currentDisplayDate.minusMonths(1);
    refreshMonthDisplay();
  }

  /**
   * The method below is used to query events on the selected day and display on the view.
   *
   * @param dto the data transfer object containing the selected date
   */
  @Override
  public void selectDay(SelectDayDto dto) {
    try {
      LocalDate selectedDate = dto.date();
      List<InterfaceEvent> events = model.queryEvents(
          currentCalendarName,
          selectedDate,
          null,
          null,
          null,
          false
      );

      view.displayEventList(events);
    } catch (Exception e) {
      view.showErrorPopup("Select Day Failed: " + e.getMessage());
    }
  }

  /**
   * The method below supports the query functionality if offered by the view.
   *
   * @param dto the data transfer object containing the search criteria (dates, times, etc.)
   */
  @Override
  public void querEvents(QueryEventDto dto) {
    try {
      List<InterfaceEvent> results;

      if (dto.isRangeQuery()) {

        LocalDate start = LocalDate.parse(dto.startDate());
        LocalDate end = LocalDate.parse(dto.endDate());
        LocalTime startTime = LocalTime.parse(dto.startTime());
        LocalTime endTime = LocalTime.parse(dto.endTime());
        results = model.queryEvents(currentCalendarName, start, startTime, end, endTime,
            false);

      } else {
        LocalDate date = LocalDate.parse(dto.singleDate());
        results = model.queryEvents(currentCalendarName, date, null, null,
            null, false);
      }

      view.displayEventList(results);

    } catch (Exception e) {
      view.showErrorPopup("Query Failed: " + e.getMessage());
    }
  }

  /**
   * The method below supports the functionality of checking if the user is busy at a given
   * date time.
   *
   * @param dto the data transfer object containing the date and time to check
   * @return Boolean false if busy or true if available.
   */
  @Override
  public boolean checkAvailability(AvailabilityDto dto) {
    try {
      LocalDateTime dateTime = dto.getDateTime();
      String dateTimeString = dateTime.toString();
      return model.isBusy(currentCalendarName, dateTimeString);

    } catch (Exception e) {
      view.showErrorPopup("Availability Check Failed: " + e.getMessage());
      return false;
    }
  }

  /**
   * The method below supports the functionality of creating a new calendar.
   *
   * @param dto the data transfer object containing the new calendar's name and timezone
   */
  @Override
  public void createCalendar(CreateCalDto dto) {
    try {
      model.createCalendar(dto.getCalendarName(), dto.getTimezone());
      refreshCalendarList();
      String displayName = getDisplayString(dto.getCalendarName());
      SelectCalDto selectDto = new SelectCalDto(displayName);
      selectCalendar(selectDto);
      view.showMessagePopup("Calendar '" + dto.getCalendarName() + "' created successfully.");
    } catch (Exception e) {
      e.setStackTrace(e.getStackTrace());
      view.showErrorPopup("Create Calendar Failed: " + e.getMessage());
    }
  }

  /**
   * The function below is used to select and then highlight the selected calendar on the view.
   *
   * @param dto the data transfer object containing the name of the calendar to select
   */
  @Override
  public void selectCalendar(SelectCalDto dto) {
    this.currentCalendarName = extractName(dto.getCalendarName());
    view.highlightActiveCalendar(getDisplayString(currentCalendarName));
    refreshMonthDisplay();
    view.displayEventList(null);
    selectDay(new SelectDayDto(LocalDate.now()));
  }

  /**
   * The method below supports the functionality of editing a property of a calendar,
   * either name or timezone.
   *
   * @param dto the data transfer object containing the new properties for the calendar
   */
  @Override
  public void editCalendar(EditCalDto dto) {
    try {
      boolean changed = false;
      String newName = dto.getNewName();
      String newTimezone = dto.getNewTimezone();

      if (newName != null && !newName.equals(currentCalendarName)) {
        model.editCalendar(currentCalendarName, "name", newName);
        this.currentCalendarName = newName;
        changed = true;
      }

      if (newTimezone != null) {
        model.editCalendar(currentCalendarName, "timezone", newTimezone);
        changed = true;
      }

      if (changed) {
        refreshCalendarList();
        view.highlightActiveCalendar(getDisplayString(currentCalendarName));
        view.showMessagePopup("Calendar updated successfully.");
      }

    } catch (Exception e) {
      view.showErrorPopup("Edit Failed: " + e.getMessage());
    }
  }

  /**
   * The method below supports the functionality of creating an event.
   * It receives the CreateEvent data transfer object which it converts to a CreateSpec object
   * which is then passed to the model. This allows us to reuse the model.
   *
   * @param dto the data transfer object containing raw user input for the event
   * @throws IllegalArgumentException if end date/ time is before start date/ time or if event
   *                                  name is empty.
   */
  @Override
  public void createEvent(CreateEventDto dto) {
    try {
      LocalDate startD = LocalDate.parse(dto.getStartDate());
      LocalTime startT = LocalTime.parse(dto.getStartTime());
      LocalDate endD = LocalDate.parse(dto.getEndDate());
      LocalTime endT = LocalTime.parse(dto.getEndTime());

      if (dto.isAllDay()) {
        endD = startD;
      }

      if (endD.isBefore(startD)) {
        throw new IllegalArgumentException("End date/time is before start date/time");
      } else if (endT.isBefore(startT) && startD.equals(endD)) {
        throw new IllegalArgumentException("End date/time is before start date/time");
      } else if (dto.getEventName().isEmpty()) {
        throw new IllegalArgumentException("Event name is null");
      }

      CreateSpec.CreateSpecBuilder builder = new CreateSpec.CreateSpecBuilder(
          dto.getEventName(), startD, startT, endD, endT);

      builder.description(dto.getDescription());
      builder.location(dto.getLocation());
      builder.status(dto.getStatus());
      addRecur(dto, builder);

      model.create(currentCalendarName, builder.build());
      selectDay(new SelectDayDto(startD));
      view.showMessagePopup("Event '" + dto.getEventName() + "' created successfully.");

    } catch (Exception e) {
      view.showErrorPopup("Create Event Failed: " + e.getMessage());
    }
  }

  /**
   * The function below is the helper function of the create event function above, its job is
   * to add recurrent event information like days of week 'MWF', recurring n times or until.
   *
   * @param dto     The view create events data transfer object
   * @param builder The builder object of CreateSpec
   */
  private void addRecur(CreateEventDto dto, CreateSpec.CreateSpecBuilder builder) {
    if (dto.isRecurring()) {
      if (!builder.getStartDate().equals(builder.getEndDate())) {
        throw new IllegalArgumentException("Recurrent events cannot span multiple days!");
      }
      String weekdays = dto.getRecurrenceDays();
      if (weekdays.isEmpty()) {
        throw new IllegalArgumentException("Recurring event must specify days (e.g. MWF)");
      }

      builder.weekdays(weekdays);
      String endRecur = dto.getRecurrenceEnd();
      LocalDate until;
      if (!endRecur.isEmpty()) {
        if (endRecur.matches("\\d+")) {
          builder.times(Integer.parseInt(endRecur));
        } else {
          try {
            until = LocalDate.parse(endRecur);
          } catch (Exception e) {
            throw new IllegalArgumentException(
                "Invalid recurrence end. Use a number (5) or date (YYYY-MM-DD).");
          }
          if (until.isBefore(builder.getEndDate())) {
            throw new IllegalArgumentException("Until date has to be after end date");
          }
          builder.until(LocalDate.parse(endRecur));
        }
      } else {
        throw new IllegalArgumentException("Recurring event must specify count or end date.");
      }
    }
  }

  /**
   * The function below supports the functionality of editing an event in the calendar.
   * It receives a view Edit events data transfer object which it converts to a EditSpec
   * object which is then passed to the model. This allows us to reuse the model.
   *
   * @param request the data transfer object containing raw user input for the event
   */
  @Override
  public void editEvent(EditEventDto request) {
    try {

      String uiProperty = request.getPropertyToEdit().toLowerCase();
      String newValue = request.getNewValue();
      boolean isDateEdit = false;
      boolean isTimeEdit = false;
      if (uiProperty.equalsIgnoreCase("Start Date")) {
        isDateEdit = true;
        uiProperty = "start";
      } else if (uiProperty.equalsIgnoreCase("Start Time")) {
        isTimeEdit = true;
        uiProperty = "start";
      } else if (uiProperty.equalsIgnoreCase("End Date")) {
        isDateEdit = true;
        uiProperty = "end";
      } else if (uiProperty.equalsIgnoreCase("End Time")) {
        isTimeEdit = true;
        uiProperty = "end";
      }

      String typeStr;
      switch (request.getScope()) {
        case FUTURE_EVENTS:
          typeStr = "events";
          break;
        case ENTIRE_SERIES:
          typeStr = "series";
          break;
        default:
          typeStr = "event";
          break;
      }

      calendar.controller.EditSpec.EditSpecBuilder builder =
          new calendar.controller.EditSpec.EditSpecBuilder(
              typeStr, uiProperty, request.getSubject(), request.getStartDate(),
              request.getStartTime(), newValue);

      addAdditional(request, builder, isDateEdit, isTimeEdit, uiProperty);
      model.edit(currentCalendarName, builder.build());
      refreshMonthDisplay();
      selectDay(new SelectDayDto(request.getStartDate()));
      view.showMessagePopup("Event updated successfully.");

    } catch (Exception e) {
      view.showErrorPopup("Edit Failed: " + e.getMessage());
    }
  }

  /**
   * The method below is a helper method of the above edit event function.
   * It is specifically checks if any of the date or time fields have to be changed and calculates
   * the minutes or days difference between the new input and old value.
   *
   * @param request    The view edit event data transfer object
   * @param builder    the builder object of EditSpec
   * @param isDateEdit True if date has to be edited else false
   * @param isTimeEdit True if time has to be edited else false.
   * @param startEnd   'start' if start date / time is to be edited or 'end' if end date / time
   *                   is to be edited.
   */
  private void addAdditional(EditEventDto request, EditSpec.EditSpecBuilder builder,
                             boolean isDateEdit,
                             boolean isTimeEdit, String startEnd) {

    builder.daysDiff(0);
    builder.minsDiff(0);
    if (request.getScope().equals(EditEventDto.Scope.SINGLE_EVENT)) {
      builder.endDate(request.getEndDate());
      builder.endTime(request.getEndTime());
    }

    if (startEnd.equals("start") || startEnd.equals("end")) {
      if (isDateEdit) {
        LocalDate newDate = LocalDate.parse(request.getNewValue());
        LocalDate oldDate =
            startEnd.equals("start") ? request.getStartDate() : request.getEndDate();
        long days = java.time.temporal.ChronoUnit.DAYS.between(oldDate, newDate);

        if (startEnd.equals("end")) {
          if (request.getEndDate().plusDays(days).isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("Edited End Date cannot be before Start Date");
          }
        }
        builder.daysDiff(days);
      } else {
        LocalTime newTime = LocalTime.parse(request.getNewValue());
        LocalTime oldTime =
            startEnd.equals("start") ? request.getStartTime() : request.getEndTime();
        long mins = java.time.Duration.between(oldTime, newTime).toMinutes();
        if (startEnd.equals("end")) {
          if (request.getEndTime().plusMinutes(mins).isBefore(request.getStartTime())) {
            throw new IllegalArgumentException("Edited End Time cannot be before Start Time");
          }
        }
        builder.minsDiff(mins);
      }
    }
  }

  /**
   * The function below supports the functionality of copying an event in the calendar to the same
   * or different calendar.
   * It receives a view copy events data transfer object which it converts to a CopySpec object
   * which is then passed to the model. This allows us to reuse the model.
   *
   * @param dto the data transfer object containing raw user input for the copy event
   */
  @Override
  public void copyEvent(CopyEventDto dto) {
    try {
      String rawTargetName = dto.getTargetCalendarName();
      String cleanTargetName = extractName(rawTargetName);
      LocalDate startDate = LocalDate.parse(dto.getStartDate());
      LocalDate targetDate = LocalDate.parse(dto.getTargetDate());
      LocalTime startTime = null;
      LocalTime targetTime = null;
      LocalDate endDate = null;
      String subject = null;

      if (dto.getMode().equals(CopyEventDto.CopyMode.SELECTED_EVENT)) {
        startTime = LocalTime.parse(dto.getStartTime());
        targetTime = LocalTime.parse(dto.getTargetTime());
        subject = dto.getSubject();
      } else if (dto.getMode().equals(CopyEventDto.CopyMode.DATE_RANGE)) {
        endDate = LocalDate.parse(dto.getEndDate());
      }

      calendar.controller.CopySpec.CopySpecBuilder builder =
          new CopySpec.CopySpecBuilder(startDate, cleanTargetName, targetDate)
              .endDate(endDate)
              .startTime(startTime)
              .targetTime(targetTime)
              .subject(subject);

      model.copy(currentCalendarName, builder.build());
      refreshMonthDisplay();
      view.showMessagePopup("Events copied successfully.");
    } catch (Exception e) {
      view.showErrorPopup("Copy Failed: " + e.getMessage());
    }
  }

  /**
   * The function below supports the functionality of exporting a calendar in the specified .csv
   * or .ical format.
   * It receives a view export events data transfer object which it uses to extract information,
   * this info is then passed to the model. This allows us to reuse the model.
   *
   * @param dto the data transfer object containing raw user input for the export.
   */
  @Override
  public void exportCalendar(ExportCalDto dto) {
    try {
      List<InterfaceEvent> allEvents = model.queryEvents(
          currentCalendarName,
          null, null, null, null,
          true
      );

      Exporter exporter;
      if (".csv".equalsIgnoreCase(dto.getFormat())) {
        exporter = new CsvExporter();
      } else {
        exporter = new IcalExporter();
      }

      String filename = dto.getFullFilename();
      exporter.export(allEvents, filename);

      String fullPath = new File(filename).getAbsolutePath();
      view.showMessagePopup("Calendar exported successfully to:\n" + fullPath);

    } catch (Exception e) {
      view.showErrorPopup("Export Calendar Failed: " + e.getMessage());
    }
  }
}
