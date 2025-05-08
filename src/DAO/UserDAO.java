package DAO;

import model.User;
import util.DatabaseConnection;

import java.sql.*;

public class UserDAO {



    /**
     * Update the password hash for a user.
     * @param userId   the user’s ID
     * @param newHash  the new password hash
     * @param conn     open Connection
     * @return true if exactly one row was updated
     */
    public boolean updatePassword(int userId, String newHash, Connection conn) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE ID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newHash);
            ps.setInt(2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * Fetch a user by numeric ID.
     * @param userId the user’s ID
     * @return User or null
     */
    public static User getUserById(int userId) throws SQLException {
        String sql = """
            SELECT ID, first_name, last_name, username, password_hash
              FROM users
             WHERE ID = ?
        """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("ID"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("username"),
                            rs.getString("password_hash")
                    );
                }
            }
        }
        return null;
    }

    /**
     * @param user The user to add to the database
     * @param connection The connection to the SQL database
     */
    public void createUser(User user, Connection connection) throws SQLException {
        String sql = """
            INSERT INTO users (first_name, last_name, username, password_hash)
            VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getUsername());
            ps.setString(4, user.getPasswordHash());

            int affected = ps.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            // Grab the auto-generated user_id
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setUserId(keys.getInt(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        }
    }

    /**
     * @param username The username to grab the user from
     * @return the user associated with the username
     */
    public static User getUserByUsername(String username) throws SQLException {
        String sql = """
            SELECT ID, first_name, last_name, username, password_hash
              FROM users
             WHERE username = ?
        """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int    id    = rs.getInt("ID");
                    String fn    = rs.getString("first_name");
                    String ln    = rs.getString("last_name");
                    String un    = rs.getString("username");
                    String phash = rs.getString("password_hash");

                    // Use the constructor that accepts userId
                    return new User(id, fn, ln, un, phash);
                }
            }
        }
        return null;
    }

    /**
     * @param user The user to update
     * @param connection The sql connection
     */
    public void updateUser(User user, Connection connection){

    }

    /**
     * @param username The username to search the database for
     * @param connection The SQL connection
     * @return true or false is username is found
     */
    public boolean doesUsernameExist(String username, Connection connection){
        // sql prompt to check if user exists
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";

        try(PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setString(1, username);
            try(ResultSet rs = stmt.executeQuery()) {
                if(rs.next()){
                    // If count > 0, username is already in database
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return false;
    }


//    /**
//     * @param email the email to check the database for
//     * @param connection the sql connection
//     * @return true or false based on the result
//     * Checks to see if the email already exists in the database
//     */
//    public boolean doesEmailExist(String email, Connection connection){
//        // sql prompt to check if user email exists
//        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
//
//        try(PreparedStatement stmt = connection.prepareStatement(sql)){
//            stmt.setString(1, email);
//            try(ResultSet rs = stmt.executeQuery()) {
//                if(rs.next()){
//                    // If count > 0, username is already in database
//                    int count = rs.getInt(1);
//                    return count > 0;
//                }
//            }
//        }catch (SQLException e){
//            e.printStackTrace();
//        }
//        return false;
//    }
}
