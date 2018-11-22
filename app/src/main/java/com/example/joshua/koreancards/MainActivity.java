package com.example.joshua.koreancards;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.TimeZone;


public class MainActivity extends Activity {
    private final String DEBUG_TAG = "DEBUG_TAG";
    private Button studyButton, dataButton, dictionaryButton, settingsButton;
    private int wordIndex = 10;
    private int wordsPerDay = 20;
    private final String KOREAN_TABLE_NAME = "KoreanTable.tmp";
    private final String ENGLISH_TABLE_NAME = "EnglishTable.tmp";

    private final int STUDY_RESULT_CODE = 1; //was 3
    private FileOutputStream fos;
    private ObjectOutputStream oos;
    private FileInputStream fis;
    private ObjectInputStream ois;
    private final String SHARED_PREF_KEY = "shared_pref_key";
    private final String DUE_DATE_KEY = "due_date_key";
    private String dueDataString = "";
    private String dueDataStringSplit[];
    private final String E_FACTOR_KEY = "e_factor_key";
    private String eFactorString = "";
    private String eFactorStringSplit[];
    private final String CARDS_PER_DAY_KEY = "cards_per_day_key";
    private int cardsPerDay; //10 is default
    private final String INDEX_KEY = "index_key";
    private int index = 0;
    private final String NEW_DAY_MILLI_KEY = "new_day_miLli_key";
    private long newDayMilli;
    private int cardsRead;
    private final String CARDS_READ_KEY = "cards_read_key";
    private boolean studyStarted;
    private final String STUDY_STARTED_KEY = "study_started_key";
    private ArrayList<Cell> bundleList;
    private Hashtable<Integer, String> koreanTable = new Hashtable<>();
    private Hashtable<Integer, String> englishTable = new Hashtable<>();
    private long[] dueDates;
    private double[] eFactors;
    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor editor;

    //private ArrayList<Cell> cellArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());//todo don't use context?
        //sharedPrefs = getApplicationContext().getSharedPreferences(SHARED_PREF_KEY, 0);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPrefs.edit();

        //Calendar calendar = Calendar.getInstance();
        //calendar.setTimeZone(TimeZone.getDefault());
        //calendar.set(Calendar.HOUR_OF_DAY, 3);
        //newDayMilli = calendar.getTimeInMillis();
        //Log.d(DEBUG_TAG, Long.toString(newDayMilli));

        //todo reswap these two functions later
        checkTime();
        if(studyStarted) {
            loadDueDates();
            bundleList = preProcessingData();
            //this will run when the milli has been incremented until all cards have been learned
            //i.e. the cardsRead equals the cardsPerDay which coordinate to new cards
            //todo increase the cardsRead, or decrement, when a review card has been moved down to a study card
        }


        studyButton=findViewById(R.id.StudyButton);
        dataButton=findViewById(R.id.DataButton);
        dictionaryButton=findViewById(R.id.DictionaryButton);
        settingsButton=findViewById(R.id.SettingsButton);
        studyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                for(int i=0;i<5667;i++) {
//                    //Log.d(DEBUG_TAG, i + "\t" + koreanTable.get(i) + "\t" + englishTable.get(i) + "\t");
//                }
//                for(int i=1;i<21;i++) {
//                    bundleList.add(new Cell(i, koreanTable.get(i), englishTable.get(i)));
//                    //Log.d(DEBUG_TAG, koreanTable.get(i) + "\t" + englishTable.get(i));
//                }
                //todo change canStudy to something useful later, for now keep as true
                if(canStudy()) {
                    Log.d(DEBUG_TAG, "Size of studylist: " + Integer.toString(bundleList.size()));
                    if(bundleList.size()>0) {
                        Intent intent = new Intent(MainActivity.this, StudyActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("NEW_WORDS", bundleList);
                        intent.putExtras(bundle);
//                newWordsList.addAll((ArrayList<Cell>) bundle.getSerializable("NEW_WORDS")
//                intent.putExtra("ENGLISH", englishWords(wordIndex, wordsPerDay));
//                intent.putExtra("KOREAN", koreanWords(wordIndex, wordsPerDay));
                        MainActivity.this.startActivityForResult(intent, STUDY_RESULT_CODE);
                    }
                }
            }
        });
        dataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DataActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });
        dictionaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DictionaryActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                MainActivity.this.startActivity(intent);
            }
        });
        createKoreanTable(KOREAN_TABLE_NAME);
        createEnglishTable(ENGLISH_TABLE_NAME);
    }

    public boolean createKoreanTable(String filename) {
        try {
//            AssetFileDescriptor assetFileDescriptor = getAssets().openFd(KOREAN_TABLE_NAME);
//            fis = assetFileDescriptor.createInputStream();
//            ois = new ObjectInputStream(fis);
//            koreanTable = (Hashtable<Integer, String>) ois.readObject();
            InputStream is = null;
            AssetManager assets = getAssets();
            is=assets.open(KOREAN_TABLE_NAME);
            ois = new ObjectInputStream(is);
            koreanTable = (Hashtable<Integer, String>) ois.readObject();
            //Log.d(DEBUG_TAG, Integer.toString(koreanTable.size()));
            return true;
        } catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean createEnglishTable(String filename) {
        try {
//            AssetFileDescriptor assetFileDescriptor = getAssets().openFd(ENGLISH_TABLE_NAME);
//            fis = assetFileDescriptor.createInputStream();
//            ois = new ObjectInputStream(fis);
//            englishTable = (Hashtable<Integer, String>) ois.readObject();
            InputStream is = null;
            AssetManager assets = getAssets();
            is=assets.open(ENGLISH_TABLE_NAME);
            ois = new ObjectInputStream(is);
            englishTable = (Hashtable<Integer, String>) ois.readObject();
            //Log.d(DEBUG_TAG, Integer.toString(englishTable.size()));
            return true;
        } catch(IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }
    public ArrayList<String> koreanWords(int wordIndex, int wordsPerDay) {
        ArrayList<String> koreanWords = new ArrayList<>();
        for(int i=wordIndex; i<(wordIndex+wordsPerDay); i++) {
            koreanWords.add(koreanTable.get(i));
        }
        return koreanWords;
    }
    public ArrayList<String> englishWords(int wordIndex, int wordsPerDay) {
        ArrayList<String> englishWords = new ArrayList<>();
        for (int i = wordIndex; i < (wordIndex + wordsPerDay); i++) {
            englishWords.add(englishTable.get(i));
        }
        return englishWords;
    }
    public ArrayList<Cell> tempCellCreation(int wordIndex, int wordsPerDay) {
        ArrayList<Cell> cellArrayList = new ArrayList<>();
        ArrayList<String> englishWords = englishWords(wordIndex, wordsPerDay);
        ArrayList<String> koreanWords = koreanWords(wordIndex, wordsPerDay);
        for(int i=wordIndex; i<(wordIndex+wordsPerDay);i++) {
            cellArrayList.add(new Cell(i, koreanWords.get(i), englishWords.get(i)));
        }
        return cellArrayList;
    }
    @Override
    protected  void onDestroy() {
        try {
            fos.close();
            oos.close();
            fis.close();
            ois.close();
        } catch(IOException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(DEBUG_TAG, "onActivityResultCalled. RequestCode: " + Integer.toString(requestCode) + " ResultCode: " + Integer.toString(resultCode));
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle = data.getExtras();
        switch(requestCode) {
            //case(Activity.RESULT_OK) : {
            case(1) : {
                Log.d(DEBUG_TAG, "requestCode");
                //deserialze  data
                //if(resultCode== Activity.RESULT_OK) {
                if(resultCode==-1) {
                    ArrayList<Cell> returnWords = new ArrayList<>();
                    returnWords.addAll((ArrayList<Cell>)bundle.getSerializable("STUDY_WORDS"));
                    for(int i=0;i<returnWords.size();i++) {
                        //Log.d(DEBUG_TAG, i + "\t" + returnWords.get(i).getKorean() + "\t" + returnWords.get(i) + "\t");
                        //Log.d(DEBUG_TAG, "IT WORKED");
                    }
                    postProcessingData(returnWords);
                    saveDueDates(returnWords);
                }
            }
        }
    }
    public void loadDueDates() {
        //todo process this data i.e. find words that are due and select new
        //todo words per day user variable and index variable
        //read language data
        createKoreanTable(KOREAN_TABLE_NAME);
        createEnglishTable(ENGLISH_TABLE_NAME);
        //create arrays to hold user result data to be passed to shared preferences
        dueDates = new long[englishTable.size()];
        eFactors = new double[englishTable.size()];

        //load the data from memory
        //sharedPrefs.getLong(NEW_DAY_MILI_KEY, newDayMilli); moved to timecheckmethod
        cardsPerDay = sharedPrefs.getInt(CARDS_PER_DAY_KEY, cardsPerDay);
        cardsRead= sharedPrefs.getInt(CARDS_READ_KEY, cardsRead);
        index = sharedPrefs.getInt(INDEX_KEY, index);
        dueDataStringSplit = sharedPrefs.getString(DUE_DATE_KEY,"").split(",");
        eFactorStringSplit = sharedPrefs.getString(E_FACTOR_KEY, "").split(",");
        //convert the data to doubles
        for(int i = 0; i < dueDataStringSplit.length; i++) {
            dueDates[i]=Long.parseLong(dueDataStringSplit[i]);
            eFactors[i]=Double.parseDouble(eFactorStringSplit[i]);
        }
    }

    public ArrayList<Cell> preProcessingData() {
        //get the cards due and the new cards based on index and cards per day
        ArrayList<Cell> studyList = new ArrayList<>();
        Long currentTime = System.currentTimeMillis();
        //check for review cards
        for(int i=0; i<dueDates.length; i++) {
            if((dueDates[i]!= 0L) && (currentTime > dueDates[i])) {
                Cell tempCell = new Cell(i,koreanTable.get(i), englishTable.get(i));
                tempCell.setDueDate(dueDates[i]);
                Log.d(DEBUG_TAG, tempCell.getKorean() + " MilliTime: " + Long.toString(newDayMilli) + " Difference: " + Long.toString(currentTime - tempCell.getDueDate()));
                tempCell.setReviewCard(1);
                studyList.add(tempCell);
            }
        }
        //get new cards due
        //bool for this already ran? once a day? Answer: this is unnecessary because it will just load less cards the more that are passed successfully
        //todo still need to set where cards e-factor is calculated
        //todo atm the system acts as if when restarted everything resets for the day except for milli time and is studying boolean
        //todo cards are not written to memory unless the session finishes
        Log.d(DEBUG_TAG, "CardsPerDay: " + Integer.toString(cardsPerDay));
        Log.d(DEBUG_TAG, "CardsRead: " + Integer.toString(cardsRead));
        int tempIndex;
        //load the specified new cards per day minus cards already read
        //todo what about when a review card fails and becomes a study card? nothing atm it will be given the proper date and will become a review again if forced reset
        //for(tempIndex = index; tempIndex < (index + (cardsPerDay - cardsRead)); tempIndex++) {
        for(tempIndex = index; tempIndex < (index + (cardsPerDay)); tempIndex++) {
            studyList.add(new Cell(tempIndex, koreanTable.get(tempIndex), englishTable.get(tempIndex)));
            Log.d(DEBUG_TAG, "Index: " + tempIndex + "\t" +koreanTable.get(tempIndex));
        }
        index = tempIndex;
        return studyList;
    }
    public void saveDueDates(ArrayList<Cell> finishedData) {
        //assumes data has been processed and new days calculated
        //set new float values for when the cards are due
        //set easiness factors
        for(Cell c: finishedData) {
            eFactors[c.getIndex()] = c.getDifficulty();
            dueDates[c.getIndex()] = c.getDueDate();
        }
        //convert both to a string
        dueDataString = new String();
        eFactorString = new String();
        for(int i = 0; i < dueDates.length; i++) {
            dueDataString += Long.toString(dueDates[i]);
            dueDataString += ",";
            eFactorString += Double.toString(eFactors[i]);
            eFactorString += ","; //todo will having a comma at the end be a problem? test this later
        }
        editor.putString(DUE_DATE_KEY, dueDataString);
        editor.putString(E_FACTOR_KEY, eFactorString);
        editor.putInt(INDEX_KEY, index);
        editor.putInt(CARDS_READ_KEY, cardsRead);
        editor.putInt(CARDS_PER_DAY_KEY, cardsPerDay);
        studyStarted = false;
        editor.putBoolean(STUDY_STARTED_KEY, studyStarted);
        editor.apply();
        Log.d(DEBUG_TAG, "Study session finished. Everything committed.");
    }
    public void postProcessingData(ArrayList<Cell> finishedData) {
        //calculate duedate, the days between and difficulty should already be calculated
        //3am?
        Log.d(DEBUG_TAG, "MilliTime: " + Long.toString(newDayMilli));
        Log.d(DEBUG_TAG, "CurrentTime: " + Long.toString(System.currentTimeMillis()));
        for(Cell c : finishedData) {
            //86400000L in a day
            c.setDueDate(newDayMilli + (100000L * c.getDaysBetweenReviews()));
            Log.d(DEBUG_TAG, "Difference until due: " + Long.toString(c.getDueDate() - System.currentTimeMillis()));
            if(c.getDaysBetweenReviews() > 0 && c.isReviewCard()==0) {
                //atm this does nothing when a session is forced down early, everything just resets if it occurs
                //this only counts the new words read
                //if the session is ended early then the review cards, if they read and failed, will have another chance to set them i.e. the player can lie
                cardsRead +=1;
            }
            Log.d(DEBUG_TAG, c.getKorean() + " ReviewValue: " + Integer.toString(c.isReviewCard()) + " MilliTime: " + Long.toString(c.getDueDate()) + " Days between review: " + Integer.toString(c.getDaysBetweenReviews()) + " CardsRead: " + Integer.toString(cardsRead));
        }
        //todo check time and increment when app loads
        //todo incomplete study session load/save

    }
    public void checkTime() {
        //check if the time is past if so call load data and process data
        //1) if a study session is interrupted just load again
        //2) load only the cards no finished with a value representing how many cards are left for the day
        Log.d(DEBUG_TAG, DUE_DATE_KEY);
        if(!sharedPrefs.contains(DUE_DATE_KEY)) {
            Log.d(DEBUG_TAG, "Creating all variables.");
            createKoreanTable(KOREAN_TABLE_NAME);
            createEnglishTable(ENGLISH_TABLE_NAME);
            //create arrays to hold user result data to be passed to shared preferences
            dueDates = new long[englishTable.size()];
            eFactors = new double[englishTable.size()];
            //data doesn't exist, create it, this will only happen once
            for (int i = 0; i < dueDates.length; i++) {
                dueDataString += Long.toString(0L);
                dueDataString += ",";
                eFactorString += Double.toString(2.5D);
                eFactorString += ",";
            }
            index = 0;
            cardsPerDay = 10;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getDefault());
            calendar.set(Calendar.HOUR_OF_DAY, 3);
            newDayMilli = calendar.getTimeInMillis();
//            newDayMilli = System.currentTimeMillis() - 60000;
            Log.d(DEBUG_TAG, Long.toString(newDayMilli));
            editor.putInt(CARDS_READ_KEY, cardsRead);
            editor.putLong(NEW_DAY_MILLI_KEY, newDayMilli);
            editor.putInt(CARDS_PER_DAY_KEY, cardsPerDay);
            editor.putInt(INDEX_KEY, index);
            editor.putString(DUE_DATE_KEY, dueDataString);
            editor.putString(E_FACTOR_KEY, eFactorString);
            editor.apply();
        } else {
            //sharedPrefs.getLong(NEW_DAY_MILLI_KEY, newDayMilli);
            newDayMilli = sharedPrefs.getLong(NEW_DAY_MILLI_KEY, newDayMilli);
            Log.d(DEBUG_TAG, "New Day Milli: " + "\t" + Long.toString(newDayMilli));
        }
        //simple test to see if the memory is working properly
        //this will always run the first time the app sets up
        Log.d(DEBUG_TAG, "Difference: " + Long.toString(newDayMilli - System.currentTimeMillis()));
        if(System.currentTimeMillis() > newDayMilli) {
            //new day to study
            //increment newDayMilli
            newDayMilli = (System.currentTimeMillis() + 100000L);
            cardsRead = 0;
            studyStarted = true;
            //todo removing this atm so only review cards will be pulled after the first session
            editor.putBoolean(STUDY_STARTED_KEY, studyStarted);
            editor.putInt(CARDS_READ_KEY, cardsRead);
            editor.putLong(NEW_DAY_MILLI_KEY, newDayMilli);
            editor.apply();
        }

//        if(System.currentTimeMillis() >  (newDayMilli + 86400000L))  {
////        if(newDayMilli < System.currentTimeMillis()) {
//            //todo change the 10800000 to the value set by user
//            newDayMilli = (System.currentTimeMillis() - (System.currentTimeMillis() % 86400000L) + 10800000L);
//            //when a day has past, or multiple days, we need to set this milli to the "new day" value
//            //this will be the current day minus the mod value so it starts the new day, then adding to the new day
//            editor.putLong(NEW_DAY_MILLI_KEY, newDayMilli);
//            editor.commit();
        //}
    }
    public boolean canStudy() {
        if(cardsRead < cardsPerDay) {
            return true;
        } else {
            return false;
        }
    }
}
