package com.sportify.bookmatch;

import com.sportify.sportcenter.courts.SportCourt;
import com.sportify.sportcenter.courts.TimeSlot;
import com.sportify.sportcenter.exceptions.SportCenterException;
import com.sportify.user.UserEntity;
import com.sportify.utilitiesui.DisappearingLabel;
import com.sportify.utilitiesui.UIController;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;


public class BookMatchViewController {

    private static final String TEXT_BUTTON = "select";
    @FXML
    private Label meanLabel;
    @FXML
    private AnchorPane anchorPaneBookMatch;
    @FXML
    private Label popUpLabel;
    @FXML
    private AnchorPane popUpAnchorPane;
    @FXML
    private Button createJoinMatchButton;
    @FXML
    private Button bookMatchButton;
    @FXML
    private Button basketButton;
    @FXML
    private Button footballButton;
    @FXML
    private Button padelButton;
    @FXML
    private Button tennisButton;
    @FXML
    private Label resultLabel;
    @FXML
    private Label errorLabel;

    private boolean basketButtonIsActive = false;
    private boolean padelButtonIsActive = false;
    private boolean tennisButtonIsActive = false;
    private boolean footballButtonIsActive = false;

    @FXML
    private ScrollPane scrollPaneBookMatch;

    private BookMatchController bookMatchController = BookMatchController.getBookMatchControllerInstance();
    private CustomTilePane customTilePane = new CustomTilePane();
    private List<SportCourt> courtsList;
    private Map<String, Double> sportCenterList;
    private String selectedSportCenter;

    @FXML
    public void initialize(){

        resultLabel.setOpacity(0);
        UserEntity user = UserEntity.getInstance();

        int numOfSports = 0;

        if (user.getPreferences().getBasket()) {

            int position = 200 + numOfSports*150 + numOfSports*100;
            numOfSports++;
            basketButton.setLayoutX(position);
            basketButton.setVisible(true);
            basketButtonIsActive = true;
            basketButton.setOnAction(event->startBookMatch(basketButton.getText()));

        }

        if (user.getPreferences().getFootball()) {

            int position = 200 + numOfSports*150 + numOfSports*100;
            numOfSports++;
            footballButton.setLayoutX(position);
            footballButton.setVisible(true);
            footballButtonIsActive = true;
            footballButton.setOnAction(event->startBookMatch(footballButton.getText()));

        }
        if (user.getPreferences().getTennis()) {

            int position = 200 + numOfSports*150 + numOfSports*100;
            numOfSports++;
            tennisButton.setLayoutX(position);
            tennisButton.setVisible(true);
            tennisButtonIsActive = true;
            tennisButton.setOnAction(event->startBookMatch(tennisButton.getText()));

        }
        if (user.getPreferences().getPadel()) {

            int position = 200 + numOfSports*150 + numOfSports*100;
            padelButton.setLayoutX(position);
            padelButton.setVisible(true);
            padelButtonIsActive = true;
            padelButton.setOnAction(event->startBookMatch(padelButton.getText()));
        }
    }

    private void hideButtons(){
        basketButton.setVisible(false);
        padelButton.setVisible(false);
        footballButton.setVisible(false);
        tennisButton.setVisible(false);
        errorLabel.setVisible(false);
    }

    private void enableButtons(){
        if(basketButtonIsActive) {
            basketButton.setVisible(true);
        }
        if(padelButtonIsActive){
            padelButton.setVisible(true);
        }
        if(footballButtonIsActive){
            footballButton.setVisible(true);
        }
        if(tennisButtonIsActive) {
            tennisButton.setVisible(true);
        }
    }

    private void enableScrollPane(){
        scrollPaneBookMatch.setVisible(true);
    }

    private void startBookMatch(String selectedSport){
        this.hideButtons();

        try {
            sportCenterList = bookMatchController.startStateMachine(selectedSport);
            enableScrollPane();
            displaySportCenters(sportCenterList);
        }
        catch(SportCenterException e){
            displayError();
        }
    }

    private void displayError(){
        errorLabel.setVisible(true);
        errorLabel.setText("""
                    There are no Sport Centers in your area.
                    Please change your address in "Settings".""");
    }

    private void deletedAllSC(){
        hidePopUpControls();
        hideButtons();
        scrollPaneBookMatch.setVisible(false);
        errorLabel.setVisible(true);
        errorLabel.setText("""
                All the Sport Centers in your area are full.
                please increase your "Radius of Interest" in your
                "Settings".""");
    }

    @FXML
    private void displaySportCenters(Map<String, Double> nearSportCenters) {

        if(nearSportCenters.size() == 0){
            deletedAllSC();
        }
        else {
            customTilePane.createCustomTilePane();
            meanLabel.setVisible(true);
            meanLabel.setText("These are the nearest Sport Center, choose one.");

            nearSportCenters.forEach((key, value) -> {
                Button selectButton = new Button(TEXT_BUTTON);
                selectButton.setOnAction(event -> selectedSportCenter(key));
                customTilePane.addElement(selectButton, "Sport Center " + key + "   (" + new DecimalFormat("##.##").format(nearSportCenters.get(key)) + " kms away)");
            });
            scrollPaneBookMatch.setContent(customTilePane.getCustomTP());
        }
    }

    private void selectedSportCenter(String sportCenterName){
        selectedSportCenter = sportCenterName;
        courtsList = bookMatchController.selectedSportCenter(sportCenterName);
        this.displayCourts(courtsList);
    }


    private void displayCourts(List<SportCourt> courtList) throws SportCenterException{

        if(courtList.isEmpty()){
            sportCenterList.remove(selectedSportCenter);
            deletedSportCenter();
            displaySportCenters(sportCenterList);
        }
        else {
            customTilePane.createCustomTilePane();
            meanLabel.setText("Select which court you want.");

            for (SportCourt element : courtList) {
                Button selectButton = new Button(TEXT_BUTTON);
                selectButton.setOnAction(event -> selectedCourt(String.valueOf(element.getCourtID())));
                customTilePane.addElement(selectButton, "Court number: " + element.getCourtID());
            }
            scrollPaneBookMatch.setContent(customTilePane.getCustomTP());
        }
    }

    private void selectedCourt(String id){
        try {
            List<TimeSlot> timeTable = bookMatchController.selectedCourt(id);
            this.displayHourSlots(timeTable);
        }
        catch(SportCenterException exception){
            for(SportCourt s: courtsList){
                if(s.getCourtID() == Integer.parseInt(id)){
                    courtsList.remove(s);
                    deletedCourt();
                    break;
                }
            }
            displayCourts(courtsList);
        }
    }

    private void displayHourSlots(List<TimeSlot> timeTable){
        customTilePane.createCustomTilePane();
        meanLabel.setText("Select which time slot you prefer.");

        for(TimeSlot element:timeTable) {
            Button selectButton = new Button(TEXT_BUTTON);
            selectButton.setOnAction(event-> selectedHourSlot(element));
            int start = element.getStartTime().getHour();
            int finish = element.getEndTime().getHour();
            int spots = element.getAvailableSpots();
            customTilePane.addElement(selectButton,"Start time: "+start+ "   Finish time: "+finish+", available spots: "+spots);
        }
        scrollPaneBookMatch.setContent(customTilePane.getCustomTP());
    }

    private void selectedHourSlot (TimeSlot hourSlot){
        bookMatchController.setSelectedTimeSlot(hourSlot);
        meanLabel.setVisible(false);

        popUpLabel.setText("""
                If you want to book a match, click Book Match.
                
                If you want to create a joinable match, click
                Create Joinable Match.
                
                """);

        showPopUpControls();
    }

    public void createBookMatch(){
        bookMatchController.bookMatch();
        restartBookMatch();
    }

    public void createJoinMatch(){
        bookMatchController.createJoinMatch();
        restartBookMatch();
    }

    private void restartBookMatch(){

        hidePopUpControls();
        enableButtons();
        resultLabel.setText("Operation successfully completed!");
        resultLabel.setStyle("-fx-text-fill: rgba(0,255,0,1);");
        displayResultLabel(resultLabel);
    }

    private void displayResultLabel(Label label){

        SequentialTransition blinkThenFade = DisappearingLabel.createTransition(label);

        blinkThenFade.play();
    }


    private void deletedSportCenter(){
        resultLabel.setText("The Sport Center you selected is full");
        resultLabel.setStyle("-fx-text-fill: rgba(255,0,0,1);");
        displayResultLabel(resultLabel);
    }

    private void deletedCourt(){
        resultLabel.setText("The Court you selected is full");
        resultLabel.setStyle("-fx-text-fill: rgba(255,0,0,1);");
        displayResultLabel(resultLabel);
    }

    private void showPopUpControls(){
        createJoinMatchButton.setVisible(true);
        bookMatchButton.setVisible(true);
        popUpLabel.setVisible(true);
        scrollPaneBookMatch.setVisible(false);
        popUpAnchorPane.setVisible(true);
    }

    private void hidePopUpControls(){
        createJoinMatchButton.setVisible(false);
        bookMatchButton.setVisible(false);
        popUpLabel.setVisible(false);
        popUpAnchorPane.setVisible(false);
    }

    //Metodi per bottoni base della UI

    public void showSettings() throws IOException {
        UIController c = UIController.getUIControllerInstance();
        c.showSettings();
    }

    public void goToHomeScreen() throws IOException {
        UIController c = UIController.getUIControllerInstance();
        c.showHomeScreen();
    }


    public void showJoinMatch() throws IOException {
        UIController c = UIController.getUIControllerInstance();
        c.showJoinMatch();

    }

    public void launchFaq() throws IOException{
        UIController c = UIController.getUIControllerInstance();
        c.showFaqs();
    }
}
