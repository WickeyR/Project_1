package service;

import DAO.UserDAO;
import model.User;
import util.DatabaseConnection;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.SQLException;

//Author: Ricky Franco
//01 April 2025
//service.AuthenticationService.java: Connects with SQL database to confirm user information
public class AuthenticationService {


    /**
     * Change a user’s password.
     * @param userId       the user’s ID
     * @param oldPassword  their current password
     * @param newPassword  the new desired password
     * @return true if change succeeded
     */
    public boolean changePassword(int userId, String oldPassword, String newPassword) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            UserDAO dao = new UserDAO();


            //Grab the users account
            User u = UserDAO.getUserById(userId);
            if (u == null) return false;

            // ensure the old password matches
            String oldHash = encryptPassword(oldPassword);
            if (!oldHash.equals(u.getPasswordHash())) {
                System.out.println("Current password incorrect.");
                return false;
            }

            // ahsh current password and update
            String newHash = encryptPassword(newPassword);
            if (newHash.equals(oldHash)) {
                System.out.println("New password must differ from old password.");
                return false;
            }
            boolean ok = dao.updatePassword(userId, newHash, conn);
            System.out.println(ok ? "Password changed." : "Failed to change password.");
            return ok;
        } catch (SQLException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }




    /**
     * @param firstName users first name
     * @param lastName users last name
     * @param username username
     * @param password password
     * @return return the user
     */
    public User registerUser(String firstName, String lastName, String username, String password) {

        //Attempt to grab connection
        try (Connection connection = DatabaseConnection.getConnection()) {

            UserDAO userDAO = new UserDAO();

            //Check if username already exists
            if (userDAO.doesUsernameExist(username, connection)) {
                System.out.println("Username already exists");
                return null;
            }

            //user does not exist, hash password and create user
            String hashedPassword = encryptPassword(password);


            //Create new user object
            User newUser = new User(firstName, lastName, username, hashedPassword);


            //Insert the new user into the database
            userDAO.createUser(newUser, connection);

            // Grant them a checking account
            AccountService acctSvc = new AccountService();
            acctSvc.openDefaultChecking(newUser.getUserId(), connection);

            //return user if success
            return newUser;


        }

        //Catch potential SQL error
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * encryps the password using a basic algorithm
     * @param password The string password before hashing
     * @return the new string of the password post encryption
     * @throws NoSuchAlgorithmException catches the algorithm not existing
     */
    public static String encryptPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));

        // Convert the byte array to a hexadecimal string
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }


    /**
     * @param username the username of the user
     * @param password the password of the user
     * @return true or false based on if password is correct
     */
    public static boolean login(String username, String password){
        try(Connection conn = DatabaseConnection.getConnection()){
            UserDAO userDAO = new UserDAO();

            //Grab the users username
            User user = userDAO.getUserByUsername(username);
            if(user == null){
                System.out.println("User not found");
                return false;
            }

            //check if the password equals the hashed password of the user stored in the database
            // if the password is correct return true
            if(user.getPasswordHash().equals(encryptPassword(password))){
                return true;
            }else{
                System.out.println("Wrong password");
                return false;
            }
        }catch (SQLException e){
            e.printStackTrace();
            return false;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}




