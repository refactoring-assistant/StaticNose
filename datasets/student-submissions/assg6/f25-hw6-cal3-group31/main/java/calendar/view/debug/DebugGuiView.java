package calendar.view.debug;

import calendar.view.GuiView;

/**
 * A debug implementation of the GuiView which captures message popups.
 */
public class DebugGuiView extends GuiView {
  public StringBuilder sb = new StringBuilder();

  @Override
  public void createMessagePopup(Object message, String title, int messageType) {
    sb.append(message + System.lineSeparator());
  }
}
