package calendar.view;

import calendar.controller.CommandException;
import calendar.controller.CommandProcessor;
import calendar.controller.CommandResult;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * CLI view responsible for interactive and headless modes.
 */
public class CommandLineView {

  private final CommandProcessor processor;

  /**
   * Creates a view that delegates user commands to the supplied processor.
   *
   * @param processor controller component to execute commands
   */
  public CommandLineView(CommandProcessor processor) {
    this.processor = processor;
  }

  /**
   * Runs the interactive REPL loop using standard input and output.
   */
  public void runInteractive() {
    try (Scanner scanner = new Scanner(System.in);
         PrintWriter out = new PrintWriter(System.out, true)) {
      boolean exit = false;
      while (!exit) {
        if (!scanner.hasNextLine()) {
          out.println("Error: Input ended before receiving an exit command.");
          break;
        }
        String line = scanner.nextLine().trim();
        if (line.isEmpty()) {
          continue;
        }
        try {
          CommandResult result = processor.process(line);
          if (!result.getMessage().isEmpty()) {
            out.println(result.getMessage());
          }
          if (result.shouldExit()) {
            exit = true;
          }
        } catch (CommandException e) {
          out.println("Error: " + e.getMessage());
        }
      }
    }
  }

  /**
   * Executes commands from a file in headless mode.
   *
   * @param filePath path to the command file
   */
  public void runHeadless(String filePath) {
    Path path = Path.of(filePath);
    boolean exitEncountered = false;
    try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
         PrintWriter out = new PrintWriter(System.out, true)) {
      String line;
      while ((line = reader.readLine()) != null) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) {
          continue;
        }
        try {
          CommandResult result = processor.process(trimmed);
          if (!result.getMessage().isEmpty()) {
            out.println(result.getMessage());
          }
          if (result.shouldExit()) {
            exitEncountered = true;
            break;
          }
        } catch (CommandException e) {
          out.println("Error: " + e.getMessage());
        }
      }
      if (!exitEncountered) {
        out.println("Error: Command file ended without an exit command.");
      }
    } catch (IOException e) {
      System.err.println("Failed to read command file: " + e.getMessage());
    }
  }
}
