# Traveler's Rest | Java Swing POS & Management System

A **medieval tavern-themed point-of-sale and management application** built in **Java Swing**. Traveler's Rest brings food and beverage ordering, room rentals, inventory, staff accounts, and sales reporting into one desktop application.

> **Status:** Coursework / desktop application prototype. Uses local text/CSV-style files; no external database or third-party dependencies.

## Features

| Area | Features |
| --- | --- |
| **Login and roles** | Admin and staff sign-in; first-run admin account creation; account credentials stored with salted PBKDF2 for newly created users |
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

## Project structure

```text
travelers-rest-pos/
├── src/
│   └── oopproject/
│       ├── Main.java                 # Application entry point
│       ├── LogIn.java                # Login + initial admin setup
│       ├── EmployeeSession.java      # Active user and role
│       ├── Admin.java                # Admin control panel
│       ├── AdminData.java            # Shared local data operations
│       ├── Dashboard.java            # Staff POS dashboard
│       ├── Menu.java                 # Food, drinks, and rentals
│       ├── CheckoutArea.java         # Shopping cart
│       ├── Receipt.java              # Receipt and cash checkout
│       ├── Inventory.java            # Inventory screen
│       ├── Room.java                 # Room management screen
│       ├── Employee.java             # Staff profile and sales
│       ├── RecentTransaction.java    # Recent sales component
│       ├── TransactionHistory.java   # History screen
│       └── TransactionSorting.java   # History filters and sorting
├── assets/                            # Artwork added locally
├── data/                              # Generated files (Git ignored)
├── sources.txt                        # Build input list
├── run.bat                            # Windows build + run
├── run.sh                             # macOS/Linux build + run
└── README.md
```

## How it works

1. Sign in as an **admin** or **staff** member.
2. Staff select food, drinks, or available rooms and add them to the checkout cart.
3. Checkout shows item subtotals, **5% sales tax**, total due, and cash/change.
4. Completing checkout saves transaction details, updates stock, and clears the cart; room purchases also update occupancy.
5. Administrators can view sales and oversee inventory, rooms, notifications, and staff accounts.

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
