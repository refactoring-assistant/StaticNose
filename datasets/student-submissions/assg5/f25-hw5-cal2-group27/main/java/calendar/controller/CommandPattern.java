package calendar.controller;

import calendar.command.Command;
import calendar.command.CommandFactory;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Associates a regex pattern with a {@link CommandFactory} that can create a {@link Command}
 * when the pattern matches an input string.
 */
public class CommandPattern {

  private final Pattern pattern;
  private final CommandFactory factory;

  /**
   * Creates a new pattern/factory pair.
   *
   * @param regex   regular expression used to parse a command line
   * @param factory factory that builds a command from the regex {@link Matcher}
   */
  public CommandPattern(String regex, CommandFactory factory) {
    this.pattern = Pattern.compile(regex);
    this.factory = factory;
  }

  /**
   * Attempts to match the given string and, if successful, creates a command.
   *
   * @param commandString raw command line input
   * @return an {@link Optional} containing the command if matched; otherwise empty
   */
  public Optional<Command> matchAndCreate(String commandString) {
    Matcher matcher = pattern.matcher(commandString);
    if (matcher.matches()) {
      return factory.createCommand(matcher);
    }
    return Optional.empty();
  }
}