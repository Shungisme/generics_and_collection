# Generics And Collection

## Overview

This project is a desktop Library Management System built with Java Swing.
It uses the Java Collections Framework for in-memory data handling and plain text files with a pipe-delimited (`|`) format for persistence.

## Technology Stack

- Java (JDK 8+)
- Java Swing (UI layer)
- Java Collections Framework (`ArrayList`, `HashMap`)
- Plain text file storage (`|` as field delimiter)

## Project Structure

```text
src/
  App.java
  model/
    Reader.java
    Book.java
    BorrowSlip.java
    Librarian.java
  service/
    ReaderService.java
    BookService.java
    BorrowService.java
  ui/
    LoginFrame.java
    MainFrame.java
    panels/
      DashboardPanel.java
  utils/
    FileManager.java
```

## Package Responsibilities

- `model`: Domain entities for the library system.
- `service`: In-memory business operations using `ArrayList` and `HashMap`.
- `ui`: Swing frames and panels.
- `utils`: File input/output utilities and delimiter helpers.

## Implemented Domain Models

### Reader

Fields:

- `readerId`
- `fullName`
- `idCard`
- `dateOfBirth`
- `gender`
- `email`
- `address`
- `cardCreatedDate`
- `cardExpiredDate`

Business rule:

- `cardExpiredDate = cardCreatedDate + 48 months`

### Book

Fields:

- `isbn`
- `title`
- `author`
- `publisher`
- `yearPublished`
- `genre`
- `price`
- `quantity`

### BorrowSlip

Fields:

- `slipId`
- `readerId`
- `borrowDate`
- `expectedReturnDate`
- `actualReturnDate`
- `List<String> isbnList`

### Librarian

Fields:

- `username`
- `password` (stored as a hash)

All model classes include:

- Private fields
- Constructor
- Getters and setters
- `toString()` implementation

## Data Format (Pipe-Delimited)

Each line represents one record, and fields are separated by `|`.

Example record (Reader):

```text
R001|Nguyen Van A|012345678901|2001-05-12|Male|a@gmail.com|Ha Noi|2026-01-01|2030-01-01
```

## Build and Run

### Bash

```bash
javac -d out $(find src -name "*.java")
java -cp out App
```

### PowerShell (Windows)

```powershell
$files = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
javac -d out $files
java -cp out App
```

## Recommended Next Steps

1. Add serialization/deserialization methods for each model.
2. Integrate services with `FileManager` for load/save workflows.
3. Implement full CRUD screens for readers, books, and borrowing/return operations.
