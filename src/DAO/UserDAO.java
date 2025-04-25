package DAO;

import model.User;
import util.DatabaseConnection;

import java.sql.*;

public class UserDAO {


    /**
     * @param user The user to add to the database
     * @param connection The connection to the SQL database
     */
    public void createUser(User user, Connection connection)  {

        // SQL statement to insert user into the database

        //Remove Email, Phone, Address,
        String sql = "INSERT INTO users (first_name, last_name, username, password_hash) VALUES (?, ?, ?, ?)";

        try(PreparedStatement statement = connection.prepareStatement(sql)){
            // set string statements for user information into SQl database
            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getUsername());
            statement.setString(4, user.getPasswordHash());




            // Execute insertion into SQL database
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param username The username to grab the user from
     * @return the user associated with the username
     */
    public static User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)){
                statement.setString(1, username);
                try(ResultSet rs = statement.executeQuery()){
                    if(rs.next()){


                        //Construct the new user and return it
                        return new User(
                                rs.getString("first_name"),
                                rs.getString("last_name"),
                                rs.getString("username"),
                                rs.getString("password_hash")
                        );
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
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
