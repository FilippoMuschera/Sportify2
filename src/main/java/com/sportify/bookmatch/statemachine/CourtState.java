package com.sportify.bookmatch.statemachine;

import com.sportify.bookmatch.BookMatchController;
import com.sportify.sportcenter.GetSportCenterDAO;
import com.sportify.sportcenter.SportCenterCourts;
import com.sportify.sportcenter.SportCenterEntity;
import com.sportify.sportcenter.courts.SportCourt;

import java.util.List;


public class CourtState implements BMStateInterface {

    private List<SportCourt> courtList;
    private BookMatchController bookMatchController = BookMatchController.getBookMatchControllerInstance();
    private static CourtState instance = null;

    protected CourtState(){}

    public static CourtState getCourtStateInstance(){
        if (CourtState.instance == null){
            CourtState.instance = new CourtState();
        }
        return CourtState.instance;
    }

    @Override
    public void entry(String sportCenterName){

        String selectedSport = bookMatchController.getSelectedSport();
        bookMatchController.setSelectedSportCenter(sportCenterName);

        SportCenterEntity entitySC = GetSportCenterDAO.getInstance().getSportCenter(sportCenterName,selectedSport);

        SportCenterCourts allCourts = entitySC.getCourts();

        switch(selectedSport){
            case "Basket":
                courtList = allCourts.getBasketCourts();
                break;
            case "Football":
                courtList = allCourts.getFootballFields();
                break;
            case "Padel":
                courtList = allCourts.getPadelCourts();
                break;
            case "Tennis":
                courtList = allCourts.getTennisCourts();
                break;
            default:
        }

        bookMatchController.setReturnCourtList(courtList);
    }

    @Override
    public void goNext(){
        BMStateMachineImplementation stateMachine = BMStateMachineImplementation.getBMStateMachineImplementation();
        stateMachine.setState(HourSlotState.getHourtSlotInstance());
    }
}
