package service;

import model.User;

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

        //Ensure username does not exist
        if(User.DoesUsernameExist(username)){
            return null;
        }

        //Ensure email does not already exist
        if(true){
            return null;
        }

        //Add user information into database



        //return the user if successfully added to database
        if(true){
            User newUser = new User(firstName, lastName, DOB, email, phoneNumber, address, username, password);
            return  newUser;
        }
        return null;

    }

    }
