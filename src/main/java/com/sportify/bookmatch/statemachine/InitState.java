package com.sportify.bookmatch.statemachine;

public class InitState implements BMStateInterface{

    @Override
    public void entry(String string) {
        //initState è uno state di "transazione"
        //non ha quindi bisogno del metodo entry
    }

    @Override
    public void goNext() {
        BMStateMachineImplementation stateMachine = BMStateMachineImplementation.getBMStateMachineImplementation();
        stateMachine.setState(new SportCenterState());
    }

    @Override
    public void goBack(){
        //InitState non necessita di metodo goBack
    }
}
