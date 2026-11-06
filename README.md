# Traveler's Rest 

A **medieval tavern-themed management application**. Traveler's Rest brings food and beverage ordering, room rentals, inventory, staff accounts, and sales reporting into one desktop application.

## Features

| Area | Features |
| --- | --- |
| **Login and roles** | Admin and staff sign-in; account credentials stored in a file|
| **Admin dashboard** | Sales overview, orders, tenant counts, notifications, product and stock management, room/tenant management, staff accounts, transactions |
| **Ordering** | Food, beverages, and rentable rooms; item quantities and cart management |
| **Checkout** | Receipt dialog, **5% sales tax**, cash paid and change, stock checks, order recording |
| **Inventory** | Food/drink stock viewing, sorting, stock reduction after checkout, staff-to-admin notifications |
| **Rooms** | Room availability, tenants, occupancy and overdue status |
| **Transaction history** | Recent transactions, ordering and filtering by order ID, amount, date, and item category |
| **Employee view** | Staff profile and personal recent sales |

## Built with

- **Java**

## Getting started

### Prerequisites

- JDK **17 or newer** (compiled successfully with JDK 21)
- Windows, macOS, or Linux with a Java desktop environment
- Original artwork PNGs for the intended tavern UI; see [assets/README.md](assets/README.md)

### Run on Windows

1. Download the repository ZIP and extract it.
2. Copy the six original images into the `assets/` folder, using the filenames listed there.
3. Open the extracted repository folder, and double-click **`run.bat`** (or run it in Command Prompt).

You can also run manually from the **repository root**:

```powershell
javac -encoding UTF-8 -d out @sources.txt
java -cp out oopproject.Main
```

## Important notes

- **Images not included:** The supplied source code referenced PNG files from a local Windows `Downloads` folder. The paths have been switched to `assets/`, but the PNG files must be provided separately. Without them, screens may not display the intended backgrounds.
- **Personal or private records are excluded:** Real user accounts and sales data belong in `data/` and are ignored by Git. Never commit customer records, real credentials, or private files.
- **Local data only:** This application does not use a hosted backend or database, and is intended for demonstration and learning rather than a production cash register.
- **Source adjustments for portability:** `LogIn.java` now calls the same `AdminData` account methods used by the admin panel, and `RecentTransaction.java` records new sales in the shared `data/` location.

## Potential improvements

- Replace file-based persistence with a database (e.g., SQLite/MySQL)
- Add tests for inventory changes, login, and transaction reconciliation
- Use a bundled resource-loading system for artwork
- Improve responsive layouts and error recovery during partial writes
- Add printable receipt/export support

---

**Project:** Traveler's Rest — Java Swing POS and Management System  
**Entry point:** `oopproject.Main`  
**Language:** Java
