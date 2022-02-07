package com.sportify.bookmatch.statemachine;

import com.sportify.bookmatch.BookMatchController;
import com.sportify.bookmatch.exception.NoTimeSlotException;
import com.sportify.sportcenter.courts.SportCourt;
import com.sportify.sportcenter.courts.TimeSlot;

import java.util.ArrayList;
import java.util.List;


public class HourSlotState implements BMStateInterface {

    private BookMatchController bookMatchController = BookMatchController.getBookMatchControllerInstance();
    private static HourSlotState instance = null;

    protected HourSlotState(){}

    public static HourSlotState getHourSlotInstance(){
        if (HourSlotState.instance == null){
            HourSlotState.instance = new HourSlotState();
        }
        return HourSlotState.instance;
    }

    @Override
    public void entry(String court) throws NoTimeSlotException {

        int maxCourtSpot = bookMatchController.getReturnCourtList().get(Integer.parseInt(court)).getMaxSpots();
        bookMatchController.setSelectedCourtID(Integer.parseInt(court));

        List<SportCourt> courtList = bookMatchController.getReturnCourtList();

        List<TimeSlot> timeTable = new ArrayList<>();
        List<TimeSlot> rawTimeTable = courtList.get(bookMatchController.getSelectedCourtID()).getBookingTable();
        for(TimeSlot t:rawTimeTable){
            if(t.getAvailableSpots() == maxCourtSpot){
                timeTable.add(t);
            }
        }
        if(timeTable.isEmpty()){
            throw new NoTimeSlotException();
        }
        bookMatchController.setReturnTimeTable(timeTable);
    }

    @Override
    public void goNext(){
        BMStateMachineImplementation stateMachine = BMStateMachineImplementation.getBMStateMachineImplementation();
        stateMachine.setState(new SelectionState());
    }

    @Override
    public void goBack(){
        BMStateMachineImplementation stateMachine = BMStateMachineImplementation.getBMStateMachineImplementation();
        stateMachine.setState(CourtState.getCourtStateInstance());
    }

}
