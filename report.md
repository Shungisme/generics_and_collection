# WORK PROGRESS REPORT

## Topic

Library Management System

## PART 1 — STUDENT INFORMATION

| Item              | Information                  |
| ----------------- | ---------------------------- |
| Student ID        | 22120118                     |
| Full Name         | Vong Sau Hung                |
| Class             | CQ2024-7                     |
| Course            | Java Application Programming |
| Lecturer          | MSc Nguyen Van Khiet         |
| AI Usage Rate (%) | 30%                          |

## PART 2 — AI USAGE DECLARATION (Form B)

### 2.1 AI Tools Used

| AI tool, version, and platform                                   | Access time (date, hour) | Prompt used                                                                           | Purpose of use                                              | AI-generated content                                                                                                       | Student's own work / edits / validation                                                                                    |
| ---------------------------------------------------------------- | ------------------------ | ------------------------------------------------------------------------------------- | ----------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------- |
| GitHub Copilot (GPT-5.3-Codex), integrated in Visual Studio Code | 16/04/2026, 20:10        | Generate sample data files for a Library Management System using pipe "\|" delimiter. | Generated sample datasets for the library system data files | Generated sample content for `librarians.txt`, `readers.txt`, `books.txt`, `borrowslips.txt` in `\|`-delimited format      | Reviewed all generated data, corrected date values, checked consistency across readers, books, and borrow/return scenarios |
| GitHub Copilot (GPT-5.3-Codex), integrated in Visual Studio Code |                          |                                                                                       | Accelerated repeated code patterns using code suggestions   | Suggested code skeletons for model/service/UI (attributes, getters/setters, constructors, panel skeletons, basic handlers) | Reviewed all suggested code, adjusted business logic, refactored where needed, and manually tested before commit           |

### 2.2 AI Citation

- Copilot. GPT-5.3-Codex, GitHub via Visual Studio Code, accessed 20:10 on April 17, 2026, prompt: "Generate sample data files for a Library Management System using pipe "|" delimiter.", used to create sample datasets for the library system; AI generated draft records for `librarians.txt`, `readers.txt`, `books.txt`, `borrowslips.txt`; student reviewed, corrected date values, and validated scenario consistency.

### 2.3 Required Evidence Checklist (attached appendix)

Appendix: prompt history evidence for sample data generation:

![AI history prompt gen data files - 01](images/ai-history-01.png)

![AI history prompt gen data files - 02](images/ai-history-02.png)

## PART 3 — FEATURE EVALUATION TABLE

| No. | Feature                                                                                                                                                                                                                       | Completion Status | Notes | Git Commits                                                  |
| --- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ----------------- | ----- | ------------------------------------------------------------ |
| 1   | Create librarian account.                                                                                                                                                                                                     |                   |       | ![feat-auth](images/feat-auth.png)                           |
| 2   | Login, logout.                                                                                                                                                                                                                |                   |       | ![feat-auth](images/feat-auth.png)                           |
| 3   | Reader management.<br>1. View reader list in the library<br>1. Add a reader<br>1. Edit reader information<br>1. Delete reader information<br>1. Search reader by ID card/CCCD<br>1. Search reader by full name                |                   |       | ![feat-reader-management](images/feat-reader-management.png) |
| 4   | Book management<br>1. View all books in the library<br>1. Add a book<br>1. Edit book information<br>1. Delete a book<br>1. Search book by ISBN<br>1. Search book by title                                                     |                   |       | ![feat-book-management](images/feat-book-management.png)     |
| 5   | Create borrow slip                                                                                                                                                                                                            |                   |       | ![feat-borrow-slip](images/feat-borrow-slip.png)             |
| 6   | Create return slip                                                                                                                                                                                                            |                   |       | ![feat-return-slip](images/feat-return-slip.png)             |
| 7   | Basic statistics<br>1. Total number of books in library<br>1. Number of books by genre<br>1. Total number of readers<br>1. Number of readers by gender<br>1. Number of currently borrowed books<br>1. List of overdue readers |                   |       | ![feat-statistics](images/feat-statistics.png)               |
