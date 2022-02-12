package com.sportify.bookmatch.statemachine;

import com.sportify.bookmatch.BookMatchController;

public class SelectionState implements BMStateInterface{

    BookMatchController bookMatchController = BookMatchController.getBookMatchControllerInstance();

    @Override
    public void entry(String operation) {
        if (operation.equals("Book Match")) {
            bookMatchController.bookMatch();
        }
        else if(operation.equals("Join Match")) {
            bookMatchController.createJoinMatch();
        }
    }
    @Override
    public void goNext(){
        BMStateMachineImplementation stateMachine = BMStateMachineImplementation.getBMStateMachineImplementation();
        stateMachine.setState(new InitState());
    }

    @Override
    public void goBack(){
        //questo metodo non viene mai invocato ma si potrebbe estendere il sistema e usarlo
    }
}
