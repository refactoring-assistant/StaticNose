package calendar.model.debug;

import java.io.IOException;

/**
 * A Debug only version of an Appendable for use in testing.
 */
public class DebugAppendable implements Appendable {
  @Override
  public Appendable append(CharSequence csq) throws IOException {
    throw new IOException("IO exception thrown");
  }

  @Override
  public Appendable append(CharSequence csq, int start, int end) throws IOException {
    throw new IOException("IO exception thrown");
  }

  @Override
  public Appendable append(char c) throws IOException {
    throw new IOException("IO exception thrown");
  }
}
