package src.main.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.util.Duration;
import src.main.Client;

import java.net.URL;
import java.util.ResourceBundle;

public class Timer implements Initializable {

    private Timeline mainTimeTimeline;
    private Timeline periodTimeline;
    private int mainTime; //以秒的方式计算，如2分钟，mainTime存有120
    private int period;
    private int periodTimes;
    private Label playerOverTimeRemain;

    private int tempPeriod;

    @FXML
    private Label timeLabel = new Label();

    public Timer() {
    }

    public void setPlayerOverTimeRemain(Label playerOverTimeRemain) {
        this.playerOverTimeRemain = playerOverTimeRemain;
    }


    /**
     * initialize periodTimeline and start it to counting second in
     * stage 2.
     */
    private void countintSecond() {
        periodTimeline = new Timeline();
        periodTimeline.setCycleCount(period);
        tempPeriod = period;
        periodTimeline.getKeyFrames().addAll(new KeyFrame(Duration.seconds(1), (ActionEvent ae) -> {
            --tempPeriod;
            displaySecond(tempPeriod);
            if (tempPeriod == 0) {
                --periodTimes;
                playerOverTimeRemain.setText(periodTimes + "次");
                if (periodTimes == 0) {
                    Client.getGameController().overTime();
                } else {
                    //this.pause();
                    Client.getGameController().reverseTurn();
                    //otherTimer.start();
                }
            }
        }));
    }

    private void displayMainTime() {
        int minute = mainTime / 60;
        int second = mainTime % 60;
        timeLabel.setText(String.format("%02d 分 %02d 秒", minute, second));
    }

    private void displaySecond(int t) {
        timeLabel.setText(String.format("%02d 秒", t));
    }

    public void start() {
        //mainTimeTimeline.play();
        /*********** test ***********/
        if (mainTime != 0) {
            mainTimeTimeline.play();
        } else {
            periodTimeline.play();
        }
        /*********** test ***********/
    }

    public void stop() {
        mainTimeTimeline.stop();
        if(periodTimeline!=null){
            periodTimeline.stop();
        }
    }

    public void pause() {
        //mainTimeTimeline.pause();
        /*********** test ***********/
        if (mainTime != 0) {
            mainTimeTimeline.pause();
        } else {
            periodTimeline.stop();
            tempPeriod = period;
            displaySecond(tempPeriod);
        }
        /*********** test ***********/
    }

    public int getPeriodTimes() {
        return this.periodTimes;
    }

    /**
     *
     * @param main
     * @param period
     * @param times
     *
     * initialize the timer. setup the mainTimeline, which is used to
     * counting second in the stage 1.
     */
    public void init(int main, int period, int times) {
        mainTime = 60 * main;
        this.period = period;
        periodTimes = times;

        mainTimeTimeline = new Timeline();
        mainTimeTimeline.setCycleCount(mainTime);
        mainTimeTimeline.getKeyFrames().addAll(new KeyFrame(Duration.seconds(1), (ActionEvent ae) -> {
            --mainTime;
            displayMainTime();
            if (mainTime == 0) {
                countintSecond();
                displaySecond(period);
                periodTimeline.play();
            }
        }));
        displayMainTime();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

}
