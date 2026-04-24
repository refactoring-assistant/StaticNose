package calendar.view.gui;

import java.awt.Color;
import java.awt.Font;

/**
 * Describes a UI theme that provides shared colors and fonts
 * for all Swing-based calendar views.
 */
public interface UiTheme {

  /**
   * Returns the main application background color.
   *
   * @return background color
   */
  Color background();

  /**
   * Returns the background color for panels and containers.
   *
   * @return panel background color
   */
  Color panelBackground();

  /**
   * Returns the background color for text fields and input components.
   *
   * @return field background color
   */
  Color fieldBackground();

  /**
   * Returns the primary text color used for main content.
   *
   * @return primary text color
   */
  Color primaryText();

  /**
   * Returns the muted text color used for secondary information.
   *
   * @return muted text color
   */
  Color mutedText();

  /**
   * Returns the accent color used for primary buttons and highlights.
   *
   * @return accent color
   */
  Color accent();

  /**
   * Returns the border color used around components.
   *
   * @return border color
   */
  Color border();

  /**
   * Returns the background color for calendar tiles or day buttons.
   *
   * @return tile background color
   */
  Color tileBackground();

  /**
   * Returns the highlight color for tiles that contain events.
   *
   * @return tile highlight color
   */
  Color tileHighlight();

  /**
   * Returns the background color for the events list area.
   *
   * @return list background color
   */
  Color listBackground();

  /**
   * Returns the background color for header rows such as weekday labels.
   *
   * @return header background color
   */
  Color headerBackground();

  /**
   * Returns the large title font for the month caption or main heading.
   *
   * @return title font
   */
  Font titleFont();

  /**
   * Returns the heading font for section titles and important labels.
   *
   * @return heading font
   */
  Font headingFont();

  /**
   * Returns the subheading font for secondary titles or emphasized labels.
   *
   * @return subheading font
   */
  Font subheadingFont();

  /**
   * Returns the default body font used for labels, inputs and buttons.
   *
   * @return body font
   */
  Font bodyFont();

  /**
   * Returns the caption font used for small secondary text.
   *
   * @return caption font
   */
  Font captionFont();

  /**
   * Returns the monospaced font used for time strings or technical text.
   *
   * @return monospaced font
   */
  Font monoFont();
}
