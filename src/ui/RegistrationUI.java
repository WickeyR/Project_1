package ui;

import DAO.AccountDAO;
import DAO.UserDAO;
import model.Account;
import model.User;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import service.AuthenticationService;
import util.DatabaseConnection;

public class RegistrationUI {

    public static void main(String[] args) throws SQLException {


        try {
            System.out.println("Attempting to connect to database...");
            //Test connection before
            Connection Connection = DatabaseConnection.getConnection();
            if (Connection != null) {
                System.out.println("Connection Established");
            }else{
                //If connection failed, quit program
                System.out.println("Connection Failed");
                System.exit(0);
            }
            //Catch SQL connection
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        //Start main menu
        MainMenu();

    }


    public static void MainMenu() throws SQLException {

        Scanner scanner = new Scanner(System.in);
        System.out.println("**********************************");
        System.out.println("Welcome to the Bank");
        System.out.println("**********************************");

        System.out.println("\nHow would you like to proceed? ");

        System.out.println("1. Create Account");
        System.out.println("2. Login");
        System.out.println("3. Exit\n");

        System.out.print("Enter your choice: ");
        switch (scanner.nextInt()){
            case 1:
                createUser();
                break;
                case 2:
                    login();
                    break;
            case 3:
                System.exit(0);
                break;

        }
    }


    public static void login() throws SQLException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("**********************************");
        System.out.println("Welcome to the Login Screen");
        System.out.println("**********************************");

        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        while(true){
            System.out.print("Enter your password: ");
            String password = scanner.nextLine();



            //Attempt to login user
            if(AuthenticationService.login(username, password)){
                //Continue with true case
                System.out.println("Login Successful");
                System.out.println("Sending you to home screen");

                //Send the currentUser to their menu
                User currentUser = UserDAO.getUserByUsername(username);
                userHomeScreen(currentUser);
                break;
            }else{
                //Continue on false case
                System.out.println("Login Failed, try again");
            }
        }


    }
    public static void createUser() throws SQLException {

        System.out.println("**********************************");
        System.out.println("Welcome to the Bank Registration Portal");
        System.out.println("Please fill out the following information:");
        System.out.println("**********************************");


        Scanner scanner = new Scanner(System.in);
        // Prompt for first name
        System.out.print("First Name: ");
        String firstName = scanner.nextLine();

        // Prompt for last name
        System.out.print("Last Name: ");
        String lastName = scanner.nextLine();

//        // Prompt for email
//        System.out.print("Email: ");
//        String email = scanner.nextLine();
//
//        // Prompt for phone number (as text to accommodate any formatting)
//        System.out.print("Phone Number: ");
//        int phoneNumber = scanner.nextInt();
//        scanner.nextLine();
//
//        // Prompt for address
//        System.out.print("Address: ");
//        String address = scanner.nextLine();

        // Prompt for username
        System.out.print("Username: ");
        String username = scanner.nextLine();

        // Prompt for password
        System.out.print("Password: ");
        String password = scanner.nextLine();
//
//        // Prompt for date of birth (the format should match your parsing logic)
//        System.out.print("Date of Birth (yyyyMMdd): ");
//        int dobInput = scanner.nextInt();


        // Create an instance of your service.AuthenticationService
        AuthenticationService authService = new AuthenticationService();

        // Call the registration method (adjust the method signature as needed)
        User newUser = authService.registerUser(firstName, lastName, username, password);

        if (newUser != null) {
            System.out.println("Registration successful! Sending you to login screen.");
            login();
        } else {
            System.out.println("Registration failed. Username or email might already exist.");
        }

        scanner.close();
    }

    public static void userHomeScreen(User currentUser) {
        boolean sessionActive = true;

        // Display the different options for the user
        System.out.println("\n\n-----------------------------");
        System.out.println("---------Your Account--------");
        System.out.println("-----------------------------\n");

        System.out.println("1 - View Account Balance");
        System.out.println("2 - Deposit or Withdraw money");
        System.out.println("3 - Send Money");
        System.out.println("4 - Make Changes To Your Account");
        System.out.println("5 - Exit");
        System.out.println("\nMake your choice: ");

        // Grab the users choice
        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();

        // The actions for each choice
        switch (choice){

            case 1:
                viewAccountBalance(currentUser);
                break;

            case 2:
                updateBalance(currentUser);
                break;
        }
    }



    public static void viewAccountBalance(User currentUser) {
        try {
            AccountDAO dao = new AccountDAO();
            List<Account> accounts = dao.getAccountsByUser(currentUser.getUserId());

            System.out.println("\n-- Your Accounts --");
            for (Account acct : accounts) {
                System.out.printf(
                        "%s (ID %d): $%.2f%n",
                        acct.getACCOUNT_TYPE(),
                        acct.getACCOUNT_NUMBER(),
                        acct.getBalance()
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        userHomeScreen(currentUser);
    }

    public static void updateBalance(User currentUser) {
        Scanner scanner = new Scanner(System.in);
        try {
            AccountDAO dao = new AccountDAO();
            List<Account> accounts = dao.getAccountsByUser(currentUser.getUserId());

            System.out.println("\nChoose an account:");
            for (int i = 0; i < accounts.size(); i++) {
                Account a = accounts.get(i);
                System.out.printf("%d) %s (ID %d) – $%.2f%n",
                        i+1, a.getACCOUNT_TYPE(), a.getACCOUNT_NUMBER(), a.getBalance());
            }
            System.out.print("Enter choice: ");
            int idx = scanner.nextInt() - 1;
            Account chosen = accounts.get(idx);

            System.out.print("D)eposit or W)ithdraw? ");
            String action = scanner.next();
            System.out.print("Amount: ");
            double amt = scanner.nextDouble();

            boolean success;
            try (Connection conn = DatabaseConnection.getConnection()) {
                if (action.equalsIgnoreCase("D")) {
                    success = dao.deposit(conn, chosen.getACCOUNT_NUMBER(), amt);
                } else {
                    success = dao.withdraw(conn, chosen.getACCOUNT_NUMBER(), amt);
                }
            }

            System.out.println(success
                    ? "Transaction successful."
                    : "Transaction failed.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        userHomeScreen(currentUser);
    }
}
