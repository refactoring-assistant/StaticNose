package calendar.view.gui;

import java.awt.Font;

/**
 * Shared font definitions for the Swing UI.
 * Uses only logical JDK fonts so it works consistently across platforms.
 */
public final class UiFonts {

  /**
   * Large title font for the month caption or main heading.
   */
  public static final Font TITLE =
      new Font("Comfortaa", Font.BOLD, 20);

  /**
   * Heading font used for section titles and important labels.
   */
  public static final Font HEADING =
      new Font("Comfortaa", Font.BOLD, 16);

  /**
   * Subheading font for secondary titles or emphasized labels.
   */
  public static final Font SUBHEADING =
      new Font("DejaVu Sans", Font.PLAIN, 14);

  /**
   * Default body font used for most labels, inputs and buttons.
   */
  public static final Font BODY =
      new Font("DejaVu Sans", Font.PLAIN, 12);

  /**
   * Caption font for smaller, secondary text.
   */
  public static final Font CAPTION =
      new Font("DejaVu Sans", Font.PLAIN, 11);

  /**
   * Monospaced font for any future time strings or technical text.
   */
  public static final Font MONO =
      new Font("Monospaced", Font.PLAIN, 12);

  private UiFonts() {
    // Prevent instantiation.
  }
}
