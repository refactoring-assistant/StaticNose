package calendar.controller;

import calendar.controller.parser.CreateCommandParser;
import calendar.controller.parser.EditCommandParser;
import calendar.controller.parser.ExportCommandParser;
import calendar.controller.parser.PrintCommandParser;
import calendar.controller.parser.ShowStatusCommandParser;
import calendar.model.Model;
import calendar.view.View;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

/**
 * This class represents the controller of the calendar application.
 */
public class ControllerV1 implements Controller {
  private final Readable reader;
  private final Map<String, Parser> parsers;

  /**
   * Initialize the controller using a specific input source.
   *
   * @param reader A readable object from where commands should be read.
   */
  public ControllerV1(Readable reader) {
    this.reader = Objects.requireNonNull(reader);

    this.parsers = new HashMap<>();
    parsers.put("create", new CreateCommandParser());
    parsers.put("print", new PrintCommandParser());
    parsers.put("export", new ExportCommandParser());
    parsers.put("edit", new EditCommandParser());
    parsers.put("show", new ShowStatusCommandParser());
  }

  /**
   * A method to start the controller.
   *
   * @param model The model controller will use for the Model layer.
   * @param view  The view controller will use for the View layer.
   */
  public void go(Model model, View view) {
    Objects.requireNonNull(model);

    Scanner scanner = new Scanner(this.reader);
    while (true) {
      String input = scanner.next();

      if (input.equals("exit")) {
        break;
      }

      Parser parser = parsers.get(input);
      if (parser == null) {
        view.render("Unknown command" + System.lineSeparator());
        scanner.nextLine();
        continue;
      }

      Command command = parser.parse(scanner);
      if (command == null) {
        view.render("Unknown command" + System.lineSeparator());
        continue;
      }

      try {
        command.execute(model, view);
      } catch (IllegalArgumentException e) {
        view.render(e.getMessage() + System.lineSeparator());
      }
    }
  }
}
