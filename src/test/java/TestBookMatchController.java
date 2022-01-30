import com.sportify.bookmatch.BookMatchController;
import com.sportify.bookmatch.statemachine.BMStateMachineImplementation;
import com.sportify.sportcenter.*;
import com.sportify.sportcenter.courts.SportCourt;
import com.sportify.sportcenter.courts.TennisCourt;
import com.sportify.sportcenter.courts.TimeSlot;
import com.sportify.user.UserEntity;
import com.sportify.user.UserPreferences;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/*************
Test di Gianluca Maccari
**************/
class TestBookMatchController {

    private String testSportCenterName = "TestSportingClub";
    private String testSport = "Tennis";
    private int testIdCourt = 0;
    private BookMatchController bookMatchController = BookMatchController.getBookMatchControllerInstance();
    private int testStartTime = 6;
    private int testFinishTime = 8;
    private List<SportCourt> testCourtList;
    private List<TimeSlot> testTimetable;
    private int testSpotsLeft = 0;


    private int testMaxSpots = 4;

    private SportCenterEntity createdEntity;


    //metodo che aggiunge la SportCenterEntity di test nel database
    private void createTestSCEntity(){

        TimeSlot timeSlot = new TimeSlot(LocalTime.of(testStartTime,0),LocalTime.of(testFinishTime,0),testMaxSpots);
        List<TimeSlot> testList = new ArrayList<TimeSlot>();
        testList.add(timeSlot);

        testTimetable = testList;

        TennisCourt testCourt = new TennisCourt(testList,testIdCourt);

        SportCenterCourts court = new SportCenterCourts();
        court.addField(testSport,testCourt);

        testCourtList = new ArrayList<SportCourt>();
        testCourtList.add(testCourt);

        SportCenterEntity testEntity = new SportCenterEntity();
        SportCenterInfo testSCInfo = new SportCenterInfo("admin@admin.it",testSportCenterName,"Viale degli ingegneri, 1, Roma, 00133",false);

        testEntity.setCourts(court);

        testEntity.setInfo(testSCInfo);

        UserEntity user = UserEntity.getInstance();
        UserPreferences preferences = new UserPreferences(3, true, true, true, true, false, "Viale degli ingegneri, 1, Roma, 00133");
        user.setPreferences(preferences);

        SportCenterTime sportCenterTime = new SportCenterTime(4,22);

        testEntity.setTimetable(sportCenterTime);

        AddSportCenterDAO testDAO = new AddSportCenterDAO();
        testDAO.addSCToDB(testEntity);
        createdEntity = testEntity;

    }

    //metodo che elimina la SportCenterEntity di test dal database
    private void deleteTestSCEntity(){
        AddSportCenterDAO testDAO = new AddSportCenterDAO();
        testDAO.rollbackSC(createdEntity);
    }

    @Test
    void testSelectedSportCenter(){
        createTestSCEntity();

        bookMatchController.istantiateStateMachine();
        bookMatchController.setSelectedSport(testSport);
        List<SportCourt> resultCourtList = bookMatchController.selectedSportCenter(testSportCenterName);
        deleteTestSCEntity();

        assertEquals(testIdCourt,resultCourtList.get(0).getCourtID());
    }

    @Test
    void testSelectedCourt(){
        createTestSCEntity();

        bookMatchController.istantiateStateMachine();
        bookMatchController.setCourtList(testCourtList);
        List<TimeSlot> resultTimeTable = bookMatchController.selectedCourt(""+testIdCourt);
        deleteTestSCEntity();

        assertEquals(testTimetable.get(0).getStartTime().getHour(),resultTimeTable.get(0).getStartTime().getHour());
    }

    @Test
    void testBookMatch(){
        createTestSCEntity();

        bookMatchController.setSelectedSportCenter(testSportCenterName);
        bookMatchController.setSelectedSport(testSport);
        bookMatchController.setSelectedTimeSlot(testTimetable.get(0));
        bookMatchController.setTimeTable(testTimetable);
        bookMatchController.bookMatch();

        SportCenterCourts resultCourts = GetSportCenterDAO.getInstance().getSportCenter(testSportCenterName,testSport).getCourts();

        List<SportCourt> resultCourtList = switch (testSport) {
            case "Basket" -> resultCourts.getBasketCourts();
            case "Football" -> resultCourts.getFootballFields();
            case "Padel" -> resultCourts.getPadelCourts();
            case "Tennis" -> resultCourts.getTennisCourts();
            default -> null;
        };

        int resultSpotsLeft = resultCourtList.get(0).getBookingTable().get(0).getAvailableSpots();
        deleteTestSCEntity();

        assertEquals(testSpotsLeft,resultSpotsLeft);
    }

}
