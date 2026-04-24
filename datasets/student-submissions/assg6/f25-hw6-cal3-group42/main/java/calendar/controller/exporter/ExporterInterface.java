package calendar.controller.exporter;

import calendar.model.event.CalendarEvent;
import java.util.List;

/**
 * Interface that represents a calendar exporter which can export a list of event objects
 * into the correctly formatted file type.
 */
public interface ExporterInterface {

  /**
   * The method call to start the exporting functionality.
   *
   * @param eventList a list of events to be saved in the export file
   *                  @throws RuntimeException for file operation error
   */
  Boolean exportHelper(List<CalendarEvent> eventList) throws RuntimeException;
}