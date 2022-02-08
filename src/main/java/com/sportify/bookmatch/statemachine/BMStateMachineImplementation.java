package com.sportify.bookmatch.statemachine;


public class BMStateMachineImplementation implements BMStateMachineInterface {

    private BMStateInterface currentState;

    private static BMStateMachineImplementation singleBMSMInstance = null;

    protected BMStateMachineImplementation(){
        currentState = new InitState();
    }

    public static BMStateMachineImplementation getBMStateMachineImplementation(){
        if (BMStateMachineImplementation.singleBMSMInstance == null){
            BMStateMachineImplementation.singleBMSMInstance = new BMStateMachineImplementation();
        }
        return BMStateMachineImplementation.singleBMSMInstance;
    }

    @Override
    public void goNext(){
        currentState.goNext();
    }

    public void executeEntry(String context){
        currentState.entry(context);
    }

    public BMStateInterface getState(){
        return this.currentState;
    }

    public void setState(BMStateInterface state){
        this.currentState = state;
    }
}

