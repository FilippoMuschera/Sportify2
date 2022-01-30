package com.sportify.signup.exceptions;

public class UserAlreadyExistsException extends Exception {
       public UserAlreadyExistsException(){
            super("*** User already registered ***");
        }
}
