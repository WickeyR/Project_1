# **SuperBank – Console Banking Suite**

---

## Team Members
- **Ricky Franco** –   Back-end (domain model, DAO layer, business rules)
- **Nicholas Perez**   – Full-stack Developer (CLI workflow, UI Design, README)
---

## Project Description
**SuperBank** is a console-based banking application that demonstrates clean **Object-Oriented Programming (OOP)**, layered architecture, and basic security practices in Java.

Two user roles interact with the system:

- **Customers** – open checking/savings accounts, move money, view history, manage passwords.
- **Bank staff (ADMIN)** – list users, accounts and transactions, and promote other admins.

The code is organized in four layers:

| Layer       | Package(s)      | Responsibility                                                           |
|-------------|-----------------|--------------------------------------------------------------------------|
| **Model**   | `model.*`       | Immutable domain objects (`User`, `Account`, `Transaction`)              |
| **DAO**     | `DAO.*`         | Plain-SQL CRUD against an embedded H2 database                           |
| **Service** | `service.*`     | Business logic: hashing, transfers, account closure                      |
| **UI**      | `ui.*`          | Text-based interaction (`RegistrationUI`) and partial GUI (`BankingApp`) |

---

## How to Run

1. **Prerequisites**
  - **JDK 21** (or ≥ 17)
  - Any IDE (IntelliJ IDEA / Eclipse / VS Code) or plain JDK tools
  - `lib/h2-2.2.224.jar` (already committed) – _no external database needed_
  - 'JavaFx 24 Jar' (code runs on newer versions of java and javafx however, edits will be made for compatability with the current code

2. **Clone & Build**
  
- Clone the repository from the Github page

3. **Compile**

- Compile and run the program in InteliJ


4. **Create and account**
- Create an account, or multiple and perform various functions


## Features Implemented

- **Secure Authentication**
  - SHA-256 password hashing; roles **CUSTOMER** and **ADMIN**
  - Auto-bootstrapped `admin/admin`
- **Account Management**
  - Checking auto-created at sign-up
  - Open extra checking or one savings (4 % APR placeholder)
  - Close account (balance must be zero)
- **Banking Operations**
  - Deposit / Withdraw
  - Transfer between customer’s accounts
  - Send money to another user’s checking
  - Full transaction logging
- **Admin Console**
  - List every user, account, and transaction
  - Promote new admins from the UI
- **OOP Principles**
  - Encapsulation – private fields, public getters/setters
  - Inheritance – `SavingsAccount` vs. `CheckingAccount`
- **GUI (JavaFX and CSS Style)
  - Clean and sleek UI design with login, account creation, and account summary functionality
  - custom SVG icon and Colorful CSS styling
  - preset with a loaded account Username: `monke`, Password: `password123`, Balance: 1000$
  
## Future Work

- Cannot create a new admin account
- Monthly interest scheduler (background job)
- Enhanced validation & exception handling
- Concurrency control (locking) for transfers

## Known Issues / Limitations

- Single-threaded – one session at a time
- No encryption-at-rest for H2 file
- Admin listings lack pagination
- No overdraft protection yet
- Mismatch Versions for Java and Javafx prohibit functionality of the GUI where it is only fully functional on JavaJDK v.23+ with JavaFx v.24
