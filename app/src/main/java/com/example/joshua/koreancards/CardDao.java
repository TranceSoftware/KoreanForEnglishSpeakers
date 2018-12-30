package com.example.joshua.koreancards;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface CardDao {
    @Insert
    void addCard(CardTable card);

    @Query("SELECT * FROM cards WHERE indexKey LIKE :i")
    CardTable getCard(int i);

    @Query("select * from cards")
    List<CardTable> getCards();

    @Update
    void updateCard(CardTable card);

    @Query("SELECT * FROM cards WHERE time < :time AND time != 0")
    List<CardTable> getCardsDue(double time);

    @Query("SELECT * FROM cards WHERE time = 0")
    List<CardTable> getReviewCards();
}
