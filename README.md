# CollectTrack: Banking Collection System

A terminal-based Java application for tracking overdue loan accounts at a bank's collections branch. Built as a college mini-project.

> **Status:** work in progress. The menu screen is being built first; the features below are the planned scope.

## Planned features

- **Customer and loan account records:** account number, customer ID, loan type and amount, EMI amount, due date, contact details
- **Overdue tracking:** accounts grouped into DPD (days past due) buckets: 0-30, 31-60 and 61-90 days
- **Paid status:** Unpaid, Stabilize (1 EMI paid), Roll Back (2 EMIs paid), Normalize (total overdue paid)
- **Remarks log:** contact outcome for each follow-up
  - Contactable: RTP (refused to pay), PTP (promised to pay), Paid, WN (wrong number), LM (left message)
  - Not contactable: Skip (customer shifted, cannot be traced), Shifted (customer said they are moving)
- **Excel import and export:** load customer data from an Excel file, edit it inside the program, and export a report
- **Email:** send the generated report from the program
- **Persistent storage:** database access through JDBC
- **Terminal UI:** arrow-key menu navigation, with colour used to show status

## Tech stack

- Java 25 LTS
- Maven (build and dependency management)
- [JLine](https://github.com/jline/jline3) 3.30.17 for terminal control (window size, key input)
- Planned: Apache POI (Excel), Jakarta Mail (email), JDBC

## Requirements

- JDK 25 LTS or newer
- Maven 3.9 or newer
- Windows or macOS, run from a real terminal (PowerShell, Windows Terminal, Terminal.app). The IDE's output panel will not work, because it is not a real console.

## Build and run

Run all commands from the project root (the folder containing `pom.xml`).

**First time, or after changing dependencies:**

```
mvn clean compile dependency:copy-dependencies
```

**After ordinary code edits:**

```
mvn compile
```

**Run (Windows PowerShell):**

```
java --enable-native-access=ALL-UNNAMED -cp "target\classes;target\dependency\*" miniproject.Menu
```

On macOS, use `:` instead of `;` and `/` instead of `\`:

```
java --enable-native-access=ALL-UNNAMED -cp "target/classes:target/dependency/*" miniproject.Menu
```

`--enable-native-access=ALL-UNNAMED` stops Java 22+ from printing a warning when JLine calls the operating system to read the terminal.

## Project structure

```
java-miniproject/
├── pom.xml                      Maven configuration and dependencies
└── src/main/java/miniproject/   Java source files
```

## Data privacy

This repository must not contain real customer data. Excel and data files are excluded through `.gitignore`; use made-up sample data for testing and demos.

## Author

Manan ([MananThakkar2244](https://github.com/MananThakkar2244))