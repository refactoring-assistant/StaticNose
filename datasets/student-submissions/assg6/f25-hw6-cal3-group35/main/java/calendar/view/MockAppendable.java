package calendar.view;

import java.io.IOException;

/**
 * A mock Appendable for testing rendering failures.
 */
public class MockAppendable implements Appendable {

  @Override
  public Appendable append(CharSequence csq) throws IOException {
    throw new IOException("Mock append failed");
  }

  @Override
  public Appendable append(CharSequence csq, int start, int end) throws IOException {
    throw new IOException("Mock append failed");
  }

  @Override
  public Appendable append(char c) throws IOException {
    throw new IOException("Mock append failed");
  }
}
