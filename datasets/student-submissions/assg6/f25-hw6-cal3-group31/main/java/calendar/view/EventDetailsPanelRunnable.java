package calendar.view;

import calendar.controller.IntViewRequestHandler;
import javax.swing.JOptionPane;

/**
 * Runnable for handling event editing in the event details panel.
 */
public class EventDetailsPanelRunnable implements Runnable {
  private final ViewEvent event;
  private final IntGuiView view;

  /**
   * Constructor for EventDetailsPanelRunnable.
   *
   * @param event the event to edit
   * @param view  the GUI view
   */
  public EventDetailsPanelRunnable(ViewEvent event, IntGuiView view) {
    this.event = event;
    this.view = view;
  }

  @Override
  public void run() {
    // Get the request handler from the view
    IntViewRequestHandler requestHandler = view.getRequestHandler();

    if (requestHandler != null) {
      // Delegate to the controller to handle the edit request
      requestHandler.handleEditEventRequest(event);
    } else {
      view.createMessagePopup(
          "Error: Request handler not available",
          "Error",
          JOptionPane.ERROR_MESSAGE);
    }
  }
}
