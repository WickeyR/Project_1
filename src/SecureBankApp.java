import DAO.AccountDAO;
import DAO.TransactionDAO;
import DAO.UserDAO;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Pair;
import model.Account;
import model.Transaction;
import model.User;
import service.AccountService;
import service.AuthenticationService;
import util.DatabaseConnection;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Java-FX front-end wired to DAO/service layer and H2 database.
 */
public class SecureBankApp extends Application {

    /* ───────── fields ───────── */
    private Stage primaryStage;
    private Scene loginScene, createScene, dashboardScene;
    private Scene adminAuthScene;
    private User currentUser;

    private final AccountDAO acctDao = new AccountDAO();
    private final TransactionDAO txDao = new TransactionDAO();
    private final AccountService acctSvc = new AccountService();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        // call methods to load scenes and build application
        this.primaryStage = stage;
        loginScene = buildLoginScene();
        createScene = buildCreateScene();
        loginScene = buildLoginScene();
        createScene = buildCreateScene();
        adminAuthScene = buildAdminAuthScene();
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("SecureBank");
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/shield_icon.png"))));
        primaryStage.show();
    }

    /* ─────────── scenes ─────────── */
    private enum Tab { LOGIN, CREATE, ADMIN }

    /***
     * Builds the tab bar for login options
     * @param active The current tab active
     * @return A tab bar for each button
     */
    private HBox buildTabBar(Tab active) {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER);
        bar.setPrefWidth(600);

        //Add button options for login-types
        Button login = new Button("Log In");
        Button create = new Button("Create Account");
        Button admin  = new Button("Admin");

        for (Button b : List.of(login, create, admin)) {
            b.setPrefWidth(200);
            b.getStyleClass().add("tab-button");
        }
        switch (active) {
            //Add styles if hovering
            case LOGIN  -> login.getStyleClass().add("active-tab");
            case CREATE -> create.getStyleClass().add("active-tab");
            case ADMIN  -> admin.getStyleClass().add("active-tab");
        }

        //When pressed load appropriate scene
        login.setOnAction(e -> primaryStage.setScene(loginScene));
        create.setOnAction(e -> primaryStage.setScene(createScene));
        admin.setOnAction(e -> primaryStage.setScene(adminAuthScene));

        bar.getChildren().addAll(login, create, admin);
        return bar;
    }

    /**
     * Build the administration auth screen for the user
     * @return the complete scene
     */
    private Scene buildAdminAuthScene() {
        VBox main = new VBox(40);
        main.setAlignment(Pos.TOP_CENTER);
        main.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, "
                + "#1a237e 0%, #3949ab 50%, #5c6bc0 100%);");
        main.setPadding(new Insets(20));

        VBox head = header();
        head.setMaxWidth(600);
        main.getChildren().add(head);

        HBox tabs = buildTabBar(Tab.ADMIN);
        tabs.setMaxWidth(400);
        main.getChildren().add(tabs);

        /* small white card with the two admin buttons */
        VBox card = new VBox(15);
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(400);
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.95);"
                        + "-fx-background-radius: 8px;"
                        + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 3);");

        //Show the buttons for login or create account
        Button adminLogin = new Button("Admin Log In");
        adminLogin.setPrefWidth(Double.MAX_VALUE);
        adminLogin.getStyleClass().add("primary-button");
        adminLogin.setOnAction(e -> adminLoginFlow());

        Button createAdmin = new Button("Create New Admin");
        createAdmin.setPrefWidth(Double.MAX_VALUE);
        createAdmin.getStyleClass().add("primary-button");
        createAdmin.setOnAction(e -> createAdminDialog());

        //Add to card
        card.getChildren().addAll(adminLogin, createAdmin);
        card.setPadding(new Insets(0));
        VBox.setMargin(card, new Insets(0));
        main.getChildren().add(card);

        main.getChildren().add(footer());

        Scene scene = new Scene(main, 1200, 900);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        return scene;
    }

    /**
     * show the administration login form to the user
     */
    private void adminLoginFlow() {
        Dialog<Pair<String, String>> dlg = new Dialog<>();
        dlg.setTitle("Admin Log In");
        dlg.setHeaderText("Enter admin credentials");
        ButtonType ok = new ButtonType("Log In", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(10);
        g.setPadding(new Insets(20));
        TextField user = new TextField();

        //Grab the needed information from the users field
        user.setPromptText("Username");
        PasswordField pw = new PasswordField();
        pw.setPromptText("Password");
        g.addRow(0, new Label("Username:"), user);
        g.addRow(1, new Label("Password:"), pw);
        dlg.getDialogPane().setContent(g);

        dlg.setResultConverter(btn -> {
            if (btn == ok) return new Pair<>(user.getText(), pw.getText());
            return null;
        });

        dlg.showAndWait().ifPresent(creds -> {
            String u = creds.getKey(), p = creds.getValue();
            try {
                if (AuthenticationService.login(u, p)) {
                    User maybe = UserDAO.getUserByUsername(u);
                    if (maybe != null && maybe.isAdmin()) {
                        currentUser = maybe;
                        primaryStage.setScene(buildAdminScene());
                    } else {
                        showAlert("Denied", "Not an admin user.");
                    }
                } else {
                    showAlert("Login failed", "Invalid credentials.");
                }
            } catch (SQLException ex) {
                showAlert("Error", ex.getMessage());
            }
        });
    }

    /**
     * Build the administration main console
     * @return the console
     */
    private Scene buildAdminScene() {
        VBox main = new VBox();
        main.setStyle("-fx-background-color:#f0f5ff;");

        main.getChildren().add(createHeader());


        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        main.getChildren().add(content);

        //Add a unique card to the admin console
        VBox adminCard = card("⚙️ Admin Console", "");
        adminCard.setMaxWidth(800);

        //provide buttons to view admin options
        Button btnUsers = new Button("List All Users");
        btnUsers.setPrefWidth(Double.MAX_VALUE);
        btnUsers.getStyleClass().add("primary-button");
        btnUsers.setOnAction(e -> listAllUsers());

        Button btnAccts = new Button("List All Accounts");
        btnAccts.setPrefWidth(Double.MAX_VALUE);
        btnAccts.getStyleClass().add("primary-button");
        btnAccts.setOnAction(e -> listAllAccounts());

        Button btnTx = new Button("List All Transactions");
        btnTx.setPrefWidth(Double.MAX_VALUE);
        btnTx.getStyleClass().add("primary-button");
        btnTx.setOnAction(e -> listAllTransactions());

        Button btnNewAdmin = new Button("Create New ADMIN");
        btnNewAdmin.setPrefWidth(Double.MAX_VALUE);
        btnNewAdmin.getStyleClass().add("primary-button");
        btnNewAdmin.setOnAction(e -> createAdminDialog());

        //Add all of the buttons to the card
        adminCard.getChildren().addAll(btnUsers, btnAccts, btnTx, btnNewAdmin);
        content.getChildren().add(adminCard);

        return style(main);
    }


    /**
     * Build the login screen
     * @return the login screen
     */
    private Scene buildLoginScene() {
        VBox main = new VBox(40);

        //Set styling
        main.setAlignment(Pos.TOP_CENTER);
        main.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, "
                + "#1a237e 0%, #3949ab 50%, #5c6bc0 100%);");

        main.setPadding(new Insets(20));
        VBox head = header();
        head.setMaxWidth(600);
        VBox.setMargin(head, new Insets(10, 0, 0, 0));
        main.getChildren().add(head);

        // Log In / Create / Admin
        HBox tabs = buildTabBar(Tab.LOGIN);
        tabs.setMaxWidth(400);
        main.getChildren().add(tabs);

        main.getChildren().add(buildLoginForm());
        main.getChildren().add(footer());

        Scene scene = new Scene(main, 1200, 900);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        return scene;
    }

    /**
     * build create user screen
     * @return the create user screen with options
     */
    private Scene buildCreateScene() {
        VBox main = new VBox(40);

        //set styling
        main.setAlignment(Pos.TOP_CENTER);
        main.setStyle("-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, "
                + "#1a237e 0%, #3949ab 50%, #5c6bc0 100%);");
        main.setPadding(new Insets(20));

        VBox head = header();
        head.setMaxWidth(600);
        VBox.setMargin(head, new Insets(10, 0, 0, 0));
        main.getChildren().add(head);

        HBox tabs = buildTabBar(Tab.CREATE);
        tabs.setMaxWidth(400);
        main.getChildren().add(tabs);

        //build the form and add it th
        main.getChildren().add(buildCreateForm());
        main.getChildren().add(footer());

        Scene scene = new Scene(main, 1200, 900);
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("/styles.css")).toExternalForm());
        return scene;
    }


    /**
     * Build the dashboard after loggin in
     * @return the completed scene
     */
    private Scene buildDashboardScene() {
        VBox main = new VBox();
        main.setStyle("-fx-background-color:#f0f5ff;");
        main.getChildren().add(buildNavBar());

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        main.getChildren().add(content);

        // Add a card displaying user information
        content.getChildren().add(card(
                "Welcome back, " + currentUser.getFirstName() + " " + currentUser.getLastName(),
                "Here's your account summary"
        ));

        //Show all of the accounts of the user
        List<Account> accounts;
        double total;
        try {
            accounts = acctDao.getAccountsByUser(currentUser.getUserId());
            total = accounts.stream().mapToDouble(Account::getBalance).sum();
        } catch (SQLException ex) {
            showAlert("DB error", ex.getMessage());
            return style(main);
        }

        content.getChildren().add(buildAccountsBox(accounts, total));

        //Add different buttons to use the account functions
        HBox actions = new HBox(20);
        actions.setMaxWidth(800);
        actions.getChildren().add(actionBox("↓", "Deposit", "Add funds", () -> depositFlow(accounts)));
        actions.getChildren().add(actionBox("↑", "Withdraw", "Withdraw funds", () -> withdrawFlow(accounts)));
        actions.getChildren().add(actionBox("⇄", "Transfer", "Move money", () -> transferFlow(accounts)));
        actions.getChildren().add(actionBox("✈", "Send Money", "To another user", this::sendMoneyFlow));
        content.getChildren().add(actions);

        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);

        Label filterLabel = new Label("Filter transactions by:");
        filterLabel.setStyle("-fx-font-size:14px;-fx-text-fill:#666666;");

        ComboBox<String> typeFilter = new ComboBox<>(
                FXCollections.observableArrayList("ALL", "CHECKING", "SAVING")
        );
        typeFilter.setValue("ALL");
        typeFilter.getStyleClass().add("text-field");

        filterBox.getChildren().addAll(filterLabel, typeFilter);
        content.getChildren().add(filterBox);

        final ScrollPane[] recentScroll = {makeRecentScroll(
                accounts.stream()
                        .filter(a -> {
                            String f = typeFilter.getValue();
                            return f.equals("ALL") || a.getACCOUNT_TYPE().equals(f);
                        })
                        .collect(Collectors.toList())
        )};
        content.getChildren().add(recentScroll[0]);

        typeFilter.setOnAction(e -> {
            List<Account> filtered = accounts.stream()
                    .filter(a -> {
                        String f = typeFilter.getValue();
                        return f.equals("ALL") || a.getACCOUNT_TYPE().equals(f);
                    })
                    .collect(Collectors.toList());
            ScrollPane updated = makeRecentScroll(filtered);
            int idx = content.getChildren().indexOf(recentScroll[0]);
            content.getChildren().set(idx, updated);
            recentScroll[0] = updated;
        });

        return style(main);
    }

    /**
     * Build the login card with information retrieval
     * @return the completed card
     */
    private VBox buildLoginForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(30));
        form.setAlignment(Pos.CENTER);
        form.setMaxWidth(400);
        form.setStyle(
                "-fx-background-color: rgba(255,255,255,0.95);"
                        + "-fx-background-radius: 8px;"
                        + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 3);");

        //Create text-fields to extract user login credentials
        TextField user = new TextField();
        user.setPromptText("Username");
        user.getStyleClass().add("text-field");

        PasswordField pass = new PasswordField();
        pass.setPromptText("Password");
        pass.getStyleClass().add("text-field");

        Button login = new Button("Log In");
        login.setPrefWidth(Double.MAX_VALUE);
        login.getStyleClass().add("primary-button");
        login.setOnAction(e -> handleLogin(user.getText(), pass.getText()));

        form.getChildren().addAll(
                new Label("Username"), user,
                new Label("Password"), pass,
                login
        );
        return form;
    }

    /**
     * Build the create user form to create a new user
     * @return the completed form
     */
    private VBox buildCreateForm() {
        VBox form = new VBox(15);
        form.setAlignment(Pos.CENTER);
        form.setMaxWidth(400);
        form.setPadding(new Insets(30));
        form.setStyle(
                "-fx-background-color: rgba(255,255,255,0.95);"
                        + "-fx-background-radius: 8px;"
                        + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 3);");

        //Add text fields for create user
        TextField fn = new TextField();
        fn.setPromptText("First Name");
        fn.getStyleClass().add("text-field");

        TextField ln = new TextField();
        ln.setPromptText("Last Name");
        ln.getStyleClass().add("text-field");

        TextField un = new TextField();
        un.setPromptText("Username");
        un.getStyleClass().add("text-field");

        PasswordField pw = new PasswordField();
        pw.setPromptText("Password");
        pw.getStyleClass().add("text-field");

        PasswordField cf = new PasswordField();
        cf.setPromptText("Confirm Password");
        cf.getStyleClass().add("text-field");

        Button create = new Button("Create Account");
        create.setPrefWidth(Double.MAX_VALUE);
        create.getStyleClass().add("primary-button");
        create.setOnAction(e ->
                handleRegistration(
                        fn.getText(), ln.getText(), un.getText(),
                        pw.getText(), cf.getText()
                )
        );

        //Add all of the text-fields
        form.getChildren().addAll(
                new Label("First Name"), fn,
                new Label("Last Name"), ln,
                new Label("Username"), un,
                new Label("Password"), pw,
                new Label("Confirm Password"), cf,
                create
        );
        return form;
    }


    /* ───────── flows ───────── */

    /**
     * Deposit flow for the user
     * @param accts the accounts to potentially deposit
     */
    private void depositFlow(List<Account> accts) {
        Account tgt = chooseAccount(accts, "Deposit to account");
        if (tgt == null) return;
        amountDialog("Deposit", amt -> txWrapper(tgt, amt, true, "GUI deposit"));
    }

    /**
     * withdraw flow for the user
     * @param accts accounts to potential take from
     */
    private void withdrawFlow(List<Account> accts) {
        Account src = chooseAccount(accts, "Withdraw from account");
        if (src == null) return;
        amountDialog("Withdraw", amt -> txWrapper(src, amt, false, "GUI withdraw"));
    }

    /**
     * transfer flow for the user
     * @param accts accounts to transfer from
     */
    private void transferFlow(List<Account> accts) {
        Account from = chooseAccount(accts, "Transfer FROM account");
        if (from == null) return;
        List<Account> others = accts.stream().filter(a -> a.getACCOUNT_NUMBER() != from.getACCOUNT_NUMBER()).collect(Collectors.toList());
        Account to = chooseAccount(others, "Transfer TO account");
        if (to == null) return;

        amountDialog("Transfer", amt -> {
            try (Connection c = DatabaseConnection.getConnection()) {
                c.setAutoCommit(false);
                if (!acctDao.withdraw(c, from.getACCOUNT_NUMBER(), amt)) {
                    showAlert("Insufficient", "Not enough funds");
                    return;
                }
                if (!acctDao.deposit(c, to.getACCOUNT_NUMBER(), amt)) {
                    c.rollback();
                    showAlert("Error", "Deposit failed");
                    return;
                }
                txDao.logTransaction(c, from.getACCOUNT_NUMBER(), "TRANSFER", amt,
                        "To acct " + to.getACCOUNT_NUMBER(), "COMPLETED");
                txDao.logTransaction(c, to.getACCOUNT_NUMBER(), "TRANSFER", amt,
                        "From acct " + from.getACCOUNT_NUMBER(), "COMPLETED");
                c.commit();
                showDashboard();
            } catch (SQLException ex) {
                showAlert("Error", ex.getMessage());
            }
        });
    }

    /**
     * Ask for a recipient username + amount, then call AccountService.transfer(...)
     * and refresh the dashboard.
     */
    private void sendMoneyFlow() {

        //Ask for username
        TextInputDialog userDlg = new TextInputDialog();
        userDlg.setTitle("Send Money");
        userDlg.setHeaderText("Enter recipient’s username");
        userDlg.setContentText("Username:");
        userDlg.showAndWait().ifPresent(recipient -> {
            if (recipient.isBlank()) {
                showAlert("Invalid", "Username cannot be empty.");
                return;
            }

            // ask for ammount
            amountDialog("Send Money", amt -> {
                if (amt <= 0) {
                    showAlert("Invalid", "Amount must be positive.");
                    return;
                }

                //perform transfer
                try {
                    boolean ok = acctSvc.transfer(
                            currentUser.getUsername(),
                            recipient.trim(),
                            amt
                    );
                    showAlert(
                            ok ? "Success" : "Failed",
                            ok
                                    ? String.format("Sent $%,.2f to %s", amt, recipient)
                                    : "Transfer failed (insufficient funds or user not found)"
                    );
                    if (ok) showDashboard();
                } catch (SQLException ex) {
                    showAlert("Error", ex.getMessage());
                }
            });
        });
    }


    /* ───── transaction helper ───── */

    /**
     * transaction wrapper account for recent transactions
     * @param acct the account to log
     * @param amt the amount to log
     * @param deposit type of transaction
     * @param desc short description
     */
    private void txWrapper(Account acct, double amt, boolean deposit, String desc) {
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            boolean ok = deposit
                    ? acctDao.deposit(c, acct.getACCOUNT_NUMBER(), amt)
                    : acctDao.withdraw(c, acct.getACCOUNT_NUMBER(), amt);

            if (!ok) {
                showAlert("Error", deposit ? "Deposit failed" : "Insufficient funds");
                return;
            }
            txDao.logTransaction(c, acct.getACCOUNT_NUMBER(),
                    deposit ? "DEPOSIT" : "WITHDRAWAL",
                    amt, desc, "COMPLETED");
            c.commit();
            showDashboard();
        } catch (SQLException ex) {
            showAlert("Error", ex.getMessage());
        }
    }

    /* ─────── helpers ─────── */

    /**
     * Allow users to scroll up or down
     * @param accts the recent transactions of all the accounts
     * @return thhe completed scroll pane
     */
    private ScrollPane makeRecentScroll(List<Account> accts) {
        VBox rec = buildRecentBox(accts);
        ScrollPane sp = new ScrollPane(rec);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        sp.setPrefViewportHeight(200);
        sp.setMaxHeight(200);
        return sp;
    }


    /**
     * Renders the blue shield banner at the very top.
     */
    private VBox createHeader() {
        VBox header = new VBox(10);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(30, 20, 20, 20));
        header.setStyle("-fx-background-color: #3366ff;");

        // Shield svg icon
        SVGPath shieldIcon = new SVGPath();
        shieldIcon.setContent("M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z");
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

    /**
     * Text dialog helper
     * @param title the title of the button
     * @param text the text of the button
     */
    private void showTextDialog(String title, String text) {
        TextArea area = new TextArea(text);
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefWidth(600);
        area.setPrefHeight(300);

        Dialog<Void> d = new Dialog<>();
        d.setTitle(title);
        d.getDialogPane().setContent(area);
        d.getDialogPane().getButtonTypes().add(ButtonType.OK);
        d.showAndWait();
    }


    /**
     * List all the users for the administration page
     */
    private void listAllUsers() {
        StringBuilder sb = new StringBuilder();
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT ID, first_name, last_name, username, role FROM users")) {
            while (rs.next()) {
                sb.append(rs.getInt("ID")).append(": ")
                        .append(rs.getString("first_name")).append(" ")
                        .append(rs.getString("last_name"))
                        .append(" (").append(rs.getString("username"))
                        .append(", role=").append(rs.getString("role")).append(")\n");
            }
        } catch (SQLException ex) {
            showAlert("Error", ex.getMessage());
            return;
        }
        showTextDialog("All Users", sb.toString());
    }

    /**
     * List all of the accounts for the administrator
     */
    private void listAllAccounts() {
        StringBuilder sb = new StringBuilder();
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT account_id, user_id, account_type, balance, interest_rate FROM account")) {
            while (rs.next()) {
                sb.append("Acct ").append(rs.getInt("account_id"))
                        .append(" | User ").append(rs.getInt("user_id"))
                        .append(" | ").append(rs.getString("account_type"))
                        .append(" | $").append(String.format("%,.2f", rs.getDouble("balance")))
                        .append(" | rate=")
                        .append(rs.getObject("interest_rate") == null ? "n/a"
                                : String.format("%.2f%%", rs.getDouble("interest_rate") * 100))
                        .append("\n");
            }
        } catch (SQLException ex) {
            showAlert("Error", ex.getMessage());
            return;
        }
        showTextDialog("All Accounts", sb.toString());
    }

    /**
     * List all  the transactions for the user
     */
    private void listAllTransactions() {
        StringBuilder sb = new StringBuilder();
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT transaction_id, account_id, transaction_type, amount, time_stamp, description, status "
                             + "FROM transactions ORDER BY time_stamp DESC")) {
            while (rs.next()) {
                sb.append(rs.getInt("transaction_id"))
                        .append(" | acct ").append(rs.getInt("account_id"))
                        .append(" | ").append(rs.getString("transaction_type"))
                        .append(" | $").append(String.format("%,.2f", rs.getDouble("amount")))
                        .append(" | ").append(rs.getTimestamp("time_stamp"))
                        .append(" | ").append(rs.getString("description"))
                        .append(" | ").append(rs.getString("status"))
                        .append("\n");
            }
        } catch (SQLException ex) {
            showAlert("Error", ex.getMessage());
            return;
        }
        showTextDialog("All Transactions", sb.toString());
    }

    /**
     * Show dialogue to create a new admin   user
     */
    private void createAdminDialog() {
        Dialog<List<String>> dlg = new Dialog<>();
        dlg.setTitle("Create New ADMIN");
        dlg.setHeaderText("Enter admin details");

        ButtonType ok = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(10);
        g.setPadding(new Insets(20));
        TextField fn = new TextField();
        fn.setPromptText("First Name");
        TextField ln = new TextField();
        ln.setPromptText("Last Name");
        TextField un = new TextField();
        un.setPromptText("Username");
        PasswordField pw = new PasswordField();
        pw.setPromptText("Password");
        g.addRow(0, new Label("First Name:"), fn);
        g.addRow(1, new Label("Last Name:"), ln);
        g.addRow(2, new Label("Username:"), un);
        g.addRow(3, new Label("Password:"), pw);
        dlg.getDialogPane().setContent(g);

        dlg.setResultConverter(btn -> btn == ok
                ? List.of(fn.getText(), ln.getText(), un.getText(), pw.getText())
                : null
        );

        dlg.showAndWait().ifPresent(vals -> {
            try {
                String sql = "INSERT INTO users (first_name,last_name,username,password_hash,role) "
                        + "VALUES (?,?,?,?, 'ADMIN')";
                String hash = AuthenticationService.encryptPassword(vals.get(3));
                try (Connection c = DatabaseConnection.getConnection();
                     PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, vals.get(0));
                    ps.setString(2, vals.get(1));
                    ps.setString(3, vals.get(2));
                    ps.setString(4, hash);
                    int r = ps.executeUpdate();
                    showAlert("Result", r == 1 ? "Admin created." : "Create failed.");
                }
            } catch (Exception ex) {
                showAlert("Error", ex.getMessage());
            }
        });
    }


    /**
     * Handle login with database
     * @param u the username
     * @param p the password
     */
    private void handleLogin(String u, String p) {
        try {
            if (AuthenticationService.login(u, p)) {
                currentUser = UserDAO.getUserByUsername(u);
                if (currentUser.isAdmin()) {
                    // build and show the admin UI
                    primaryStage.setScene(buildAdminScene());
                } else {
                    showDashboard();
                }
            } else {
                showAlert("Login failed", "Incorrect credentials");
            }
        } catch (SQLException ex) {
            showAlert("Error", ex.getMessage());
        }
    }


    /**
     * handle user registration
     * @param fn first name
     * @param ln last name
     * @param un username
     * @param pw password
     * @param cf confirm password
     */
    private void handleRegistration(String fn, String ln, String un, String pw, String cf) {
        if (fn.isBlank() || ln.isBlank() || un.isBlank() || pw.isBlank()) {
            showAlert("Missing", "All fields required");
            return;
        }
        if (!pw.equals(cf)) {
            showAlert("Mismatch", "Passwords do not match");
            return;
        }
        User u = AuthenticationService.registerUser(fn, ln, un, pw);
        if (u == null) {
            showAlert("Taken", "Username exists");
        } else {
            showAlert("Success", "Account created");
            primaryStage.setScene(loginScene);
        }
    }

    /**
     * show the dashboard to the user
     */
    private void showDashboard() {
        dashboardScene = buildDashboardScene();
        primaryStage.setScene(dashboardScene);
    }

    /* ───────── UI building blocks ───────── */

    /**
     * Create a root scaffold for formatting
     * @return the completed scaffold
     */
    private VBox scaffoldRoot() {
        VBox r = new VBox(20);
        r.setAlignment(Pos.TOP_CENTER);
        r.setStyle("-fx-background-color:#f0f5ff;");
        r.getChildren().add(header());
        r.getChildren().add(new HBox());
        VBox form = new VBox(15);
        form.setPadding(new Insets(20, 30, 30, 30));
        form.setMaxWidth(400);
        form.setStyle("-fx-background-color:white;-fx-background-radius:8px;");
        r.getChildren().add(form);
        r.getChildren().add(footer());
        return r;
    }

    /**
     * Add the tab bar for the login options
     * @param root the root box
     * @param isLoginTab will display different results
     * @param isAdminTab admin tab
     */
    private void addTabBar(VBox root, boolean isLoginTab, boolean isAdminTab) {
        HBox bar = (HBox) root.getChildren().get(1);
        bar.getChildren().clear();
        bar.setPrefWidth(600);

        Button userTab = new Button("Log In");
        Button regTab = new Button("Create Account");
        Button admTab = new Button("Admin");

        for (Button b : List.of(userTab, regTab, admTab)) {
            b.setPrefWidth(200);
            b.getStyleClass().add("tab-button");
        }
        if (isLoginTab) userTab.getStyleClass().add("active-tab");
        else if (isAdminTab) admTab.getStyleClass().add("active-tab");
        else regTab.getStyleClass().add("active-tab");

        userTab.setOnAction(e -> primaryStage.setScene(loginScene));
        regTab.setOnAction(e -> primaryStage.setScene(createScene));
        admTab.setOnAction(e -> primaryStage.setScene(adminAuthScene));

        bar.getChildren().addAll(userTab, regTab, admTab);
    }


    /**
     *  helper to make labeled text-fields
     * @param root the root box
     * @param label the label to place
     * @param prompt the prompt of the text-field
     * @return the completed text-field
     */
    private TextField labelled(VBox root, String label, String prompt) {
        return labelled(root, label, prompt, false);
    }
    /**
     *  helper to make labeled text-fields
     * @param root the root box
     * @param label the label to place
     * @param prompt the prompt of the text-field
     * @return the completed text-field
     */
    private TextField labelled(VBox root, String label, String prompt, boolean pw) {
        VBox form = (VBox) root.getChildren().get(2);
        form.getChildren().add(new Label(label));
        TextField f = pw ? new PasswordField() : new TextField();
        f.setPromptText(prompt);
        f.getStyleClass().add("text-field");
        form.getChildren().add(f);
        return f;
    }

    /**
     * Helper method to create buttons
     * @param txt the text of the button
     * @param root the root box to place it in
     * @return the complete button
     */
    private Button primaryButton(String txt, VBox root) {
        VBox form = (VBox) root.getChildren().get(2);
        Button b = new Button(txt);
        b.setPrefWidth(Double.MAX_VALUE);
        b.getStyleClass().add("primary-button");
        form.getChildren().add(b);
        return b;
    }

    /**
     * build a navigation bard
     * @return the completed navigation bar
     */
    private HBox buildNavBar() {
        HBox nav = new HBox();
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setPadding(new Insets(0, 20, 0, 20));
        nav.setPrefHeight(60);
        nav.setStyle("-fx-background-color:#3366ff;");

        Label lbl = new Label("$ SecureBank");
        lbl.setStyle("-fx-text-fill:white;-fx-font-size:20px;-fx-font-weight:bold;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        // ────── OPTIONS MENU ──────
        MenuButton opts = new MenuButton("Options");
        opts.getItems().addAll(
                new MenuItem("Open Savings") {{
                    setOnAction(e -> {
                        try {
                            openSavingsFlow();
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        }
                    });
                }},
                new MenuItem("Close Account") {{
                    setOnAction(e -> closeAccountFlow());
                }},
                new MenuItem("Change Password") {{
                    setOnAction(e -> changePasswordFlow());
                }},
                new MenuItem("Open Checking") {{
                    setOnAction(e -> openCheckingFlow());
                }}
        );

        // reuse the same styling as logout button
        opts.getStyleClass().add("logout-button");

        Button out = new Button("Logout");
        out.getStyleClass().add("logout-button");
        out.setOnAction(e -> primaryStage.setScene(loginScene));
        nav.getChildren().addAll(lbl, sp, opts, out);

        return nav;
    }

    /**
     * box that shows to show user accounts
     * @param accts the type of accounts they have
     * @param total the total amount in the account
     * @return the completed box
     */
    private VBox buildAccountsBox(List<Account> accts, double total) {
        VBox box = new VBox(15);
        box.setPadding(new Insets(30));
        box.setMaxWidth(800);
        box.setStyle("-fx-background-color:#3366ff;-fx-background-radius:8px;");
        Label tot = new Label(String.format("Total Balance  $%,.2f", total));
        tot.setStyle("-fx-text-fill:white;-fx-font-size:32px;-fx-font-weight:bold;");
        box.getChildren().add(tot);
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, yyyy");
        accts.stream()
                .sorted(Comparator.comparing(Account::getACCOUNT_NUMBER))
                .forEach(a -> {
                    Label line = new Label(String.format(
                            "%s (ID %d) – $%,.2f – opened %s – status %s",
                            a.getACCOUNT_TYPE(),
                            a.getACCOUNT_NUMBER(),
                            a.getBalance(),
                            a.getDATE_OPENED().format(fmt),
                            a.getStatus()
                    ));
                    line.setStyle("-fx-text-fill:white;");
                    box.getChildren().add(line);
                });
        return box;
    }

    /**
     * Loads up the recent transactions
     * @param accts the accounts to list the recents of
     * @return the completed recent transactions box
     */
    private VBox buildRecentBox(List<Account> accts) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setMaxWidth(800);
        box.setStyle("-fx-background-color:white;-fx-background-radius:8px;");
        HBox head = new HBox(8);
        head.setAlignment(Pos.CENTER_LEFT);
        head.getChildren().addAll(new Label("⏱"), new Label("Recent Activity"));
        box.getChildren().add(head);
        List<Transaction> recent = accts.stream()
                .flatMap(a -> {
                    try {
                        return txDao.getTransactionsByAccount(a.getACCOUNT_NUMBER(), 5).stream();
                    } catch (SQLException ex) {
                        return List.<Transaction>of().stream();
                    }
                })
                .sorted(Comparator.comparing(Transaction::getTIME_STAMP).reversed())
                .limit(10)
                .collect(Collectors.toList());
        if (recent.isEmpty()) {
            box.getChildren().add(new Label("No transactions yet"));
        } else {
            recent.forEach(t -> box.getChildren().add(new Label(String.format(
                    "%-10s $%,.2f %s",
                    t.getTRANSCATION_TYPE(),
                    t.getAMOUNT(),
                    t.getTIME_STAMP()
            ))));
        }
        return box;
    }

    /**
     * Helper for the card
     * @param title set the title of the card
     * @param subtitle set the subtitle of the card
     * @return the completed card
     */
    private VBox card(String title, String subtitle) {
        VBox v = new VBox(10);
        v.setPadding(new Insets(20));
        v.setMaxWidth(800);
        v.setStyle("-fx-background-color:white;-fx-background-radius:8px;");
        Label t = new Label(title);
        t.setStyle("-fx-font-size:24px;-fx-font-weight:bold;");
        Label s = new Label(subtitle);
        s.setStyle("-fx-text-fill:#666666;");
        v.getChildren().addAll(t, s);
        return v;
    }

    /**
     * Handles actions
     * @param icon the icon / logo
     * @param title the title of the action
     * @param subtitle the subtitle of the action
     * @param action the action to handle itself
     * @return the completed vbox for the action
     */
    private VBox actionBox(String icon, String title, String subtitle, Runnable action) {
        VBox box = new VBox(10);
        box.setPadding(new Insets(20));
        box.setPrefWidth(260);
        box.setStyle("-fx-background-color:white;-fx-background-radius:8px;");
        HBox ic = new HBox();
        ic.setPrefSize(40, 40);
        ic.setAlignment(Pos.CENTER);
        ic.setStyle(icon.equals("↓")
                ? "-fx-background-color:#e0f7e6;-fx-background-radius:20px;"
                : "-fx-background-color:#ffebee;-fx-background-radius:20px;"
        );
        Label i = new Label(icon);
        i.setStyle(icon.equals("↓")
                ? "-fx-text-fill:#4CAF50;-fx-font-size:20px;"
                : "-fx-text-fill:#F44336;-fx-font-size:20px;"
        );
        ic.getChildren().add(i);
        Label t = new Label(title);
        t.setStyle("-fx-font-size:18px;-fx-font-weight:bold;");
        Label sub = new Label(subtitle);
        sub.setStyle("-fx-text-fill:#666666;");
        box.getChildren().addAll(ic, t, sub);
        box.setOnMouseClicked(e -> action.run());
        return box;
    }

    /**
     * A helper to set the styling
     * @param root the root pane to place styling on
     * @return the completed style
     * */
    private Scene style(Pane root) {
        Scene s = new Scene(root, 850, 700);
        s.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/styles.css")).toExternalForm());
        return s;
    }

    /* ───────── dialogs & selection ───────── */

    /**
     * Helper to display the amount to display
     */
    private interface AmountHandler {
        void apply(double amt);
    }

    /**
     * prompts the user for the amount to either deposit or withdraw
      * @param title the title for the dialog
     * @param handler handles the amount
     */
    private void amountDialog(String title, AmountHandler handler) {
        Dialog<Double> d = new Dialog<>();
        d.setTitle(title);
        d.setHeaderText("Enter amount");
        ButtonType ok = new ButtonType(title.split(" ")[0], ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setPadding(new Insets(20, 150, 10, 10));
        g.setHgap(10);
        g.setVgap(10);
        TextField amt = new TextField();
        amt.setPromptText("Amount");
        g.addRow(0, new Label("Amount:"), amt);
        d.getDialogPane().setContent(g);
        amt.requestFocus();

        d.setResultConverter(btn -> {
            if (btn == ok) {
                try {
                    return Double.parseDouble(amt.getText());
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
            return null;
        });

        d.showAndWait().ifPresent(a -> {
            if (a > 0) handler.apply(a);
            else showAlert("Invalid", "Enter a positive amount");
        });
    }

    /**
     * allows user to choose the type of account to perform an action on
     * @param accts allows user to choose accounts
     * @param header the header
     * @return return the chose account screen
     */
    private Account chooseAccount(List<Account> accts, String header) {
        if (accts.isEmpty()) {
            showAlert("No accounts", "You don’t have any accounts yet.");
            return null;
        }
        List<String> labels = accts.stream()
                .map(a -> String.format("%s – ID %d – $%,.2f",
                        a.getACCOUNT_TYPE(),
                        a.getACCOUNT_NUMBER(),
                        a.getBalance()))
                .collect(Collectors.toList());

        ChoiceDialog<String> dlg = new ChoiceDialog<>(labels.get(0), labels);
        dlg.setTitle("Select Account");
        dlg.setHeaderText(header);
        dlg.setContentText("Account:");

        return dlg.showAndWait()
                .flatMap(sel -> {
                    int idx = labels.indexOf(sel);
                    return idx >= 0 ? java.util.Optional.of(accts.get(idx))
                            : java.util.Optional.empty();
                })
                .orElse(null);
    }

    /* ───────── alert ───────── */

    /**
     * shows a specific alert
     * @param t the string of the alert
     * @param m the content of the alert
     */
    private void showAlert(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }

    /* ───────── header/footer ───────── */

    /**
     * set the header attributes
     * @return the header
     */
    private VBox header() {
        VBox h = new VBox(10);
        h.setAlignment(Pos.CENTER);
        h.setPadding(new Insets(30, 20, 20, 20));
        h.setStyle("-fx-background-color:#3366ff;");
        SVGPath shield = new SVGPath();
        shield.setContent("M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z");
        shield.setFill(Color.WHITE);
        shield.setScaleX(2);
        shield.setScaleY(2);
        StackPane icon = new StackPane(shield);
        icon.setPrefHeight(60);
        Label title = new Label("SecureBank");
        title.setStyle("-fx-text-fill:white;-fx-font-size:24px;-fx-font-weight:bold;");
        Label sub = new Label("Your trusted financial partner");
        sub.setStyle("-fx-text-fill:white;-fx-font-size:16px;");
        h.getChildren().addAll(icon, title, sub);
        return h;
    }

    /**
     * set the footer to contain copyright information
     * @return return the footer
     */
    private VBox footer() {
        VBox f = new VBox(5);
        f.setAlignment(Pos.CENTER);
        f.setPadding(new Insets(20));
        f.getChildren().addAll(
                new Label("© 2025 SecureBank. All rights reserved."),
                new Label("Secure. Reliable. Trusted.")
        );
        return f;
    }

    // ───── Open Savings GUI ─────

    /**
     * open dialogue to let user open a savings account
     * @throws SQLException to handle issues with sql
     */
    private void openSavingsFlow() throws SQLException {
        // pick a checking account
        List<Account> checkings = acctDao.getAccountsByUser(currentUser.getUserId()).stream()
                .filter(a -> "CHECKING".equals(a.getACCOUNT_TYPE()))
                .collect(Collectors.toList());
        Account chk = chooseAccount(checkings, "Select Checking to fund Savings");
        if (chk == null) return;

        // ask for amount
        amountDialog("Open Savings", amt -> {
            try {
                boolean ok = acctSvc.openSavingsFromChecking(
                        currentUser.getUserId(),
                        chk.getACCOUNT_NUMBER(),
                        amt
                );
                showAlert(
                        ok ? "Success" : "Failed",
                        ok ? "Savings account opened" : "Could not open savings"
                );
                if (ok) showDashboard();
            } catch (SQLException ex) {
                showAlert("Error", ex.getMessage());
            }
        });
    }

    // ───── Close Account GUI ─────

    /**
     * Allow the user to close the account
     */
    private void closeAccountFlow() {
        List<Account> accts;
        try {
            accts = acctDao.getAccountsByUser(currentUser.getUserId());
        } catch (SQLException ex) {
            showAlert("DB error", ex.getMessage());
            return;
        }
        Account toClose = chooseAccount(accts, "Select account to close");
        if (toClose == null) return;
        if (toClose.getBalance() != 0.0) {
            showAlert("Cannot close", "Balance must be zero to close an account.");
            return;
        }

        // confirm dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Closure");
        confirm.setHeaderText("Close account ID " + toClose.getACCOUNT_NUMBER() + "?");
        confirm.setContentText("This action cannot be undone.");
        confirm.showAndWait()
                .filter(btn -> btn == ButtonType.OK)
                .ifPresent(btn -> {
                    boolean ok = acctSvc.closeAccount(
                            currentUser.getUserId(),
                            toClose.getACCOUNT_NUMBER()
                    );
                    showAlert(
                            ok ? "Closed" : "Failed",
                            ok ? "Account closed." : "Could not close account."
                    );
                    if (ok) showDashboard();
                });
    }

    // ───── Change Password GUI ─────

    /**
     * allow the user to change the password
     */
    private void changePasswordFlow() {
        Dialog<List<String>> dlg = new Dialog<>();
        dlg.setTitle("Change Password");
        dlg.setHeaderText("Enter current and new password");

        ButtonType changeBtn = new ButtonType("Change", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(changeBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        PasswordField oldPf = new PasswordField();
        oldPf.setPromptText("Current password");
        PasswordField newPf = new PasswordField();
        newPf.setPromptText("New password");
        PasswordField confPf = new PasswordField();
        confPf.setPromptText("Confirm new pw");

        grid.addRow(0, new Label("Current:"), oldPf);
        grid.addRow(1, new Label("New:"), newPf);
        grid.addRow(2, new Label("Confirm:"), confPf);

        dlg.getDialogPane().setContent(grid);

        dlg.setResultConverter(btn -> {
            if (btn == changeBtn) {
                return List.of(oldPf.getText(), newPf.getText(), confPf.getText());
            }
            return null;
        });

        dlg.showAndWait().ifPresent(list -> {
            String oldPw = list.get(0), newPw = list.get(1), conf = list.get(2);
            if (!newPw.equals(conf)) {
                showAlert("Mismatch", "New passwords do not match.");
                return;
            }
            boolean ok = new AuthenticationService()
                    .changePassword(currentUser.getUserId(), oldPw, newPw);
            showAlert(
                    ok ? "Success" : "Failed",
                    ok ? "Password changed; please log in again."
                            : "Could not change password."
            );
            if (ok) primaryStage.setScene(loginScene);
        });
    }

    // ───── Open Checking GUI ─────

    /**
     * open a new checking account
     */
    private void openCheckingFlow() {
        TextInputDialog dlg = new TextInputDialog("0");
        dlg.setTitle("Open Checking");
        dlg.setHeaderText("Optional initial deposit (or 0)");
        dlg.setContentText("Amount:");

        dlg.showAndWait().ifPresent(input -> {
            try {
                double amt = Double.parseDouble(input);
                try (Connection c = DatabaseConnection.getConnection()) {
                    c.setAutoCommit(false);
                    acctSvc.openDefaultChecking(currentUser.getUserId(), c);

                    if (amt > 0) {
                        // fund new checking from existing checking
                        List<Account> accts = acctDao.getAccountsByUser(currentUser.getUserId());
                        // assume the newest checking has the highest ID
                        Account newChk = accts.stream()
                                .filter(a -> "CHECKING".equals(a.getACCOUNT_TYPE()))
                                .max(Comparator.comparing(Account::getACCOUNT_NUMBER))
                                .orElseThrow();
                        Account src = accts.stream()
                                .filter(a -> a.getACCOUNT_NUMBER() != newChk.getACCOUNT_NUMBER()
                                        && "CHECKING".equals(a.getACCOUNT_TYPE()))
                                .findFirst()
                                .orElseThrow();
                        acctDao.withdraw(c, src.getACCOUNT_NUMBER(), amt);
                        acctDao.deposit(c, newChk.getACCOUNT_NUMBER(), amt);
                        txDao.logTransaction(c, src.getACCOUNT_NUMBER(),
                                "TRANSFER", amt, "Fund new checking", "COMPLETED");
                        txDao.logTransaction(c, newChk.getACCOUNT_NUMBER(),
                                "TRANSFER", amt, "Seeded from existing", "COMPLETED");
                    }
                    c.commit();
                    showAlert("Success", "New checking opened.");
                    showDashboard();
                }
            } catch (NumberFormatException | SQLException ex) {
                showAlert("Error", ex.getMessage());
            }
        });
    }
}