package parallelhierarchy.case2good;

import java.util.Objects;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

enum OperatingSystem {
  IOS, ANDROID
}

class PlatformDetector {
  public static OperatingSystem detectPlatform() {
    String os = System.getProperty("os.name", "").toLowerCase();
    if (os.contains("mac") || os.contains("ios")) {
      return OperatingSystem.IOS;
    } else {
      return OperatingSystem.ANDROID;
    }
  }

  public static UIFactory getFactory() {
    return new PlatformUIFactory();
  }
}

abstract class AbstractAlertButton {
  abstract void render();
}

class PlatformAlertButton extends AbstractAlertButton {
  private final OperatingSystem operatingSystem;

  private static final Map<OperatingSystem, Runnable> RENDERERS = new EnumMap<>(OperatingSystem.class);
  static {
    RENDERERS.put(OperatingSystem.IOS, () -> System.out.println("🐦 Rendering iOS‑style button"));
    RENDERERS.put(OperatingSystem.ANDROID, () -> System.out.println("🤖 Rendering Android‑style button"));
  }

  public PlatformAlertButton() {
    this(PlatformDetector.detectPlatform());
  }

  public PlatformAlertButton(OperatingSystem operatingSystem) {
    this.operatingSystem = Objects.requireNonNull(operatingSystem);
  }

  @Override
  void render() {
    Runnable renderer = RENDERERS.get(operatingSystem);
    if (renderer == null) {
      throw new IllegalStateException("Unknown operatingSystem: " + operatingSystem);
    }
    renderer.run();
  }
}

abstract class AbstractAlertDialog {
  abstract void show(String message);
}

class PlatformAlertDialog extends AbstractAlertDialog {
  private final OperatingSystem operatingSystem;

  private static final Map<OperatingSystem, Consumer<String>> DISPLAYERS = new EnumMap<>(OperatingSystem.class);
  static {
    DISPLAYERS.put(OperatingSystem.IOS, message -> System.out.println("🐦 iOS Alert: " + message));
    DISPLAYERS.put(OperatingSystem.ANDROID, message -> System.out.println("🤖 Android Alert: " + message));
  }

  public PlatformAlertDialog() {
    this(PlatformDetector.detectPlatform());
  }

  public PlatformAlertDialog(OperatingSystem operatingSystem) {
    this.operatingSystem = Objects.requireNonNull(operatingSystem);
  }

  @Override
  void show(String message) {
    Consumer<String> displayer = DISPLAYERS.get(operatingSystem);
    if (displayer == null) {
      throw new IllegalStateException("Unknown operatingSystem: " + operatingSystem);
    }
    displayer.accept(message);
  }
}

interface UIFactory {
  AbstractAlertButton createButton();
  AbstractAlertDialog createAlertDialog();
}

class PlatformUIFactory implements UIFactory {
  private final OperatingSystem operatingSystem;

  public PlatformUIFactory() {
    this(PlatformDetector.detectPlatform());
  }

  public PlatformUIFactory(OperatingSystem operatingSystem) {
    this.operatingSystem = Objects.requireNonNull(operatingSystem);
  }

  @Override
  public AbstractAlertButton createButton() {
    return new PlatformAlertButton(operatingSystem);
  }

  @Override
  public AbstractAlertDialog createAlertDialog() {
    return new PlatformAlertDialog(operatingSystem);
  }
}