package com.sportify.bookmatch.statemachine;

import com.sportify.bookmatch.BookMatchController;
import com.sportify.bookmatch.exception.NoSportCenterException;
import com.sportify.geolocation.Geolocator;
import com.sportify.sportcenter.GetSportCenterDAO;
import com.sportify.sportcenter.exceptions.SportCenterException;
import com.sportify.user.UserEntity;

import java.util.Map;


public class SportCenterState implements BMStateInterface {

    private BookMatchController bookMatchController = BookMatchController.getBookMatchControllerInstance();
    private static final int MAX_NUMBER_OF_RESULTS = 3;
    private static SportCenterState instance = null;
    private static final int RETRY_CONSTANT = 30;


    protected SportCenterState(){}

    public static SportCenterState getSportCenterState(){
        if (SportCenterState.instance == null){
            SportCenterState.instance = new SportCenterState();
        }
        return SportCenterState.instance;
    }

    @Override
    public void entry(String userSelectedSport) throws NoSportCenterException{

        bookMatchController.setSelectedSport(userSelectedSport);

        String userAddress = UserEntity.getInstance().getPreferences().getUserAddress();
        Geolocator g = Geolocator.getInstance();
        double lat = g.getLat(userAddress);
        double lng = g.getLng(userAddress);

        Map<String, Double> nearSportCenters = null;

        try {
            nearSportCenters = GetSportCenterDAO.getInstance().getNearSportCenters(userSelectedSport, MAX_NUMBER_OF_RESULTS, lat, lng);
        }
        catch(SportCenterException exception1){
            UserEntity user = UserEntity.getInstance();
            int currentDistance = user.getPreferences().getSortingDistance();
            user.getPreferences().setRadiusOfInterest(currentDistance+RETRY_CONSTANT);
            try{
                nearSportCenters = GetSportCenterDAO.getInstance().getNearSportCenters(userSelectedSport, MAX_NUMBER_OF_RESULTS, lat, lng);
            }
            catch(SportCenterException exception2){
                user.getPreferences().setRadiusOfInterest(currentDistance);
                throw new NoSportCenterException();
            }
            user.getPreferences().setRadiusOfInterest(currentDistance);
        }
        bookMatchController.setNearSportCentersMap(nearSportCenters);
    }

    @Override
    public void goNext(){
        BMStateMachineImplementation stateMachine = BMStateMachineImplementation.getBMStateMachineImplementation();
        stateMachine.setState(CourtState.getCourtStateInstance());
    }
}
