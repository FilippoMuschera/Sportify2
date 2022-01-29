import com.sportify.bookmatch.BookMatchController;
import com.sportify.sportcenter.*;
import com.sportify.sportcenter.courts.SportCourt;
import com.sportify.sportcenter.courts.TennisCourt;
import com.sportify.sportcenter.courts.TimeSlot;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


//Test di Gianluca Maccari

public class TestBookMatchController {

    private String testSportCenterName = "Test Sporting Club";
    private String testSport = "Football";
    private int testIdCourt = 3;
    private BookMatchController bookMatchController = BookMatchController.getBookMatchControllerInstance();
    private int testStartTime = 6;
    private int testFinishTime = 8;
    private List<SportCourt> testCourtList;
    private List<TimeSlot> testTimetable;
    private int testSpotsLeft = 0;

    //per cambiare tipo di TimeSlot occhio ai maxSpots e il testSport
    //Football 10, Padel 4, Tennis 4, Basket 10

    private int testMaxSpots = 10;

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
        SportCenterInfo testSCInfo = new SportCenterInfo("",testSportCenterName,"",false);

        testEntity.setCourts(court);

        testEntity.setInfo(testSCInfo);

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

        bookMatchController.setSelectedSport(testSport);
        List<SportCourt> resultCourtList = bookMatchController.selectedSportCenter(testSportCenterName);
        deleteTestSCEntity();

        assertEquals(testCourtList,resultCourtList);
    }

    @Test
    void testSelectedCourt(){
        createTestSCEntity();

        List<TimeSlot> resultTimeTable = bookMatchController.selectedCourt(""+testIdCourt);
        deleteTestSCEntity();

        assertEquals(testTimetable,resultTimeTable);
    }

    @Test
    void testBookMatch(){
        createTestSCEntity();

        bookMatchController.setSelectedTimeSlot(testTimetable.get(0));
        bookMatchController.setTimeTable(testTimetable);
        bookMatchController.bookMatch();

        SportCenterCourts resultCourts = GetSportCenterDAO.getInstance().getSportCenter(testSportCenterName,testSport).getCourts();
        List<SportCourt> resultCourtList = null;

        switch(testSport){
            case "Basket":
                resultCourtList = resultCourts.getBasketCourts();
                break;
            case "Football":
                resultCourtList = resultCourts.getFootballFields();
                break;
            case "Padel":
                resultCourtList = resultCourts.getPadelCourts();
                break;
            case "Tennis":
                resultCourtList = resultCourts.getTennisCourts();
                break;
        }
        int resultSpotsLeft = resultCourtList.get(0).getBookingTable().get(0).getAvailableSpots();
        deleteTestSCEntity();

        assertEquals(testSpotsLeft,resultSpotsLeft);
    }

}
