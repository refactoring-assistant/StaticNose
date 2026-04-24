package calendar.command.export;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A factory that resolves an {@link Export} implementation from a file name.
 */
public final class ExportRegistry {

  private static final List<Function<String, Export>> FACTORIES = new ArrayList<>();

  static {
    register(ExportCsv::new);
    register(ExportIcal::new);
  }

  private ExportRegistry() {
  }

  /**
   * Returns an {@link Export} that supports the given file name, otherwise if no registered
   * exporter supports it, a placeholder exporter that prints an error is returned.
   *
   * @param fileName the output file name
   * @return a supporting exporter, otherwise {@code UnsupportedExport}
   */
  public static Export resolve(String fileName) {
    for (Function<String, Export> factory : FACTORIES) {
      Export candidate = factory.apply(fileName);
      if (candidate.supports(fileName)) {
        return candidate;
      }
    }
    return new AbstractExportCommand.UnsupportedExport(fileName);
  }

  /**
   * Registers a factory capable of constructing an {@link Export} for a given filename.
   *
   * @param factory a function that accepts a filename and returns an exporter
   */
  public static void register(Function<String, Export> factory) {
    FACTORIES.add(factory);
  }
}
