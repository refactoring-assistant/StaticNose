package calendar.view;

import calendar.model.Events;
import java.util.List;

/**
 * Class to create View component (the V of MVC pattern) to display business information.
 * - DOES NOT directly access the model
 * */
public class ViewConsole implements InteractiveView {

  /**
   * Public function to display all the provided events.
   *
   * @param events all the provided events
   * */
  @Override
  public void showEvents(List<Events> events) {
    if (events.isEmpty()) {
      System.out.println("No events");
      return;
    }

    System.out.println("---- Events ----");
    for (Events event : events) {
      System.out.println("Subject: " + event.getSubject());
      System.out.println(" Start: " + event.getStartTime());
      System.out.println(" End: " + event.getEndTime());

      if (event.isAllDay()) {
        System.out.println("Type: All-day event");
      }
      if (event.getDescription() != null) {
        System.out.println(event.getDescription());
      }
      if (event.getLocation() != null) {
        System.out.println(" at " + event.getLocation());
      }
      System.out.println(" Status: " + event.getStatus());
      if (event.getIdSeries() != null) {
        System.out.println(" Part of recurring series");
      }
      System.out.println("--------------");
    }
    System.out.println("Total number of events: " + events.size());
  }

  /**
   * Public function to show the encountered error.
   *
   * @param error the error obtained
   * */
  @Override
  public void dispError(String error) {
    System.out.println("ERROR: " + error);
  }

  /**
   * Public function to display the event status on UI.
   *
   * @param status the event's status
   * */
  @Override
  public void showStatus(String status) {
    System.out.println("STATUS: " + status);
  }

  /**
   * Public function to display success message.
   *
   * @param msg the success message
   * */
  @Override
  public void dispSuccess(String msg) {
    System.out.println("SUCCESS: " + msg);
  }

  /**
   * Public function to signal end of execution.
   * */
  public void dispEnd() {
    System.out.println("FINISHED execution. Have a Good Day!");
  }
}
