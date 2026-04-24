package calendar.view.gui;

import java.awt.Color;

/**
 * Centralized UI color palette used by the Swing views.
 * This version uses a light theme similar in spirit to modern calendar apps.
 */
public final class UiColors {

  private UiColors() {
    // Prevent instantiation.
  }

  /** Main application background color. */
  public static final Color BACKGROUND = new Color(0xF5F5F5);

  /** Background color for panels and containers. */
  public static final Color PANEL_BACKGROUND = new Color(0xFFFFFF);

  /** Background color for text fields and other input components. */
  public static final Color FIELD_BACKGROUND = new Color(0xFFFFFF);

  /** Primary text color. */
  public static final Color PRIMARY_TEXT = new Color(0x202124);

  /** Muted or secondary text color. */
  public static final Color MUTED_TEXT = new Color(0x5F6368);

  /** Accent color used for primary buttons and highlights. */
  public static final Color ACCENT = new Color(0x1A73E8);

  /** Border color for components. */
  public static final Color BORDER = new Color(0xDADCE0);

  /** Background color for calendar tiles / day buttons. */
  public static final Color TILE_BACKGROUND = new Color(0xFFFFFF);

  /** Highlight color for days that contain events. */
  public static final Color TILE_HIGHLIGHT = new Color(0x8BF8FD);

  /** Background color for the events list on the right side. */
  public static final Color LIST_BACKGROUND = new Color(0xFFFFFF);

  /** Background color for header rows such as weekday labels. */
  public static final Color HEADER_BACKGROUND = new Color(0xF1F3F4);
}
