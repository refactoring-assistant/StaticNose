package calendar.controller;

import calendar.model.CalendarModel;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;


/**
 * Handles the main program loop — reads user commands,
 * passes them to the parser + executor, and manages modes (interactive/headless).
 */
public class CalendarController {

  private final CalendarModel model;
  private final CommandParser parser;
  private final CommandExecutor executor;

  /**
   * Creates a new CalendarController.
   *
   * @param model Calendar Model
   */
  public CalendarController(CalendarModel model) {
    this.model = model;
    this.parser = new CommandParser();
    this.executor = new CommandExecutor(model);
  }

  /**
   * Runs the program in interactive mode.
   */
  public void runInteractive() {
    Scanner scanner = new Scanner(System.in);
    System.out.println("Interactive Calendar Mode. Type 'exit' to quit.");
    while (true) {
      System.out.print("> ");
      String input = scanner.nextLine().trim();
      if (input.equalsIgnoreCase("exit")) {
        break;
      }
      List<String> tokens = parser.parse(input);
      executor.execute(tokens);
    }
    scanner.close();
    System.out.println("Exiting Calendar.");
  }

  /**
   * Runs the program in headless mode from a commands file.
   *
   * @param filePath path to the commands file
   */
  public void runHeadless(String filePath) {
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty()) {
          continue;
        }
        System.out.println("> " + line);
        if (line.equalsIgnoreCase("exit")) {
          System.out.println("Exiting Calendar.");
          return;
        }
        List<String> tokens = parser.parse(line);
        executor.execute(tokens);
      }
      System.out.println("Error: File ended without 'exit' command.");
    } catch (IOException e) {
      System.out.println("Error reading file: " + e.getMessage());
    }
  }
}

