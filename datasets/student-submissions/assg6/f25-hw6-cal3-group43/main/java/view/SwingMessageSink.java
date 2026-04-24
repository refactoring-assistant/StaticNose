package view;

import java.awt.Component;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import messaging.MessageLevel;
import messaging.MessageSink;

/**
 * Message sink that surfaces controller/model errors as Swing dialogs while still delegating
 * to an optional downstream sink (e.g., console logging).
 */
public final class SwingMessageSink implements MessageSink {

  private final Component parent;
  private final MessageSink delegate;

  /**
   * Creates a sink that will center dialogs on the given parent component.
   *
   * @param parent   dialog parent (may be {@code null})
   * @param delegate optional downstream sink to keep existing behavior
   */
  public SwingMessageSink(Component parent, MessageSink delegate) {
    this.parent = parent;
    this.delegate = delegate;
  }

  @Override
  public void accept(MessageLevel level, String message) {
    if (delegate != null) {
      delegate.accept(level, message);
    }
    if (level != MessageLevel.ERROR) {
      return;
    }
    String text = (message == null) ? "" : message;
    SwingUtilities.invokeLater(() ->
        JOptionPane.showMessageDialog(
            parent,
            text,
            "Error",
            JOptionPane.ERROR_MESSAGE));
  }
}
