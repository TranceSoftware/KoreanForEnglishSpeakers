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

    public CardTable(int indexKey, String nativeWord, String foreignWord, double factor, long time, int days, int streak) {
        this.indexKey = indexKey;
        this.nativeWord = nativeWord;
        this.foreignWord = foreignWord;
        this.factor = factor;
        this.time = time;
        this.days = days;
        this.streak = streak;
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
        return new CardTable(this.indexKey, this.nativeWord, this.foreignWord, this.factor, this.time, this.days, this.streak);
    }
}
