package model;

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





}

