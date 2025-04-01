//Author: Ricky Franco
//25 Mar 2025
//User.java:

import java.util.Objects;

public class User {


    final int USER_ID;
    final String FIRST_NAME;
    final String LAST_NAME;

    final int DATE_OF_BIRTH;
    private String email;

    private int phoneNumber;

    private String address;

    private String username;
    private String passwordHash;


    /**
     * @param firstName    first name of the user
     * @param lastName     last name of the user
     * @param DOB          users date of birth
     * @param email        users email
     * @param phoneNumber  users phone number
     * @param address      users address
     * @param username     users username to login
     * @param passwordHash users password to login
     */
    public User(String firstName, String lastName, int DOB, String email, int phoneNumber,
                String address, String username, String passwordHash) {

        //Initialize user information
        this.USER_ID = userid;
        this.FIRST_NAME = firstName;
        this.LAST_NAME = lastName;
        this.DATE_OF_BIRTH = DOB;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        if (DoesUsernameExist(username)) {
            this.username = username;
        }else{
            //Prompt the user for the username again
        }
        this.passwordHash = HashPassword(passwordHash);
    }





    //------------------ SETTER METHODS --------------//

    /**
     * @param newEmail The new email address
     * @return true or false based on success
     * setEmail: Allows user to potentially set new email
     */
    public boolean setEmail(String newEmail){
        boolean isValid = true;
        //Check if email is different from current email

        //Check to make sure email not already in the database

        if(isValid){
            this.email = newEmail;

            //Update the email in the SQL database

            //Email change success
            return true;
        }else{

            //Email change failure
            return false;
        }
    }

    /**
     * @param newPhoneNumber the new potential phone number for the user
     * @return true or false based on success
     * setPhoneNumber: Allows user to potentially set new phone number
     */
    public boolean setPhoneNumber(int newPhoneNumber){
        Boolean isValid = true;
        //Check if the phone number is different from the current phone number

        //Check to make sure phone number not already in the database

        if(isValid){
            this.phoneNumber = newPhoneNumber;

            //Update the phone number in the SQL database

            //phone number change success
            return true;
        }else{

            //phone number change failure
            return false;
        }
    }


    /**
     * @param newAddress The potential new address
     * @return true or false based on success
     * setAddress: Allows user to potentially change their address
     */
    public boolean setAddress(String newAddress){
        //Check if newAddress is distinct from current
        if(!Objects.equals(getAddress(), newAddress)){
            this.address = newAddress;

            //Return Success
            return true;
        }
        //newAddress is identical to current
        return false;
    }

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

    //------------------ GETTER METHODS --------------//

    /**
     * @return the email address of the user
     * getEmail: Returns the users email
     */
    public String getEmail(){
        return this.email;
    }

    /**
     * @return users phone number
     * getPhoneNumber: Getter to return the number of the user
     */
    public int getPhoneNumber(){
        return this.phoneNumber;
    }


    /**
     * @return users' address
     * getAddress: Getter method to return users' address
     */
    public String getAddress() {
        return address;
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

