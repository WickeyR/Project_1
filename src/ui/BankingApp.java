package ui;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import util.DatabaseConnection;
import java.util.Objects;

public class BankingApp extends Application {

    private Stage primaryStage;
    private Scene loginScene;
    private Scene createAccountScene;
    private Scene dashboardScene;

    private final Map<String, BankingApp.UserAccount> userAccounts = new HashMap<>();
    private BankingApp.UserAccount currentUser;

    public static void main(String[] args) {
        launch(args);
        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                System.out.println("Connection Established");
                conn.close();
            } else {
                System.out.println("Connection Failed");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        // Set up mock data
        setupMockData();

        // Create scenes
        loginScene = createLoginScene();
        createAccountScene = createAccountCreationScene();

        // Set initial scene
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("SecureBank");
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/shield_icon.png"))));
        primaryStage.show();
    }

    private void setupMockData() {
        // Add a test user
        BankingApp.UserAccount testUser = new BankingApp.UserAccount("monke", "password");
        testUser.setAccountNumber("3430093770");
        testUser.setBalance(1000.00);
        userAccounts.put("monke", testUser);
    }

    private Scene createLoginScene() {
        // Main container
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setStyle("-fx-background-color: #f0f5ff;");

        // Header with logo and text
        VBox header = createHeader();

        // Tab selection
        HBox tabContainer = new HBox();
        tabContainer.setPrefWidth(400);

        Button loginTab = new Button("Log In");
        loginTab.setPrefWidth(200);
        loginTab.getStyleClass().add("tab-button");
        loginTab.getStyleClass().add("active-tab");

        Button createAccountTab = new Button("Create Account");
        createAccountTab.setPrefWidth(200);
        createAccountTab.getStyleClass().add("tab-button");

        // Set action for tab switching
        createAccountTab.setOnAction(e -> primaryStage.setScene(createAccountScene));

        tabContainer.getChildren().addAll(loginTab, createAccountTab);

        // Form fields
        VBox formContainer = new VBox(15);
        formContainer.setPadding(new Insets(20, 30, 30, 30));
        formContainer.setMaxWidth(400);
        formContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8px;");

        Label usernameLabel = new Label("Username");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.getStyleClass().add("text-field");

        Label passwordLabel = new Label("Password");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");
        passwordField.getStyleClass().add("text-field");

        Button loginButton = new Button("Log In");
        loginButton.setPrefWidth(Double.MAX_VALUE);
        loginButton.getStyleClass().add("primary-button");

        // Login functionality
        loginButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (validateLogin(username, password)) {
                currentUser = userAccounts.get(username);
                dashboardScene = createDashboardScene();
                primaryStage.setScene(dashboardScene);
            } else {
                showAlert("Invalid Login", "Username or password is incorrect.");
            }
        });

        formContainer.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField, loginButton);

        // Footer
        VBox footer = createFooter();

        // Add all components to main container
        mainContainer.getChildren().addAll(header, tabContainer, formContainer, footer);

        // Create scene with CSS
        Scene scene = new Scene(mainContainer, 850, 700);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

        return scene;
    }

    private Scene createAccountCreationScene() {
        // Main container
        VBox mainContainer = new VBox(20);
        mainContainer.setAlignment(Pos.TOP_CENTER);
        mainContainer.setStyle("-fx-background-color: #f0f5ff;");

        // Header with logo and text
        VBox header = createHeader();

        // Tab selection
        HBox tabContainer = new HBox();
        tabContainer.setPrefWidth(400);

        Button loginTab = new Button("Log In");
        loginTab.setPrefWidth(200);
        loginTab.getStyleClass().add("tab-button");

        Button createAccountTab = new Button("Create Account");
        createAccountTab.setPrefWidth(200);
        createAccountTab.getStyleClass().add("tab-button");
        createAccountTab.getStyleClass().add("active-tab");

        // Set action for tab switching
        loginTab.setOnAction(e -> primaryStage.setScene(loginScene));

        tabContainer.getChildren().addAll(loginTab, createAccountTab);

        // Form fields
        VBox formContainer = new VBox(15);
        formContainer.setPadding(new Insets(20, 30, 30, 30));
        formContainer.setMaxWidth(400);
        formContainer.setStyle("-fx-background-color: white; -fx-background-radius: 8px;");

        Label usernameLabel = new Label("Username");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Choose a username");
        usernameField.getStyleClass().add("text-field");

        Label passwordLabel = new Label("Password");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Choose a password");
        passwordField.getStyleClass().add("text-field");

        Label confirmPasswordLabel = new Label("Confirm Password");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm your password");
        confirmPasswordField.getStyleClass().add("text-field");

        Button createAccountButton = new Button("Create Account");
        createAccountButton.setPrefWidth(Double.MAX_VALUE);
        createAccountButton.getStyleClass().add("primary-button");

        // Account creation functionality
        createAccountButton.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showAlert("Missing Information", "Please fill in all fields.");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showAlert("Password Mismatch", "Passwords do not match.");
                return;
            }

            if (userAccounts.containsKey(username)) {
                showAlert("Username Taken", "This username is already in use.");
                return;
            }

            // Create new account
            BankingApp.UserAccount newAccount = new BankingApp.UserAccount(username, password);
            newAccount.setAccountNumber(generateAccountNumber());
            newAccount.setBalance(1000.00); // Starting balance
            userAccounts.put(username, newAccount);

            showAlert("Account Created", "Your account has been created successfully!");
            primaryStage.setScene(loginScene);
        });

        formContainer.getChildren().addAll(usernameLabel, usernameField, passwordLabel, passwordField,
                confirmPasswordLabel, confirmPasswordField, createAccountButton);

        // Footer
        VBox footer = createFooter();

        // Add all components to main container
        mainContainer.getChildren().addAll(header, tabContainer, formContainer, footer);

        // Create scene with CSS
        Scene scene = new Scene(mainContainer, 850, 700);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

        return scene;
    }

    private Scene createDashboardScene() {
        // Main container
        VBox mainContainer = new VBox();
        mainContainer.setStyle("-fx-background-color: #f0f5ff;");

        // Top navigation bar
        HBox navBar = new HBox();
        navBar.setPrefHeight(60);
        navBar.setAlignment(Pos.CENTER_LEFT);
        navBar.setPadding(new Insets(0, 20, 0, 20));
        navBar.setStyle("-fx-background-color: #3366ff;");

        Label logoLabel = new Label("$ SecureBank");
        logoLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutButton = new Button("Logout");
        logoutButton.getStyleClass().add("logout-button");

        logoutButton.setOnAction(e -> {
            currentUser = null;
            primaryStage.setScene(loginScene);
        });

        navBar.getChildren().addAll(logoLabel, spacer, logoutButton);

        // Content area
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        // Welcome section
        VBox welcomeBox = new VBox(5);
        welcomeBox.setPadding(new Insets(20));
        welcomeBox.setMaxWidth(800);
        welcomeBox.setStyle("-fx-background-color: white; -fx-background-radius: 8px;");

        Label welcomeLabel = new Label("Welcome back, " + currentUser.getUsername());
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label summaryLabel = new Label("Here's your account summary");
        summaryLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666666;");

        welcomeBox.getChildren().addAll(welcomeLabel, summaryLabel);

        // Account information
        VBox accountBox = new VBox(10);
        accountBox.setPadding(new Insets(30));
        accountBox.setMaxWidth(800);
        accountBox.setStyle("-fx-background-color: #3366ff; -fx-background-radius: 8px;");

        Label accountNumberTitle = new Label("Account Number");
        accountNumberTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        Label accountNumberValue = new Label(currentUser.getAccountNumber());
        accountNumberValue.setStyle("-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;");

        Label balanceTitle = new Label("Available Balance");
        balanceTitle.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        Label balanceValue = new Label(String.format("$%.2f", currentUser.getBalance()));
        balanceValue.setStyle("-fx-text-fill: white; -fx-font-size: 36px; -fx-font-weight: bold;");

        accountBox.getChildren().addAll(accountNumberTitle, accountNumberValue, balanceTitle, balanceValue);

        // Action buttons
        HBox actionButtons = new HBox(20);
        actionButtons.setMaxWidth(800);

        VBox depositBox = new VBox(10);
        depositBox.setPadding(new Insets(20));
        depositBox.setPrefWidth(390);
        depositBox.setStyle("-fx-background-color: white; -fx-background-radius: 8px;");

        HBox depositIconContainer = new HBox();
        depositIconContainer.setPrefSize(40, 40);
        depositIconContainer.setStyle("-fx-background-color: #e0f7e6; -fx-background-radius: 20px;");
        depositIconContainer.setAlignment(Pos.CENTER);

        Label depositIcon = new Label("↓");
        depositIcon.setStyle("-fx-text-fill: #4CAF50; -fx-font-size: 20px;");

        depositIconContainer.getChildren().add(depositIcon);

        Label depositTitle = new Label("Deposit");
        depositTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label depositDescription = new Label("Add funds to your account");
        depositDescription.setStyle("-fx-text-fill: #666666;");

        depositBox.getChildren().addAll(depositIconContainer, depositTitle, depositDescription);

        depositBox.setOnMouseClicked(e -> {
            showDepositDialog();
        });

        VBox withdrawBox = new VBox(10);
        withdrawBox.setPadding(new Insets(20));
        withdrawBox.setPrefWidth(390);
        withdrawBox.setStyle("-fx-background-color: white; -fx-background-radius: 8px;");

        HBox withdrawIconContainer = new HBox();
        withdrawIconContainer.setPrefSize(40, 40);
        withdrawIconContainer.setStyle("-fx-background-color: #ffebee; -fx-background-radius: 20px;");
        withdrawIconContainer.setAlignment(Pos.CENTER);

        Label withdrawIcon = new Label("↑");
        withdrawIcon.setStyle("-fx-text-fill: #F44336; -fx-font-size: 20px;");

        withdrawIconContainer.getChildren().add(withdrawIcon);

        Label withdrawTitle = new Label("Withdraw");
        withdrawTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label withdrawDescription = new Label("Withdraw funds from your account");
        withdrawDescription.setStyle("-fx-text-fill: #666666;");

        withdrawBox.getChildren().addAll(withdrawIconContainer, withdrawTitle, withdrawDescription);

        withdrawBox.setOnMouseClicked(e -> {
            showWithdrawDialog();
        });

        actionButtons.getChildren().addAll(depositBox, withdrawBox);

        // Recent Activity
        VBox activityBox = new VBox(10);
        activityBox.setPadding(new Insets(20));
        activityBox.setMaxWidth(800);
        activityBox.setStyle("-fx-background-color: white; -fx-background-radius: 8px;");

        HBox activityHeader = new HBox(10);
        activityHeader.setAlignment(Pos.CENTER_LEFT);

        Label clockIcon = new Label("⏱");
        clockIcon.setStyle("-fx-font-size: 18px;");

        Label activityTitle = new Label("Recent Activity");
        activityTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        activityHeader.getChildren().addAll(clockIcon, activityTitle);

        Label noActivityLabel = new Label("Your transaction history will appear here");
        noActivityLabel.setStyle("-fx-text-fill: #666666;");
        noActivityLabel.setPadding(new Insets(20, 0, 20, 0));
        noActivityLabel.setAlignment(Pos.CENTER);
        noActivityLabel.setMaxWidth(Double.MAX_VALUE);

        activityBox.getChildren().addAll(activityHeader, noActivityLabel);

        content.getChildren().addAll(welcomeBox, accountBox, actionButtons, activityBox);

        // Add all components to main container
        mainContainer.getChildren().addAll(navBar, content);

        // Create scene with CSS
        Scene scene = new Scene(mainContainer, 850, 700);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());

        return scene;
    }

    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(30, 20, 20, 20));
        header.setStyle("-fx-background-color: #3366ff;");

        // Shield logo
        SVGPath shieldIcon = new SVGPath();
        shieldIcon.setContent("M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 19.93c-3.95-1.04-7-5.1-7-9.93V6.3l7-3.11v17.74z");
        shieldIcon.setFill(Color.WHITE);
        shieldIcon.setScaleX(2);
        shieldIcon.setScaleY(2);

        StackPane iconPane = new StackPane(shieldIcon);
        iconPane.setPrefHeight(60);

        Label titleLabel = new Label("SecureBank");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        Label subtitleLabel = new Label("Your trusted financial partner");
        subtitleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        header.getChildren().addAll(iconPane, titleLabel, subtitleLabel);

        return header;
    }

    private VBox createFooter() {
        VBox footer = new VBox(5);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20));

        Label copyrightLabel = new Label("© 2023 SecureBank. All rights reserved.");
        copyrightLabel.setStyle("-fx-text-fill: #666666;");

        Label taglineLabel = new Label("Secure. Reliable. Trusted.");
        taglineLabel.setStyle("-fx-text-fill: #666666;");

        footer.getChildren().addAll(copyrightLabel, taglineLabel);

        return footer;
    }

    private void showDepositDialog() {
        // Create dialog
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Deposit Funds");
        dialog.setHeaderText("Enter deposit amount");

        // Set the button types
        ButtonType depositButtonType = new ButtonType("Deposit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(depositButtonType, ButtonType.CANCEL);

        // Create the amount field
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        grid.add(new Label("Amount:"), 0, 0);
        grid.add(amountField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the amount field by default
        amountField.requestFocus();

        // Convert the result to a double when the deposit button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == depositButtonType) {
                try {
                    return Double.parseDouble(amountField.getText());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
            return null;
        });

        // Show dialog and process result
        dialog.showAndWait().ifPresent(amount -> {
            if (amount > 0) {
                currentUser.setBalance(currentUser.getBalance() + amount);
                dashboardScene = createDashboardScene(); // Refresh dashboard
                primaryStage.setScene(dashboardScene);
                showAlert("Deposit Successful", String.format("$%.2f has been added to your account.", amount));
            } else {
                showAlert("Invalid Amount", "Please enter a valid amount greater than zero.");
            }
        });
    }

    private void showWithdrawDialog() {
        // Create dialog
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Withdraw Funds");
        dialog.setHeaderText("Enter withdrawal amount");

        // Set the button types
        ButtonType withdrawButtonType = new ButtonType("Withdraw", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(withdrawButtonType, ButtonType.CANCEL);

        // Create the amount field
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField amountField = new TextField();
        amountField.setPromptText("Amount");

        grid.add(new Label("Amount:"), 0, 0);
        grid.add(amountField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        // Request focus on the amount field by default
        amountField.requestFocus();

        // Convert the result to a double when the withdraw button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == withdrawButtonType) {
                try {
                    return Double.parseDouble(amountField.getText());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
            return null;
        });

        // Show dialog and process result
        dialog.showAndWait().ifPresent(amount -> {
            if (amount <= 0) {
                showAlert("Invalid Amount", "Please enter a valid amount greater than zero.");
            } else if (amount > currentUser.getBalance()) {
                showAlert("Insufficient Funds", "You do not have enough funds for this withdrawal.");
            } else {
                currentUser.setBalance(currentUser.getBalance() - amount);
                dashboardScene = createDashboardScene(); // Refresh dashboard
                primaryStage.setScene(dashboardScene);
                showAlert("Withdrawal Successful", String.format("$%.2f has been withdrawn from your account.", amount));
            }
        });
    }

    private boolean validateLogin(String username, String password) {
        if (userAccounts.containsKey(username)) {
            BankingApp.UserAccount account = userAccounts.get(username);
            return account.getPassword().equals(password);
        }
        return false;
    }

    private String generateAccountNumber() {
        // Simple account number generator for demo purposes
        return "34" + (int)(Math.random() * 90000000 + 10000000);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // User account model
    private static class UserAccount {
        private final String username;
        private final String password;
        private String accountNumber;
        private double balance;

        public UserAccount(String username, String password) {
            this.username = username;
            this.password = password;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getAccountNumber() {
            return accountNumber;
        }

        public void setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
        }

        public double getBalance() {
            return balance;
        }

        public void setBalance(double balance) {
            this.balance = balance;
        }
    }
}





