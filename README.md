# CollectTrack: Banking Collection System

A terminal-based Java application for tracking overdue loan accounts at a bank's collections branch. Built as a college mini-project.

## Features

- **Add single customer:** interactive terminal entry form with live field validation and duplicate checks.
- **Import from Excel:** batch import customer and loan records from `.xlsx` files with row-by-row validation.
- **Update one or many:**
  - Update a single customer via an interactive full-screen editor with live DPD, bucket, and status preview.
  - Update multiple customers via Excel with difference detection and optional new account creation.
- **List with sort and filter:** responsive paginated customer list supporting sorting (DPD high/low, Name A-Z, EMI) and DPD bucket filtering.
- **Account detail view:** comprehensive view of customer contact information, loan specifications, and overdue calculations.
- **Export to Excel:** export all recalculated customer and loan accounts to a formatted `.xlsx` workbook (with bold frozen headers, auto-filter, and auto-sized columns).
- **DPD buckets with colours:** visual ANSI colour coding for Days Past Due buckets (0-30 Green, 31-60 Yellow, 61-90 Red, 90+ Magenta) and payment statuses.

## Tech stack

- Java 17 LTS or newer
- Maven (build and dependency management)
- [JLine](https://github.com/jline/jline3) 3.30.17 for terminal UI and key navigation
- MySQL Connector/J 5.1.48 for JDBC persistence
- [Apache POI](https://poi.apache.org/) 5.2.5 for Excel import and export

## Requirements

- JDK 17 or newer
- Maven 3.9 or newer
- MySQL Server (database `collecttrack_demo`)
- Windows or macOS, run from a real terminal (PowerShell, Windows Terminal, Terminal.app). The IDE output panel is not a real console and will not support raw terminal mode.

## Build and run

Run all commands from the project root (the folder containing `pom.xml`).

**First time, or after clean:**

```bash
mvn clean compile dependency:copy-dependencies
```

**After ordinary code edits:**

```bash
mvn compile
```

**Run (Windows PowerShell):**

```powershell
java --enable-native-access=ALL-UNNAMED -cp "target\classes;target\dependency\*" miniproject.Menu
```

**Run (macOS / Linux):**

```bash
java --enable-native-access=ALL-UNNAMED -cp "target/classes:target/dependency/*" miniproject.Menu
```

`--enable-native-access=ALL-UNNAMED` prevents JVM warnings when JLine accesses native terminal libraries.

## Project structure

```
java-miniproject/
├── pom.xml                      Maven configuration and dependencies
└── src/main/java/miniproject/   Java source files
```

## Data privacy

This repository must not contain real customer data. Excel and data files are excluded through `.gitignore`; use synthetic test data for demos and development.

## Author

Manan ([MananThakkar2244](https://github.com/MananThakkar2244))