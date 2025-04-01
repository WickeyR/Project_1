# Bank Management System – OOP Project - NICHOLAS PEREZ | Ricky Franco

## 1. Project Overview

### Title:
**Bank Management System**

### Description:
This project aims to develop a **secure and efficient banking application** using **Object-Oriented Programming (OOP)** principles. The system will allow users to **manage bank accounts, perform transactions, and generate reports** while ensuring data integrity, security, and scalability.

### Key Functionalities:
- **model.User Management:** Secure authentication system with role-based access control (customers and bank staff).
- **model.Account Handling:** Creation, updating, and deletion of different account types.
- **model.Transaction Processing:** Real-time fund transfers, deposits, and withdrawals with validation.
- **Reporting & Analytics:** model.Transaction history, balance inquiries, and statement generation.

---

## 2. Key Features

### 1. model.User Authentication
- Secure login system using **password hashing (e.g., bcrypt, SHA-256)**.
- **Role-based access control**: Different functionalities for customers and bank employees.
- **Session management**: Automatic logout after inactivity for security.

### 2. Core Functionalities
- **model.Account Management**
  - Customers can create, update, or delete **Checking or Savings accounts**.
  - Bank staff can view and manage customer accounts.
- **model.Transaction Processing**
  - **Deposits, withdrawals, and fund transfers** with real-time updates.
  - **model.Transaction logs** to track and validate account activities.
  - **Overdraft protection** for specific account types.
- **Reporting & Analysis**
  - Customers can generate **account statements** and view transaction histories.
  - Bank employees can access **user statistics and financial summaries**.

### 3. Data Processing & Management
- **Database:** Uses **MySQL/PostgreSQL** to store structured financial data.
- **CRUD operations** for managing user and transaction records.
- **Security Measures:**
  - **Encryption** for sensitive data (e.g., AES for passwords).
  - **Data validation** to prevent unauthorized access or fraud.

---

## 3. OOP Principles Applied

### 1. Encapsulation
- **Sensitive account data** (e.g., balance, transactions) is private.
- Access is controlled via **getter/setter methods**, preventing direct modification.

### 2. Inheritance
- **Base Class:** `BankAccount` (common attributes and methods).
- **Derived Classes:**
  - `model.SavingsAccount` (interest rate applied).
  - `CheckingAccount` (overdraft protection enabled).

### 3. Polymorphism
- **model.Transaction methods** behave differently based on account type.
- **Overriding example:** `calculateInterest()` behaves differently in `model.SavingsAccount` than in `CheckingAccount`.

### 4. Abstraction
- **Interface `TransactionInterface`** defines `deposit()`, `withdraw()`, and `transfer()`.
- Concrete classes (`CheckingAccount`, `model.SavingsAccount`) implement these methods.

---

## 4. Technologies & Tools

### 1. Programming Language:
- **Java** (main language for OOP implementation).

### 2. GUI Framework (If applicable):
- **JavaFX** or **Swing** for designing a user-friendly interface.

### 3. Database Management:
- **MySQL/PostgreSQL** for structured financial data storage.

### 4. Security & Authentication:
- **Password hashing** with bcrypt or SHA-256.
- **Role-based access control** to limit unauthorized access.

### 5. Development Tools:
- **IDE:** IntelliJ IDEA, Eclipse
- **Version Control:** Git/GitHub for source code management

---

