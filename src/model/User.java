package model;//Author: Ricky Franco
//25 Mar 2025
//model.User.java:

import java.util.Objects;

public class User {


    private Integer userId;
     final String FIRST_NAME;
     final String LAST_NAME;
    private String username;
    private String passwordHash;
    private final String role;

    /**
     * @param firstName    first name of the user
     * @param lastName     last name of the user
     * @param username     users username to login
     * @param passwordHash users password to login
     */
    public User(String firstName, String lastName, String username, String passwordHash) {
        this(null, firstName, lastName, username, passwordHash, "USER");
    }

    public User(Integer userId, String firstName, String lastName,
                String username, String passwordHash, String role) {
        this.userId       = userId;
        this.FIRST_NAME   = firstName;
        this.LAST_NAME    = lastName;
        this.username     = username;
        this.passwordHash = passwordHash;
        this.role         = role;
    }

    public void setUserId(int id) {
        this.userId = id;
    }

    public int getUserId() {
        return userId;
    }

    public String getRole() { return role; }
    public boolean isAdmin() { return "ADMIN".equals(role); }

    /**
     * @param newUsername the potential new username
     * @return true or false based on the result
     * setUsername: Allows the user to potentially change their username
     */
    public boolean setUsername(String newUsername){
        if(DoesUsernameExist(username)){
            this.username = username;
            return true;
        }
        return false;
    }

    /**
     * @param newPassword The potential new password for the user
     * @return True or false based on the result
     * setPassword: A setter method for the user to potentially change their password
     */
    public boolean setPassword(String newPassword){
        if(!this.username.equals(HashPassword(newPassword))) {
            this.passwordHash = HashPassword(newPassword);

            //Store the new password in the database

            return true;
        }
        return false;
    }



    /**
     * @return return users username
     * getUsername: Getter method to return users' address
     */
    public String getUsername() {

        return username;
    }

    /**
     * @return returns the users' hashed password
     * getPasswordHash: Getter method to return users' address
     */
    public String getPasswordHash() {
        return passwordHash;
    }


    public String getFirstName() {
        return FIRST_NAME;
    }

    public String getLastName() {
        return LAST_NAME;
    }





    //------------- Other methods---------------------


    /**
     * @param username the username to be checked
     * @return true or false based on a result of database
     * DoesUsernameExist: Checks database to check if username exists or not
     */
    public static boolean DoesUsernameExist(String username){
        return false;
    }


    /***
     *
     * @param passwordHash the password to be hashed
     * @return a fully hashed password
     * HashPassword: Perform a hash algorithm to encrypt the user's password
     */
    private static String HashPassword(String passwordHash){
        return "";
    }


}

