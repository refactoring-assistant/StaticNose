package shotgun.case2good;

interface ISensor {
  double takeNewReading();
  double lastReading();
}

interface IDiscreteSensor extends ISensor {
  boolean status();
  void flipStatus();
  void setStatus(boolean value);

  @Override
  default double lastReading() {
    return 0;
  }

  @Override
  default double takeNewReading() {
    return 0;
  }
}

abstract class Sensor implements ISensor {
  protected double currentValue;
  protected double lastValue;
  protected final Battery battery;

  public Sensor(double value, Battery battery) {
    this.currentValue = this.lastValue = value;
    this.battery      = battery;
  }

  public Sensor(double value) {
    this(value, new Battery(1.0));
  }

  public Sensor() {
    this(0);
  }

  @Override
  public double lastReading() {
    return lastValue;
  }

  public double getBatteryLevel() {
    return battery.getLevel();
  }
}

class Battery {
  private double level;
  private final double minLevel;
  private final double drainPerUse;

  public Battery(double initialLevel, double minLevel, double drainPerUse) {
    if (initialLevel < 0 || initialLevel > 1.0)
      throw new IllegalArgumentException("initialLevel must be 0.0–1.0");
    this.level       = initialLevel;
    this.minLevel    = minLevel;
    this.drainPerUse = drainPerUse;
  }

  public Battery(double initialLevel) {
    this(initialLevel, 0.1, 0.05);
  }

  public void check() {
    if (level < minLevel) {
      throw new IllegalStateException(
        String.format("Battery too low (%.2f < %.2f)", level, minLevel)
      );
    }
  }

  public void use() {
    level = Math.max(0.0, level - drainPerUse);
  }

  public double getLevel() {
    return level;
  }
}

class AtmosphericSensor extends Sensor {
  public AtmosphericSensor(double value, Battery battery) {
    super(value, battery);
  }

  public AtmosphericSensor(double value) {
    super(value);
  }

  public AtmosphericSensor() {
    super();
  }

  @Override
  public double takeNewReading() {
    battery.check();
    lastValue     = currentValue;
    currentValue  = SensorData.currentReading();
    battery.use();
    return currentValue;
  }
}

class WaterSensor extends Sensor implements IDiscreteSensor {
  private boolean flooding;

  public WaterSensor(double value, Battery battery) {
    super(value, battery);
    this.flooding = value > 0.5;
  }

  public WaterSensor(double value) {
    super(value);
  }

  public WaterSensor() {
    super();
  }

  @Override
  public double takeNewReading() {
    battery.check();
    lastValue     = currentValue;
    currentValue  = SensorData.currentReading();
    flooding      = (currentValue > 0.5);
    battery.use();
    return currentValue;
  }

  @Override
  public boolean status() {
    return flooding;
  }

  @Override
  public void flipStatus() {
    flooding = !flooding;
  }

  @Override
  public void setStatus(boolean value) {
    flooding = value;
  }
}

class SmokeSensor extends Sensor implements IDiscreteSensor {
  private boolean smokeDetected;
  private static final double SMOKE_THRESHOLD = 0.7;

  public SmokeSensor(double value, Battery battery) {
    super(value, battery);
    this.smokeDetected = value > SMOKE_THRESHOLD;
  }

  public SmokeSensor(double value) {
    super(value);
  }

  public SmokeSensor() {
    super();
  }

  @Override
  public double takeNewReading() {
    battery.check();
    lastValue        = currentValue;
    currentValue     = SensorData.currentReading();
    smokeDetected    = (currentValue > SMOKE_THRESHOLD);
    battery.use();
    return currentValue;
  }

  @Override
  public boolean status() {
    return smokeDetected;
  }

  @Override
  public void flipStatus() {
    smokeDetected = !smokeDetected;
  }

  @Override
  public void setStatus(boolean value) {
    smokeDetected = value;
  }
}


class SensorData {
  private static double[] readings = {
    0.1, 0.4, 0.0, 0.51, 0.5, 0.7, 0.0, 2.2, 1.0
  };
  private static int counter = 0;

  public static double currentReading() {
    int idx = counter++;
    if (counter >= readings.length) counter = 0;
    return readings[idx];
  }

  public static void reset() {
    counter = 0;
  }
}
