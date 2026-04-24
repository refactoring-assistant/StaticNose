package calendar.controller.commands;

import calendar.controller.Command;
import calendar.model.Event;
import calendar.model.Model;
import calendar.util.PathObj;
import calendar.view.View;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * This class represents the command used to export all events to csv file.
 */
public class ExportCommand implements Command {
  private final String filePath;

  /**
   * This constructor initializes the command with the target file location.
   *
   * @param filePath The path to a file that should contain the exported events in String format.
   */
  public ExportCommand(String filePath) {
    this.filePath = Objects.requireNonNull(filePath);
  }

  @Override
  public void execute(Model model, View view) {
    StringBuilder builder = new StringBuilder();

    builder.append("Subject,Description,Start Date,Start Time,End Date,End Time")
        .append(System.lineSeparator());

    DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("hh:mm a");

    for (Event e : model.allEvents()) {
      builder.append(
              String.format("%s,%s,%s,%s,%s,%s", escape(e.subject()), escape(e.description()),
                  e.startsAt().format(dateFmt), e.startsAt().format(timeFmt),
                  e.endsAt().format(dateFmt), e.endsAt().format(timeFmt)))
          .append(System.lineSeparator());
    }

    writeToFile(builder.toString());
  }

  /**
   * This method creates and returns a buffer writer that writes to a file at the given path.
   *
   * <p>This helper method's access has been relaxed to protected in order to help mock a buffered
   * writer during testing.
   *
   * @param filePath String path to the target file.
   * @return A buffered writer that writes to the target file.
   * @throws IOException If it wasn't possible to create a writer to the requested file location.
   */
  protected BufferedWriter newWriter(String filePath) throws IOException {
    return Files.newBufferedWriter(PathObj.getPathObj(filePath));
  }

  private String escape(String value) {
    if (value == null) {
      return "";
    }

    if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
      value = value.replace("\"", "\"\"");
      return "\"" + value + "\"";
    }
    return value;
  }

  /**
   * This method writes the textual content to the file.
   *
   * @param content The textual content in String format.
   */
  protected void writeToFile(String content) {
    try (BufferedWriter writer = newWriter(filePath)) {
      writer.write(content);
    } catch (IOException e) {
      throw new IllegalStateException(e.getMessage());
    }
  }
}
