package com.sportify.cli;

import com.sportify.joinmatch.JoinMatchBean;
import com.sportify.joinmatch.JoinMatchController;
import com.sportify.joinmatch.ResultElement;
import com.sportify.joinmatch.ResultSetEntity;
import com.sportify.sportcenter.exceptions.SportCenterException;
import com.sportify.user.UserEntity;
import com.sportify.user.UserPreferences;

import java.util.ArrayList;
import java.util.Scanner;

import static java.lang.System.*;

public class JoinMatchCLI {

    public void showJoinMatchCLI() {
        while (this.joinMatch() > 0) {
            //interagisci con l'utente fino a ottenere un input corretto o fino a tornare alla home
        }
        //completato lo use case si torna alla home
        CLIController controller = CLIController.getIstance();
        controller.loadHomeScreen(); //ritorna alla home screen
    }

    private int joinMatch() {
        out.println("""
                        What sport do you want to play?
                        1 : Football
                        2 : Padel
                        3 : Basket
                        4 : Tennis
                        (5 : Return to Home Screen)
                """);
        int sport;
        Scanner scanner = new Scanner(in);
        try {
            sport = Integer.parseInt(scanner.nextLine());

        } catch (NumberFormatException e){
            err.println("Invalid selection");
            return 1;
        }
        UserPreferences p = UserEntity.getInstance().getPreferences();
        String sportString;
        String errorMessage = "This sport is not selected in your settings!";
        switch (sport) {
            case 1 : {
                if (!p.getFootball()) {
                    err.println(errorMessage);
                    return 1;
                }
                sportString = "Football";
                break;
            }

            case 2 : {
                if (!p.getPadel()) {
                    err.println(errorMessage);
                    return 1;
                }
                sportString = "Padel";
                break;
            }
            case 3 : {
                if (!p.getBasket()) {
                    err.println(errorMessage);
                    return 1;
                }
                sportString = "Basket";
                break;
            }
            case 4 : {
                if (!p.getTennis()) {
                    err.println(errorMessage);
                    return 1;
                }
                sportString = "Tennis";
                break;
            }
            default : {
                return 0;

            }

        }
        JoinMatchBean bean = new JoinMatchBean();
        bean.setSelectedSport(sportString);
        out.println("At what time would you like to play?");
        try {
            bean.setPreferredStartingTime(scanner.nextLine());
            out.println("Which is the highest number of results you want to see?");
            bean.setMaxResults(scanner.nextLine());
            out.println("""
                    Do you prefer closer joinable matches or the ones where more people have already joined?
                    Please select 1 for "closer matches" ot "2" for "more people" (default is 2)
                    """);
            int filter = scanner.nextInt();
            if (filter == 1){
                bean.setDistanceIsImportant(true);
                bean.setAvailableSpotIsImportant(false);
            }
            else {
                bean.setAvailableSpotIsImportant(true);
                bean.setDistanceIsImportant(false);
            }


        } catch (IllegalArgumentException e){
            err.println(e.getMessage());
            return 1;
        }

        JoinMatchController controller = new JoinMatchController();
       try {
           controller.findJoinableMatch(bean);
       } catch (SportCenterException e){
           err.println("""
               Unfortunately, we couldn't find any joinable match in your area.
                    Please change your address in Settings and try again.
                    """);
       }
        ResultSetEntity resultSet = bean.getResultSet();
        ArrayList<ResultElement> resultList = (ArrayList<ResultElement>) resultSet.getElements();
        out.println("Select the number of the match you want to join:");
        int counter = 0;
        for (ResultElement r : resultList){
            out.println();
            out.println("Match [" + counter + "]: " + r.getNameSC() + " ("+ r.getDistance() +" kms away), court number: "+
                    r.getCourtID() + ", from " + r.getTimeSlot().getStartTime() + " to " + r.getTimeSlot().getEndTime() +
                    ", " + r.getTimeSlot().getAvailableSpots() + " available spots");
            counter++;
        }
        int selection = scanner.nextInt();
        if (selection > counter){
            err.println("Invalid match selection");
            return 1;
        }
        ResultElement selectedMatch = resultList.get(selection);

        controller.joinMatch(selectedMatch);

        out.println("Match booked correctly, you'll receive a reminder email shortly!");


        return 0;
    }
}
