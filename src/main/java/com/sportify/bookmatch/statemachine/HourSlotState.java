package com.sportify.bookmatch.statemachine;

import com.sportify.bookmatch.BookMatchController;
import com.sportify.sportcenter.courts.SportCourt;
import com.sportify.sportcenter.courts.TimeSlot;
import com.sportify.sportcenter.exceptions.SportCenterException;

import java.util.ArrayList;
import java.util.List;


public class HourSlotState implements BMStateInterface {

    private BookMatchController bookMatchController = BookMatchController.getBookMatchControllerInstance();

    @Override
    public void entry(String court) throws SportCenterException {

        int maxCourtSpot = bookMatchController.getCourtList().get(Integer.parseInt(court)).getMaxSpots();
        bookMatchController.setSelectedCourtID(Integer.valueOf(court));

        List<SportCourt> courtList = bookMatchController.getCourtList();

        List<TimeSlot> timeTable = new ArrayList<>();
        List<TimeSlot> rawTimeTable = courtList.get(bookMatchController.getSelectedCourtID()).getBookingTable();
        for(TimeSlot t:rawTimeTable){
            if(t.getAvailableSpots() == maxCourtSpot){
                timeTable.add(t);
            }
        }
        if(timeTable.isEmpty()){
            throw new SportCenterException("List<TimeSlot> is null, change the selected court.");
        }
        bookMatchController.setTimeTable(timeTable);
    }
}
