package service;

import DAO.UserDAO;
import model.User;
import util.DatabaseConnection;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
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
     * @param email users email
     * @param password users password
     * @return true or false based on login success
     */
    public boolean Login(String email, String password){
        //Attempt to connect to log in to account through sql
        if(true){



            //Pass true which connects the user to their dashboard
            return true;
        }


        //Case if password does not exist
        return false;
    }


    /**
     * @return true or false based on successful logout
     */
    public boolean Logout(){
        if(true){
            return true;
        }
        //If login is not successful
        return false;
    }


    /**
     * @param firstName users first name
     * @param lastName users last name
     * @param DOB users date of birth
     * @param email users email
     * @param phoneNumber users phone number
     * @param address users address
     * @param username username
     * @param password password
     * @return return the user
     */
    public User registerUser(String firstName, String lastName, int DOB, String email,
                             int phoneNumber, String address, String username, String password) {

        //Attempt to grab connection
        try (Connection connection = DatabaseConnection.getConnection()) {

            UserDAO userDAO = new UserDAO();

            //Check if username already exists
            if (userDAO.doesUsernameExist(username, connection)) {
                System.out.println("Username already exists");
                return null;
            }
            //Check if email already exists
            if (userDAO.doesEmailExist(email, connection)) {
                System.out.println("Email already exists");
                return null;
            }

            //user does not exist, hash password and create user
            String hashedPassword = hashPassword(password);


            //Create new user object
            User newUser = new User(firstName, lastName, DOB, email, phoneNumber, address, username, hashedPassword);


            //Insert the new user into the database
            userDAO.createUser(newUser, connection);

            //return user if success
            return newUser;


        }

        //Catch potential SQL error
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }
    public static String hashPassword(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hashedPassword = factory.generateSecret(spec).getEncoded();

        // Convert byte array to a hexadecimal string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashedPassword) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

}




