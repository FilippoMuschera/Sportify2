package com.sportify.bookmatch.statemachine;

public interface BMStateInterface {

    public void entry(String string);
    public void goNext();
    public void goBack();
}
