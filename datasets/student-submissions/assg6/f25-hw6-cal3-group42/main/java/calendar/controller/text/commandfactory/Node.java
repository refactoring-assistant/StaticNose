package calendar.controller.text.commandfactory;

import calendar.model.CalendarManagerInterface;
import calendar.view.CalendarViewInterface;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that implements the node interface and creates the node with functionality. Each node
 * represents a command tree unit which is either a literal command or a placeholder value.
 * If at this node a command can be called, it is a leaf node which has a command factory class
 * attached to it.
 */
public class Node implements NodeInterface {
  private final String commandName;
  private final Boolean placeholder;
  private Boolean optional = false;
  private final Map<String, Node> nodeMap = new HashMap<>();
  private CommandFactory command;
  private String query;

  /**
   * Constructor to initialize the literal command name and stores if this is a placeholder or not.
   *
   * @param commandName literal command string
   * @param placeholder if this node is a placeholder for a value
   */
  public Node(String commandName, Boolean placeholder) {
    this.commandName = commandName.toLowerCase().trim();
    this.placeholder = placeholder;
  }

  @Override
  public Node addNode(String commandName, Boolean placeholder) {
    Node childNode = new Node(commandName, placeholder);
    this.nodeMap.put(commandName, childNode);
    return childNode;
  }

  @Override
  public Node getChild(String commandName) {
    Node child = nodeMap.getOrDefault(commandName.toLowerCase().trim(), null);
    if (child != null) {
      return child;
    }
    for (Node node : this.nodeMap.values()) {
      if (node.isPlaceholder()) {
        return node;
      }
    }
    return null;
  }

  @Override
  public void setCommand(CommandFactory command) {
    this.command = command;
  }

  @Override
  public boolean hasCommand() {
    return this.command != null;
  }

  @Override
  public String getCommandName() {
    return this.commandName;
  }

  @Override
  public boolean isPlaceholder() {
    return this.placeholder;
  }

  @Override
  public boolean isOptional() {
    return this.optional;
  }

  @Override
  public void setOptional(boolean optional) {
    this.optional = optional;
  }

  @Override
  public void setQuery(String query) {
    this.query = query;
  }

  @Override
  public String getQuery() {
    return this.query;
  }

  @Override
  public CommandInterface getExecutable(Map<String, Object> parameters,
                                        CalendarManagerInterface calendar,
                                        CalendarViewInterface view) throws IllegalStateException {
    if (this.command == null) {
      throw new IllegalStateException("Command is null for this node: " + this.commandName);
    }
    return this.command.getExecutable(parameters, calendar, view);
  }
}