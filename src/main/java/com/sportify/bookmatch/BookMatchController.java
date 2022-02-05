package com.sportify.bookmatch;

import com.sportify.bookmatch.exception.DeletedCourtException;
import com.sportify.bookmatch.exception.NoSportCenterException;
import com.sportify.bookmatch.exception.NoTimeSlotException;
import com.sportify.bookmatch.statemachine.BMStateMachineImplementation;
import com.sportify.email.EmailThread;
import com.sportify.sportcenter.AddSportCenterDAO;
import com.sportify.sportcenter.GetSportCenterDAO;
import com.sportify.sportcenter.SportCenterInfo;
import com.sportify.sportcenter.courts.SportCourt;
import com.sportify.sportcenter.courts.TimeSlot;
import com.sportify.user.UserEntity;

import java.util.List;
import java.util.Map;


public class BookMatchController {

    private BMStateMachineImplementation stateMachine;
    private String selectedSport;
    private int selectedCourtID;
    private TimeSlot selectedTimeSlot;
    private String selectedSportCenter;

    //attributi che sono usati dal bookMatchViewController
    private Map<String, Double> returnNearSportCenters;
    private List<TimeSlot> returnTimeTable;
    private List<SportCourt> returnCourtList;

    private static com.sportify.bookmatch.BookMatchController singleBookMatchControllerInstance = null;

    protected BookMatchController(){
        stateMachine = BMStateMachineImplementation.getBMStateMachineImplementation();
    }

    public static BookMatchController getBookMatchControllerInstance(){
        if (BookMatchController.singleBookMatchControllerInstance == null){
            BookMatchController.singleBookMatchControllerInstance = new BookMatchController();
        }
        return BookMatchController.singleBookMatchControllerInstance;
    }

    public void executeState(String contextString) throws NoSportCenterException, DeletedCourtException{

        stateMachine.getState().goNext();
        try{
            stateMachine.getState().entry(contextString);
        }
        catch(NoTimeSlotException e){

            for(SportCourt s: returnCourtList){
                if(s.getCourtID() == Integer.parseInt(contextString)){
                    returnCourtList.remove(s);
                    break;
                }
            }
            throw new DeletedCourtException();
        }

    }

    public void createJoinMatch(){

        selectedTimeSlot.setAvailableSpots(selectedTimeSlot.getAvailableSpots()-1);
        updateAvailableSpots();
        this.sendEmail();
    }

    public void bookMatch(){

        selectedTimeSlot.setAvailableSpots(0);
        updateAvailableSpots();
        this.sendEmail();
    }

    private void updateAvailableSpots(){
        AddSportCenterDAO newAddSportCenterDAO = new AddSportCenterDAO();
        int spots = selectedTimeSlot.getAvailableSpots();
        int startTime = selectedTimeSlot.getStartTime().getHour();
        int finishTime = selectedTimeSlot.getEndTime().getHour();
        newAddSportCenterDAO.updateTimeSlot(spots, selectedCourtID, selectedSport, selectedSportCenter, startTime, finishTime);
    }

    public void sendEmail(){

        UserEntity user = UserEntity.getInstance();
        SportCenterInfo sportCenterInfo = GetSportCenterDAO.getInstance().getSportCenter(selectedSportCenter,selectedSport).getInfo();
        int startTime = selectedTimeSlot.getStartTime().getHour();
        int finishTime = selectedTimeSlot.getEndTime().getHour();
        SportCenterInfo infoSportCenter = GetSportCenterDAO.getInstance().getSportCenter(selectedSportCenter,selectedSport).getInfo();
        if(user.getPreferences().isNotifications()) {
            EmailThread playerEmailThread = new EmailThread(selectedSport, selectedCourtID, startTime, finishTime, infoSportCenter.getSportCenterAddress());
            playerEmailThread.setPlayer(true);
            playerEmailThread.start();
        }
        if(sportCenterInfo.isNotifications()) {
            EmailThread ownerEmailThread = new EmailThread(infoSportCenter.getOwnerEmail(), selectedSport, selectedCourtID, startTime, finishTime);
            ownerEmailThread.setOwner(true);
            ownerEmailThread.start();
        }
    }

    public void executeGoBack(){
        stateMachine.getState().goBack();
    }

    public void setReturnCourtList(List<SportCourt> list){
        this.returnCourtList = list;
    }

    public List<SportCourt> getReturnCourtList(){
        return this.returnCourtList;
    }

    public String getSelectedSport(){
        return this.selectedSport;
    }

    public void setSelectedSport(String sport){
        selectedSport = sport;
    }

    public void setSelectedTimeSlot(TimeSlot hourSlot){
        selectedTimeSlot = hourSlot;
    }

    public void setSelectedSportCenter(String selectedSportCenter){
        this.selectedSportCenter = selectedSportCenter;
    }

    public void setNearSportCentersMap(Map<String, Double> nearSportCenters){
        this.returnNearSportCenters = nearSportCenters;
    }

    public Map<String, Double> getReturnNearSportCenters(){
        return returnNearSportCenters;
    }

    public int getSelectedCourtID(){
        return selectedCourtID;
    }

    public void setSelectedCourtID(int selectedCourtID){
        this.selectedCourtID = selectedCourtID;
    }

    public void setReturnTimeTable(List<TimeSlot> returnTimeTable){
        this.returnTimeTable = returnTimeTable;
    }

    public List<TimeSlot> getReturnTimeTable(){
        return returnTimeTable;
    }
}

