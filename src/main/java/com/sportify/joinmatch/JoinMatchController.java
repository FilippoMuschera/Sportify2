package com.sportify.joinmatch;

import com.sportify.email.EmailThread;
import com.sportify.geolocation.Geolocator;
import com.sportify.sportcenter.AddSportCenterDAO;
import com.sportify.sportcenter.GetSportCenterDAO;
import com.sportify.sportcenter.SportCenterEntity;
import com.sportify.sportcenter.SportCenterInfo;
import com.sportify.sportcenter.courts.SportCourt;
import com.sportify.sportcenter.courts.TimeSlot;
import com.sportify.sportcenter.exceptions.SportCenterException;
import com.sportify.user.UserEntity;
import com.sportify.user.UserPreferences;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class JoinMatchController {

    private ResultSetEntity resultSet;
    private boolean isDistanceImportant;
    private boolean isAvailableSpotsImportant;


    public void findJoinableMatch(JoinMatchBean bean) throws SportCenterException{

        int maxSportCenter = 20; //valore arbitrario per la ricerca degli sport center
        resultSet = new ResultSetEntity();

        //Settiamo gli attributi per ponderare il peso di distanza e posti disponibili
        this.isDistanceImportant = bean.isDistanceIsImportant();
        this.isAvailableSpotsImportant = bean.isAvailableSpotIsImportant();

        //Per prima cosa bisogna trovare gli sport Center nel raggio dell'utente
        GetSportCenterDAO getSportCenterDAO = GetSportCenterDAO.getInstance();
        Geolocator g = Geolocator.getInstance();
        UserPreferences preferences = UserEntity.getInstance().getPreferences();

        //Uso la getNearSportCenter per trovare (al più) bean.getMaxResults() campi sportivi nel raggio selezionato dall'utente
        Map<String, Double> sportCenterMap = null;
        try {
             sportCenterMap = getSportCenterDAO.getNearSportCenters(bean.getSelectedSport(), maxSportCenter,
                    g.getLat(preferences.getUserAddress()), g.getLng(preferences.getUserAddress()));
        } catch (SportCenterException e){


            // Se non sono stati trovati i campi, proviamo a ripetere la ricerca con un raggio (temporaneamente) più ampio
            UserPreferences p = UserEntity.getInstance().getPreferences();
            int userRadius = p.getSortingDistance();
            int temporaryRadius = 25;
            p.setRadiusOfInterest(temporaryRadius);

            //Ora proviamo a ripetere la ricerca
            try {
                sportCenterMap = getSportCenterDAO.getNearSportCenters(bean.getSelectedSport(), maxSportCenter,
                        g.getLat(preferences.getUserAddress()), g.getLng(preferences.getUserAddress()));

                //Se la ricerca questa volta va a buon fine, reimpostiamo le opzioni dell'utente e procediamo con lo
                //use case

                p.setRadiusOfInterest(userRadius);

            } catch (SportCenterException ex){
                //Se anche questa volta non è stato possibile trovare dei centri sportivi non possiamo fare altro che
                //restituire un errore all'utente (che verrà quindi gestito dalla view dello use case)
                p.setRadiusOfInterest(userRadius);
                throw new SportCenterException("Impossibile trovare campo anche con raggio maggiorato");
            }

        }


        //Per ogni sport center che abbiamo trovato andiamo a caricare i campi relativi allo sport scelto dall'utente
        for (Map.Entry<String, Double> entry : sportCenterMap.entrySet()){
            SportCenterEntity currentSportCenter = getSportCenterDAO.getSportCenter(entry.getKey(), bean.getSelectedSport()); //carichiamo la SportCenterEntity dal DB
            for (SportCourt court : this.getSelectedSportCourts(currentSportCenter, bean.getSelectedSport())){

                //Per ogni campo sportivo scorriamo i time slot
                for (TimeSlot t : court.getBookingTable()){

                    //Il primo TimeSlot che inizia dopo l'orario scelto (o esattamente all'orario scelto) viene selezionato
                    if ((t.getStartTime().isAfter(LocalTime.of(bean.getPreferredStartingTime(), 0)) ||
                            t.getStartTime().getHour() == bean.getPreferredStartingTime()) &&
                    t.getAvailableSpots() < court.getMaxSpots()){

                        //Si costruisce il ResultElement
                        ResultElement resultElement = new ResultElement();
                        resultElement.setCourtID(court.getCourtID());
                        resultElement.setNameSC(currentSportCenter.getInfo().getSportCenterName());
                        resultElement.setTimeSlot(t);
                        resultElement.setDistance(entry.getValue());
                        resultElement.setMaxSpots(court.getMaxSpots());
                        resultElement.setSport(bean.getSelectedSport());

                        //Si aggiunge il ResultElement al ResultSet
                        this.resultSet.addElement(resultElement);
                        break; //si esce dal ciclo for sui TimeSlot perché abbiamo preso quello che ci interessava
                    }
                }
            }
        }
        //A questo punto il nostro ResultSetEntity aggrega tutti i ResultElement che ci servono, ora bisogna ordinarli
        //in base al loro indexValue
        this.evaluateIndexValues();
        int min = Math.min(this.resultSet.getElements().size(), bean.getMaxResults());

        this.checkForEmptyList(min);

        this.shrinkResult(min);


        bean.setResultSet(this.resultSet);


    }

    private void checkForEmptyList(int min) throws SportCenterException {
        if (min == 0){ //ci sono sport center che rispettano i parametri, ma non ci sono joinable matches in questi sc
            // e allora la lista degli sport center sarà vuota
            throw new SportCenterException("Non ci sono joinable matches disponibili");

        }
    }

    private void shrinkResult(int min){
        ArrayList<ResultElement> shrunkList = new ArrayList<>();

        for (int i = 0; i< min; i++){
            shrunkList.add(this.resultSet.getElements().get(i));
        }
        resultSet.setElementsList(shrunkList);

    }

    private void evaluateIndexValues() {

        //Setta l'indexValue di ogni ResultElement
        for (ResultElement element : resultSet.getElements())
            element.setIndexValue(this.calculateIndexValue(element));



        Comparator<ResultElement> indexValueComparator = Comparator.comparingDouble(ResultElement::getIndexValue);
        // Il comparatore compara gli index value dei ResultElement


        this.resultSet.getElements().sort(indexValueComparator); //ordiniamo gli elementi in ordine crescente d'indexValue


    }

    private double calculateIndexValue(ResultElement element) {

        /*
         * L'indexValue viene usato per classificare i vari JoinableMatch dal migliore al peggiore. Per calcolarlo si
         * usano: il raggio di ricerca scelto dall'utente, il numero massimo di posti che un campo può avere, la distanza
         * del campo dall'utente e i posti liberi rimanenti.
         * La formula base è (distanzaDallUtente/RaggioSceltoDallUtente) + (postiRimasti/massimoNumeroDiPosti).
         * Inoltre, per tenere conto delle preferenze dell'utente si "pesano" questi due elementi: il primo elemento è
         * moltiplicato per pondDist, il secondo per pondAvailableSpots. In base alle scelte dell'utente, queste due variabili
         * possono avere "highWeight" o "lowWeight", così da dare più peso al fatto che un joinableMatch sia in un campo
         * vicino, oppure che rimangano pochi posti (e che quindi sia quasi completo).
         * I ResultElement vengono poi ordinati per ordine crescente di indexValue: un valore di indexValue più vicino allo
         * zero è preferibile, perchè significa che il joinable match rispecchia meglio le richieste dell'utente.
         */

        double selectedDistance = UserEntity.getInstance().getPreferences().getSortingDistance();
        double maxAvailableSpots = element.getMaxSpots();
        double highWeight = 5;
        double lowWeight = 1;
        double pondDist = isDistanceImportant ? highWeight : lowWeight;
        double pondAvailableSpots = isAvailableSpotsImportant ? highWeight : lowWeight;

        double distValue = (element.getDistance()/selectedDistance)*pondDist;
        double availableSpotsValue = (element.getTimeSlot().getAvailableSpots()/maxAvailableSpots)*pondAvailableSpots;
        return (distValue + availableSpotsValue);
    }

    private List<SportCourt> getSelectedSportCourts(SportCenterEntity currentSportCenter, String selectedSport) {
        return switch (selectedSport) {
            case "Football" -> currentSportCenter.getCourts().getFootballFields();
            case "Padel" -> currentSportCenter.getCourts().getPadelCourts();
            case "Tennis" -> currentSportCenter.getCourts().getTennisCourts();
            case "Basket" -> currentSportCenter.getCourts().getBasketCourts();
            default -> null;
        };
    }

    public void joinMatch(ResultElement selectedMatch){

        AddSportCenterDAO newAddSportCenterDAO = new AddSportCenterDAO();
        int spots = selectedMatch.getTimeSlot().getAvailableSpots();
        int selectedCourtID = selectedMatch.getCourtID();
        String selectedSport = selectedMatch.getSport();
        String selectedSportCenter = selectedMatch.getNameSC();
        int startTime = selectedMatch.getTimeSlot().getStartTime().getHour();
        int finishTime = selectedMatch.getTimeSlot().getEndTime().getHour();

        newAddSportCenterDAO.updateTimeSlot(spots-1, selectedCourtID, selectedSport,
                selectedSportCenter, startTime, finishTime);

    }


    public void sendEmails(ResultElement selectedMatch) {

        UserPreferences preferences = UserEntity.getInstance().getPreferences();
        SportCenterInfo infoSportCenter = GetSportCenterDAO.getInstance().getSportCenter(selectedMatch.getNameSC(),
                selectedMatch.getSport()).getInfo();
        int startTime = selectedMatch.getTimeSlot().getStartTime().getHour();
        int finishTime = selectedMatch.getTimeSlot().getEndTime().getHour();

        //Se l'utente che sta usando l'app ha le notifiche attive si invia la mail promemoria del match
        if (preferences.isNotifications()){



            EmailThread playerEmailThread = new EmailThread(selectedMatch.getSport(), selectedMatch.getCourtID(),
                    startTime, finishTime, infoSportCenter.getSportCenterAddress());
            playerEmailThread.setPlayer(true);
            playerEmailThread.start();
        }

        //Se l'owner del campo ha le notifiche attive, e il match è al completo => inviamo la mail all'owner per avvisarlo
        if (infoSportCenter.isNotifications() && selectedMatch.getTimeSlot().getAvailableSpots() == 0){
            EmailThread ownerEmailThread = new EmailThread(infoSportCenter.getOwnerEmail(),
                    selectedMatch.getSport(), selectedMatch.getCourtID(), startTime, finishTime);
            ownerEmailThread.setOwner(true);
            ownerEmailThread.start();
        }



    }


}
