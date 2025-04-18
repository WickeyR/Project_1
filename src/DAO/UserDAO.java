package DAO;

import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {


    /**
     * @param user The user to add to the database
     * @param connection The connection to the SQL database
     */
    public void createUser(User user, Connection connection){

    }

    /**
     * @param username The username to grab the user from
     * @param connection The sql connection
     * @return the user associated with the username
     */
    public User getUserByUsername(String username, Connection connection){


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


    /**
     * @param email the email to check the database for
     * @param connection the sql connection
     * @return true or false based on the result
     * Checks to see if the email already exists in the database
     */
    public boolean doesEmailExist(String email, Connection connection){
        // sql prompt to check if user email exists
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";

        try(PreparedStatement stmt = connection.prepareStatement(sql)){
            stmt.setString(1, email);
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
