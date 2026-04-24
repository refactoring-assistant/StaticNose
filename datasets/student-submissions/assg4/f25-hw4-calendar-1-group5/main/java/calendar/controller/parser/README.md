# Parser V1

This version of the parser can parse following command strings:

## Creating Events

Creates a single event in the calendar. Note \<dateString> is a string of the form "YYYY-MM-DD" \<timeString> is a
string of the form "hh:mm" and \<dateStringTtimeString> is a string of the form "YYYY-MM-DDThh::mm".

- `create event <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString>`

Creates an event series that repeats N times on specific weekdays. Note \<weekdays> is a sequence of characters where
character denotes a day of the week, e.g., MRU. 'M' is Monday, 'T' is Tuesday, 'W' is Wednesday, 'R' is Thursday, 'F' is
Friday, 'S' is Saturday, and 'U' is Sunday.

- `create event <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString> repeats <weekdays> for <N> times`

Creates an event series until a specific date (inclusive).

-
`create event <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString> repeats <weekdays> until <dateString>`

Creates a single all day event.

- `create event <eventSubject> on <dateString>`

Creates a series of all day events that repeats N times on specific weekdays.

- `create event <eventSubject> on <dateString> repeats <weekdays> for <N> times`

Creates a series of all day events until a specific date (inclusive).

- `create event <eventSubject> on <dateString> repeats <weekdays> until <dateString>`

For all of the above, the subject may have multiple words. Only in this case, the subject must be enclosed in double
quotes.

## Editing Events

Identify the event that has the given subject and starts at the given date and time, and edit its property. This results
in change in property for a single instance (irrespective of whether the identified event is single or part of a
series).

- `edit event <property> <eventSubject> from <dateStringTtimeString> to <dateStringTtimeString> with <NewPropertyValue>`

Identify the event(s) that has the given subject and starts at the given date and time and edit its property. If this
event is part of a series then the properties of all events in that series that start at or after the given date and
time should be changed. If this event is not part of a series then this has the same effect as the command above.

- `edit events <property> <eventSubject> from <dateStringTtimeString> with <NewPropertyValue>`

Identify the event that has the given subject and starts at the given date and time and edit its property. If this event
is part of a series then the properties of all events in that series should be changed. If this event is not part of a
series then this has the same effect as the first edit command.

- `edit series <property> <eventSubject> from <dateStringTtimeString> with <NewPropertyValue>`

For all these queries the <property> field may be one of the following:

- `subject` - String
- `start` - dateStringTtimeString
- `end` - dateStringTtimeString
- `description` - String
- `location` - String
- `status`- String

## Queries

Prints a bulleted list of all events on that day along with their start and end time and location (if any).

- `print events on <dateString>`

Prints a bulleted list of all events that partly or completely lie in the given interval. Each event should be listed in
a single line and must be in the following format: <subject> starting on <startdate> at <starttime>, ending on <enddate>
at <endtime> including their start and end times and location (if any).

- `print events from <dateStringTtimeString> to <dateStringTtimeString>`

## Miscellaneous

Exports the calendar as a csv file that can be imported to Google Calendar app. The command should also print the
absolute path of the generated csv file. Note all file system paths processed by your program must be platform
independent, i.e., the src and tests should not depend on OS specific details of a file path.

- `export cal fileName.csv`

Prints busy status if the user has events scheduled on a given day and time, otherwise, available.

- `show status on <dateStringTtimeString>`

Your program must halt with an error if a user enters an unexpected command. The error must clearly indicate which
command was invalid and why.

- `exit`
