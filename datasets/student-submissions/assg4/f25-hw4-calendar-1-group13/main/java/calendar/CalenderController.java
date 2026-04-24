package calendar;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Calendar controller class.
 */
public class CalenderController implements IcalendarController {

  private final Icalender calenderModel;
  private final CalenderView calenderView;
  private final Scanner in;

  /**
   * calenderController is a calendar controller constructor to set the controller.
   *
   * @param c  the Calendar Model.
   * @param in the Input Stream.
   * @param cv the Calendar View.
   */
  public CalenderController(Icalender c, InputStream in, CalenderView cv) {
    calenderModel = c;
    calenderView = cv;
    this.in = new Scanner(in);
  }


  private void addToEventCase1(StringBuilder subject, String[] splitString, int w) {
    String startDateTime;
    String endDateTime;
    String repeatDays;
    startDateTime = splitString[w + 1];
    endDateTime = splitString[w + 3];
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm");
    LocalDateTime startTime = LocalDateTime.parse(startDateTime, formatter);
    LocalDateTime endTime = LocalDateTime.parse(endDateTime, formatter);
    repeatDays = splitString[w + 5];
    String repeatCount;
    repeatCount = splitString[w + 7];
    int r = Integer.parseInt(repeatCount);
    EventSeries e = new EventSeries(subject.toString(), startTime, endTime, repeatDays, r, null);
    try {
      calenderModel.addEvent(e);
      calenderView.addEventSucess();
    } catch (IllegalArgumentException ex) {
      calenderView.eventExistsError();
    }
  }

  private void addEventSeriesCase1(String[] splitString) {
    splitString = Arrays.copyOfRange(splitString, 2, splitString.length);
    int w = 0;
    StringBuilder subject = new StringBuilder();
    if (splitString[0].contains("\"")) {
      w = 1;
      subject.append(splitString[0].substring(1)).append(" ");
      while (!splitString[w].contains("\"")) {
        subject.append(splitString[w]).append(" ");
        w = w + 1;
      }
      subject.append(splitString[w], 0, splitString[w].length() - 1);
    } else {
      subject = new StringBuilder(splitString[w]);
    }
    w = w + 1;
    String[] checkString = Arrays.copyOfRange(splitString, w, splitString.length);
    if (checkString.length == 9) {
      addToEventCase1(subject, splitString, w);
    } else {
      calenderView.showError();
    }
  }

  private void addToEventCase2(StringBuilder subject, String[] splitString, int w) {
    String startDateTime;
    String endDateTime;
    String repeatDays;
    startDateTime = splitString[w + 1];
    endDateTime = splitString[w + 3];
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm");
    LocalDateTime startTime = LocalDateTime.parse(startDateTime, formatter);
    LocalDateTime endTime = LocalDateTime.parse(endDateTime, formatter);
    repeatDays = splitString[w + 5];
    String repeatCount;
    repeatCount = splitString[w + 7];
    DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate untilDate = LocalDate.parse(repeatCount, formatter1);
    EventSeries e =
        new EventSeries(subject.toString(), startTime, endTime, repeatDays, null, untilDate);
    try {
      calenderModel.addEvent(e);
      calenderView.addEventSucess();
    } catch (IllegalArgumentException ex) {
      calenderView.eventExistsError();
    }
  }

  private void addEventSeriesCase2(String[] splitString) {
    splitString = Arrays.copyOfRange(splitString, 2, splitString.length);
    int w = 0;
    StringBuilder subject = new StringBuilder();
    if (splitString[0].contains("\"")) {
      w = 1;
      subject.append(splitString[0].substring(1)).append(" ");
      while (!splitString[w].contains("\"")) {
        subject.append(splitString[w]).append(" ");
        w = w + 1;
      }
      subject.append(splitString[w], 0, splitString[w].length() - 1);
    } else {
      subject = new StringBuilder(splitString[w]);
    }
    w = w + 1;
    String[] checkString = Arrays.copyOfRange(splitString, w, splitString.length);
    if (checkString.length == 8) {
      addToEventCase2(subject, splitString, w);
    } else {
      calenderView.showError();
    }
  }

  private void addToEventCase3(StringBuilder subject, String[] splitString, int w) {
    String startDateTime;
    String repeatDays;
    String repeatCount;
    startDateTime = splitString[w + 1];
    DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate startTime = LocalDate.parse(startDateTime, formatter1);
    repeatDays = splitString[w + 3];
    repeatCount = splitString[w + 5];
    int r = Integer.parseInt(repeatCount);
    EventSeries e = new EventSeries(subject.toString(), startTime, repeatDays, r, null);
    try {
      calenderModel.addEvent(e);
      calenderView.addEventSucess();
    } catch (IllegalArgumentException ex) {
      calenderView.eventExistsError();
    }
  }

  private void addEventSeriesCase3(String[] splitString) {
    splitString = Arrays.copyOfRange(splitString, 2, splitString.length);
    int w = 0;
    StringBuilder subject = new StringBuilder();
    if (splitString[0].contains("\"")) {
      w = 1;
      subject.append(splitString[0].substring(1)).append(" ");
      while (!splitString[w].contains("\"")) {
        subject.append(splitString[w]).append(" ");
        w = w + 1;
      }
      subject.append(splitString[w], 0, splitString[w].length() - 1);
    } else {
      subject = new StringBuilder(splitString[w]);
    }
    w = w + 1;
    String[] checkString = Arrays.copyOfRange(splitString, w, splitString.length);
    if (checkString.length == 7) {
      addToEventCase3(subject, splitString, w);
    } else {
      calenderView.showError();
    }
  }

  private void addToEventCase4(StringBuilder subject, String[] splitString, int w) {
    String startDateTime;
    String repeatDays;
    String repeatCount;
    startDateTime = splitString[w + 1];
    DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    LocalDate startTime = LocalDate.parse(startDateTime, formatter1);
    repeatDays = splitString[w + 3];
    repeatCount = splitString[w + 5];
    LocalDate untilDate = LocalDate.parse(repeatCount, formatter1);
    EventSeries e = new EventSeries(subject.toString(), startTime, repeatDays, null, untilDate);
    try {
      calenderModel.addEvent(e);
      calenderView.addEventSucess();
    } catch (IllegalArgumentException ex) {
      calenderView.eventExistsError();
    }
  }

  private void addEventSeriesCase4(String[] splitString) {
    splitString = Arrays.copyOfRange(splitString, 2, splitString.length);
    int w = 0;
    StringBuilder subject = new StringBuilder();
    if (splitString[0].contains("\"")) {
      w = 1;
      subject.append(splitString[0].substring(1)).append(" ");
      while (!splitString[w].contains("\"")) {
        subject.append(splitString[w]).append(" ");
        w = w + 1;
      }
      subject.append(splitString[w], 0, splitString[w].length() - 1);
    } else {
      subject = new StringBuilder(splitString[w]);
    }
    w = w + 1;
    String[] checkString = Arrays.copyOfRange(splitString, w, splitString.length);
    if (checkString.length == 6) {
      addToEventCase4(subject, splitString, w);
    } else {
      calenderView.showError();
    }
  }

  private void createSeries(String option, String[] splitString) {
    if (option.contains("from") && option.contains("to") && option.contains("for")
        && option.contains("times")
        && option.indexOf("from") < option.indexOf("to")
        && option.indexOf("to") < option.indexOf("for")
        && option.indexOf("for") < option.indexOf("times")) {
      this.addEventSeriesCase1(splitString);
    } else if (option.contains("from") && option.contains("to") && option.contains("until")
        && option.indexOf("from") < option.indexOf("to")
        && option.indexOf("to") < option.indexOf("until")) {
      this.addEventSeriesCase2(splitString);
    } else if (option.contains("on") && option.contains("for") && option.contains("times")
        && option.indexOf("on") < option.indexOf("for")
        && option.indexOf("for") < option.indexOf("times")) {
      this.addEventSeriesCase3(splitString);
    } else if (option.contains("on") && option.contains("until")
        && option.indexOf("on") < option.indexOf("until")) {
      this.addEventSeriesCase4(splitString);
    } else {
      calenderView.showError();
    }
  }

  private void createSingleEvent(String[] splitString) {
    Event e = null;
    if (splitString.length <= 3) {
      calenderView.showError();
    } else {
      splitString = Arrays.copyOfRange(splitString, 2, splitString.length);
      int w = 0;
      StringBuilder subject = new StringBuilder();
      if (splitString[0].contains("\"")) {
        w = 1;
        subject.append(splitString[0].substring(1)).append(" ");
        while (!splitString[w].contains("\"")) {
          subject.append(splitString[w]).append(" ");
          w = w + 1;
        }
        subject.append(splitString[w], 0, splitString[w].length() - 1);
      } else {
        subject = new StringBuilder(splitString[w]);
      }
      w = w + 1;
      editSingleEventHelper(splitString, e, w, subject);

    }
  }

  private void editSingleEventHelper(String[] splitString, Event e, int w, StringBuilder subject) {
    String startDateTime = null;
    String endDateTime = null;
    String[] checkString = Arrays.copyOfRange(splitString, w, splitString.length);
    if (splitString[w].contains("from")) {
      fromSingleEvent(splitString, startDateTime, endDateTime, subject, checkString, subject, e, w);
    } else {
      onSingleEvent(splitString, startDateTime, endDateTime, subject, checkString, subject, e, w);
    }
  }

  private void fromSingleEvent(String[] splitString, String startDateTime, String endDateTime,
                               StringBuilder subject, String[] checkString,
                               StringBuilder stringBuilder, Event e, int w) {
    if (checkString.length == 4) {
      startDateTime = splitString[w + 1];
      endDateTime = splitString[w + 3];
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm");
      LocalDateTime startTime = LocalDateTime.parse(startDateTime, formatter);
      LocalDateTime endTime = LocalDateTime.parse(endDateTime, formatter);
      e = new Event(subject.toString(), startTime, endTime);
      try {
        calenderModel.addEvent(e);
        calenderView.addEventSucess();
      } catch (IllegalArgumentException ex) {
        calenderView.eventExistsError();
      }
    } else {
      calenderView.showError();
    }
  }

  private void onSingleEvent(String[] splitString, String startDateTime, String endDateTime,
                             StringBuilder subject, String[] checkString,
                             StringBuilder stringBuilder, Event e, int w) {
    if (checkString.length == 2) {
      startDateTime = splitString[w + 1];
      e = new Event(subject.toString(), LocalDate.parse(startDateTime));
      try {
        calenderModel.addEvent(e);
        calenderView.addEventSucess();
      } catch (IllegalArgumentException ex) {
        calenderView.eventExistsError();
      }
    } else {
      calenderView.showError();
    }
  }

  private void createEventHelper(String option) {
    String[] splitString = option.split("\\s+");
    if (option.contains("repeats")) {
      createSeries(option, splitString);
    } else {
      createSingleEvent(splitString);
    }
  }

  /**
   * Executes the controller.
   *
   * @param z    the mode to be run
   * @param path the path for headless mode
   * @throws ParseException incase there is a parse exception.
   */
  public void go(int z, String path) throws ParseException {

    BufferedReader br = null;
    if (path != null) {
      try {
        br = new BufferedReader(new FileReader(path));
      } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
      }
    }

    String option;
    while (true) {
      if (z == 0) {
        calenderView.showOptions();
        System.out.println("Enter string");
        option = in.nextLine();
      } else {
        try {
          option = br.readLine();
          if (option == null) {
            break;
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      if (option.contains("create event")) {
        createEventHelper(option);
      } else if (option.contains("edit")) {
        String[] splitString = option.split("\\s+");
        editEventHelper(option, splitString);
      } else if (option.contains("print events")) {
        printEventHelper(option);
      } else if (option.contains("show status on")) {
        isBusy(option);
      } else if (option.contains("exit")) {
        break;
      } else if (option.contains("export cal")) {
        try {
          String[] splitString = option.split("\\s+");
          String s = calenderModel.exportToCsv(splitString[2]);
          calenderView.displayAbsolutePath(s);

        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      } else {
        calenderView.showError();
      }
    }
  }

  /**
   * This method is a helper for show status.
   *
   * @param option the string that is the input.
   */
  private void isBusy(String option) {
    String[] splitString = option.split("\\s+");
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm");
    LocalDateTime startTime = LocalDateTime.parse(splitString[3], formatter);
    boolean a = calenderModel.isBusyAt(startTime);
    calenderView.isBusy(a);
  }

  /**
   * A helper to parse the print event.
   *
   * @param option the option string.
   */
  private void printEventHelper(String option) {
    List<Event> events;
    if (option.contains("on")) {
      String[] splitString = option.split("\\s+");
      String date = splitString[3];
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
      LocalDate startTime = LocalDate.parse(date, formatter);
      events = calenderModel.displayEventOn(startTime);
      calenderView.showEvents(events);
    } else if (option.contains("from")) {
      String[] splitString = option.split("\\s+");
      String startDateTime = splitString[3];
      String endDateTime = splitString[5];
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm");
      LocalDateTime startTime = LocalDateTime.parse(startDateTime, formatter);
      LocalDateTime endTime = LocalDateTime.parse(endDateTime, formatter);
      events = calenderModel.displayEventBetween(startTime, endTime);
      calenderView.showEvents(events);
    } else {
      calenderView.showError();
    }
  }

  /**
   * The first case for edit event in the form edit event property
   * Subject from dateStringTtimeString to dateStringTtimeString with NewPropertyValue.
   *
   * @param splitString the option string that is split.
   */
  private void editEventCase1(String[] splitString) {
    int w;
    int k;
    StringBuilder subject = new StringBuilder();
    if (splitString[3].contains("\"")) {
      w = 4;
      subject.append(splitString[3].substring(1)).append(" ");
      while (!splitString[w].contains("\"")) {
        subject.append(splitString[w]).append(" ");
        w = w + 1;
      }
      subject.append(splitString[w], 0, splitString[w].length() - 1);
      w = w + 1;
    } else {
      subject = new StringBuilder(splitString[3]);
      w = 4;
    }
    k = w;
    editEventCase1Helper(subject, k, w, splitString);

  }

  /**
   * Handles the event series assignment.
   *
   * @param subject     subject of the event.
   * @param k           helper k for indexing
   * @param w           helper w for indexing
   * @param splitString the option string split.
   */
  private void editEventCase1Helper(StringBuilder subject, int k, int w, String[] splitString) {
    StringBuilder newProperty = new StringBuilder();
    if (splitString[w + 5].contains("\"")) {
      newProperty.append(splitString[w + 5].substring(1)).append(" ");
      w = w + 6;
      while (!splitString[w].contains("\"")) {
        newProperty.append(splitString[w]).append(" ");
        w = w + 1;
      }
      newProperty.append(splitString[w], 0, splitString[w].length() - 1);
    } else {
      newProperty = new StringBuilder(splitString[w + 5]);
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm");
    LocalDateTime fromDate = LocalDateTime.parse(splitString[k + 1], formatter);
    calenderModel.editEvent(subject.toString(), fromDate, splitString[2],
        newProperty.toString());
    calenderView.editSucess();
  }

  /**
   * Handles the event series assignment.
   *
   * @param subject     subject of the event.
   * @param k           helper k for indexing
   * @param w           helper w for indexing
   * @param splitString the option string split.
   */
  private void editEventCase2Helper(StringBuilder subject, int k, int w, String[] splitString) {
    StringBuilder newProperty = new StringBuilder();
    if (splitString[w + 3].contains("\"")) {
      newProperty.append(splitString[w + 3].substring(1)).append(" ");
      w = w + 4;
      while (!splitString[w].contains("\"")) {
        newProperty.append(splitString[w]).append(" ");
        w = w + 1;
      }
      newProperty.append(splitString[w], 0, splitString[w].length() - 1);
    } else {
      newProperty = new StringBuilder(splitString[w + 3]);
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm");
    LocalDateTime fromDate = LocalDateTime.parse(splitString[k + 1], formatter);
    calenderModel.editEventsFromDate(subject.toString(), fromDate, splitString[2],
        newProperty.toString());
    calenderView.editSucess();
  }

  /**
   * The second case for edit event in the form edit events property
   * Subject from dateStringTtimeString with NewPropertyValue.
   *
   * @param splitString the option string that is split.
   */
  private void editEventCase2(String[] splitString) {
    int w;
    int k;
    StringBuilder subject = new StringBuilder();
    if (splitString[3].contains("\"")) {
      w = 4;
      subject.append(splitString[3].substring(1)).append(" ");
      while (!splitString[w].contains("\"")) {
        subject.append(splitString[w]).append(" ");
        w = w + 1;
      }
      subject.append(splitString[w], 0, splitString[w].length() - 1);
      w = w + 1;
    } else {
      subject = new StringBuilder(splitString[3]);
      w = 4;
    }
    k = w;
    editEventCase2Helper(subject, k, w, splitString);
  }

  /**
   * The third case for edit event in the form edit series property
   * Subject from dateStringTtimeString with NewPropertyValue.
   *
   * @param splitString the option string that is split.
   */
  private void editEventCase3(String[] splitString) {
    int w;
    int k;
    StringBuilder subject = new StringBuilder();
    if (splitString[3].contains("\"")) {
      w = 4;
      subject.append(splitString[3].substring(1)).append(" ");
      while (!splitString[w].contains("\"")) {
        subject.append(splitString[w]).append(" ");
        w = w + 1;
      }
      subject.append(splitString[w], 0, splitString[w].length() - 1);
      w = w + 1;
    } else {
      subject = new StringBuilder(splitString[3]);
      w = 4;
    }
    k = w;
    editEventCase3Helper(subject, k, w, splitString);
  }

  /**
   * Handles the event series assignment.
   *
   * @param subject     subject of the event.
   * @param k           helper k for indexing
   * @param w           helper w for indexing
   * @param splitString the option string split.
   */
  private void editEventCase3Helper(StringBuilder subject, int k, int w, String[] splitString) {
    StringBuilder newProperty = new StringBuilder();
    if (splitString[w + 3].contains("\"")) {
      newProperty.append(splitString[w + 3].substring(1)).append(" ");
      w = w + 4;
      while (!splitString[w].contains("\"")) {
        newProperty.append(splitString[w]).append(" ");
        w = w + 1;
      }
      newProperty.append(splitString[w], 0, splitString[w].length() - 1);
    } else {
      newProperty = new StringBuilder(splitString[w + 3]);
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-ddHH:mm");
    LocalDateTime fromDate = LocalDateTime.parse(splitString[k + 1], formatter);
    calenderModel.editEventsFromDate(subject.toString(), fromDate, splitString[2],
        newProperty.toString());
    calenderView.editSucess();
  }

  private void editEventHelper(String option, String[] splitString) {
    if (option.contains("event") && option.contains("from") && option.contains("to")
        && splitString.length >= 10) {
      editEventCase1(splitString);
    } else if (option.contains("events") && option.contains("from") && splitString.length >= 8) {
      editEventCase2(splitString);
    } else if (option.contains("series") && option.contains("from") && splitString.length >= 8) {
      editEventCase3(splitString);
    } else if (option.contains("export cal")) {
      try {
        String a = calenderModel.exportToCsv(splitString[2]);
        calenderView.displayAbsolutePath(a);

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      calenderView.showError();
    }
  }
}
