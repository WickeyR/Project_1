
import DAO.AccountDAO;
import DAO.TransactionDAO;
import DAO.UserDAO;
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
import model.Account;
import model.Transaction;
import model.User;
import service.AccountService;
import service.AuthenticationService;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
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
    private User  currentUser;

    private final AccountDAO     acctDao = new AccountDAO();
    private final TransactionDAO txDao   = new TransactionDAO();
    private final AccountService acctSvc = new AccountService();

    public static void main(String[] args) { launch(args); }

    @Override public void start(Stage stage) {
        this.primaryStage = stage;
        loginScene  = buildLoginScene();
        createScene = buildCreateScene();
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("SecureBank");
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(
                getClass().getResourceAsStream("/shield_icon.png"))));
        primaryStage.show();
    }

    /* ─────────── scenes ─────────── */

    private Scene buildLoginScene() {
        VBox root = scaffoldRoot();
        addTabBar(root, true);

        TextField u = labelled(root, "Username", "Enter your username");
        PasswordField p = (PasswordField) labelled(root, "Password", "Enter your password", true);
        primaryButton("Log In", root).setOnAction(e -> handleLogin(u.getText(), p.getText()));

        return style(root);
    }

    private Scene buildCreateScene() {
        VBox root = scaffoldRoot();
        addTabBar(root, false);

        TextField fn = labelled(root, "First Name", "Enter your first name");
        TextField ln = labelled(root, "Last Name", "Enter your last name");
        TextField un = labelled(root, "Username", "Choose a username");
        PasswordField pw = (PasswordField) labelled(root, "Password", "Choose a password", true);
        PasswordField cf = (PasswordField) labelled(root, "Confirm Password", "Re-enter password", true);
        primaryButton("Create Account", root)
                .setOnAction(e -> handleRegistration(fn.getText(), ln.getText(), un.getText(), pw.getText(), cf.getText()));

        return style(root);
    }

    private Scene buildDashboardScene() {
        VBox main = new VBox();
        main.setStyle("-fx-background-color:#f0f5ff;");
        main.getChildren().add(buildNavBar());

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        main.getChildren().add(content);

        // Welcome card
        content.getChildren().add(card(
                "Welcome back, " + currentUser.getFirstName() + " " + currentUser.getLastName(),
                "Here's your account summary"
        ));

        // Fetch accounts
        List<Account> accounts;
        double total;
        try {
            accounts = acctDao.getAccountsByUser(currentUser.getUserId());
            total = accounts.stream().mapToDouble(Account::getBalance).sum();
        } catch (SQLException ex) {
            showAlert("DB error", ex.getMessage());
            return style(main);
        }

        // Accounts summary card
        content.getChildren().add(buildAccountsBox(accounts, total));

        // Action buttons
        HBox actions = new HBox(20);
        actions.setMaxWidth(800);
        actions.getChildren().add(actionBox("↓", "Deposit",  "Add funds",   () -> depositFlow(accounts)));
        actions.getChildren().add(actionBox("↑", "Withdraw", "Withdraw",   () -> withdrawFlow(accounts)));
        actions.getChildren().add(actionBox("⇄", "Transfer", "Move money", () -> transferFlow(accounts)));
        actions.getChildren().add(actionBox("✈", "Send Money", "To another user", this::sendMoneyFlow));

        content.getChildren().add(actions);

        // Recent transactions
        content.getChildren().add(buildRecentBox(accounts));

        return style(main);
    }

    /* ───────── flows ───────── */

    private void depositFlow(List<Account> accts) {
        Account tgt = chooseAccount(accts, "Deposit to account");
        if (tgt == null) return;
        amountDialog("Deposit", amt -> txWrapper(tgt, amt, true, "GUI deposit"));
    }

    private void withdrawFlow(List<Account> accts) {
        Account src = chooseAccount(accts, "Withdraw from account");
        if (src == null) return;
        amountDialog("Withdraw", amt -> txWrapper(src, amt, false, "GUI withdraw"));
    }

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
                txDao.logTransaction(c, to.getACCOUNT_NUMBER(),   "TRANSFER", amt,
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
        // 1) ask for recipient username
        TextInputDialog userDlg = new TextInputDialog();
        userDlg.setTitle("Send Money");
        userDlg.setHeaderText("Enter recipient’s username");
        userDlg.setContentText("Username:");
        userDlg.showAndWait().ifPresent(recipient -> {
            if (recipient.isBlank()) {
                showAlert("Invalid", "Username cannot be empty.");
                return;
            }

            // 2) ask for amount
            amountDialog("Send Money", amt -> {
                if (amt <= 0) {
                    showAlert("Invalid", "Amount must be positive.");
                    return;
                }

                // 3) perform transfer
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

    private void handleLogin(String u, String p) {
        try {
            if (AuthenticationService.login(u, p)) {
                currentUser = UserDAO.getUserByUsername(u);
                showDashboard();
            } else {
                showAlert("Login failed", "Incorrect credentials");
            }
        } catch (SQLException ex) {
            showAlert("Error", ex.getMessage());
        }
    }

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

    private void showDashboard() {
        dashboardScene = buildDashboardScene();
        primaryStage.setScene(dashboardScene);
    }

    /* ───────── UI building blocks ───────── */

    private VBox scaffoldRoot() {
        VBox r = new VBox(20);
        r.setAlignment(Pos.TOP_CENTER);
        r.setStyle("-fx-background-color:#f0f5ff;");
        r.getChildren().add(header());
        r.getChildren().add(new HBox());
        VBox form = new VBox(15);
        form.setPadding(new Insets(20,30,30,30));
        form.setMaxWidth(400);
        form.setStyle("-fx-background-color:white;-fx-background-radius:8px;");
        r.getChildren().add(form);
        r.getChildren().add(footer());
        return r;
    }

    private void addTabBar(VBox r, boolean loginActive) {
        HBox bar = (HBox)r.getChildren().get(1);
        bar.setPrefWidth(400);
        Button b1 = new Button("Log In");
        Button b2 = new Button("Create Account");
        b1.setPrefWidth(200); b2.setPrefWidth(200);
        b1.getStyleClass().add("tab-button"); b2.getStyleClass().add("tab-button");
        if (loginActive) b1.getStyleClass().add("active-tab"); else b2.getStyleClass().add("active-tab");
        b1.setOnAction(e -> primaryStage.setScene(loginScene));
        b2.setOnAction(e -> primaryStage.setScene(createScene));
        bar.getChildren().addAll(b1,b2);
    }

    private TextField labelled(VBox root, String label, String prompt) {
        return labelled(root, label, prompt, false);
    }
    private TextField labelled(VBox root, String label, String prompt, boolean pw) {
        VBox form = (VBox)root.getChildren().get(2);
        form.getChildren().add(new Label(label));
        TextField f = pw ? new PasswordField() : new TextField();
        f.setPromptText(prompt);
        f.getStyleClass().add("text-field");
        form.getChildren().add(f);
        return f;
    }

    private Button primaryButton(String txt, VBox root) {
        VBox form = (VBox)root.getChildren().get(2);
        Button b = new Button(txt);
        b.setPrefWidth(Double.MAX_VALUE);
        b.getStyleClass().add("primary-button");
        form.getChildren().add(b);
        return b;
    }

    private HBox buildNavBar() {
        HBox nav = new HBox();
        nav.setAlignment(Pos.CENTER_LEFT);
        nav.setPadding(new Insets(0,20,0,20));
        nav.setPrefHeight(60);
        nav.setStyle("-fx-background-color:#3366ff;");

        Label lbl = new Label("$ SecureBank");
        lbl.setStyle("-fx-text-fill:white;-fx-font-size:20px;-fx-font-weight:bold;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

           // ────── OPTIONS MENU ──────
                MenuButton opts = new MenuButton("Options");
           opts.getItems().addAll(
                      new MenuItem("Open Savings")    {{ setOnAction(e -> {
                          try {
                              openSavingsFlow();
                          } catch (SQLException ex) {
                              throw new RuntimeException(ex);
                          }
                      }); }},
                      new MenuItem("Close Account")   {{ setOnAction(e -> closeAccountFlow()); }},
                      new MenuItem("Change Password") {{ setOnAction(e -> changePasswordFlow()); }},
                      new MenuItem("Open Checking")   {{ setOnAction(e -> openCheckingFlow()); }}
                            );

           // reuse the same styling as logout button
          opts.getStyleClass().add("logout-button");

                Button out = new Button("Logout");
        out.getStyleClass().add("logout-button");
        out.setOnAction(e -> primaryStage.setScene(loginScene));
         nav.getChildren().addAll(lbl, sp, opts, out);

        return nav;
    }

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

    private Scene style(Pane root) {
        Scene s = new Scene(root, 850, 700);
        s.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/styles.css")).toExternalForm());
        return s;
    }

    /* ───────── dialogs & selection ───────── */

    private interface AmountHandler { void apply(double amt); }

    private void amountDialog(String title, AmountHandler handler) {
        Dialog<Double> d = new Dialog<>();
        d.setTitle(title);
        d.setHeaderText("Enter amount");
        ButtonType ok = new ButtonType(title.split(" ")[0], ButtonBar.ButtonData.OK_DONE);
        d.getDialogPane().getButtonTypes().addAll(ok, ButtonType.CANCEL);

        GridPane g = new GridPane();
        g.setPadding(new Insets(20,150,10,10));
        g.setHgap(10);
        g.setVgap(10);
        TextField amt = new TextField();
        amt.setPromptText("Amount");
        g.addRow(0, new Label("Amount:"), amt);
        d.getDialogPane().setContent(g);
        amt.requestFocus();

        d.setResultConverter(btn -> {
            if (btn == ok) {
                try { return Double.parseDouble(amt.getText()); }
                catch (NumberFormatException e) { return 0.0; }
            }
            return null;
        });

        d.showAndWait().ifPresent(a -> {
            if (a > 0) handler.apply(a);
            else showAlert("Invalid", "Enter a positive amount");
        });
    }

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

    private void showAlert(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }

    /* ───────── header/footer ───────── */

    private VBox header() {
        VBox h = new VBox(10);
        h.setAlignment(Pos.CENTER);
        h.setPadding(new Insets(30,20,20,20));
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
        PasswordField confPf= new PasswordField();
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
