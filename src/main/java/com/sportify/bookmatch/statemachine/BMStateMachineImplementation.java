package com.sportify.bookmatch.statemachine;


public class BMStateMachineImplementation implements BMStateMachineInterface {

    private BMStateInterface currentState;

    private static BMStateMachineImplementation singleBMSMInstance = null;

    protected BMStateMachineImplementation(){
        initializeState();
    }

    public static BMStateMachineImplementation getBMStateMachineImplementation(){
        if (BMStateMachineImplementation.singleBMSMInstance == null){
            BMStateMachineImplementation.singleBMSMInstance = new BMStateMachineImplementation();
        }
        return BMStateMachineImplementation.singleBMSMInstance;
    }

    private void initializeState(){
        currentState = new InitState();
    }

    @Override
    public BMStateInterface getState(){
        return this.currentState;
    }

    public void setState(BMStateInterface state){
        this.currentState = state;
    }
}

