package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    //Declare variables for SQL connection
    private static final String URL = "jdbc:mysql://localhost:3307/";
    private static final String USER = "root";
    private static final String PASSWORD = "rick";


    /**
     * @return A sql connection to the database
     * @throws SQLException if any error occurs
     */
    public static Connection getConnection() throws SQLException {

        return DriverManager.getConnection(URL, USER, PASSWORD);

    }
}
