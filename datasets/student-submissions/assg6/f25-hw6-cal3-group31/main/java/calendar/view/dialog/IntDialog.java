package calendar.view.dialog;

/**
 * Interface for dialog components in the calendar application.
 * Dialogs are used to collect user input for various operations.
 *
 * @param <T> the type of result this dialog produces
 */
public interface IntDialog<T> {
  /**
   * Displays the dialog and waits for user input.
   *
   * @return the result of the dialog interaction, or null if cancelled
   */
  T showDialog();

  /**
   * Sets the parent component for this dialog.
   * This is used for proper dialog positioning and modality.
   *
   * @param parent the parent component
   */
  void setParent(java.awt.Component parent);
}

