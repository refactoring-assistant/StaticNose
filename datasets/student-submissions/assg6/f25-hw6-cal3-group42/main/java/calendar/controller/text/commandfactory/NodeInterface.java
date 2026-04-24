package calendar.controller.text.commandfactory;

import calendar.model.CalendarManagerInterface;
import calendar.view.CalendarViewInterface;
import java.util.Map;

/**
 * Interface for a node in the command tree structure.
 * It describes all available public methods for each node.
 */
public interface NodeInterface {

  /**
   * Adds a node to the given node with the name and if it is a placeholder or not.
   *
   * @param commandName   the literal command string
   * @param isPlaceholder if this is a placeholder or not
   * @return the newly created node
   */
  Node addNode(String commandName, Boolean isPlaceholder);

  /**
   * Gets the next placeholder node connected to this node.
   *
   * @param commandName the literal command string
   * @return the node attached next to the given node
   */
  Node getChild(String commandName);

  /**
   * Sets the command factory class to this node if this node is the leaf node, i.e. a node where
   * the command string can stop and execute.
   *
   * @param command the command factory class
   */
  void setCommand(CommandFactory command);

  /**
   * Checks if this node has a command factory class attached to it.
   *
   * @return true if this node has a command otherwise false
   */
  boolean hasCommand();

  /**
   * Gets the literal command string of this node.
   *
   * @return a string literal command
   */
  String getCommandName();

  /**
   * Checks is this node is a placeholder node, i.e. a node which stores input values.
   *
   * @return true if this node is a placeholder node
   */
  boolean isPlaceholder();

  /**
   * Checks is this node is an optional node, i.e. a node which is an optional value.
   *
   * @return true if this node is a placeholder node
   */
  boolean isOptional();

  /**
   * Sets the property of this node if this is an optional value.
   *
   * @param optional true if this node is optional
   */
  void setOptional(boolean optional);

  /**
   * Set the query for this command branch.
   * This is the expected query format that this branch expects to get.
   *
   * @param query string value for the query line
   */
  void setQuery(String query);


  /**
   * Set the query for this command branch.
   * This is the expected query format that this branch expects to get.
   *
   * @return a string of the expected command query
   */
  String getQuery();

  /**
   * Returns back a CommandInterface object which is the executable that the controller can use to
   * delegate the correct command to the model.
   *
   * @param parameters the list of all placeholder values
   * @param calendar   the active calendar
   * @param view       the calendar view
   * @return the executable object
   * @throws IllegalStateException for a call to this command with missing command
   */
  CommandInterface getExecutable(Map<String, Object> parameters, CalendarManagerInterface calendar,
                                 CalendarViewInterface view) throws IllegalStateException;
}
