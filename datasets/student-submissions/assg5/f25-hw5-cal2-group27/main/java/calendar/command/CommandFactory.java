package calendar.command;

import java.util.Optional;
import java.util.regex.Matcher;

/**
 * Factory for creating {@link Command} instances from a regex {@link Matcher}.
 */
@FunctionalInterface
public interface CommandFactory {

  /**
   * Creates a command using captured arguments from the given matcher.
   *
   * @param matcher a matcher positioned at a successful match (never {@code null})
   * @return an optional command; empty if captured arguments are invalid
   */
  Optional<Command> createCommand(Matcher matcher);
}