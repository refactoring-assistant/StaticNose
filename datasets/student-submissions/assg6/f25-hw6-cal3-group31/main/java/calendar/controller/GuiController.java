package calendar.controller;

import calendar.model.Date;
import calendar.model.IntCalendarManager;
import calendar.model.IntEvent;
import calendar.model.Location;
import calendar.model.Status;
import calendar.model.Time;
import calendar.view.EventDetailsPanel;
import calendar.view.EventDetailsPanelRunnable;
import calendar.view.IntGuiView;
import calendar.view.ViewEvent;
import calendar.view.dialog.CalendarDialogResult;
import calendar.view.dialog.DialogFactory;
import calendar.view.dialog.EventDialogResult;
import calendar.view.dialog.IntDialog;
import java.awt.Component;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.zone.ZoneRulesException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.swing.JOptionPane;

/**
 * Controller for the GUI version of the Calendar program. Handles requests coming in from
 * the GUI view and queries the model for the required data for the GUI view to display.
 */
public class GuiController implements IntController, IntViewRequestHandler {
  private final IntCalendarManager model;
  private final IntGuiView view;
  private final DialogFactory dialogFactory;

  /**
   * GuiController constructor.
   *
   * @param calendarManager the IntCalendarManager to use
   * @param guiView         the IntGuiView to use
   */
  public GuiController(IntCalendarManager calendarManager, IntGuiView guiView,
                       DialogFactory dialogFactory) {
    this.model = Objects.requireNonNull(calendarManager);
    this.view = Objects.requireNonNull(guiView);
    this.dialogFactory = dialogFactory;
  }

  @Override
  public void go() {

    // Set up a default calendar
    ZoneId systemDefaultZoneId;
    systemDefaultZoneId = ZoneId.systemDefault();

    view.setRequestHandler(this);
    model.createCalendar("Default Calendar", systemDefaultZoneId);
    view.addCalendar(model.getActiveCalendarName());

    // Hand off control of the program to the view
    view.go();
  }

  @Override
  public List<ViewEvent> handleGetViewEventsInRange(String calendarName,
                                                    int startYear, int startMonth, int startDay,
                                                    int endYear, int endMonth, int endDay) {
    List<ViewEvent> result = new ArrayList<>();

    Date startDate = new Date(startYear, startMonth, startDay);
    Time startTime = new Time(0, 0);
    Date endDate = new Date(endYear, endMonth, endDay);
    Time endTime = new Time(23, 59);

    try {
      List<IntEvent> events = model.getCalendar(calendarName).getEventsInRange(
          startDate, startTime, endDate, endTime);
      for (int i = 0; i < events.size(); i++) {
        IntEvent event = events.get(i);

        String locationStr = event.getLocation() != null ? event.getLocation().toString() : null;
        String statusStr = event.getStatus() != null ? event.getStatus().toString() : null;

        ViewEvent viewEvent = new ViewEvent(
            event.getSubject(),
            event.getStartDate().getYear(), event.getStartDate().getMonth(),
            event.getStartDate().getDay(),
            event.getStartTime().getHour(), event.getStartTime().getMinute(),
            event.getEndDate().getYear(), event.getEndDate().getMonth(),
            event.getEndDate().getDay(),
            event.getEndTime().getHour(), event.getEndTime().getMinute(),
            event.getDescription(), locationStr, statusStr);

        result.add(viewEvent);
      }
    } catch (IllegalArgumentException e) {
      view.createMessagePopup(
          e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); // If getCalendar failed
    }

    return result;
  }

  @Override
  public CalendarDialogResult handleCreateCalendarRequest() {
    // Get existing calendar names for validation
    Set<String> existingNames = new HashSet<>(model.getAllCalendarNames());

    // Create and show dialog
    IntDialog<CalendarDialogResult> dialog = dialogFactory.createCalendarDialog(existingNames);
    dialog.setParent((Component) view);
    CalendarDialogResult result = dialog.showDialog();

    if (result != null) {
      try {
        // Convert String timezone to ZoneId
        ZoneId timezone = ZoneId.of(result.getTimezone());

        // Call model
        model.createCalendar(result.getName(), timezone);

        // Update view
        view.addCalendar(result.getName());

        view.createMessagePopup(
            "Calendar '" + result.getName() + "' created successfully!",
            "Success",
            JOptionPane.INFORMATION_MESSAGE);
        //        JOptionPane.showMessageDialog((Component) view,
        //            "Calendar '" + result.getName() + "' created successfully!",
        //            "Success",
        //            JOptionPane.INFORMATION_MESSAGE);
      } catch (Exception e) {
        view.createMessagePopup("Error creating calendar: " + e.getMessage(),
            "Error", JOptionPane.ERROR_MESSAGE);
        //        JOptionPane.showMessageDialog((Component) view,
        //            "Error creating calendar: " + e.getMessage(),
        //            "Error",
        //            JOptionPane.ERROR_MESSAGE);
      }
    }

    return result;
  }

  @Override
  public EventDialogResult handleCreateEventRequest(int initialYear, int initialMonth,
                                                    int initialDay) {
    // Check if there's an active calendar
    if (model.getAllCalendarNames().isEmpty()) {
      view.createMessagePopup(
          "Please create a calendar first before adding events.",
          "No Calendar",
          JOptionPane.WARNING_MESSAGE);
      //      JOptionPane.showMessageDialog((Component) view,
      //          "Please create a calendar first before adding events.",
      //          "No Calendar",
      //          JOptionPane.WARNING_MESSAGE);
      return null;
    }

    // Create and show dialog
    IntDialog<EventDialogResult> dialog = dialogFactory.createEventDialog(
        initialYear, initialMonth, initialDay);
    dialog.setParent((Component) view);
    EventDialogResult result = dialog.showDialog();

    if (result != null) {
      try {
        // Convert primitives to model objects
        Date startDate = new Date(result.getStartYear(), result.getStartMonth(),
            result.getStartDay());
        Time startTime = new Time(result.getStartHour(), result.getStartMinute());
        Date endDate = new Date(result.getEndYear(), result.getEndMonth(), result.getEndDay());
        Time endTime = new Time(result.getEndHour(), result.getEndMinute());

        String description = result.getDescription();
        Location location = result.getLocation() != null
            ? Location.valueOf(result.getLocation()) : null;
        Status status = result.getStatus() != null ? Status.valueOf(result.getStatus()) : null;

        // Use the most complete overload available
        if (description != null && location != null && status != null) {
          model.getActiveCalendar().createEvent(
              result.getSubject(), startDate, startTime, endDate, endTime,
              description, location, status);
        } else if (description != null) {
          model.getActiveCalendar().createEvent(
              result.getSubject(), startDate, startTime, endDate, endTime,
              description);
        } else if (location != null) {
          model.getActiveCalendar().createEvent(
              result.getSubject(), startDate, startTime, endDate, endTime,
              location);
        } else if (status != null) {
          model.getActiveCalendar().createEvent(
              result.getSubject(), startDate, startTime, endDate, endTime,
              status);
        } else {
          model.getActiveCalendar().createEvent(
              result.getSubject(), startDate, startTime, endDate, endTime);
        }

        // Refresh the calendar display to show the new event
        view.refreshCalendarDisplay();

        view.createMessagePopup(
            "Event '" + result.getSubject() + "' created successfully!",
            "Success",
            JOptionPane.INFORMATION_MESSAGE);
        //        JOptionPane.showMessageDialog((Component) view,
        //            "Event '" + result.getSubject() + "' created successfully!",
        //            "Success",
        //            JOptionPane.INFORMATION_MESSAGE);
      } catch (Exception e) {
        view.createMessagePopup(
            "Error creating event: " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
        //        JOptionPane.showMessageDialog((Component) view,
        //            "Error creating event: " + e.getMessage(),
        //            "Error",
        //            JOptionPane.ERROR_MESSAGE);
      }
    }

    return result;
  }

  @Override
  public Set<String> getExistingCalendarNames() {
    return new HashSet<>(model.getAllCalendarNames());
  }

  @Override
  public EventDetailsPanel handleViewEventDetailsRequest(ViewEvent event) {
    // Create a details panel with an edit callback
    EventDetailsPanel detailsPanel = new EventDetailsPanel(event,
        new EventDetailsPanelRunnable(event, view));

    // Show the details panel in a dialog

    view.createMessagePopup(detailsPanel, "Event Details", JOptionPane.PLAIN_MESSAGE);
    //    JOptionPane.showMessageDialog((Component) view,
    //        detailsPanel,
    //        "Event Details",
    //        JOptionPane.PLAIN_MESSAGE);

    return detailsPanel;
  }

  @Override
  public EventDialogResult handleEditEventRequest(ViewEvent event) {
    // Create and show edit dialog
    EventDialogResult eventData = new EventDialogResult(
        event.getSubject(),
        event.getStartYear(), event.getStartMonth(), event.getStartDay(),
        event.getStartHour(), event.getStartMinute(),
        event.getEndYear(), event.getEndMonth(), event.getEndDay(),
        event.getEndHour(), event.getEndMinute(),
        event.getDescription(), event.getLocation(), event.getStatus());

    IntDialog<EventDialogResult> dialog = dialogFactory.createEditEventDialog(eventData);
    dialog.setParent((Component) view);
    EventDialogResult result = dialog.showDialog();

    if (result != null) {
      try {
        // Convert primitives to model objects
        Date oldStartDate = new Date(event.getStartYear(), event.getStartMonth(),
            event.getStartDay());
        Time oldStartTime = new Time(event.getStartHour(), event.getStartMinute());
        Date oldEndDate = new Date(event.getEndYear(), event.getEndMonth(), event.getEndDay());
        Time oldEndTime = new Time(event.getEndHour(), event.getEndMinute());

        // Track the current event identifier for subsequent edits
        String currentSubject = event.getSubject();
        Date currentStartDate = oldStartDate;
        Time currentStartTime = oldStartTime;
        Date currentEndDate = oldEndDate;
        Time currentEndTime = oldEndTime;

        // Edit subject first if changed
        if (!event.getSubject().equals(result.getSubject())) {
          model.getActiveCalendar().editEvent(
              currentSubject, currentStartDate, currentStartTime,
              currentEndDate, currentEndTime, "subject", result.getSubject());
          currentSubject = result.getSubject();
        }

        // Edit description if changed
        if (!Objects.equals(event.getDescription(), result.getDescription())) {
          String desc = result.getDescription() != null ? result.getDescription() : "";
          model.getActiveCalendar().editEvent(
              currentSubject, currentStartDate, currentStartTime,
              currentEndDate, currentEndTime, "description", desc);
        }

        // Edit location if changed
        if (!Objects.equals(event.getLocation(), result.getLocation())) {
          String loc = result.getLocation() != null ? result.getLocation() : "";
          model.getActiveCalendar().editEvent(
              currentSubject, currentStartDate, currentStartTime,
              currentEndDate, currentEndTime, "location", loc);
        }

        // Edit status if changed
        if (!Objects.equals(event.getStatus(), result.getStatus())) {
          String status = result.getStatus() != null ? result.getStatus() : "";
          model.getActiveCalendar().editEvent(
              currentSubject, currentStartDate, currentStartTime,
              currentEndDate, currentEndTime, "status", status);
        }

        // Edit end date/time if changed
        if (event.getEndYear() != result.getEndYear()
            || event.getEndMonth() != result.getEndMonth()
            || event.getEndDay() != result.getEndDay()
            || event.getEndHour() != result.getEndHour()
            || event.getEndMinute() != result.getEndMinute()) {
          String newEnd = String.format("%04d-%02d-%02dT%02d:%02d",
              result.getEndYear(), result.getEndMonth(), result.getEndDay(),
              result.getEndHour(), result.getEndMinute());
          model.getActiveCalendar().editEvent(
              currentSubject, currentStartDate, currentStartTime,
              currentEndDate, currentEndTime, "end", newEnd);
          currentEndDate = new Date(result.getEndYear(), result.getEndMonth(),
              result.getEndDay());
          currentEndTime = new Time(result.getEndHour(), result.getEndMinute());
        }

        // Edit start date/time LAST since it's used to identify the event
        if (event.getStartYear() != result.getStartYear()
            || event.getStartMonth() != result.getStartMonth()
            || event.getStartDay() != result.getStartDay()
            || event.getStartHour() != result.getStartHour()
            || event.getStartMinute() != result.getStartMinute()) {
          String newStart = String.format("%04d-%02d-%02dT%02d:%02d",
              result.getStartYear(), result.getStartMonth(), result.getStartDay(),
              result.getStartHour(), result.getStartMinute());
          model.getActiveCalendar().editEvent(
              currentSubject, currentStartDate, currentStartTime,
              currentEndDate, currentEndTime, "start", newStart);
        }

        // Refresh the calendar display to show the updated event
        view.refreshCalendarDisplay();

        view.createMessagePopup(
            "Event '" + result.getSubject() + "' updated successfully!",
            "Success",
            JOptionPane.INFORMATION_MESSAGE);
      } catch (Exception e) {
        view.createMessagePopup(
            "Error editing event: " + e.getMessage(),
            "Error",
            JOptionPane.ERROR_MESSAGE);
      }
    }

    return result;
  }

  @Override
  public void handleSwitchCalendarRequest(String calendarName) {
    try {
      model.setActiveCalendar(calendarName);
    } catch (IllegalArgumentException e) {
      view.createMessagePopup(
          "Error switching calendar: " + e.getMessage(),
          "Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }

}
