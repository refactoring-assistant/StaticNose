package calendar.controller.text;

import calendar.controller.text.commandfactory.CommandInterface;
import calendar.controller.text.commandfactory.CopyEvent;
import calendar.controller.text.commandfactory.CopyEventsBetween;
import calendar.controller.text.commandfactory.CopyEventsOn;
import calendar.controller.text.commandfactory.CreateAllDayEvent;
import calendar.controller.text.commandfactory.CreateAllDayEventSeriesFor;
import calendar.controller.text.commandfactory.CreateAllDayEventSeriesUntil;
import calendar.controller.text.commandfactory.CreateCalendar;
import calendar.controller.text.commandfactory.CreateEventSeriesFor;
import calendar.controller.text.commandfactory.CreateEventSeriesUntil;
import calendar.controller.text.commandfactory.CreateSingleEvent;
import calendar.controller.text.commandfactory.EasterEgg;
import calendar.controller.text.commandfactory.EditCalendar;
import calendar.controller.text.commandfactory.EditEvents;
import calendar.controller.text.commandfactory.EditSeries;
import calendar.controller.text.commandfactory.EditSingleEvent;
import calendar.controller.text.commandfactory.ExportCalendar;
import calendar.controller.text.commandfactory.Node;
import calendar.controller.text.commandfactory.NodeInterface;
import calendar.controller.text.commandfactory.PrintEventsFrom;
import calendar.controller.text.commandfactory.PrintEventsOn;
import calendar.controller.text.commandfactory.ShowStatus;
import calendar.controller.text.commandfactory.UseCalendar;
import calendar.model.CalendarManagerInterface;
import calendar.view.CalendarViewInterface;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that parses a given command string, follows the correct code path to call the correct
 * model method for the parsed command.
 * A command tree is created to lay out the possible command paths that the user is allowed to
 * navigate through. An invalid command item or wrong sequence would trigger error logging
 * by the view. For a valid command, the parser will send back the command method that the given
 * command string represents to the controller.
 */
public class CommandParser {

  private final NodeInterface rootNode;
  private final Map<String, Object> parameters = new HashMap<>();
  private ZoneId zoneId;

  /**
   * Constructor that creates the command tree and sets the root node.
   */
  public CommandParser() {
    this.rootNode = new Node("root", false);

    createCommandTree();
    editCommandTree();
    printCommandTree();
    copyCommandTree();
    miscellaneousCommandTree();
  }

  /**
   * Helper method to add the given command snippet to the given node.
   * The command items will have a string that represents the command
   * and whether it is a placeholder or not.
   *
   * @param currentNode  The given node to which the next command snippet is to be added to
   * @param commandItems A list of command and if it is a placeholder
   * @return The new node created by the given command
   */
  private NodeInterface buildCommandTree(NodeInterface currentNode, List<Object[]> commandItems,
                                         String query) {
    for (Object[] pair : commandItems) {
      String key = (String) pair[0];
      Boolean value = (Boolean) pair[1];
      currentNode = currentNode.addNode(key, value);
      currentNode.setQuery(query);
    }
    return currentNode;
  }

  /**
   * Creates the command tree for the 'create' calendar commands.
   */
  private void createCommandTree() {

    final List<Object[]> createCalendar =
        Arrays.asList(new Object[][] {{"calendar", false}, {"--name", false},
            {"calname", true}, {"--timezone", false}, {"timezone", true}});

    final List<Object[]> createSingleEvent =
        Arrays.asList(new Object[][] {{"event", false}, {"subject", true}});

    final List<Object[]> createSingleEventFrom =
        Arrays.asList(
            new Object[][] {{"from", false}, {"start", true}, {"to", false}, {"end", true}});

    final List<Object[]> createEventSeries =
        Arrays.asList(new Object[][] {{"repeats", false}, {"weekdays", true}});

    final List<Object[]> createEventSeriesFor =
        Arrays.asList(new Object[][] {{"for", false}, {"ndays", true}, {"times", false}});

    final List<Object[]> createEventSeriesUntil =
        Arrays.asList(new Object[][] {{"until", false}, {"untilDate", true}});

    final List<Object[]> createAllDayEvent =
        Arrays.asList(new Object[][] {{"on", false}, {"onDate", true}});

    String createQuery = "create calendar --name <calendar name> --timezone <timezone>";

    NodeInterface crCreateNode = this.rootNode.addNode("create", false);
    crCreateNode.setQuery("create (calendar | event)...");
    NodeInterface currentNode = buildCommandTree(crCreateNode, createCalendar, createQuery);
    currentNode.setCommand(CreateCalendar::new);

    createQuery = "create event <event name> from <start date time> to <end date time>";
    currentNode = crCreateNode;
    currentNode = buildCommandTree(currentNode, createSingleEvent, createQuery);
    final NodeInterface crSubjectNode = currentNode;

    currentNode = buildCommandTree(currentNode, createSingleEventFrom, createQuery);
    currentNode.setCommand(CreateSingleEvent::new);

    createQuery = "create event <event name> from <start date time> to "
        + "<end date time> repeats <weekdays> for <n> times";
    currentNode = buildCommandTree(currentNode, createEventSeries, createQuery);
    final NodeInterface crWeekdaysNode = currentNode;

    currentNode = buildCommandTree(currentNode, createEventSeriesFor, createQuery);
    currentNode.setCommand(CreateEventSeriesFor::new);

    createQuery = "create event <event name> from <start date time> to "
        + "<end date time> repeats <weekdays> until <date>";
    currentNode = crWeekdaysNode;
    currentNode = buildCommandTree(currentNode, createEventSeriesUntil, createQuery);
    currentNode.setCommand(CreateEventSeriesUntil::new);

    createQuery = "create event <event name> on <date>";
    currentNode = crSubjectNode;
    currentNode = buildCommandTree(currentNode, createAllDayEvent, createQuery);
    currentNode.setCommand(CreateAllDayEvent::new);

    createQuery = "create event <event name> on <date> repeats <weekdays> "
        + "(for <n> times | until <date>)";
    currentNode = buildCommandTree(currentNode, createEventSeries, createQuery);
    final NodeInterface crOnWeekdaysNode = currentNode;

    createQuery = "create event <event name> on <date> repeats <weekdays> for <n> times";
    currentNode = buildCommandTree(currentNode, createEventSeriesFor, createQuery);
    currentNode.setCommand(CreateAllDayEventSeriesFor::new);

    createQuery = "create event <event name> on <date> repeats <weekdays> until <date>";
    currentNode = crOnWeekdaysNode;
    currentNode = buildCommandTree(currentNode, createEventSeriesUntil, createQuery);
    currentNode.setCommand(CreateAllDayEventSeriesUntil::new);
  }

  /**
   * Creates the command tree for the 'edit' calendar commands.
   */
  private void editCommandTree() {

    final List<Object[]> editCalendar =
        Arrays.asList(new Object[][] {{"calendar", false}, {"--name", false}, {"calname", true},
            {"--property", false}, {"property", true}, {"value", true}});

    final List<Object[]> editEventProperty =
        Arrays.asList(new Object[][] {{"property", true}, {"subject", true}, {"from", false},
            {"start", true}});

    final List<Object[]> editEventPropertyTo =
        Arrays.asList(new Object[][] {{"to", false}, {"end", true}});

    final List<Object[]> editEventPropertyWith =
        Arrays.asList(new Object[][] {{"with", false}, {"value", true}});

    String editQuery = "edit calendar --name <calendar name> --property <property> <value>";
    NodeInterface edEditNode = this.rootNode.addNode("edit", false);
    edEditNode.setQuery("edit (calendar | event | events | series)...");
    NodeInterface currentNode = buildCommandTree(edEditNode, editCalendar, editQuery);
    currentNode.setCommand(EditCalendar::new);

    editQuery = "edit event <property> <event name> from <start date time> to "
        + "<end date time> with <value>";
    currentNode = edEditNode;
    currentNode = currentNode.addNode("event", false);
    currentNode = buildCommandTree(currentNode, editEventProperty, editQuery);
    currentNode = buildCommandTree(currentNode, editEventPropertyTo, editQuery);
    currentNode = buildCommandTree(currentNode, editEventPropertyWith, editQuery);
    currentNode.setCommand(EditSingleEvent::new);

    editQuery = "edit events <property> <event name> from <start date time> with <value>";
    currentNode = edEditNode;
    currentNode = currentNode.addNode("events", false);
    currentNode = buildCommandTree(currentNode, editEventProperty, editQuery);
    currentNode = buildCommandTree(currentNode, editEventPropertyWith, editQuery);
    currentNode.setCommand(EditEvents::new);

    editQuery = "edit series <property> <event name> from <start date time> with <value>";
    currentNode = edEditNode;
    currentNode = currentNode.addNode("series", false);
    currentNode = buildCommandTree(currentNode, editEventProperty, editQuery);
    currentNode = buildCommandTree(currentNode, editEventPropertyWith, editQuery);
    currentNode.setCommand(EditSeries::new);
  }

  /**
   * Creates the command tree for the 'print' calendar commands.
   */
  private void printCommandTree() {

    final List<Object[]> printEvents =
        Arrays.asList(new Object[][] {{"print", false}, {"events", false}});

    final List<Object[]> printEventsOn =
        Arrays.asList(new Object[][] {{"on", false}, {"date", true}});

    final List<Object[]> printEventsFrom =
        Arrays.asList(
            new Object[][] {{"from", false}, {"start", true}, {"to", false}, {"end", true}});

    String printQuery = "print events on <date>";
    NodeInterface currentNode = buildCommandTree(this.rootNode, printEvents, printQuery);
    final NodeInterface prEventsNode = currentNode;

    currentNode = buildCommandTree(currentNode, printEventsOn, printQuery);
    currentNode.setCommand(PrintEventsOn::new);

    printQuery = "print events from <start date time> to <end date time>";
    currentNode = prEventsNode;
    currentNode = buildCommandTree(currentNode, printEventsFrom, printQuery);
    currentNode.setCommand(PrintEventsFrom::new);
  }

  /**
   * Creates the command tree for copy commands.
   */
  private void copyCommandTree() {

    final List<Object[]> copyEvent =
        Arrays.asList(new Object[][] {{"event", false}, {"subject", true}, {"on", false},
            {"start", true}, {"--target", false}, {"target", true}, {"to", false},
            {"targetstart", true}});
    final List<Object[]> copyEventsOn =
        Arrays.asList(new Object[][] {{"on", false}, {"sourcedate", true},
            {"--target", false}, {"target", true}, {"to", false}, {"targetdate", true}});
    final List<Object[]> copyEventsBetween =
        Arrays.asList(new Object[][] {{"between", false}, {"startdate", true},
            {"and", false}, {"enddate", true}, {"--target", false}, {"target", true},
            {"to", false}, {"targetdate", true}});

    String copyQuery = "copy event <event name> on <start date time> "
        + "--target <target calendar> to <target date time>";
    NodeInterface cpCopyNode = this.rootNode.addNode("copy", false);
    cpCopyNode.setQuery("copy (event | events)...");
    NodeInterface currentNode = buildCommandTree(cpCopyNode, copyEvent, copyQuery);
    currentNode.setCommand(CopyEvent::new);

    copyQuery = "copy events <event name> on <start date> --target "
        + "<target calendar> to <target date>";
    currentNode = cpCopyNode;
    currentNode = currentNode.addNode("events", false);
    final NodeInterface cpEventsNode = currentNode;
    currentNode = buildCommandTree(currentNode, copyEventsOn, copyQuery);
    currentNode.setCommand(CopyEventsOn::new);

    copyQuery = "copy events between <start date> and <end date> --target "
        + "<target calendar> to <target date>";
    currentNode = cpEventsNode;
    currentNode = buildCommandTree(currentNode, copyEventsBetween, copyQuery);
    currentNode.setCommand(CopyEventsBetween::new);
  }

  /**
   * Creates the command tree for the other calendar commands.
   */
  private void miscellaneousCommandTree() {
    String miscQuery = "export cal <filename>";
    NodeInterface miExportNode = this.rootNode.addNode("export", false);
    miExportNode.setQuery(miscQuery);
    miExportNode = miExportNode.addNode("cal", false);
    miExportNode = miExportNode.addNode("filename", true);
    miExportNode.setCommand(ExportCalendar::new);

    miscQuery = "show status on <start date time>";
    NodeInterface miShowNode = this.rootNode.addNode("show", false);
    miShowNode.setQuery(miscQuery);
    miShowNode = miShowNode.addNode("status", false);
    miShowNode.setQuery(miscQuery);
    miShowNode.setOptional(true);
    miShowNode = miShowNode.addNode("on", false);
    miShowNode = miShowNode.addNode("start", true);
    miShowNode.setCommand(ShowStatus::new);

    NodeInterface miLocation = this.rootNode.addNode("location", false);
    miLocation.setOptional(true);
    miLocation = miLocation.addNode("location", true);
    miLocation.setOptional(true);

    NodeInterface miDescription = this.rootNode.addNode("description", false);
    miDescription.setOptional(true);
    miDescription = miDescription.addNode("description", true);
    miDescription.setOptional(true);

    NodeInterface miStatus = this.rootNode.addNode("status", false);
    miStatus.setOptional(true);
    miStatus = miStatus.addNode("status", true);
    miStatus.setOptional(true);

    miscQuery = "use calendar --name <calendar name>";
    NodeInterface miUse = this.rootNode.addNode("use", false);
    miUse.setQuery(miscQuery);
    miUse = miUse.addNode("calendar", false);
    miUse.setQuery(miscQuery);
    miUse = miUse.addNode("--name", false);
    miUse = miUse.addNode("calname", true);
    miUse.setCommand(UseCalendar::new);

    NodeInterface miEasterEgg = this.rootNode.addNode("happy", false);
    miEasterEgg.setOptional(true);
    miEasterEgg.setCommand(EasterEgg::new);
    miEasterEgg = this.rootNode.addNode("halloween", false);
    miEasterEgg.setOptional(false);
  }

  /**
   * Parses the given input command by traversing the command tree.
   * Converts given string to tokens and follows the path to retrieve the required command method
   * and also stores the placeholder values in parameters.
   *
   * @param inputString command input from user
   * @param calendar    active calendar which holds specific model and logic
   * @param view        calendar view responsible for display
   * @return a command interface type which the controller can delegate the model to
   * @throws IllegalArgumentException If given command is invalid or out of order
   */
  public CommandInterface parse(String inputString, CalendarManagerInterface calendar,
                                CalendarViewInterface view)
      throws IllegalArgumentException {
    parameters.clear();

    try {
      calendar.getActiveCalendar();
      this.setZone(calendar.getTimeZone());
    } catch (Exception e) {
      this.setZone(ZoneId.of("America/New_York"));
    }

    List<String> tokens = tokenize(inputString.trim());
    NodeInterface currentNode = this.rootNode;
    NodeInterface executingNode = null;
    String property = "";

    for (String token : tokens) {
      NodeInterface nextNode = currentNode.getChild(token);
      String expectedQuery = ". Expected: " + currentNode.getQuery();
      if (nextNode == null) {
        nextNode = this.rootNode.getChild(token);
        if (nextNode == null) {
          throw new IllegalArgumentException("Invalid item in input: " + token + expectedQuery);
        }
      }
      String commandName = nextNode.getCommandName();
      property = propertyCheck(token, commandName, property);
      extractValue(token, nextNode, commandName, property);
      if (nextNode.hasCommand()) {
        executingNode = nextNode;
      }
      checkOptional(token, commandName, nextNode);
      currentNode = nextNode;
    }
    if (executingNode == null) {
      throw new IllegalArgumentException("Incomplete command input: Missing tokens."
          + " Expected: " + currentNode.getQuery());
    }
    return executingNode.getExecutable(parameters, calendar, view);
  }

  /**
   * Check if the next node is an optional value, and if it's optional property is set properly.
   *
   * @param token       the token value being checked
   * @param commandName the literal command name of the node
   * @param nextNode    the node being checked
   * @throws IllegalArgumentException for invalid input
   */
  private static void checkOptional(String token, String commandName, NodeInterface nextNode)
      throws IllegalArgumentException {
    if (commandName.equals("location") || commandName.equals("description")
        || commandName.equals("status")
        || commandName.equals("happy") || commandName.equals("halloween")) {
      if (!nextNode.isOptional()) {
        throw new IllegalArgumentException(
            "Optional parameter is not set as optional property: " + token);
      }
    }
  }

  /**
   * For a placeholder node, extract the value in the correct type and add to the parameters list.
   *
   * @param token       the current token value
   * @param nextNode    the node being checked
   * @param commandName the literal command name of the node
   * @param property    the property name if a property value is being edited
   */
  private void extractValue(String token, NodeInterface nextNode, String commandName,
                            String property) {
    if (nextNode.isPlaceholder()) {
      if (commandName.equals("value")
          && (property.equals("start") || property.equals("end"))) {
        parameters.put(commandName, convertValue("start", token));
      } else if (commandName.equals("value")
          && (property.equals("timezone"))) {
        parameters.put(commandName, convertValue("timezone", token));
      } else {
        parameters.put(commandName, convertValue(commandName, token));
      }
    }
  }

  /**
   * To check if the current token is a property that is to be edited.
   * If so, it remembers which property to change later.
   *
   * @param token       the current token value
   * @param commandName the literal command name of the node
   * @param property    the property which is to be edited later
   * @return either the property value if seen otherwise the last property value
   */
  private static String propertyCheck(String token, String commandName, String property) {
    if (commandName.equals("property")) {
      property = token;
    }
    return property;
  }

  /**
   * Converts given string input separated by space into tokens.
   *
   * @param inputString command input from user
   * @return tokens as a list of string
   */
  private List<String> tokenize(String inputString) {
    List<String> tokens = new ArrayList<>();
    boolean inQuotes = false;
    StringBuilder s = new StringBuilder();

    for (char c : inputString.toCharArray()) {
      if (c == '"') {
        inQuotes = !inQuotes;
      } else if (c == ' ' && !inQuotes) {
        if (s.length() > 0) {
          tokens.add(s.toString());
          s.setLength(0);
        }
      } else {
        s.append(c);
      }
    }
    tokens.add(s.toString());
    return tokens;
  }

  /**
   * Converts the string into the correct datatype to store in parameters.
   *
   * @param commandName the command string
   * @param token       the given placeholder input value
   * @return the type converted value
   * @throws IllegalArgumentException for invalid input
   */
  private Object convertValue(String commandName, String token) throws IllegalArgumentException {
    try {
      if (commandName.equalsIgnoreCase("ndays")) {
        try {
          Integer.parseInt(token);
        } catch (Exception e) {
          throw new IllegalArgumentException("Invalid integer value format: " + token);
        }
      }
      if (commandName.endsWith("start")
          || commandName.endsWith("end")) {
        return parseDateTime(token);
      } else if (commandName.endsWith("date")) {
        return parseDate(token);
      }
      if (commandName.equalsIgnoreCase("timezone")) {
        try {
          return ZoneId.of(token);
        } catch (Exception e) {
          throw new IllegalArgumentException("Invalid time zone format: " + token);
        }
      }
      return token;
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  /**
   * Converts string datetime to required data type.
   *
   * @param token input string value
   * @return value in required datatype
   * @throws IllegalArgumentException for invalid input
   */
  private Object parseDateTime(String token) throws IllegalArgumentException {
    try {
      return LocalDateTime.parse(token).atZone(this.getZone());
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid date time value format: " + token);
    }
  }

  /**
   * Converts string date to required data type.
   *
   * @param token input string value
   * @return value in required datatype
   * @throws IllegalArgumentException for invalid input
   */
  private Object parseDate(String token) throws IllegalArgumentException {
    try {
      return LocalDate.parse(token);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid date value format: " + token);
    }
  }

  /**
   * Sets the timezone for this parser.
   */
  private void setZone(ZoneId timezone) {
    this.zoneId = timezone;
  }

  /**
   * Return the saved zone ID of this parser.
   *
   * @return zone ID of this parser for date time formatting
   */
  private ZoneId getZone() {
    return this.zoneId;
  }

}
