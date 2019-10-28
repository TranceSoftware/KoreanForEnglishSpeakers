package com.example.joshua.koreancards;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "cards")
public class CardTable implements Serializable {
    @PrimaryKey
    private int indexKey;
    private String nativeWord;
    private String foreignWord;
    private double factor;
    private long time;
    private int days;
    private int streak;
    private String pos;
    private String continuousTense;
    private String pastTense;
    private String perfectTense;
    private String perfectContinuous;

//    public CardTable(int indexKey, String nativeWord, String pos, String foreignWord, double factor, long time, int days, int streak) {
//        this.indexKey = indexKey;
//        this.nativeWord = nativeWord;
//        this.foreignWord = foreignWord;
//        this.factor = factor;
//        this.time = time;
//        this.days = days;
//        this.streak = streak;
//        this.pos = pos;
//    }
    public CardTable(int indexKey, String pos, String nativeWord, String foreignWord, double factor, long time, int days, int streak, String continuousTense, String pastTense, String perfectTense, String perfectContinuous) {
        this.indexKey = indexKey;
        this.nativeWord = nativeWord;
        this.foreignWord = foreignWord;
        this.factor = factor;
        this.time = time;
        this.days = days;
        this.streak = streak;
        this.pos = pos;
        this.continuousTense = continuousTense;
        this.pastTense = pastTense;
        this.perfectTense = perfectTense;
        this.perfectContinuous = perfectContinuous;
    }


    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public String getNativeWord() {
        return nativeWord;
    }

    public void setNativeWord(String nativeWord) {
        this.nativeWord = nativeWord;
    }

    public String getForeignWord() {
        return foreignWord;
    }

    public void setForeignWord(String foreignWord) {
        this.foreignWord = foreignWord;
    }

    public int getIndexKey() {
        return indexKey;
    }

    public void setIndexKey(int indexKey) {
        this.indexKey = indexKey;
    }

    public double getFactor() {
        return factor;
    }

    public void setFactor(double eFactor) {
        this.factor = eFactor;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public CardTable createCopy() {
        return new CardTable(this.indexKey, this.pos, this.nativeWord, this.foreignWord, this.factor, this.time, this.days, this.streak, this.continuousTense, this.pastTense, this.perfectTense, this.perfectContinuous);
    }

    public String getContinuousTense() {
        return continuousTense;
    }

    public void setContinuousTense(String continuousTense) {
        this.continuousTense = continuousTense;
    }

    public String getPastTense() {
        return pastTense;
    }

    public void setPastTense(String pastTense) {
        this.pastTense = pastTense;
    }

    public String getPerfectTense() {
        return perfectTense;
    }

    public void setPerfectTense(String perfectTense) {
        this.perfectTense = perfectTense;
    }

    public String getPerfectContinuous() {
        return perfectContinuous;
    }

    public void setPerfectContinuous(String perfectContinuous) {
        this.perfectContinuous = perfectContinuous;
    }
}
