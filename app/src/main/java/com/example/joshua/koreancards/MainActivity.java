package com.example.joshua.koreancards;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.TimeZone;

public class MainActivity extends Activity implements TimePickerDialog.OnTimeSetListener {
    private final String DEBUG_TAG = "DEBUG_TAG";
    private Button studyButton, dataButton, dictionaryButton, settingsButton;
    private int wordIndex = 10;
    private int wordsPerDay = 20;
    private final String KOREAN_TABLE_NAME = "KoreanTable.tmp";
    private final String ENGLISH_TABLE_NAME = "EnglishTable.tmp";
    private final int STUDY_RESULT_CODE = 1; //was 3
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
    private boolean cardsLoaded;
    private final String CARDS_LOADED_KEY = "cards_loaded_key";
    private ArrayList<Cell> bundleList;
    private Hashtable<Integer, String> koreanTable = new Hashtable<>();
    private Hashtable<Integer, String> englishTable = new Hashtable<>();
    private long[] dueDates;
    private double[] eFactors;
    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor editor;
    private BroadcastReceiver broadcastReceiver;
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private StudyFragment studyFragment;
    private ListView dictionaryListView;
    private EditText searchText;
    private CustomAdapter customAdapter;
    private ArrayList<Cell> dictionaryList;
    //private ArrayList<Cell> cellArrayList = new ArrayList<>();

    /*
     *
     * END OF MAIN ACTIVITY VARIABLES
     *
     */

    private Calendar calendar = Calendar.getInstance();
    private ArrayList<Cell> everyList = new ArrayList<>();
    private Cell currentCell = null;
    private Button koreanWordButton, showAnswerButton, wrongAnswerButton, correctAnswerButton, timerButton;
    private LocalBroadcastManager localBroadcastManager;
    //TODO api catch
    private Resources resources;
    private SoundPool soundPool;
    private int mSoundId;

    private ArrayList<Button> mainList = new ArrayList<>();
    private ArrayList<Button> studyList = new ArrayList<>();

    private Button pickTime;
    private TextView settingText;
    private EditText cardsPerDayText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);

//        broadcastReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                Log.d(DEBUG_TAG, "LocalBroadCastReceiver called.");
//                Bundle bundle = intent.getExtras();
//                ArrayList<Cell> pauseWords = new ArrayList<>();
//                pauseWords.addAll((ArrayList<Cell>)bundle.getSerializable("STUDY_WORDS"));
//                for(int i = 0; i<pauseWords.size(); i++) {
//                    Log.d(DEBUG_TAG, pauseWords.get(i).getKorean() + "\t" + Integer.toString(i));
//                }
//                postProcessingData(pauseWords);
//                saveDueDates(pauseWords);
//            }
//        };//todo api check
//        if(findViewById(R.id.fragment_container) != null) {
//            if(savedInstanceState != null) {
//                return;
//            }
//        }
//        studyFragment = new StudyFragment();
//        fragmentManager = getFragmentManager();
//        fragmentTransaction = fragmentManager.beginTransaction();
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
        loadDueDates();
//        bundleList = preProcessingData();
        if(studyStarted) {
//            loadDueDates();
            bundleList = preProcessingData();

            //this will run when the milli has been incremented until all cards have been learned
            //i.e. the cardsRead equals the cardsPerDay which coordinate to new cards
            //todo increase the cardsRead, or decrement, when a review card has been moved down to a study card
        }
        dictionaryListView = (ListView)findViewById(R.id.dictionaryListView);
        searchText = (EditText)findViewById(R.id.searchButton);
        //todo make this a method
        dictionaryList = new ArrayList<>();
        for(int i = 0; i < englishTable.size(); i++) {
            Cell tempCell = new Cell(i, koreanTable.get(i), englishTable.get(i));
            tempCell.setDifficulty(eFactors[i]);
            dictionaryList.add(tempCell);
        }
        Log.d(DEBUG_TAG, "DictionaryList: " + Integer.toString(dictionaryList.size()));
        customAdapter = new CustomAdapter(this, R.layout.customlayout, dictionaryList);
        dictionaryListView.setAdapter(customAdapter);
        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String searchString = editable.toString().trim().toLowerCase();
//                if(searchString!=null) {
//                    for (int i = 1; i < dictionaryList.size(); i++) {
//                        if (dictionaryList.get(i).getEnglish().contains(searchString) || dictionaryList.get(i).getKorean().contains(searchString)) {
//                            dictionaryListView.setSelection(i);
//                        }
//                    }
//                }
                MainActivity.this.customAdapter.getFilter().filter(searchString);
            }
        });
        studyButton=findViewById(R.id.StudyButton);
        mainList.add(studyButton);
        dataButton=findViewById(R.id.DataButton);
        mainList.add(dataButton);
        dictionaryButton=findViewById(R.id.DictionaryButton);
        mainList.add(dictionaryButton);
        settingsButton=findViewById(R.id.SettingsButton);
        mainList.add(settingsButton);
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
                if(studyStarted) {
                    Log.d(DEBUG_TAG, "BundleList size: " + bundleList.size());
                    changeLayout(1);
                }
//                checkTime();
//                Log.d(DEBUG_TAG, "StudyStarted: " + Boolean.toString(studyStarted));
//                if(studyStarted) {
//                    loadDueDates();
//                    Log.d(DEBUG_TAG, "List size: " + bundleList.size());
//                    bundleList=preProcessingData();
//                    Log.d(DEBUG_TAG, "Size of studyList: " + Integer.toString(bundleList.size()));
//                    if(bundleList.size()>0) {
//                        //Intent intent = new Intent(MainActivity.this, StudyActivity.class);
//                        Bundle bundle = new Bundle();
//                        bundle.putSerializable("NEW_WORDS", bundleList);
//                        //intent.putExtras(bundle);
////                newWordsList.addAll((ArrayList<Cell>) bundle.getSerializable("NEW_WORDS")
////                intent.putExtra("ENGLISH", englishWords(wordIndex, wordsPerDay));
////                intent.putExtra("KOREAN", koreanWords(wordIndex, wordsPerDay));
//                        //MainActivity.this.startActivityForResult(intent, STUDY_RESULT_CODE);
//                        studyFragment.setArguments(bundle);
//                        fragmentTransaction.add(R.id.fragment_container, studyFragment,"STUDY_ID");
//                        //fragmentTransaction.addToBackStack(null);
//                        fragmentTransaction.commit();
//                    }
//                }
            }
        });
        dataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(MainActivity.this, DataActivity.class);
                //MainActivity.this.startActivity(intent);
            }
        });
        dictionaryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLayout(2);
                //Intent intent = new Intent(MainActivity.this, DictionaryActivity.class);
                //MainActivity.this.startActivity(intent);
            }
        });
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLayout(3);
                //Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                //MainActivity.this.startActivity(intent);
            }
        });
        createKoreanTable(KOREAN_TABLE_NAME);
        createEnglishTable(ENGLISH_TABLE_NAME);
        /*
         *
         * END OF ON CREATE FOR MAIN ACTIVITY
         *
         */
        calendar.setTimeInMillis(System.currentTimeMillis());
        resources = this.getResources();

        //TODO custom layout with all buttons
        //TODO move some of the below onCreat() to a new function which plays which this view loads
        koreanWordButton = findViewById(R.id.KoreanWordButton);
        studyList.add(koreanWordButton);
        showAnswerButton = findViewById(R.id.ShowAnswerButton);
        studyList.add(showAnswerButton);
        wrongAnswerButton = findViewById(R.id.WrongAnswerButton);
        studyList.add(wrongAnswerButton);
        correctAnswerButton = findViewById(R.id.CorrectAnswerButton);
        studyList.add(correctAnswerButton);
        timerButton = findViewById(R.id.TimerButton);
        studyList.add(timerButton);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder().setMaxStreams(10).build();
        } else {
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 1);
        }
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int soundStatus) {
                soundPool.play(sampleId, 1.0f, 1.0f, 0, 0, 1.0f);
            }
        });

        //add in the resuming aspect later or just looped through new words and move the ones that have streaks
//        for(int i=0;i<7;i++) {
//            Cell tempCell = newWordsList.remove(0);
//            tempCell.setList(1);
//            tempCell.setMilliSeconds(System.currentTimeMillis() + (1000 * tempCell.getWaitTime(tempCell.getStreak())));
//            subSetList.add(tempCell);
//        }
        koreanWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //play audio later
            }
        });
        showAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showAnswerButton.setText(englishWords.get(index));
                if (currentCell.getSessionStreak() == 4) {
                    showAnswerButton.setText(currentCell.getKorean());
                } else {
                    showAnswerButton.setText(currentCell.getEnglish());
                }
            }
        });
        wrongAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatedAnswerMade(false, currentCell);
                showAnswerButton.setText("?");
                //updateDisplay(false, currentCell);
                //buttonList.get(currentCell.getButtonIndex()).setTextColor(Color.BLACK);
                currentCell = updatedNextCard(currentCell);
                koreanWordButton.setText(currentCell.getKorean());
                //buttonList.get(currentCell.getButtonIndex()).setTextColor(Color.BLUE);
                nextRound();
            }
        });
        correctAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d(DEBUG_TAG, Integer.toString(index));
//                showAnswerButton.setText("?");
//                if(index >= (koreanWords.size()-1)) {
//                    index=0;
//                } else {
//                    index+=1;
//                }
//                koreanWordButton.setText(koreanWords.get(index));
                updatedAnswerMade(true, currentCell);
                showAnswerButton.setText("?");
                //change text displayed or set to audio i.e. hide
                //updateDisplay(true, currentCell);
                //buttonList.get(currentCell.getButtonIndex()).setTextColor(Color.BLACK);
                currentCell = updatedNextCard(currentCell);
                //buttonList.get(currentCell.getButtonIndex()).setTextColor(Color.BLUE);
                koreanWordButton.setText(currentCell.getKorean());

//                Log.d(DEBUG_TAG, "NewWordList");
//                for (int i = 0; i < everyList.size(); i++) {
//                    Log.d(DEBUG_TAG, i + "\t" + everyList.get(i).getKorean() + "\t" + Integer.toString(everyList.get(i).getListID()) + "\t" + Integer.toString(everyList.get(i).getCardsBetween()) + "\t" + everyList.get(i).getSessionStreak() + "\t" + (everyList.get(i).getMilliSeconds() - System.currentTimeMillis()));
//                }
//                Log.d(DEBUG_TAG, "\n" + "LearningList");
//                for (int i = 0; i < learningList.size(); i++) {
//                    Log.d(DEBUG_TAG, i + "\t" + learningList.get(i).getKorean() + "\t" + Integer.toString(learningList.get(i).getCardsBetween()) + "\t" + learningList.get(i).getRetrievalStreak() + "\t" + (learningList.get(i).getMilliSeconds() - System.currentTimeMillis()));
//                }
//                Log.d(DEBUG_TAG, "\n" + "RetrievalList");
//                for (int i = 0; i < retrievalList.size(); i++) {
//                    Log.d(DEBUG_TAG, i + "\t" + retrievalList.get(i).getKorean() + "\t" + Integer.toString(retrievalList.get(i).getCardsBetween()) + "\t" + retrievalList.get(i).getRetrievalStreak() + "\t" + (retrievalList.get(i).getMilliSeconds() - System.currentTimeMillis()));
//                }
//                Log.d(DEBUG_TAG, "\n" + "FinishedList");
//                for (int i = 0; i < finishedList.size(); i++) {
//                    Log.d(DEBUG_TAG, i + "\t" + finishedList.get(i).getKorean() + "\t" + Integer.toString(finishedList.get(i).getCardsBetween()) + "\t" + finishedList.get(i).getRetrievalStreak() + "\t" + (finishedList.get(i).getMilliSeconds() - System.currentTimeMillis()));
//                }
                nextRound();
            }
        });
        pickTime = (Button)findViewById(R.id.pick_time);
        pickTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment timePicker = new TimePickerFragment();
                timePicker.show(getFragmentManager(), "time picker");
                //todo import vs v4
            }
        });
        settingText = (TextView)findViewById(R.id.settingText);
        cardsPerDayText = (EditText)findViewById(R.id.cardsPerDay);
        cardsPerDayText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId==EditorInfo.IME_ACTION_DONE) {
//                    Log.d(DEBUG_TAG, "New cardsPerDay: " + cardsPerDayText.toString().trim());
                    cardsPerDay = Integer.parseInt(cardsPerDayText.getText().toString().trim());
                    Log.d(DEBUG_TAG, "New cardsPerDay: " + Integer.toString(cardsPerDay));
                    editor.putInt(CARDS_PER_DAY_KEY, cardsPerDay);
                    return true;
                }
                return false;
            }
        });
    }
    @Override
    public void onBackPressed() {
        //todo make this unique for each view. Study view the worst. For now change to originaly
        changeLayout(0);
    }
    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Log.d(DEBUG_TAG, "NEW TIME OF DAY:  " + hourOfDay + ":" + minute);
        calendar.setTimeInMillis(newDayMilli);
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        newDayMilli = calendar.getTimeInMillis();
        editor.putLong(NEW_DAY_MILLI_KEY, newDayMilli);
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
    @Override
    protected  void onDestroy() {
        try {
//            fos.close();
//            oos.close();
//            fis.close();
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
        Log.d(DEBUG_TAG, "Loading new cards.");
        for (int i = 0; i < dueDates.length; i++) {
            if ((dueDates[i] != 0L) && (currentTime > dueDates[i])) {
                Cell tempCell = new Cell(i, koreanTable.get(i), englishTable.get(i));
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
        int tempIndex = index;
        //load the specified new cards per day minus cards already read
        //todo what about when a review card fails and becomes a study card? nothing atm it will be given the proper date and will become a review again if forced reset
        //for(tempIndex = index; tempIndex < (index + (cardsPerDay - cardsRead)); tempIndex++) {
        if(!cardsLoaded) { //cards have not been loaded
            Log.d(DEBUG_TAG, "Loading cards.");
            for (tempIndex = index; tempIndex < (index + (cardsPerDay)); tempIndex++) {
                dueDates[tempIndex] = 1L;
                studyList.add(new Cell(tempIndex, koreanTable.get(tempIndex), englishTable.get(tempIndex)));
                Log.d(DEBUG_TAG, "Index: " + tempIndex + "\t" + koreanTable.get(tempIndex));
            }
        } else {
            Log.d(DEBUG_TAG, "Reloading cards.");
            for(int i = 0; i < dueDates.length; i++) {
                if(dueDates[i]==1L) {
                    studyList.add(new Cell(tempIndex, koreanTable.get(tempIndex), englishTable.get(tempIndex)));
                    Log.d(DEBUG_TAG, "Index: " + tempIndex + "\t" + koreanTable.get(tempIndex));
                }
            }
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
//        boolean notDone = true;
//        Log.d(DEBUG_TAG, "dueDate array size: " + Integer.toString(dueDates.length));
//        for(int i = 0; i < dueDates.length; i++) {
//            if(dueDates[i]==1L) {
//                Log.d(DEBUG_TAG, Integer.toString(i) + "-1");
//                notDone=false;
//            }
//        }
        boolean notDone = false;
        Log.d(DEBUG_TAG, "dueDate array size: " + Integer.toString(dueDates.length));
        for(int i = 0; i < dueDates.length; i++) {
            if(dueDates[i]==1L) {
                Log.d(DEBUG_TAG, Integer.toString(i) + "-1");
                notDone=true;
            }
        }
        if(!notDone) {
            cardsLoaded=false;
            studyStarted=false;
        }
        Log.d(DEBUG_TAG, "StudyStarted: " + Boolean.toString(studyStarted));
//        studyStarted=false;
        editor.putString(DUE_DATE_KEY, dueDataString);
        editor.putString(E_FACTOR_KEY, eFactorString);
        editor.putInt(INDEX_KEY, index);
        editor.putInt(CARDS_READ_KEY, cardsRead);
        editor.putInt(CARDS_PER_DAY_KEY, cardsPerDay);
        editor.putBoolean(STUDY_STARTED_KEY, studyStarted);
        editor.putBoolean(CARDS_LOADED_KEY, cardsLoaded);
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
        //Log.d(DEBUG_TAG, DUE_DATE_KEY);
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
            studyStarted=true;
            cardsLoaded = false;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getDefault());
            calendar.set(Calendar.HOUR_OF_DAY, 3);
            newDayMilli = calendar.getTimeInMillis();
//            newDayMilli = System.currentTimeMillis() - 60000;
            Log.d(DEBUG_TAG, Long.toString(newDayMilli));
            editor.putBoolean(CARDS_LOADED_KEY, cardsLoaded);
            editor.putBoolean(STUDY_STARTED_KEY, studyStarted);
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
            newDayMilli = (System.currentTimeMillis() + 3600000L); //one hour
//            newDayMilli = (System.currentTimeMillis() - (System.currentTimeMillis() % 86400000L) + 10800000L);
            cardsRead = 0;
            studyStarted = true;
            cardsLoaded=false;
            //todo removing this atm so only review cards will be pulled after the first session
            editor.putBoolean(CARDS_LOADED_KEY, cardsLoaded);
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

    /*
     *
     * END OF MAIN ACTIVITY
     *
     */


    //TODO function called at the start to speak
    public void playSound(String rawIdentifier) {
        Log.d(DEBUG_TAG, rawIdentifier);
        //int rawId = resources.getIdentifier(rawIdentifier, "raw", this.getPackageName());
        int rawId = getResources().getIdentifier(rawIdentifier, null, this.getPackageName());
        //int soundId = soundPool.load(this, R.raw.k0, 1);
        int soundId = soundPool.load(this, rawId, 1);
        //soundPool.play(soundId, 1, 1, 1, 0, 1);
        //soundPool.release();
        //soundPool = null;
        //TODO set and release this only when the current card swaps so it doesn't reload at each button press
    }

    public double calEasinessFactor(double oldEasyFactor, double quality) { //quality is 0-5 originally
        double newFactor = oldEasyFactor - (0.8 + (0.28 * quality) - (0.02 * quality * quality));
        if(newFactor<1.3) {
            newFactor=1.3;
        }
        return newFactor;
    }

    public int calRepititionInterval(int oldInterval, double easyFactor) {
        //todo make this based off of interval like 1/4 if wrong?
//        if (streak == 1) {
//            return 1;
//        } else if (streak == 2) {
//            return 3;
//        } else {
//            return (int) Math.ceil((double) oldInterval * easyFactor);
//        }
        if(oldInterval==0) {
            return 1;
        } else {
            return (int) Math.ceil((double) oldInterval * easyFactor);
        }
    }


    public void nextRound() {
        if (currentCell.getSessionStreak() < 4) {
            //playSound(currentCell.getRawIdentifier());
        }
        if (currentCell.getSessionStreak() <= 2) {
            koreanWordButton.setText(currentCell.getKorean());
        } else if (currentCell.getSessionStreak() == 3) {
            koreanWordButton.setText("♫");
        } else {
            koreanWordButton.setText(currentCell.getEnglish());
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        //createDisplayDeck();
        //buttonList.get(currentCell.getButtonIndex()).setTextColor(Color.BLUE);
//        nextRound();
    }
    @Override
    public void onPause() {
        finishSession(false);
        postProcessingData(everyList);
        saveDueDates(everyList);
        super.onPause();
    }
//    @Override
//    public void onDestroy() {
//        //return cells after calculating e-factor and days between
//        Log.d(DEBUG_TAG, "onDestroy called");
//        for(Cell c : everyList) {
//            Log.d(DEBUG_TAG, c.getKorean() + Integer.toString(c.getListID()));
//            if(c.getListID()==3 || c.isReviewCard()==2) {
//                c.setDaysBetweenReviews(calRepititionInterval(c.getDaysBetweenReviews(), c.getDifficulty()));
//                Log.d(DEBUG_TAG, Integer.toString(c.getDaysBetweenReviews()));
//            }
//        }
//        Intent resultIntent = new Intent();
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("STUDY_WORDS", everyList);
//        resultIntent.putExtras(bundle);
//        setResult(Activity.RESULT_OK, resultIntent);
//        super.onDestroy();
//    }

    public Cell updatedNextCard(Cell cell) {
        //todo remove current card? previous card?
        //todo keep retr list certain size and fix stage duration
        //everyList = new ArrayList<>();
        //everyList.addAll(learningList);
        //everyList.addAll(retrievalList);
        //if(cell.getListID()<3) {
        //    tempCell = everyList.remove(everyList.indexOf(cell));
        //}
        //do the review cards first
        for(Cell c : everyList) { //change this to a 0, 1, 2 system where 0 isn't a retrieval card, 1 is and not reviewed, and 2 is reviewed and done, 3 is a failed review
            switch(c.isReviewCard()) {
                case(1):
                    return c;
            }
        }
        double time = System.currentTimeMillis();
        int i = 0;
        int index = -1;
        for (Cell c : everyList) {
            if (c.getMilliSeconds() < time && c.getListID() == 2) { //find card whose retrieval time is less than the current time i.e. it's due
                time = c.getMilliSeconds(); //replacing it with the card's time means we can keep the same variable and find the card most due i.e. lowest card time
                //but time must be set to the current time first so that this process only finds the lowest time value of times below current time
                index = i;
            }
            i++;
        }
        if (index != -1) { //index is set to something in the list, return retrieval card
            return everyList.get(index);
        } else {
            //search for learning list card due
            i = 0; //reset i, index is still -1
            int interval = 1; //like time we set it to the value we want the cards to be lower than i.e. 1 because 0 means its due
            for (Cell c : everyList) {
                if (c.getCardsBetween() < interval && c.getListID() == 1) {
                    interval = c.getCardsBetween();
                    index = i;
                }
                i++;
            }
            if (index != -1) { //return learning card
                return everyList.get(index);
            } else {//search for a new card which has a default value of max int
                i = 0;
                for (Cell c : everyList) {
                    if (c.getCardsBetween() == Integer.MAX_VALUE && (c.getListID() == 1 || c.getListID() == 0)) {
                        index = i;
                        //todo: next and previous cards
                        break; //no reason not to take the first new card and end the loo[
                    }
                    i++;
                }
                if (index != -1) {//return new card
                    everyList.get(index).setListID(1);
                    return everyList.get(index);
                } else { //rare case, return the learning card closest to being due
                    i = 0;
                    index = -1; //0 instead of -1 because we will at least return something
                    interval = Integer.MAX_VALUE;
                    for (Cell c : everyList) {
                        if (c.getCardsBetween() < interval && c.getListID() == 1) {
                            index = i;
                            interval = c.getCardsBetween();
                        }
                        i++;
                    }
                    if (index != -1) {
                        return everyList.get(index);
                    } else {
                        //at this we only have cards that are in the retrieval stage, we just need to find the card most overdue even if its not even due
                        //this is not ideal for memorizing, normally you want a good consolidation period, so this is only done when near the end of the session when no learning cards are left
                        //answerMade() function will make sure that the deck always has enough cards so that we are just repeatedly seeing the same card at the end of the study session
                        i = 0;
                        index = 1;
                        time = everyList.get(0).getMilliSeconds(); //we will at least use the first card TODO add empty list calls OnStop and
                        for (Cell c : everyList) {
                            if (c.getMilliSeconds() < time && c.getListID() == 2) {
                                index = i;
                                time = c.getMilliSeconds();
                            }
                            i++;
                        }
                        return everyList.get(index);
                    }
                }
            }
        }
    }

    public void updatedAnswerMade(boolean result, Cell cell) {
        //everyList = new ArrayList<>();
        //everyList.addAll(learningList);
        //everyList.addAll(retrievalList);
        //decrease all cards between values
        //do not update
        if(cell.isReviewCard()==1) {
            if(result) {
                //retrieval was successful
                //todo add user response more than just correct or incorrect
                cell.setDifficulty(calEasinessFactor(cell.getDifficulty(), 4)); //normal response of 4 for now, e-factor actually will not change this way
                cell.setReviewCard(2);
                //cell.setRetrievalStreak(cell.getRetrievalStreak()+1);
            } else {
                cell.setDifficulty(calEasinessFactor(cell.getDifficulty(), 0)); //todo reset?
                //cell.setRetrievalStreak(0);//todo change this later, atm this will break it into a retrieval day of 1,2, etc.
                //now add it to the study deck by making it finished
                cell.setDaysBetweenReviews(0);
                cell.setReviewCard(3); //was 1
            }
        } else {
            for (Cell c : everyList) {
                if (c.getListID() == 1) {
                    c.setCardsBetween(c.getCardsBetween() - 1);
                }
            }
            if (result) {
                //increase streak
                cell.setSessionStreak(cell.getSessionStreak() + 1);
                //modify cell's content based on what it's session streak is
                //0-2 is learning - set new cards between value
                //3 is make it retrieval by increasing it's ListID value and setting a new time
                //5 is move it to final list by increasing ListID
                if (cell.getSessionStreak() < 3) {
                    cell.setCardsBetween(cell.getSessionStreak());
                } else if (cell.getSessionStreak() == 3) {
                    cell.setListID(2);//retrieval state
                    cell.setMiliSeconds(System.currentTimeMillis() + 1000L);
                } else if (cell.getSessionStreak() == 4) {
                    cell.setMiliSeconds(System.currentTimeMillis() + 1000L);//todo this should be a variable constant probably
                } else {
                    cell.setListID(3);
                    //check to see if the session is finished
                    boolean finished = true;
                    for (Cell c : everyList) {
                        if (c.getListID() < 3) {
                            finished = false;
                        }
                    }
                    if (finished) {
                        //finishSession(finished); //this should call onDestroy
                        //finish();
                        finishSession(finished);
                        postProcessingData(everyList);
                        saveDueDates(everyList);
                        changeLayout(0);
                    }
                }
            } else {
                if (cell.getSessionStreak() > 1) {
                    cell.setSessionStreak(cell.getSessionStreak() - 1);
                }
                if (cell.getSessionStreak() < 2) {
                    cell.setCardsBetween(cell.getSessionStreak());
                } else if (cell.getSessionStreak() == 3) {
                    cell.setCardsBetween(cell.getSessionStreak());
                    cell.setListID(1);
                } else {
                    cell.setMiliSeconds(System.currentTimeMillis() + 10000L);
                }
            }
        }
    }
    public void finishSession(boolean finished) {
        Log.d(DEBUG_TAG, "StudyActivity calling finishedSession");
        for(Cell c : everyList) {
            // Log.d(DEBUG_TAG, "StudyActivity calling finishedSession" +c.getKorean() + Integer.toString(c.getListID()));
            Log.d(DEBUG_TAG, c.getKorean() + "\t" + Integer.toString(c.getListID()) + "\t" + Integer.toString(c.isReviewCard()));
            if(c.getListID()==3 || c.isReviewCard()==2) {
                //only finished cards(ID=3), which includes failed review cards that depreciated, and successful review cards will be given new dates
                //all others will be as zero
                c.setDaysBetweenReviews(calRepititionInterval(c.getDaysBetweenReviews(), c.getDifficulty()));
                //Log.d(DEBUG_TAG, Integer.toString(c.getDaysBetweenReviews()));
            }
        }
//        Intent resultIntent = new Intent();
//        Bundle bundle = new Bundle();
//        bundle.putSerializable("STUDY_WORDS", everyList);
//        resultIntent.putExtras(bundle);
//        if(finished) {
//            setResult(Activity.RESULT_OK, resultIntent);
//        } else {
//            Log.d(DEBUG_TAG, "LocalBroadcast sending.");
//            localBroadcastManager.sendBroadcast(resultIntent);
//        }
    }
    /*
     *
     * new methods
     *
     */
    public void changeLayout(int session) {
        if(session==1) { //study layout
            Log.d(DEBUG_TAG, "Change to study layout.");
            for(Button b : mainList) {
                b.setVisibility(View.GONE);
            }
            for(Button b : studyList) {
                b.setVisibility(View.VISIBLE);
            }
            dictionaryListView.setVisibility(View.GONE);
            searchText.setVisibility(View.GONE);
            pickTime.setVisibility(View.GONE);
            settingText.setVisibility(View.GONE);
            cardsPerDayText.setVisibility(View.GONE);
            Log.d(DEBUG_TAG, "BundleList size: " + Integer.toString(bundleList.size()));
            Log.d(DEBUG_TAG, "Everylist size: " + Integer.toString(everyList.size()));
            everyList = bundleList;
            bundleList = new ArrayList<>();
            currentCell = everyList.get(0);
            currentCell.setListID(1);
            koreanWordButton.setText(currentCell.getKorean());
//        Log.d(DEBUG_TAG, currentCell.getIndex() + "\t" + currentCell.getKorean() + "\t" + currentCell.getEnglish());
            showAnswerButton.setText("?");
            nextRound();
        } else if(session==0) { //mainlayout
            Log.d(DEBUG_TAG, "Changed to main layout.");
            for(Button b : mainList) {
                b.setVisibility(View.VISIBLE);
            }
            for(Button b : studyList) {
                b.setVisibility(View.GONE);
            }
            dictionaryListView.setVisibility(View.GONE);
            searchText.setVisibility(View.GONE);
            pickTime.setVisibility(View.GONE);
            settingText.setVisibility(View.GONE);
            cardsPerDayText.setVisibility(View.GONE);
        }else if(session==2){ //dictionary layout
            for(Button b : mainList) {
                b.setVisibility(View.GONE);
            }
            for(Button b : studyList) {
                b.setVisibility(View.GONE);
            }
            dictionaryListView.setVisibility(View.VISIBLE);
            searchText.setVisibility(View.VISIBLE);
            pickTime.setVisibility(View.GONE);
            settingText.setVisibility(View.GONE);
            cardsPerDayText.setVisibility(View.GONE);
        } else if(session==3) { //settings layout
            for(Button b : mainList) {
                b.setVisibility(View.GONE);
            }
            for(Button b : studyList) {
                b.setVisibility(View.GONE);
            }
            dictionaryListView.setVisibility(View.GONE);
            searchText.setVisibility(View.GONE);
            pickTime.setVisibility(View.VISIBLE);
            settingText.setVisibility(View.VISIBLE);
            cardsPerDayText.setVisibility(View.VISIBLE);
        }
    }
}
