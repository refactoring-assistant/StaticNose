package calendar.view.gui;

import java.awt.Color;
import java.awt.Font;

/**
 * Light UI theme that uses a palette and typography similar
 * in spirit to modern web-based calendar applications.
 */
public class LightTheme implements UiTheme {

  @Override
  public Color background() {
    return new Color(0xF5F5F5);
  }

  @Override
  public Color panelBackground() {
    return Color.WHITE;
  }

  @Override
  public Color fieldBackground() {
    return Color.WHITE;
  }

  @Override
  public Color primaryText() {
    return new Color(0x202124);
  }

  @Override
  public Color mutedText() {
    return new Color(0x5F6368);
  }

  @Override
  public Color accent() {
    return new Color(0x1A73E8);
  }

  @Override
  public Color border() {
    return new Color(0xDADCE0);
  }

  @Override
  public Color tileBackground() {
    return Color.WHITE;
  }

  @Override
  public Color tileHighlight() {
    return new Color(0xD2E3FC);
  }

  @Override
  public Color listBackground() {
    return Color.WHITE;
  }

  @Override
  public Color headerBackground() {
    return new Color(0xF1F3F4);
  }

  @Override
  public Font titleFont() {
    return new Font("SansSerif", Font.BOLD, 20);
  }

  @Override
  public Font headingFont() {
    return new Font("SansSerif", Font.BOLD, 16);
  }

  @Override
  public Font subheadingFont() {
    return new Font("SansSerif", Font.PLAIN, 14);
  }

  @Override
  public Font bodyFont() {
    return new Font("SansSerif", Font.PLAIN, 12);
  }

  @Override
  public Font captionFont() {
    return new Font("SansSerif", Font.PLAIN, 11);
  }

  @Override
  public Font monoFont() {
    return new Font("Monospaced", Font.PLAIN, 12);
  }
}
