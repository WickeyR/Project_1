package DAO;

import model.User;

import java.sql.Connection;

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

        return false;

    }


    public boolean doesEmailExist(String email, Connection connection){


        return false;
    }
}
