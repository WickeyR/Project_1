package ui;

import model.User;

import java.time.LocalDate;
import java.util.Scanner;
public class RegistrationUI {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("**********************************");
        System.out.println("Welcome to the Bank Registration Portal");
        System.out.println("Please fill out the following information:");
        System.out.println("**********************************");

        // Prompt for first name
        System.out.print("First Name: ");
        String firstName = scanner.nextLine();

        // Prompt for last name
        System.out.print("Last Name: ");
        String lastName = scanner.nextLine();

        // Prompt for email
        System.out.print("Email: ");
        String email = scanner.nextLine();

        // Prompt for phone number (as text to accommodate any formatting)
        System.out.print("Phone Number: ");
        String phoneNumber = scanner.nextLine();

        // Prompt for address
        System.out.print("Address: ");
        String address = scanner.nextLine();

        // Prompt for username
        System.out.print("Username: ");
        String username = scanner.nextLine();

        // Prompt for password
        System.out.print("Password: ");
        String password = scanner.nextLine();

        // Prompt for date of birth (the format should match your parsing logic)
        System.out.print("Date of Birth (yyyy-MM-dd): ");
        String dobInput = scanner.nextLine();
        LocalDate dateOfBirth = null;
        try {
            dateOfBirth = LocalDate.parse(dobInput);
        } catch (Exception e) {
            System.out.println("Invalid date format. Please enter the date as yyyy-MM-dd.");
            System.exit(0);
        }

        // Create an instance of your service.AuthenticationService
        AuthenticationService authService = new AuthenticationService();

        // Call the registration method (adjust the method signature as needed)
        User newUser = authService.registerUser(firstName, lastName, dateOfBirth, email,
                phoneNumber, address, username, password);

        if (newUser != null) {
            System.out.println("Registration successful! Welcome, " + newUser.getUsername() + "!");
        } else {
            System.out.println("Registration failed. Username or email might already exist.");
        }

        scanner.close();
    }}
}
