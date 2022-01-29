package com.sportify.addsportcenter;

import com.sportify.geolocation.Geolocator;
import com.sportify.sportcenter.*;
import com.sportify.sportcenter.courts.TimeSlot;
import com.sportify.sportcenter.exceptions.SportCenterException;
import com.sportify.user.UserEntity;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


public class AddSportCenterController {
    public void addSportCenter(AddSportCenterBean bean) throws SportCenterException, IllegalArgumentException {


        //Calcolo coordinate del campo dall'indirizzo
        Geolocator g = Geolocator.getInstance();
        double lat = g.getLat(bean.getSportCenterAddress());
        double lng = g.getLng(bean.getSportCenterAddress());
        if (lat == -1 || lng == -1){
            throw new IllegalArgumentException("The address is incorrect, try again");
        }
        SportCenterEntity sportCenter = new SportCenterEntity();
        sportCenter.setCoordinates(lat, lng);

        //Aggiungo le info al campo sportivo
        UserEntity user = UserEntity.getInstance();
        SportCenterInfo info = new SportCenterInfo(user.getEmail(), bean.getSportCenterName(), bean.getSportCenterAddress(), user.getPreferences().isNotifications());
        sportCenter.setInfo(info);

        //Aggiungo gli orari al campo sportivo

        SportCenterTime times = new SportCenterTime(bean.getOpeningHour(), bean.getClosingHour());
        sportCenter.setTimetable(times);

        List<LocalTime> timeSlots = this.getTimeSlotsHours(sportCenter);

        //Aggiungo i campi disponibili al campo sportivo
        SportCenterCourts courts = new SportCenterCourts(bean.getNumOfFootballFields(), bean.getNumOfPadelCourts(),
                bean.getNumOfBasketCourts(), bean.getNumOfTennisCourts(), timeSlots);
        sportCenter.setCourts(courts);


        AddSportCenterDAO dao = new AddSportCenterDAO();
        try {
            dao.addSCToDB(sportCenter);

        } catch (SportCenterException e){
            /* Se una delle query per aggiungere lo sport center al DB non va a buon fine, bisogna essere sicuri che non
            * rimanga traccia dello sport center sul database: sarebbe infatti scorretto lo scenario in cui l'aggiunta delle info
            * dello sport center sia andata a buon fine, ma fallisca l'aggiunta dei campi o dei timeslot, perchè l'aggiunta
            * dello sport center sarebbe incompleta. In questo caso quindi, dobbiamo eliminare tutte le possibili tracce dello sport center
            * dal database. Per farlo abbiamo bisogno di eliminare lo sport center dalla tabella delle informazioni dello sport center.
            * Tutte le altre tabelle (riguardanti lo sport center) hanno infatti una foreign key verso questa tabella, impostata con
            * "ON DELETE CASCADE". Eliminando dunque lo Sport Center da questa tabella, a cascata verranno eliminate anche le informazioni sui campi
            * e/o time slot dalle altre tabelle.*/
            dao.rollbackSC(sportCenter);
            throw new SportCenterException("Unable to add the Sport Center"); //rilancio l'eccezione per la View
        }

    }

   private List<LocalTime> getTimeSlotsHours(SportCenterEntity sc) {
        LocalTime opening = sc.getTimetable().getOpeningTime();
        LocalTime closing = sc.getTimetable().getClosingTime();
        LocalTime actualTime = opening;
        List<LocalTime> timeSlots = new ArrayList<>();
        while (actualTime.plusHours(TimeSlot.DEFAULT_SLOT_DURATION).isBefore(closing)) { //c'è uno slot di DEFAULT_TIME disponibile
            LocalTime newI = LocalTime.of(actualTime.getHour(), 0);
            LocalTime newF = LocalTime.of(actualTime.plusHours(TimeSlot.DEFAULT_SLOT_DURATION).getHour(), 0);

            timeSlots.add(newI);
            timeSlots.add(newF);
            actualTime = actualTime.plusHours(TimeSlot.DEFAULT_SLOT_DURATION);
        }
        if (actualTime.isBefore(closing)) { //Se il DEFAULT_SLOT_DURATION viene impostato a 1h questa porzione di codice
            //non verrà mai eseguita. Non è comunque un caso che si prevede di usare.
            LocalTime newI = LocalTime.of(actualTime.getHour(), 0);
            LocalTime newF = LocalTime.of(closing.getHour(), 0);

            timeSlots.add(newI);
            timeSlots.add(newF);
        }

        return timeSlots;
    }

}
