package calendar.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * Displays a single event inside the IntCalendarDisplay.
 */
public class CalendarDisplayEventPanel extends JPanel {
  private final JLabel eventLabel;
  private final ViewEvent event;
  private final Runnable onClickCallback;

  /**
   * CalendarDisplayEventPanel constructor.
   *
   * @param labelText the text that will be shown in the label of this panel
   * @param event the ViewEvent associated with this panel
   * @param onClickCallback callback to invoke when the event is clicked
   */
  public CalendarDisplayEventPanel(String labelText, ViewEvent event, Runnable onClickCallback) {
    this.event = event;
    this.onClickCallback = onClickCallback;

    this.setBorder(new EmptyBorder(2, 10, 2, 10));
    this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

    eventLabel = new JLabel(labelText);
    eventLabel.setBorder(new EmptyBorder(2, 5, 2, 5));
    eventLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
    eventLabel.setOpaque(true);
    eventLabel.setBackground(Color.BLACK);
    eventLabel.setForeground(Color.WHITE);
    Dimension preferred = eventLabel.getPreferredSize();
    eventLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE,
        preferred.height));
    eventLabel.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseEntered(MouseEvent e) {
        eventLabel.setBackground(Color.DARK_GRAY);
      }

      @Override
      public void mouseExited(MouseEvent e) {
        eventLabel.setBackground(Color.BLACK);
      }

      @Override
      public void mousePressed(MouseEvent e) {
        if (onClickCallback != null) {
          onClickCallback.run();
        }
      }
    });


    this.add(eventLabel);
  }
}
