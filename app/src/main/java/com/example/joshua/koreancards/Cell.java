package com.example.joshua.koreancards;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by Joshua on 6/1/2018.
 */

public class Cell implements Serializable {
    private int buttonIndex = -1; //
    //set this and remove this
    private int reviewCard = 0;

    public int isReviewCard() {
        return reviewCard;
    }

    public void setReviewCard(int reviewCard) {
        this.reviewCard = reviewCard;
    }
    public long dueDate = 0L;

    public long getDueDate() {
        return dueDate;
    }

    public void setDueDate(long dueDate) {
        this.dueDate = dueDate;
    }

    private Cell nextCell = null;
    private int index;
    private String rawIdentifier = "";
    private String korean;
    private String english;
    private long milliSeconds=Long.MAX_VALUE;
    private double difficulty; //[0.0, 1.0], default is 0.3
    //okay nvm 1.3-2.5
    private int daysBetweenReviews;
    private int date;//change later
    private int oldCellIndex;
    private int sessionStreak=0;
    private int listID = 0; //0-alpha, 1-beta, 2-gamma. 3-theta
    private int cardsBetween=Integer.MAX_VALUE;
    private int retrievalStreak=0;
    private int[] cardBetweenSet={1,3,7,7,7,7,7,7,7,7,7,7,7,7,7,7};

    public int getRetrievalStreak() {
        return retrievalStreak;
    }

    public void setRetrievalStreak(int retrievalStreak) {
        this.retrievalStreak = retrievalStreak;
    }

    public int getCardsBetween() {
        return cardsBetween;
    }

    public void setCardsBetween(int cardsBetween) {
        this.cardsBetween = cardsBetween;
    }

    public int getSessionStreak() {
        return sessionStreak;
    }

    public void setSessionStreak(int sessionStreak) {
        this.sessionStreak = sessionStreak;
    }

    public int getOldIndex() {
        return oldCellIndex;
    }

    public void setOldIndex(int oldIndex) {
        this.oldCellIndex = oldIndex;
    }

    public Cell(int index, String korean, String english) {
        this.index = index;
        this.korean = korean;
        this.english = english;
    }

    public int getIndex() {
        return this.index;
    }

    public String getKorean() {
        return this.korean;
    }

    public String getEnglish() {
        return this.english;
    }

    public long getMilliSeconds() {
        return this.milliSeconds;
    }

    public int  getConsolidationDate() {
        return this.date;
    }

    public void setMiliSeconds(long miliSeconds) {
        this.milliSeconds = miliSeconds;
    }

    public void setConsolidationDate(int date) {
        this.date = date;
    }

    public double getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(double difficulty) {
        this.difficulty = difficulty;
    }

    public int getDaysBetweenReviews() {
        return daysBetweenReviews;
    }

    public void setDaysBetweenReviews(int daysBetweenReviews) {
        this.daysBetweenReviews = daysBetweenReviews;
    }

    public int getListID() {
        return listID;
    }

    public void setListID(int listID) {
        this.listID = listID;
    }

    public int[] getCardBetweenSet() {
        return cardBetweenSet;
    }
    public int getGap(int i) {
        return getCardBetweenSet()[i];
    }

    public Cell getNextCell() {
        return nextCell;
    }

    public void setNextCell(Cell prevoiusCell) {
        this.nextCell = prevoiusCell;
    }

    public int getButtonIndex() {
        return buttonIndex;
    }

    public void setButtonIndex(int buttonIndex) {
        this.buttonIndex = buttonIndex;
    }

    public String getRawIdentifier() {
        return rawIdentifier;
    }

    public void setRawIdentifier(String rawIdentifier) {
        this.rawIdentifier = rawIdentifier;
    }

}
