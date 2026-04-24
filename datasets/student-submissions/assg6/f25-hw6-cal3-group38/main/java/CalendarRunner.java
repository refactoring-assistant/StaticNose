import calendar.controller.CalendarController;
import calendar.view.CalendarGuiView;
import calendar.view.HeadlessView;
import calendar.view.InteractiveView;

/**
 * Main program runner for Calendar Application.
 */
public class CalendarRunner {
  private boolean testingMode = false;

  /**
   * Main method to start the calendar application.
   *
   * @param args command line arguments
   */
  public static void main(String[] args) {
    new CalendarRunner().run(args);
  }

  /**
   * Runnable method for testing.
   */
  public void run(String[] args) {
    CalendarController controller = new CalendarController();

    if (args.length == 0) {
      startGuiMode(controller);
    } else if (args.length >= 2 && "--mode".equalsIgnoreCase(args[0])) {
      if ("interactive".equalsIgnoreCase(args[1])) {
        startInteractiveMode(controller);
      } else if ("headless".equalsIgnoreCase(args[1]) && args.length >= 3) {
        startHeadlessMode(controller, args[2]);
      } else {
        printUsage();
      }
    } else {
      printUsage();
    }
  }

  /**
   * Start GUI mode.
   */
  protected void startGuiMode(CalendarController controller) {
    if (testingMode) {
      System.out.println("GUI mode started (testing)");
      return;
    }

    // Set modern look and feel
    try {
      for (javax.swing.UIManager.LookAndFeelInfo info :
          javax.swing.UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          javax.swing.UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (Exception e) {
      // Use default look and feel
    }

    java.awt.EventQueue.invokeLater(() -> {
      CalendarGuiView view = new CalendarGuiView(controller);
      view.setVisible(true);
    });
  }

  /**
   * Start interactive mode.
   */
  protected void startInteractiveMode(CalendarController controller) {
    if (testingMode) {
      System.out.println("Interactive mode started (testing)");
      return;
    }
    InteractiveView view = new InteractiveView(controller);
    view.start();
  }

  /**
   * Start headless mode.
   */
  protected void startHeadlessMode(CalendarController controller, String filePath) {
    if (testingMode) {
      System.out.println("Headless mode started with file: " + filePath + " (testing)");
      return;
    }
    HeadlessView view = new HeadlessView(controller);
    view.processFile(filePath);
  }

  /**
   * Prints usage instructions.
   */
  protected void printUsage() {
    System.out.println("📅 Calendar Application Usage:");
    System.out.println("  GUI Mode:          java -jar calendar.jar");
    System.out.println("  Interactive Mode:  java -jar calendar.jar --mode interactive");
    System.out.println("  Headless Mode:     java -jar calendar.jar --mode headless <script-file>");
    System.out.println("  Default:           java -jar calendar.jar (starts GUI mode)");
  }

  /**
   * Enable testing mode.
   */
  public void setTestingMode(boolean testingMode) {
    this.testingMode = testingMode;
  }
}