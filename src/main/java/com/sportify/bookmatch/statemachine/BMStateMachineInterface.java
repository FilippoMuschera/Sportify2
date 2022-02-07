package com.sportify.bookmatch.statemachine;

public interface BMStateMachineInterface {

    public void goNext();

    public void executeEntry(String context);

}
