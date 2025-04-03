package ui;

import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public class BankingApp {


    public static void main(String[] args) {

        try {
            Connection conn = DatabaseConnection.getConnection();
            if (conn != null) {
                System.out.println("Connection Established");
                conn.close();
            }
            else{
            System.out.println("Connection Failed");
        }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}
