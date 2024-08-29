# v2.6.2

- Logs : console appender should not log all the messages (for all loggers in the system) (#28)
- Minor dependency updates

# v2.6.1

- Remove duplicate dependencies (jakarta mail/activation) (#27)

# v2.6.0

- Upgrade subethasmtp to 7.1 (#25)

# v2.5.1

- Minor fixes (#23, #24)

# v2.5.0

- Feature: Change eml file name pattern on disk (#16)
- Fix: Make it clear that option 'background' implies activation of option 'start-server' (#17)
- Fix: Avoid to save unnecessary properties to user config file (#19)
- Fix: Parse option 'port' at startup (#18)
- Refactoring: Prefer Java 8 Date and Time API
- Refactoring: Use Java 7 New I/O APIs
- Refactoring: Fat Jar file : prefer Maven Shade Plugin (#21)
- Refactoring: Use Publisher/Subscriber with emails events (replaces deprecated Observable) (#14)

# v2.4.0

- Use Java 17

# v2.3.5

- Fix: Check if SMTPServer instance exists before closing
- Upgrade miglayout-swing to 11.2

# v2.3.4

- Refactoring : Use `org.apache.commons.cli.Option.Builder` provided with commons-cli 1.5
- Add option "--help" to CLI
- Upgrade commons-cli to 1.6.0
- Upgrade commons-io to 2.15.0

# v2.3.3

- Ensure compatibility with **Java 21**

# v2.3.2

- Upgrade subethasmtp to 6.0.7
- Upgrade logback to 1.4.11
- Upgrade commons-io to 2.14.0

# v2.3.1

- Upgrade logback to 1.4.7
- Upgrade commons-io to 2.12.0

# v2.3.0

- Migration to package _jakarta.mail_ (see [Jakarta Mail](https://jakarta.ee/specifications/mail/2.0/jakarta-mail-spec-2.0.html))

# v2.2.3

- Refactoring: Replace old _org.subethamail:subethasmtp_ by _com.github.davidmoten:subethasmtp_ ([GitHub project](https://github.com/davidmoten/subethasmtp))

# v2.2.2

- Upgrade logback to 1.4.3
- Upgrade commons-io to 2.11.0
- Upgrade commons-cli to 1.5.0

# v2.2.1

- Dependencies upgrade

# v2.2

- Use Java 11

# v2.1

- Use Java 8
- Various plugins and dependencies updates