package com.example.joshua.koreancards;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
//import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.TimeZone;

public class MainActivity extends Activity implements TimePickerDialog.OnTimeSetListener {
    private final String DEBUG_TAG = "DEBUG_TAG";
    private Button studyButton, dataButton, dictionaryButton, settingsButton, saveButton, loadButton;
    private final String KOREAN_TABLE_NAME = "KoreanTable.tmp";
    private final String ENGLISH_TABLE_NAME = "EnglishTable.tmp";
    private ObjectInputStream ois;
    private final String DUE_DATE_KEY = "due_date_key";
    private String dueDataString = "";
    private final String E_FACTOR_KEY = "e_factor_key";
    private String eFactorString = "";
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
    private ListView dictionaryListView, loadFileListView;
    private EditText searchText;
    private CustomAdapter customAdapter;
    private ArrayAdapter arrayAdapter;
    private ArrayList<Cell> dictionaryList;
    private ArrayList<CardTable> reviewCardList;
    private ArrayList<CardTable> allCardList;
    private ArrayList<CardTable> newCards;
    private CardTable tempCard;
    //private ArrayList<Cell> cellArrayList = new ArrayList<>();

    /*
     *
     * END OF MAIN ACTIVITY VARIABLES
     *
     */

    private Calendar calendar = Calendar.getInstance();
    private ArrayList<Cell> everyList = new ArrayList<>();
    private Cell currentCell = null;
    private Button koreanWordButton, showAnswerButton, wrongAnswerButton, correctAnswerButton, timerButton, undoButton;
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

    private static CardDatabase cardDatabase;
    private int[] gapMap = new int[]{1,2,3,4,5,6};
//    private int studyReviewMode = -1;
    private boolean studyReviewMode = false;

    private TextView timerText;

    private TextToSpeech englishTextToSpeech;
    private TextToSpeech koreanTextToSpeech;

    private Stack<CardTable> tempCardHistory = new Stack<>();
    private Stack<ArrayList<CardTable>> newCardHistory = new Stack<>();
    private Stack<ArrayList<CardTable>> reviewCardHistory = new Stack<>();

    private ArrayList<CardTable> initialReviewList = new ArrayList<>();
    private ArrayList<CardTable> initialNewCardList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_layout);

        allCardList = new ArrayList<>();
        newCards = new ArrayList<>();
        reviewCardList = new ArrayList<>();

        cardDatabase = Room.databaseBuilder(getApplicationContext(), CardDatabase.class, "carddb").allowMainThreadQueries().build();
        englishTextToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    englishTextToSpeech.setLanguage(Locale.ENGLISH);
                }
            }
        });
        koreanTextToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    koreanTextToSpeech.setLanguage(Locale.KOREAN);
                }
            }
        });

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        editor = sharedPrefs.edit();
        initialTasks();


        dictionaryListView = (ListView)findViewById(R.id.dictionaryListView);
        loadFileListView = findViewById(R.id.loadFileListView);
        searchText = (EditText)findViewById(R.id.searchButton);
        //todo make this a method
        dictionaryList = new ArrayList<>();
        Log.d(DEBUG_TAG, "DictionaryList: " + Integer.toString(dictionaryList.size()));
        customAdapter = new CustomAdapter(this, R.layout.customlayout, allCardList);
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
                //todo change canStudy to something useful later, for now keep as true
                if(newCards.size()>0 || reviewCardList.size()>0) {
                    Log.d(DEBUG_TAG, "ReviewList size: " + Integer.toString(reviewCardList.size()));
//                    Log.d(DEBUG_TAG, "BundleList size: " + bundleList.size());
                    changeLayout(1);
                }
                //todo check if anything changes between getting cards from database and here
                for(CardTable cT: newCards) {
                    initialNewCardList.add(cT.createCopy());
                }
                for(CardTable cT: reviewCardList) {
                    initialReviewList.add(cT.createCopy());
                }
                //todo why check?
                checkDatabase();
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
            }
        });
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLayout(3);
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
        undoButton = findViewById(R.id.UndoButton);
        studyList.add(undoButton);
        timerText = findViewById(R.id.TimerText);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool.Builder().setMaxStreams(1).build();
        } else {
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 1);
        }
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int soundStatus) {
                soundPool.play(sampleId, 1.0f, 1.0f, 0, 0, 1.0f);
            }
        });

        koreanWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo play audio later
            }
        });
        showAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(tempCard.getStreak() <= 3) {
                    showAnswerButton.setText(tempCard.getNativeWord());
                } else if(tempCard.getStreak()==4 || studyReviewMode==true) {
                    showAnswerButton.setText(tempCard.getNativeWord() + " - " + tempCard.getForeignWord());
                } else {
                    showAnswerButton.setText(tempCard.getForeignWord());
                }
            }
        });
        wrongAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatedCardAnswer(false);
                checkDeckStatus();
                int i = 0;
                for(CardTable cT : newCards) {
                    Log.d(DEBUG_TAG, i + " - " + cT.getForeignWord() + "\tDays: " + cT.getDays() + "\tStreak: " + cT.getStreak());
                    i+=1;
                }
                int j = 0;
                for(CardTable cT : reviewCardList) {
                    Log.d(DEBUG_TAG, j + " - " + cT.getForeignWord() + "\tDays: " + cT.getDays() + "\tStreak: " + cT.getStreak());
                    j+=1;
                }
                Log.d(DEBUG_TAG, "AnswerHistory Size: " + Integer.toString(tempCardHistory.size()));
                Log.d(DEBUG_TAG, "NewCardList size: " + Integer.toString(newCards.size()));
                Log.d(DEBUG_TAG, "ReviewList Size: " + Integer.toString(reviewCardList.size()));
                Log.d(DEBUG_TAG, "Boolean status: " + Boolean.toString(studyReviewMode));
                if(newCards.size() > 0 || reviewCardList.size() > 0) { //todo this will change, most likely reviewcardList and boolean check
                    tempCard = updatedNextCard();
                    nextRound();
                    showAnswerButton.setText("?");
                }


            }
        });
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                undoButtonPressed();
                int i = 0;
                for(CardTable cT : newCards) {
                    Log.d(DEBUG_TAG, i + " - " + cT.getForeignWord() + "\tDays: " + cT.getDays() + "\tStreak: " + cT.getStreak());
                    i+=1;
                }
                int j = 0;
                for(CardTable cT : reviewCardList) {
                    Log.d(DEBUG_TAG, j + " - " + cT.getForeignWord() + "\tDays: " + cT.getDays() + "\tStreak: " + cT.getStreak());
                    j+=1;
                }
                Log.d(DEBUG_TAG, "AnswerHistory Size: " + Integer.toString(tempCardHistory.size()));
                Log.d(DEBUG_TAG, "NewCardList size: " + Integer.toString(newCardHistory.size()));
                Log.d(DEBUG_TAG, "ReviewList Size: " + Integer.toString(reviewCardHistory.size()));
                Log.d(DEBUG_TAG, "Boolean status: " + Boolean.toString(studyReviewMode));
                nextRound();
                //check status of deck?
            }
        });
        correctAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = 0;
                for(CardTable cT : newCards) {
                    Log.d(DEBUG_TAG, i + " - " + cT.getForeignWord() + "\tDays: " + cT.getDays() + "\tStreak: " + cT.getStreak());
                    i+=1;
                }
                int j = 0;
                for(CardTable cT : reviewCardList) {
                    Log.d(DEBUG_TAG, j + " - " + cT.getForeignWord() + "\tDays: " + cT.getDays() + "\tStreak: " + cT.getStreak());
                    j+=1;
                }
                Log.d(DEBUG_TAG, "AnswerHistory Size: " + Integer.toString(tempCardHistory.size()));
                Log.d(DEBUG_TAG, "NewCardList size: " + Integer.toString(newCards.size()));
                Log.d(DEBUG_TAG, "ReviewList Size: " + Integer.toString(reviewCardList.size()));
                Log.d(DEBUG_TAG, "Boolean status: " + Boolean.toString(studyReviewMode));
                updatedCardAnswer(true);
                checkDeckStatus();
                if(newCards.size() > 0 || reviewCardList.size()>0) {
                    tempCard = updatedNextCard();
                    nextRound();
                    showAnswerButton.setText("?");
                }
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
        loadButton = findViewById(R.id.LoadButton);
        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readDatabaseSave();
            }
        });
        saveButton = findViewById(R.id.SaveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeDatabaseSave();
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
        int soundId = soundPool.load(this, R.raw.a, 1);
        //soundPool.play(soundId, 1, 1, 1, 0, 1);
        //soundPool.release();
        //soundPool = null;
        //TODO set and release this only when the current card swaps so it doesn't reload at each button press
    }
    public void updatedPlaySound(String text) {
        koreanTextToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    public double calEasinessFactor(double oldEasyFactor, double quality) { //quality is 0-5 originally
        double newFactor = oldEasyFactor - (0.8 + (0.28 * quality) - (0.02 * quality * quality));
        if(newFactor<1.3) {
            newFactor=1.3;
        }
        return newFactor;
    }

    public int calRepetitionInterval(int oldInterval, double easyFactor) {
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
        } else if(oldInterval==1) {
            return 2;
        } else {
            return (int) Math.ceil((double) oldInterval * easyFactor);
        }
    }


    public void nextRound() {
        if(tempCard.getStreak()<4) {
            playSound(Integer.toString(tempCard.getIndexKey()) + "_" + tempCard.getForeignWord() + ".mp3");
//            updatedPlaySound(tempCard.getForeignWord());
        }
        if(tempCard.getStreak() <= 3) {
            koreanWordButton.setText(tempCard.getForeignWord());
        } else if(tempCard.getStreak()==4 || studyReviewMode==true) {
            koreanWordButton.setText("♫");
//            updatedPlaySound(tempCard.getForeignWord());
            playSound(Integer.toString(tempCard.getIndexKey()) + "_" + tempCard.getForeignWord() + ".mp3");
        } else {
            koreanWordButton.setText(tempCard.getNativeWord());
        }
    }
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
            loadButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            if(reviewCardList.size() >0) {
                tempCard = reviewCardList.get(0);
            } else {
                tempCard = newCards.get(0);
            }
//            koreanWordButton.setText(currentCell.getKorean());
            koreanWordButton.setText(tempCard.getForeignWord());
//        Log.d(DEBUG_TAG, currentCell.getIndex() + "\t" + currentCell.getKorean() + "\t" + currentCell.getEnglish());
            showAnswerButton.setText("?");
            nextRound();
            timerText.setVisibility(View.GONE);
            loadFileListView.setVisibility(View.GONE);
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
            loadButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            timerText.setVisibility(View.GONE);
            loadFileListView.setVisibility(View.GONE);
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
            loadButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            timerText.setVisibility(View.GONE);
            loadFileListView.setVisibility(View.GONE);
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
            loadButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            timerText.setVisibility(View.GONE);
            loadFileListView.setVisibility(View.GONE);
        } else if(session==4) {
            for(Button b : mainList) {
                b.setVisibility(View.GONE);
            }
            for(Button b : studyList) {
                b.setVisibility(View.GONE);
            }
            dictionaryListView.setVisibility(View.GONE);
            searchText.setVisibility(View.GONE);
            pickTime.setVisibility(View.GONE);
            settingText.setVisibility(View.GONE);
            cardsPerDayText.setVisibility(View.GONE);
            loadButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            timerText.setVisibility(View.VISIBLE);
            loadFileListView.setVisibility(View.GONE);
        } else if(session==5) {
            for(Button b : mainList) {
                b.setVisibility(View.GONE);
            }
            for(Button b : studyList) {
                b.setVisibility(View.GONE);
            }
            dictionaryListView.setVisibility(View.GONE);
            searchText.setVisibility(View.GONE);
            pickTime.setVisibility(View.GONE);
            settingText.setVisibility(View.GONE);
            cardsPerDayText.setVisibility(View.GONE);
            loadButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            timerText.setVisibility(View.GONE);
            loadFileListView.setVisibility(View.VISIBLE);
        }
    }
    public void setupDatabase() {
        BufferedReader bufferedReader = null;
        try {
//            bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("WordPairs.txt"), "UTF-8"));
            bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("1_500_translated.txt"), "UTF-8"));

            String tempLine;
            String[] splitTempLine;
            int loopIndex = 0;
            while((tempLine = bufferedReader.readLine()) != null) {
                splitTempLine = tempLine.split(",", 2);
//                Log.d(DEBUG_TAG, tempLine + "\tsplitTempLine Length: " + Integer.toString(splitTempLine.length));
                MainActivity.cardDatabase.cardDao().addCard(new CardTable(loopIndex, splitTempLine[0].trim(), splitTempLine[1].trim(), 2.5, Long.MAX_VALUE, 0, (short) 0));
                loopIndex += 1;
            }
            Log.d(DEBUG_TAG, "Database initialized. Size: " + Integer.toString(loopIndex));
        } catch (IOException e) {
            Log.e(DEBUG_TAG, "Setup database has failed.", e);
            //log the exception
        } finally {
            if(bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Log.e(DEBUG_TAG, "Could not close the buffered reader.", e);
                }
            }
        }
    }
    public void loadDeck() {
        Log.d(DEBUG_TAG, "All cards loaded.");
        allCardList = new ArrayList<>(MainActivity.cardDatabase.cardDao().getCards());

    }
    public void cardsDue() {
        Log.d(DEBUG_TAG, "Review cards loaded.");
        reviewCardList = new ArrayList<>(MainActivity.cardDatabase.cardDao().getCardsDue(System.currentTimeMillis()));
    }
    public void updateCard(CardTable cardTable) {
        MainActivity.cardDatabase.cardDao().updateCard(cardTable);
    }
    public void getCard(int index) {
        MainActivity.cardDatabase.cardDao().getCard(index);
    }
    public void initialTasks() {
        //index, cardsperday, time, timezone?, actual, time?
        Log.d(DEBUG_TAG, "InitialTasks called");
        loadSharedPreferences();
        loadDeck();
        checkDueTime();
        loadStartedCards();
        checkDatabase();
    }
    public void loadStartedCards() {
        newCards.addAll(MainActivity.cardDatabase.cardDao().getReviewCards());
    }
    public void loadSharedPreferences() {
        if(!sharedPrefs.contains(INDEX_KEY)) {
            index = 0;
            cardsPerDay = 10;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getDefault());
            calendar.set(Calendar.HOUR_OF_DAY, 3);
            newDayMilli = calendar.getTimeInMillis();
//            newDayMilli = System.currentTimeMillis() - 60000;
            //todo hour
            Log.d(DEBUG_TAG, Long.toString(newDayMilli));
            editor.putInt(INDEX_KEY, index);
            editor.putInt(CARDS_PER_DAY_KEY, cardsPerDay);
            editor.putLong(NEW_DAY_MILLI_KEY, newDayMilli);
            editor.apply();
            setupDatabase();
        } else {
            //sharedPrefs.getLong(NEW_DAY_MILLI_KEY, newDayMilli);
            index = sharedPrefs.getInt(INDEX_KEY, index);
            cardsPerDay = sharedPrefs.getInt(CARDS_PER_DAY_KEY, cardsPerDay);
            newDayMilli = sharedPrefs.getLong(NEW_DAY_MILLI_KEY, newDayMilli);
            Log.d(DEBUG_TAG, "New Day Milli: " + "\t" + Long.toString(newDayMilli));
        }
    }
    public void checkDueTime() {
        Log.d(DEBUG_TAG, "Difference: " + Long.toString(newDayMilli - System.currentTimeMillis()));
        cardsDue();
        if(System.currentTimeMillis() > newDayMilli) {
            //todo this is failing early in the morning obviously
            //new day to study
            //increment newDayMilli
            newDayMilli = (System.currentTimeMillis() + 3600000L); //one hour
//            newDayMilli = (System.currentTimeMillis() - (System.currentTimeMillis() % 86400000L) + 10800000L);
            editor.putLong(NEW_DAY_MILLI_KEY, newDayMilli);
            editor.apply();
            loadNewCards();
        }
    }
    public void loadNewCards() {
        Log.d(DEBUG_TAG, "Loading new cads.");
        for(int i = 0;i<cardsPerDay;i++) {
            newCards.add(MainActivity.cardDatabase.cardDao().getCard(index+i));
            newCards.get(i).setTime(0);
            MainActivity.cardDatabase.cardDao().updateCard(newCards.get(i)); //cards now marked as study cards
        }
        index += cardsPerDay;
        editor.putInt(INDEX_KEY, index);
        editor.apply();
    }
    public void undoButtonPressed() {
        //todo remove
        if(!tempCardHistory.empty()) {
            tempCard = tempCardHistory.pop();
            //was the card removed at this point?
            boolean tempFound = false;
            for(CardTable cT : newCards) {
                if(tempCard.getIndexKey() == cT.getIndexKey()) {
                    tempFound = true;
                }
            }
            for(CardTable cT : reviewCardList) {
                if(tempCard.getIndexKey() == cT.getIndexKey()) {
                    tempFound = true;
                }
            }

            if(!tempFound) {
                //card missing, need to undo the write with the copy
                for(CardTable cT : initialNewCardList) {
                    if(cT.getIndexKey() == tempCard.getIndexKey()) {
                        MainActivity.cardDatabase.cardDao().updateCard(cT);
                    }
                }
                for(CardTable cT : initialReviewList) {
                    if(cT.getIndexKey() == tempCard.getIndexKey()) {
                        MainActivity.cardDatabase.cardDao().updateCard(cT);
                    }
                }
            }

            newCards = newCardHistory.pop();
            reviewCardList = reviewCardHistory.pop();
            //if in study mode check put back in newcards
            //check to see if anything changes at the time of assignment or if tempcard can just change
        }

    }
    public void updatedCardAnswer(boolean result) {
        //only method to change the card data



        tempCardHistory.push(tempCard.createCopy());
        ArrayList<CardTable> newCardsCopy = new ArrayList<>();
        for(CardTable cT: newCards) {
            newCardsCopy.add(cT.createCopy());
        }
        newCardHistory.push(newCardsCopy);
        ArrayList<CardTable> reviewCardListCopy = new ArrayList<>();
        for(CardTable cT: reviewCardList) {
            reviewCardListCopy.add(cT.createCopy());
        }
        reviewCardHistory.push(reviewCardListCopy);
        //todo move this to the end, maybe?

        if(result) {
            //correct answer
            Log.d(DEBUG_TAG, "Time value: " + Long.toString(tempCard.getTime()));
            if(tempCard.getTime()!=0) {
                //review card answered correctly. Increase streak, calculate new factor, days, time and then enter it into the database
                //todo make the database interaction asynchronous calls
                Log.d(DEBUG_TAG, "Writing card to database");
//                short tempShort = tempCard.getStreak();
//                tempShort+=((short)1);
//                tempCard.setStreak(tempShort);
//                tempCard.setFactor(calEasinessFactor(tempCard.getFactor(), 3)); //3 for now
//                tempCard.setDays(calRepetitionInterval(tempCard.getDays(),tempCard.getFactor()));
//                tempCard.setTime(newDayMilli + (tempCard.getDays()-1)*86400);//subtract one because newdaymilli is always ahead todo check clock function
//                MainActivity.cardDatabase.cardDao().updateCard(tempCard);
                cardDone(tempCard, 2.5);
                reviewCardList.remove(0);
            } else if(studyReviewMode==true) {
                //correct answer by a study review session
                tempCard.setDays(0);
                tempCard.setStreak((short)0);
//                newCards.remove(tempCard);
                reviewCardList.remove(0);
                Log.d(DEBUG_TAG, "Writing card to database");
                cardDone(tempCard, 2.5);
                //todo correct answer and write it
            } else {
                for(CardTable cT : newCards) {
                    if(cT.getStreak()!=0 && cT.getStreak()!=6) {
                        cT.setDays(cT.getDays() - 1);
                    }
//                    Log.d(DEBUG_TAG, cT.getForeignWord() + "\tDays: " + cT.getDays() + "\tStreak: " + cT.getStreak());
                }
                short tempShort = tempCard.getStreak();
                if(tempShort < 6) {
                    tempShort += ((short) 1);
                }
                tempCard.setStreak(tempShort);
                tempCard.setDays(gapMap[tempCard.getStreak()-1]);
            }
        } else {
            for(CardTable cT : newCards) {
                if(studyReviewMode==false && cT.getStreak()!=0 && cT.getStreak()!=6) {
                    cT.setDays(cT.getDays() - 1);
                }
//                Log.d(DEBUG_TAG, cT.getForeignWord() + "\tDays: " + cT.getDays() + "\tStreak: " + cT.getStreak());
            }
            //word incorrect
            if(tempCard.getTime()!=0) {
                Log.d(DEBUG_TAG, "InitialReview card because time is: " + Long.toString(tempCard.getTime()));
                //review card failed, move it to the study list and reset the days and streak. Do not touch the factor
                tempCard.setStreak((short) 0);
                tempCard.setDays(0);
                newCards.add(tempCard);
                reviewCardList.remove(0);
            }else if (studyReviewMode==true){
                tempCard.setStreak((short)3);
                tempCard.setDays(0);
                //review method failed
                reviewCardList.remove(0);
                newCards.add(tempCard);
            } else {
                //study card failed, decrease the streak by 1 and reset the days
                if(tempCard.getStreak()!=0) {
                    short tempShort = tempCard.getStreak();
                    tempShort-=1;
                    tempCard.setStreak(tempShort);
                    tempCard.setDays(gapMap[tempCard.getStreak()]);
                } else {
                    tempCard.setDays(0);
                }
            }
        }
    }
    public CardTable updatedNextCard() {
        if(reviewCardList.size()>0 && studyReviewMode==false) {
            Log.d(DEBUG_TAG, "returning new reviewCard");
            //still cards to review
            return reviewCardList.get(0);
        } else {
            if(studyReviewMode==true) {
                //every time this method is called the value studyreviewmode will be safe
                //it will be set in a check deck status method
//                int temptInt = studyReviewMode;
//                studyReviewMode+=1; //using this temp variable so we can change the value of studyReviewMode before the return
//                return newCards.get(temptInt);
                return reviewCardList.get(0);
            } else {
                //get the card most due, based on the lowest days value and for equal values choose the high streak
                int index = -1;
                short tempStreak = -1; //this okay? todo
                int tempDays = Integer.MAX_VALUE;
                for(int i = 0; i<newCards.size();i++) {
                    if(newCards.get(i).getDays() <= tempDays) {
                        //card is due, and if another card is due because it's already been found, that means this one is more due
                        index = i;
                        tempStreak = newCards.get(i).getStreak();
                        tempDays = newCards.get(i).getDays();
                    } else if(newCards.get(i).getDays() == tempDays && newCards.get(i).getStreak() < tempStreak){
                        //in the case of two cards being overdue, choose the one with the lower streak value so they can catch up
                        index = i;
                        tempStreak = newCards.get(i).getStreak();
                        tempDays = newCards.get(i).getDays();
                    }
                }
                if(tempDays <0) {
                    //card due was found, return it
                    return newCards.get(index);
                } else {
                    //no new card, return new one
                    for(CardTable cT : newCards) {
                        if(cT.getStreak()==0) {
                            return cT;
                        }
                    }
                    //no new cards, return closest card
                    return newCards.get(index);

                    //no card is actually due, get a new one with the value of int max
                }
            }
        }
    }
    public void checkDeckStatus() {
        //check if it's time to do the study mode
        if(studyReviewMode==true) {
            //review session in progress
//            if(studyReviewMode==newCards.size()) {
            if(reviewCardList.size()==0) {
                //review session finished
                //todo all review words should be done
                studyReviewMode=false;
                //set all words with a streak less than 6 to streak 3 and countdown 0
                //remove the others which have been added to the database
//                ArrayList<Integer> tempArray = new ArrayList<>();
//                for(CardTable cardTable : newCards) {
//                for(int i = 0; i < newCards.size(); i++) {
//                    if(newCards.get(i).getStreak()==6) {
//                        //was remembered after consolidation time
//                        //remove from list
//                        //todo
////                        newCards.get(i).setDays(0);
////                        newCards.get(i).setStreak((short)0);
////                        cardDone(newCards.get(i), 2.5); //todo algorithm based on how many tries?
////                        newCards.remove(cardTable);
////                        if(newCards.size()==0) {
////                            changeLayout(0);
////                        }
////                        tempArray.add(i);
//                    } else {
//                        //word wasn't remembered hence its strike was reduced below 6
//                        //set streak to 3 and due to 0
////                        newCards.get(i).setStreak((short)3);
////                        newCards.get(i).setDays(0);
//                    }
//                }
                if(newCards.size()==0) {
//                    newCards.clear();
//                    tempArray.clear();
                    changeLayout(0);
                    checkDatabase();
                }
                //check post study and post 10min break
                //remove done cards
//                for(int i : tempArray) {
//                    newCards.remove(i);
//                }
            }
        } else {
            //still learning new words
            boolean startTimer = true;
            for (CardTable ct : newCards) {
                if (ct.getStreak() < 6) { //not done reviewing
                    startTimer = false;
                }
            }
            if (startTimer) {
                //start timer and review session after so set the boolean here
//                studyReviewMode = 0;
//                //todo timer and review, for now end it
//                for(CardTable cT : newCards) {
//                    cT.setDays(0);
//                    cT.setStreak((short)0);
//                    cardDone(cT, 2.5); //todo make this a method
//                    //newCards.remove(cT);
//                }
//                newCards.clear();
//                changeLayout(0);
                studyReviewMode = true; //reviewmode will start automatically when timer ends
                reviewCardList = new ArrayList<>(newCards);
                newCards.clear();
                changeLayout(4);
                startStudyTimer(60000, 1000);
                checkDatabase();
            }
        }
    }
    public void cardDone(CardTable cardTable, double answerQuality) {
        //todo change layout
//        short tempShort = cardTable.getStreak();
        short tempShort = 0;
        for(CardTable cT : initialNewCardList) {
            if(cT.getIndexKey() == cardTable.getIndexKey()) {
                tempShort = cardTable.getStreak();
            }
        }
        for(CardTable cT : initialReviewList) {
            if(cT.getIndexKey() == cardTable.getIndexKey()) {
                tempShort = cardTable.getStreak();
            }
        }
        tempShort += (short) 1;
        cardTable.setStreak(tempShort);
        cardTable.setFactor(calEasinessFactor(cardTable.getFactor(), answerQuality)); //probably wrong
        cardTable.setDays(calRepetitionInterval(cardTable.getDays(), cardTable.getFactor()));
        cardTable.setTime(newDayMilli+((86400000)*(cardTable.getDays()-1)));
        MainActivity.cardDatabase.cardDao().updateCard(cardTable);
    }
    public void startStudyTimer(int duration, int tick) {
        new CountDownTimer(duration, tick) {
            public void onTick(long millisUntilFinished) {
                timerText.setText(Long.toString(millisUntilFinished/1000));
            }
            public void onFinish() {
                changeLayout(1);
            }
        }.start();
    }
    public void checkDatabase() {
        //temporary method for debugging to see the values in the database
        for(int i = 0; i<20; i++) {
            CardTable tempCard = MainActivity.cardDatabase.cardDao().getCard(i);
            Log.d(DEBUG_TAG, "CardData: "+ Integer.toString(tempCard.getIndexKey()) + "-" + tempCard.getForeignWord() + "-" + tempCard.getNativeWord() + " Factor: " + Double.toString(tempCard.getFactor()) +
            " Time: " + Long.toString(tempCard.getTime()) + " Days: " + tempCard.getDays() + " Streak: " + tempCard.getStreak());
        }
    }
    public void writeDatabaseSave() {
        getWritePermission();
       String state = Environment.getExternalStorageState();
       if(!Environment.MEDIA_MOUNTED.equals(state)) {
           //if not mounted we cannot write
           Log.d(DEBUG_TAG,"Write failed.");
           return;
       }
       File file = new File(Environment.getExternalStorageDirectory(), "Test_Writing_File");
       FileOutputStream fos = null;
       try {
           file.createNewFile();
           fos = new FileOutputStream(file, true);
           ObjectOutputStream os = new ObjectOutputStream(fos);

           ArrayList<CardTable> cardsModified = new ArrayList<>(MainActivity.cardDatabase.cardDao().getModifiedCards(Long.MAX_VALUE));

           for(CardTable cT: cardsModified) {
               os.writeObject(cT);
           }
           os.writeObject(null);
           //os.writeObject(tempCard);
           Log.d(DEBUG_TAG, "Card written.");
           //todo flush?
           os.close();
           fos.close();
       } catch (Exception e) {
           Log.e(DEBUG_TAG, "Card failed to write.", e);
           e.printStackTrace();
       }
    }
    public void readDatabaseSave() {
        getReadPermission();
//        baseAdapter = new B
//        loadFileListView.setAdapter();
        String state = Environment.getExternalStorageState();
        if(!Environment.MEDIA_MOUNTED.equals(state)) {
            //if not mounted we cannot write
            Log.d(DEBUG_TAG,"Write failed.");
            return;
        }
//        File sdCard = Environment.getExternalStorageDirectory();
//        File dirs = new File(sdCard.getAbsolutePath());
//        if(dirs.exists()) {
////            ArrayList<File> files  = new ArrayList<File>(Arrays.asList(dirs.listFiles()));
//            File[] files = dirs.listFiles();
//             arrayAdapter = new ArrayAdapter<>(this, R.layout.load_files_list_layout, files);
//            loadFileListView.setAdapter(arrayAdapter);
//            changeLayout(5);
//            //todo add in button click listener
//        }


        File file = new File(Environment.getExternalStorageDirectory(), "Test_Writing_File");
        FileInputStream fis = null;
        boolean moreToRead = true;
        try {
            fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            do {
                Object loadedObject = ois.readObject();
                if(!loadedObject.equals(null)) {
                    //todo loaded object not null
                    //write to database after it has been cleared
                } else {
                    moreToRead = false;
                }
            } while(moreToRead);
        } catch (Exception e) {
            Log.e(DEBUG_TAG, "Card failed to write.", e);
            e.printStackTrace();
        }
    }
    public void getWritePermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //permission not granted
            //show explanation?
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //show explanation if needed
            }
            //ask for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
        }
    }
    public void getReadPermission() {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //permission not granted
            //show explanation?
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                //show explanation if needed
            }
            //ask for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(MainActivity.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}
