package ui;

import DAO.AccountDAO;
import DAO.TransactionDAO;
import DAO.UserDAO;
import model.Account;
import model.Transaction;
import model.User;
import service.AccountService;
import service.AuthenticationService;
import util.DatabaseConnection;

import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class RegistrationUI {

    private static final Scanner SC = new Scanner(System.in);

    public static void main(String[] args) {
        try (Connection c = DatabaseConnection.getConnection()) {
            System.out.println("Attempting to connect to database...");
            System.out.println(c != null ? "Connection Established" : "Connection Failed");
            if (c == null) System.exit(0);
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }
        mainMenuLoop();
    }

    private static void mainMenuLoop() {
        while (true) {
            System.out.println("**********************************");
            System.out.println("Welcome to the Bank");
            System.out.println("**********************************");
            System.out.println("\nHow would you like to proceed? ");
            System.out.println("1. Create Account");
            System.out.println("2. Login");
            System.out.println("3. Exit\n");
            System.out.print("Enter your choice: ");

            String choice = SC.nextLine().trim();
            switch (choice) {
                case "1" -> createUser();
                case "2" -> login();
                case "3" -> System.exit(0);
                default  -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void login() {
        System.out.println("**********************************");
        System.out.println("Welcome to the Login Screen");
        System.out.println("**********************************");
        System.out.print("Enter your username: ");
        String username = SC.nextLine().trim();

        while (true) {
            System.out.print("Enter your password: ");
            String password = SC.nextLine();
            if (AuthenticationService.login(username, password)) {
                System.out.println("Login Successful");
                try {
                    User currentUser = UserDAO.getUserByUsername(username);
                    userScreenLoop(currentUser);
                    break;
                } catch (SQLException e) {
                    e.printStackTrace();
                    break;
                }
            } else {
                System.out.println("Login Failed, try again");
            }
        }
    }

    private static void createUser() {
        System.out.println("**********************************");
        System.out.println("Welcome to the Bank Registration Portal");
        System.out.println("Please fill out the following information:");
        System.out.println("**********************************");

        System.out.print("First Name: ");
        String firstName = SC.nextLine().trim();
        System.out.print("Last Name: ");
        String lastName = SC.nextLine().trim();
        System.out.print("Username: ");
        String username = SC.nextLine().trim();
        System.out.print("Password: ");
        String password = SC.nextLine();

        if (firstName.isEmpty() || lastName.isEmpty() || username.isEmpty() || password.isEmpty()) {
            System.out.println("All fields are required.");
            return;
        }

        AuthenticationService authService = new AuthenticationService();
        User newUser = authService.registerUser(firstName, lastName, username, password);

        if (newUser == null) {
            System.out.println("Registration failed. Username might already exist.");
            return;
        }

        System.out.print("Open a savings account now at 4% APR? (Y/N): ");
        if (SC.nextLine().trim().equalsIgnoreCase("Y")) {
            System.out.print("How much would you like to move from your checking to savings? ");
            double amt;
            try { amt = Double.parseDouble(SC.nextLine()); }
            catch (NumberFormatException ex) { System.out.println("Invalid amount."); return; }

            if (amt <= 0) { System.out.println("Amount must be positive."); return; }

            try (Connection conn = DatabaseConnection.getConnection()) {
                AccountDAO acctDao = new AccountDAO();
                Integer chkId = acctDao.getCheckingIdForUser(newUser.getUserId(), conn);
                if (chkId == null) {
                    System.out.println("Could not locate your checking account.");
                } else {
                    boolean ok = new AccountService().openSavingsFromChecking(newUser.getUserId(), chkId, amt);
                    System.out.println(ok ? "Savings account created!" : "Could not open savings account.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        login();
    }

    private static void userScreenLoop(User currentUser) {
        while (true) {
            System.out.println("\n\n-----------------------------");
            System.out.println("---------Your Account--------");
            System.out.println("-----------------------------\n");

            System.out.println("1 - View Account Balance");
            System.out.println("2 - Deposit or Withdraw money");
            System.out.println("3 - View Recent Activity");
            System.out.println("4 - Send Money");
            System.out.println("5 - Manage Account");
            System.out.println("6 - Exit");
            System.out.print("\nMake your choice: ");

            String choice = SC.nextLine().trim();
            switch (choice) {
                case "1" -> viewBalances(currentUser);
                case "2" -> depositWithdraw(currentUser);
                case "3" -> viewRecent(currentUser);
                case "4" -> sendMoneyFlow(currentUser);
                case "5" -> manageAccountFlow(currentUser);
                case "6" -> {
                    // back to main menu
                    return;
                }
                default  -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void sendMoneyFlow(User user) {
        System.out.print("Recipient’s username: ");
        String recipient = SC.nextLine().trim();

        System.out.print("Amount to send: ");
        double amt;
        try {
            amt = Double.parseDouble(SC.nextLine());
            if (amt <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
            return;
        }

        try {
            boolean ok = new AccountService()
                    .transfer(user.getUsername(), recipient, amt);
            System.out.println(ok
                    ? "Transfer successful."
                    : "Transfer failed (insufficient funds?).");
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }




    private static void adminScreenLoop(User admin) {
        while (true) {
            System.out.println("\n\n===== ADMIN CONSOLE =====");
            System.out.println("1) List all users");
            System.out.println("2) List all accounts");
            System.out.println("3) List all transactions");
            System.out.println("4) Create new ADMIN");
            System.out.println("5) Logout");
            System.out.print("Choice: ");

            String c = SC.nextLine().trim();
            switch (c) {
                case "1" -> listAllUsers();
                case "2" -> listAllAccounts();
                case "3" -> listAllTransactions();
                case "4" -> createAdminFlow();
                case "5" -> { return; }
                default  -> System.out.println("Invalid choice.");
            }
        }
    }

    private static void listAllUsers() {
        System.out.println("\n-- All Users --");
        String sql = "SELECT ID, first_name, last_name, username, role FROM users";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf(
                        "%d: %s %s (username=%s, role=%s)%n",
                        rs.getInt("ID"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("username"),
                        rs.getString("role")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error listing users: " + e.getMessage());
        }
    }

    private static void listAllAccounts() {
        System.out.println("\n-- All Accounts --");
        String sql = """
          SELECT account_id, user_id, account_type, balance, interest_rate
            FROM account
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf(
                        "Acct %d | User %d | %-8s | $%,.2f | rate=%s%n",
                        rs.getInt("account_id"),
                        rs.getInt("user_id"),
                        rs.getString("account_type"),
                        rs.getDouble("balance"),
                        rs.getObject("interest_rate") == null
                                ? "n/a"
                                : String.format("%.2f%%", rs.getDouble("interest_rate")*100)
                );
            }
        } catch (SQLException e) {
            System.out.println("Error listing accounts: " + e.getMessage());
        }
    }

    private static void listAllTransactions() {
        System.out.println("\n-- All Transactions --");
        String sql = """
          SELECT transaction_id, account_id, transaction_type,
                 amount, time_stamp, description, status
            FROM transactions
           ORDER BY time_stamp DESC
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                System.out.printf(
                        "%d | acct %d | %-10s | $%,.2f | %s | %s | %s%n",
                        rs.getInt("transaction_id"),
                        rs.getInt("account_id"),
                        rs.getString("transaction_type"),
                        rs.getDouble("amount"),
                        rs.getTimestamp("time_stamp"),
                        rs.getString("description"),
                        rs.getString("status")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error listing transactions: " + e.getMessage());
        }
    }

    private static void createAdminFlow() {
        System.out.println("\n-- Create New ADMIN --");
        System.out.print("First Name: ");
        String fn = SC.nextLine().trim();
        System.out.print("Last Name: ");
        String ln = SC.nextLine().trim();
        System.out.print("Username: ");
        String un = SC.nextLine().trim();
        System.out.print("Password: ");
        String pw = SC.nextLine();

        if (fn.isEmpty() || ln.isEmpty() || un.isEmpty() || pw.isEmpty()) {
            System.out.println("All fields are required.");
            return;
        }

        try {
            String hashed = AuthenticationService.encryptPassword(pw);
            String sql = """
              INSERT INTO users
                (first_name, last_name, username, password_hash, role)
              VALUES (?, ?, ?, ?, 'ADMIN')
            """;
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, fn);
                ps.setString(2, ln);
                ps.setString(3, un);
                ps.setString(4, hashed);
                int rows = ps.executeUpdate();
                System.out.println(rows == 1
                        ? "Admin created."
                        : "Failed to create admin."
                );
            }
        } catch (SQLException | NoSuchAlgorithmException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void viewBalances(User user) {
        try {
            AccountDAO dao = new AccountDAO();
            List<Account> accts = dao.getAccountsByUser(user.getUserId());
            if (accts.isEmpty()) System.out.println("No accounts.");
            else {
                System.out.println("\n-- Your Accounts --");
                for (Account a : accts)
                    System.out.printf("%s (ID %d): $%.2f%n", a.getACCOUNT_TYPE(), a.getACCOUNT_NUMBER(), a.getBalance());
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
    private static void manageAccountFlow(User user) {
        while (true) {
            System.out.println("\n---- Manage Account ----");
            System.out.println("1. Open Savings Account");
            System.out.println("2. Close Account");
            System.out.println("3. Change Password");
            System.out.println("4. Open Checking Account");
            System.out.println("5. Back");
            System.out.print("Enter your choice: ");

            String c = SC.nextLine().trim();
            switch (c) {
                case "1" -> openSavingsFlow(user);
                case "2" -> closeAccountFlow(user);
                case "3" -> changePasswordFlow(user);
                case "4" -> openCheckingFlow(user);
                case "5" -> {
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }
    private static void openCheckingFlow(User user) {
        System.out.println("---- Open New Checking Account ----");
        System.out.print("Initial deposit amount (or 0): ");
        double amt;
        try {
            amt = Double.parseDouble(SC.nextLine());
            if (amt < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            // 1) create the new checking account
            AccountService svc = new AccountService();
            svc.openDefaultChecking(user.getUserId(), conn);
            // 2) if they want to seed it, deposit from an existing checking
            if (amt > 0) {
                AccountDAO acctDao = new AccountDAO();
                Integer newId = acctDao.getCheckingIdForUser(user.getUserId(), conn);
                if (newId == null) {
                    System.out.println("Could not find newly created checking account.");
                } else {
                    // assume they use their *first* checking to fund the new one
                    Integer srcId = acctDao.getCheckingIdForUser(user.getUserId(), conn);
                    if (srcId.equals(newId)) {
                        System.out.println("No other checking to fund from. Please deposit manually.");
                    } else if (acctDao.withdraw(conn, srcId, amt) &&
                            acctDao.deposit(conn, newId, amt)) {
                        System.out.println("Deposited $" + amt + " into account ID " + newId);
                    } else {
                        System.out.println("Could not transfer funds automatically. Please deposit manually.");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Error opening checking: " + e.getMessage());
        }
    }


    private static void changePasswordFlow(User user) {
        System.out.println("---- Change Password ----");
        System.out.print("Enter current password: ");
        String oldPw = SC.nextLine();
        System.out.print("Enter new password: ");
        String newPw = SC.nextLine();
        System.out.print("Confirm new password: ");
        String confirm = SC.nextLine();

        if (!newPw.equals(confirm)) {
            System.out.println("Passwords do not match.");
            return;
        }
        if (newPw.isEmpty()) {
            System.out.println("New password cannot be empty.");
            return;
        }

        boolean ok = new AuthenticationService()
                .changePassword(user.getUserId(), oldPw, newPw);
        if (ok) {
            System.out.println("Password changed. Please log in again.");
            login();
        }}

    private static void depositWithdraw(User user) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            AccountDAO acctDao = new AccountDAO();
            TransactionDAO txDao = new TransactionDAO();
            List<Account> accts = acctDao.getAccountsByUser(user.getUserId());
            if (accts.isEmpty()) { System.out.println("No accounts."); return; }

            for (int i = 0; i < accts.size(); i++)
                System.out.printf("%d) %s (ID %d) – $%.2f%n",
                        i + 1, accts.get(i).getACCOUNT_TYPE(), accts.get(i).getACCOUNT_NUMBER(), accts.get(i).getBalance());

            System.out.print("Enter choice: ");
            int idx = Integer.parseInt(SC.nextLine()) - 1;
            if (idx < 0 || idx >= accts.size()) { System.out.println("Bad choice."); return; }
            Account chosen = accts.get(idx);

            System.out.print("D)eposit or W)ithdraw? ");
            String action = SC.nextLine().trim();
            System.out.print("Amount: ");
            double amt = Double.parseDouble(SC.nextLine());
            if (amt <= 0) { System.out.println("Amount must be positive."); return; }

            boolean success = false;

            if (action.equalsIgnoreCase("D") && "SAVING".equals(chosen.getACCOUNT_TYPE())) {
                Integer chkId = acctDao.getCheckingIdForUser(user.getUserId(), conn);
                if (chkId == null) { System.out.println("Need a checking account."); return; }
                if (!acctDao.withdraw(conn, chkId, amt)) { System.out.println("Insufficient funds."); return; }
                if (acctDao.deposit(conn, chosen.getACCOUNT_NUMBER(), amt)) {
                    txDao.logTransaction(conn, chkId, "WITHDRAWAL", amt, "Transfer to savings", "COMPLETED");
                    txDao.logTransaction(conn, chosen.getACCOUNT_NUMBER(), "DEPOSIT", amt, "Transfer from checking", "COMPLETED");
                    success = true;
                }
            } else {
                if (action.equalsIgnoreCase("D"))
                    success = acctDao.deposit(conn, chosen.getACCOUNT_NUMBER(), amt);
                else
                    success = acctDao.withdraw(conn, chosen.getACCOUNT_NUMBER(), amt);

                if (success)
                    txDao.logTransaction(conn, chosen.getACCOUNT_NUMBER(),
                            action.equalsIgnoreCase("D") ? "DEPOSIT" : "WITHDRAWAL",
                            amt, null, "COMPLETED");
            }
            System.out.println(success ? "Transaction successful." : "Transaction failed.");
        } catch (SQLException | NumberFormatException | InputMismatchException e) {
            System.out.println("Invalid input.");
        }
    }

    private static void viewRecent(User user) {
        try {
            AccountDAO acctDao = new AccountDAO();
            TransactionDAO txDao = new TransactionDAO();
            List<Account> accts = acctDao.getAccountsByUser(user.getUserId());
            if (accts.isEmpty()) { System.out.println("No accounts."); return; }

            for (int i = 0; i < accts.size(); i++)
                System.out.printf("%d) %s (ID %d)%n", i + 1, accts.get(i).getACCOUNT_TYPE(), accts.get(i).getACCOUNT_NUMBER());
            System.out.print("Enter choice: ");
            int idx = Integer.parseInt(SC.nextLine()) - 1;
            if (idx < 0 || idx >= accts.size()) { System.out.println("Bad choice."); return; }
            int acctId = accts.get(idx).getACCOUNT_NUMBER();

            List<Transaction> txs = txDao.getTransactionsByAccount(acctId, 10);
            if (txs.isEmpty()) System.out.println("No transactions yet.");
            else {
                System.out.println("\n--- Recent Transactions ---");
                for (Transaction t : txs)
                    System.out.printf("%-4d %-10s $%-9.2f %s%n",
                            t.getTRANSACTION_NUMBER(), t.getTRANSCATION_TYPE(), t.getAMOUNT(), t.getTIME_STAMP());
            }
        } catch (SQLException | NumberFormatException e) { e.printStackTrace(); }
    }

    private static void openSavingsFlow(User user) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            AccountDAO acctDao = new AccountDAO();
            Integer chkId = acctDao.getCheckingIdForUser(user.getUserId(), conn);
            if (chkId == null) { System.out.println("Need a checking account first."); return; }
            boolean hasSav = acctDao.getAccountsByUser(user.getUserId())
                    .stream().anyMatch(a -> "SAVING".equals(a.getACCOUNT_TYPE()));
            if (hasSav) { System.out.println("You already have a savings account."); return; }

            System.out.println("Current offer: 4% APR, paid monthly.");
            System.out.print("Proceed? (Y/N) ");
            if (!SC.nextLine().trim().equalsIgnoreCase("Y")) return;

            System.out.print("Amount to move from Checking ➜ Savings: ");
            double amt = Double.parseDouble(SC.nextLine());
            if (amt <= 0) { System.out.println("Amount must be positive."); return; }

            boolean ok = new AccountService().openSavingsFromChecking(user.getUserId(), chkId, amt);
            System.out.println(ok ? "Savings account created." : "Operation failed.");
        } catch (SQLException | NumberFormatException e) { e.printStackTrace(); }
    }
    private static void closeAccountFlow(User user) {
        try {
            AccountDAO dao = new AccountDAO();
            List<Account> accts = dao.getAccountsByUser(user.getUserId());
            if (accts.isEmpty()) {
                System.out.println("No accounts to close.");
                return;
            }

            System.out.println("\nWhich account would you like to close?");
            for (int i = 0; i < accts.size(); i++) {
                Account a = accts.get(i);
                System.out.printf("%d) %s (ID %d) – $%.2f%n",
                        i + 1, a.getACCOUNT_TYPE(), a.getACCOUNT_NUMBER(), a.getBalance());
            }
            System.out.print("Enter choice: ");
            int idx = Integer.parseInt(SC.nextLine()) - 1;
            if (idx < 0 || idx >= accts.size()) {
                System.out.println("Invalid choice.");
                return;
            }
            Account chosen = accts.get(idx);

            if (chosen.getBalance() != 0.0) {
                System.out.println("Balance must be zero to close.");
                return;
            }

            System.out.print("Confirm close of account ID "
                    + chosen.getACCOUNT_NUMBER() + "? (Y/N): ");
            if (!SC.nextLine().trim().equalsIgnoreCase("Y")) {
                return;
            }

            boolean success = new AccountService()
                    .closeAccount(user.getUserId(), chosen.getACCOUNT_NUMBER());
            System.out.println(success
                    ? "Account closed."
                    : "Could not close account.");
        } catch (SQLException | NumberFormatException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }



}
