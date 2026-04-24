package messagechains.case2good;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.List;
import java.time.LocalDateTime;
import java.time.LocalTime;

class AttendanceManager {
  private final Map<Integer, Employee> employeesById;
  private LocalDateTime lastAccessTime;

  public AttendanceManager(List<Employee> employees) {
    this.employeesById = employees.stream()
      .collect(Collectors.toMap(Employee::getId, Function.identity()));
  }

  public Schedule getSchedule(int employeeId, LocalDate date) {
    updateAccessTime();
    Employee emp = employeesById.get(employeeId);
    if (emp == null) {
      throw new IllegalArgumentException("No employee with ID " + employeeId);
    }
    return emp.getSchedule(date);
  }

  public boolean isEmployeeOnShift(int employeeId, LocalDate date) {
    updateAccessTime();
    return getSchedule(employeeId, date).isOnShift();
  }

  public boolean isEmployeeOnShift(int employeeId, LocalDate date, java.time.LocalDateTime asOf) {
    updateAccessTime();
    return getSchedule(employeeId, date).isOnShift(asOf);
  }

  public LocalDateTime getLastAccessTime() {
    return lastAccessTime;
  }

  private void updateAccessTime() {
    this.lastAccessTime = LocalDateTime.now();
  }
}

class Employee {
  private final int id;
  private final String name;
  private final Map<LocalDate, Schedule> schedules;

  public Employee(int id, String name, Map<LocalDate, Schedule> schedules) {
    this.id = id;
    this.name = name;
    this.schedules = schedules;
  }

  public int getId() {
    return id;
  }

  Schedule getSchedule(LocalDate date) {
    return schedules.getOrDefault(date, Schedule.offDay(date));
  }
}

class Schedule {
  private final LocalDateTime start;
  private final LocalDateTime end;

  private Schedule(LocalDateTime start, LocalDateTime end) {
    this.start = start;
    this.end   = end;
  }

  public static Schedule forShift(LocalDate date, LocalTime startTime, LocalTime endTime) {
    return new Schedule(
      LocalDateTime.of(date, startTime),
      LocalDateTime.of(date, endTime)
    );
  }

  public static Schedule offDay(LocalDate date) {
    LocalDateTime dt = date.atStartOfDay();
    return new Schedule(dt, dt);
  }

  protected boolean isOnShift(LocalDateTime asOf) {
    return !asOf.isBefore(start) && !asOf.isAfter(end);
  }

  protected boolean isOnShift() {
    return isOnShift(LocalDateTime.now());
  }
}