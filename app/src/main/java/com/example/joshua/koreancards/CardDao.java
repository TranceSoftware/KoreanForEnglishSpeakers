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
    List<CardTable> getStudyCards();

    @Query("SELECT * FROM cards WHERE time != :l")
    List<CardTable> getModifiedCards(Long l); //must be handed Long.MAX_VALUE to work properly

//    @Query("SELECT * FROM cards WHERE streak > 0")
@Query("SELECT * FROM cards WHERE time != 9223372036854775807")
    List<CardTable> getLearnedWords();

    @Query("SELECT * FROM cards WHERE time == :l")
    List<CardTable> getNewCards(Long l); //will return all newcards if given Long Max value
}
