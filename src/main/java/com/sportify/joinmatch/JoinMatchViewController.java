package com.sportify.joinmatch;

import com.sportify.bookmatch.CustomTilePane;
import com.sportify.sportcenter.exceptions.SportCenterException;
import com.sportify.user.UserEntity;
import com.sportify.utilitiesui.DisappearingLabel;
import com.sportify.utilitiesui.UIController;
import javafx.animation.SequentialTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.text.DecimalFormat;


public class JoinMatchViewController {

    @FXML
    private Label explainLabel;
    @FXML
    private ToggleButton basketToggleButton;
    @FXML
    private ToggleButton footballToggleButton;
    @FXML
    private ToggleButton padelToggleButton;
    @FXML
    private ToggleButton tennisToggleButton;
    @FXML
    private Label startTimeLabel;
    @FXML
    private Label filterLabel;
    @FXML
    private Label sportLabel;
    @FXML
    private Label resultLabel;
    @FXML
    private Button startButton;
    @FXML
    private TextField resultTextField;
    @FXML
    private TextField hourTextField;
    @FXML
    private ToggleButton distanceToggle;
    @FXML
    private ToggleButton spotsToggle;
    @FXML
    private ScrollPane scrollPaneJoinMatch;
    @FXML
    private Label outcomeLabel;
    @FXML
    private Button infoButton;

    private JoinMatchBean beanJoinMatch = new JoinMatchBean();

    private JoinMatchController joinMatchController = new JoinMatchController();

    private boolean hasFootball = false;
    private boolean hasPadel = false;
    private boolean hasBasket = false;
    private boolean hasTennis = false;


    public void initialize(){

        outcomeLabel.setOpacity(0);
        explainLabel.setOpacity(0);

        UserEntity user = UserEntity.getInstance();

        int numOfSports = 0;

        if (user.getPreferences().getBasket()) {

            int position = 550;
            numOfSports++;
            basketToggleButton.setLayoutX(position);
            basketToggleButton.setVisible(true);
            basketToggleButton.setOnAction(event->beanJoinMatch.setSelectedSport("Basket"));
            this.hasBasket = true;

        }

        if (user.getPreferences().getFootball()) {

            int position = 550 + numOfSports*100 + numOfSports*75;
            numOfSports++;
            footballToggleButton.setLayoutX(position);
            footballToggleButton.setVisible(true);
            footballToggleButton.setOnAction(event->beanJoinMatch.setSelectedSport("Football"));
            this.hasFootball = true;

        }
        if (user.getPreferences().getTennis()) {

            int position = 550 + numOfSports*100 + numOfSports*75;
            numOfSports++;
            tennisToggleButton.setLayoutX(position);
            tennisToggleButton.setVisible(true);
            tennisToggleButton.setOnAction(event->beanJoinMatch.setSelectedSport("Tennis"));
            this.hasTennis = true;

        }
        if (user.getPreferences().getPadel()) {

            int position = 550 + numOfSports*100 + numOfSports*75;
            padelToggleButton.setLayoutX(position);
            padelToggleButton.setVisible(true);
            padelToggleButton.setOnAction(event->beanJoinMatch.setSelectedSport("Padel"));
            this.hasPadel = true;
        }
    }


    public void startJoinMatch(){
        hideControls();
        outcomeLabel.setOpacity(0);
        try{
            beanJoinMatch.setPreferredStartingTime(hourTextField.getText());
            beanJoinMatch.setMaxResults(resultTextField.getText());
            beanJoinMatch.setDistanceIsImportant(distanceToggle.isSelected());
            beanJoinMatch.setAvailableSpotIsImportant(spotsToggle.isSelected());
        }
        catch(IllegalArgumentException e){
            scrollPaneJoinMatch.setVisible(false);
            this.hideControls();
            this.initialize();
            this.showControls();
            outcomeLabel.setText(e.getMessage());
            outcomeLabel.setTextFill(Color.RED);
            outcomeLabel.setVisible(true);
            outcomeLabel.setOpacity(1);
            return;

        }
        try {
            joinMatchController.findJoinableMatch(beanJoinMatch);
        } catch (SportCenterException e){
            scrollPaneJoinMatch.setVisible(false);
            this.hideControls();
            this.initialize();
            this.showControls();
            outcomeLabel.setText("""
               Unfortunately, we couldn't find any joinable match in your area.
                    Please change your address in Settings and try again.
                    """);
            outcomeLabel.setTextFill(Color.RED);
            outcomeLabel.setOpacity(1);
            outcomeLabel.setVisible(true);
            return;
        }
        ResultSetEntity resultSet = beanJoinMatch.getResultSet();
        scrollPaneJoinMatch.setVisible(true);

        CustomTilePane customTilePane = new CustomTilePane();
        customTilePane.createCustomTilePane();

        for(ResultElement r: resultSet.getElements()){

            Button newButton = new Button("select");
            String sportCenter = r.getNameSC();
            double dist = r.getDistance();
            int courtId = r.getCourtID();
            int start = r.getTimeSlot().getStartTime().getHour();
            int finish = r.getTimeSlot().getEndTime().getHour();
            int spots = r.getTimeSlot().getAvailableSpots();
            newButton.setOnAction(event->selectedMatch(r));
            customTilePane.addElement(newButton,sportCenter+" ("+
                    new DecimalFormat("##.##").format(dist)+" kms away), court number: "+courtId+", "
                    +start+":"+finish + ", " + spots + " available spots");

        }

        scrollPaneJoinMatch.setContent(customTilePane.getCustomTP());
    }

    private void hideControls(){
        basketToggleButton.setVisible(false);
        footballToggleButton.setVisible(false);
        padelToggleButton.setVisible(false);
        tennisToggleButton.setVisible(false);
        startTimeLabel.setVisible(false);
        filterLabel.setVisible(false);
        sportLabel.setVisible(false);
        resultLabel.setVisible(false);
        startButton.setVisible(false);
        resultTextField.setVisible(false);
        hourTextField.setVisible(false);
        distanceToggle.setVisible(false);
        spotsToggle.setVisible(false);
        infoButton.setVisible(false);
        explainLabel.setMouseTransparent(true);
    }
    
    private void showControls(){
        startTimeLabel.setVisible(true);
        filterLabel.setVisible(true);
        sportLabel.setVisible(true);
        resultLabel.setVisible(true);
        startButton.setVisible(true);
        resultTextField.setVisible(true);
        hourTextField.setVisible(true);
        distanceToggle.setVisible(true);
        spotsToggle.setVisible(true);
        infoButton.setVisible(true);
    }

    private void selectedMatch(ResultElement selectedMatch){
        joinMatchController.joinMatch(selectedMatch);
        joinMatchController.sendEmails(selectedMatch);
        this.restartJoinMatch();
    }

    private void restartJoinMatch() {
        scrollPaneJoinMatch.setVisible(false);
        this.hideControls();
        this.showControls();
        //Mostra nuovamente i bottoni degli sport preferiti dall'utente
        if(hasBasket) {
            basketToggleButton.setVisible(true);
        }
        if(hasPadel){
            padelToggleButton.setVisible(true);
        }
        if(hasFootball){
            footballToggleButton.setVisible(true);
        }
        if(hasTennis) {
            tennisToggleButton.setVisible(true);
        }
        outcomeLabel.setText("Match joined successfully!");
        outcomeLabel.setTextFill(Color.GREEN);

        SequentialTransition blinkThenFade = DisappearingLabel.createTransition(outcomeLabel);

        blinkThenFade.play();
        
    }


    public void showSettings() throws IOException {
        UIController c = UIController.getUIControllerInstance();
        c.showSettings();
    }

    public void goToHome() throws IOException {
        UIController c = UIController.getUIControllerInstance();
        c.showHomeScreen();
    }

    public void showCreateMatch() throws IOException {
        UIController c = UIController.getUIControllerInstance();
        c.showCreateMatch();
    }

    public void launchFaq() throws IOException {
        UIController c = UIController.getUIControllerInstance();
        c.showFaqs();
    }


    public void showInfo() {
        explainLabel.setOpacity(1);
    }

    public void hideInfo() {
        explainLabel.setOpacity(0);
    }
}
