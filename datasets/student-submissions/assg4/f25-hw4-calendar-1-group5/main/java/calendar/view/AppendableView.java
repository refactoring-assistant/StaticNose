package calendar.view;

import java.io.IOException;

/**
 * This class represents a View that renders calendar messages.
 */
public class AppendableView implements View {
  private final Appendable out;

  /**
   * Initialize the view with the target appendable.
   *
   * @param out An appendable where all messages are rendered.
   */
  public AppendableView(Appendable out) {
    if (out == null) {
      throw new IllegalArgumentException("Appendable is null");
    }

    this.out = out;
  }

  @Override
  public void render(String message) {
    try {
      out.append(message);
    } catch (IOException e) {
      throw new IllegalStateException(e.getMessage());
    }
  }
}
