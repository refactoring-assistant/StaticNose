package calendar;

import java.text.ParseException;

/**
 * Calendar Controller Interface.
 */
public interface IcalendarController {
  /**
   * Executes the controller.
   *
   * @param z    the mode to be run
   * @param path the path for headless mode
   * @throws ParseException incase there is a parse exception.
   */
  void go(int z, String path) throws ParseException;
}
