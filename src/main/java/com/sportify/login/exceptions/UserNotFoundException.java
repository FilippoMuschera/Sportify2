package com.sportify.login.exceptions;

public class UserNotFoundException extends Exception{
    public UserNotFoundException() {
        super("The user cannot be find in the Database");
    }
}
