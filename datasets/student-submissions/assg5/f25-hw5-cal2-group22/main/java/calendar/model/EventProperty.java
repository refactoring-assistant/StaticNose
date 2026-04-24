package calendar.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Editable properties of an event and how to apply them.
 */
public enum EventProperty {

  SUBJECT {
    @Override
    public AbstractEvent apply(AbstractEvent e, String v) {
      return e.toBuilder()
          .copyFrom(e)
          .subject(v)
          .build();
    }
  },

  START_DATE {
    @Override
    public AbstractEvent apply(AbstractEvent e, String v) {
      LocalDate d = LocalDate.parse(v);
      LocalDateTime newStart = LocalDateTime.of(d, e.getStart().toLocalTime());
      LocalDateTime newEnd = e.getEnd().isBefore(newStart) ? newStart : e.getEnd();
      return e.toBuilder()
          .copyFrom(e)
          .start(newStart)
          .end(newEnd)
          .build();
    }
  },

  START_TIME {
    @Override
    public AbstractEvent apply(AbstractEvent e, String v) {
      LocalTime t = LocalTime.parse(v);
      LocalDateTime newStart = LocalDateTime.of(e.getStart().toLocalDate(), t);
      LocalDateTime newEnd = e.getEnd().isBefore(newStart) ? newStart : e.getEnd();
      return e.toBuilder()
          .copyFrom(e)
          .start(newStart)
          .end(newEnd)
          .build();
    }
  },

  END_DATE {
    @Override
    public AbstractEvent apply(AbstractEvent e, String v) {
      LocalDate d = LocalDate.parse(v);
      LocalDateTime newEnd = LocalDateTime.of(d, e.getEnd().toLocalTime());
      return e.toBuilder()
          .copyFrom(e)
          .end(newEnd)
          .build();
    }
  },

  END_TIME {
    @Override
    public AbstractEvent apply(AbstractEvent e, String v) {
      LocalTime t = LocalTime.parse(v);
      LocalDateTime newEnd = LocalDateTime.of(e.getEnd().toLocalDate(), t);
      return e.toBuilder()
          .copyFrom(e)
          .end(newEnd)
          .build();
    }
  },

  ALL_DAY_EVENT {
    @Override
    public AbstractEvent apply(AbstractEvent e, String v) {
      boolean allDay = Boolean.parseBoolean(v == null ? "false" :
          v.trim().toLowerCase());
      if (!allDay) {
        return e;
      }
      LocalDate day = e.getStart().toLocalDate();
      LocalDateTime newStart = day.atTime(8, 0);
      LocalDateTime newEnd = day.atTime(17, 0);
      return e.toBuilder()
          .copyFrom(e)
          .start(newStart)
          .end(newEnd)
          .build();
    }
  },

  LOCATION {
    @Override
    public AbstractEvent apply(AbstractEvent e, String v) {
      return e.toBuilder()
          .copyFrom(e)
          .location(AbstractEvent.parseLocation(v))
          .build();
    }
  },

  DESCRIPTION {
    @Override
    public AbstractEvent apply(AbstractEvent e, String v) {
      return e.toBuilder()
          .copyFrom(e)
          .description(v)
          .build();
    }
  },

  STATUS {
    @Override
    public AbstractEvent apply(AbstractEvent e, String v) {
      return e.toBuilder()
          .copyFrom(e)
          .status(AbstractEvent.parseStatus(v))
          .build();
    }
  };

  /**
   * Apply this property change to the given event.
   *
   * @return Abstract event.
   */
  public abstract AbstractEvent apply(AbstractEvent e, String v);
}
