package com.example.joshua.koreancards;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;


@Database(entities ={CardTable.class},version=1,exportSchema = false)
public abstract class CardDatabase extends RoomDatabase {
    public abstract CardDao cardDao();
}
