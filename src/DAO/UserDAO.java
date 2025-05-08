package DAO;

import model.User;
import util.DatabaseConnection;

import java.sql.*;

public class UserDAO {


    /**
     * @param user The user to add to the database
     * @param connection The connection to the SQL database
     */
    public void createUser(User user, Connection connection) throws SQLException {
        String sql = """
    INSERT INTO users
      (first_name, last_name, username, password_hash, role)
    VALUES (?,?,?,?,?)
  """;
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getUsername());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getRole());            // ← new
            int affected = ps.executeUpdate();
            if (affected == 0)
                throw new SQLException("Creating user failed, no rows affected.");
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) user.setUserId(keys.getInt(1));
                else throw new SQLException("Creating user failed, no ID obtained.");
            }
        }
    }

    /**
     * Lets the user potentially update their password
     * @param userId the user id of the account
     * @param newPasswordHash the new password
     * @param conn the connectoin to the database
     * @return true or false based on success
     * @throws SQLException ensures proper conn3ection
     */
    public boolean updatePassword(int userId,
                                  String newPasswordHash,
                                  Connection conn) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE ID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setInt   (2, userId);
            return ps.executeUpdate() == 1;
        }
    }

    /**
     * returns a user object
     * @param id the id to fetch the user of
     * @return a user object
     * @throws SQLException
     */
    public static User getUserById(int id) throws SQLException {
        String sql = """
      SELECT ID, first_name, last_name, username, password_hash, role
        FROM users
       WHERE ID = ?
    """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new User(
                        rs.getInt   ("ID"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role")      // if you have a role column
                );
            }
        }
    }


    /**
     * @param username The username to grab the user from
     * @return the user associated with the username
     */
    public static User getUserByUsername(String username) throws SQLException {
        String sql = """
    SELECT ID, first_name, last_name, username, password_hash, role
      FROM users
     WHERE username = ?
  """;
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new User(
                        rs.getInt   ("ID"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        rs.getString("role")         // ← new
                );
            }
        }
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

}
