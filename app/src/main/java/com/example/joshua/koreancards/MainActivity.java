package com.example.joshua.koreancards;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.TimePickerDialog;
import android.arch.persistence.room.Room;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Stack;
import java.util.TimeZone;

public class MainActivity extends Activity implements TimePickerDialog.OnTimeSetListener {
    private final String DEBUG_TAG = "DEBUG_TAG";
    private Button studyButton, dictionaryButton, settingsButton, saveButton, loadButton;
    private final String CARDS_PER_DAY_KEY = "cards_per_day_key";
    private int cardsPerDay; //10 is default
    private final String INDEX_KEY = "index_key";
    private int index = 0;
    private final String NEW_DAY_MILLI_KEY = "new_day_miLli_key";
    private final String TIME_STUDIED = "time_studied";
    private long newDayMilli;
    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor editor;
    private ListView dictionaryListView, loadFileListView;
    private EditText searchText;
    private CustomAdapter customAdapter;
    private ArrayAdapter arrayAdapter;
    private ArrayList<Cell> dictionaryList;
    private ArrayList<CardTable> reviewCardList;
    private ArrayList<CardTable> allCardList;
    private ArrayList<CardTable> newCards;
    private CardTable tempCard;

    private Calendar calendar = Calendar.getInstance();
    private Button showAnswerButton, wrongAnswerButton, correctAnswerButton, undoButton;
    //TODO api catch
    private SoundPool soundPool;
    private Button pickTime;
    private TextView settingText, koreanWordButton, studyModeText, wordsLearnedText, infoText, waitingText;
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

    private int viewID = -1;

    private NotificationManagerCompat notificationManagerCompat;

    private boolean waitingTimer = false;
    private int timeStudied = 0;
    private int timeInterval = 0;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(viewID == -1) {
            super.onCreate(savedInstanceState);

            notificationManagerCompat = NotificationManagerCompat.from(this);

            setContentView(R.layout.test_layout);

            allCardList = new ArrayList<>();
            newCards = new ArrayList<>();
            reviewCardList = new ArrayList<>();

            cardDatabase = Room.databaseBuilder(getApplicationContext(), CardDatabase.class, "carddb").allowMainThreadQueries().build();
            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            editor = sharedPrefs.edit();
            initialTasks();
            /*
             *
             * END OF ON CREATE FOR MAIN ACTIVITY
             *
             */
            calendar.setTimeInMillis(System.currentTimeMillis());

            //TODO custom layout with all buttons
            //TODO move some of the below onCreate() to a new function which plays which this view loads
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
            updatedChangeLayout(0);
        } else {
            updatedChangeLayout(viewID);
        }
    }

    @Override
    public void onBackPressed() {
        //todo make this unique for each view. Study view the worst. For now change to originaly
        updatedChangeLayout(0);
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
    @Override
    protected  void onDestroy() {
//        try {
////            fos.close();
////            oos.close();
////            fis.close();
////            ois.close();
//        } catch(IOException e) {
//            e.printStackTrace();
//        }
        super.onDestroy();
    }

    public void playSound(String rawIdentifier) {
//        try {
//            AssetFileDescriptor afd = getAssets().openFd(rawIdentifier);
//            MediaPlayer mediaPlayer = new MediaPlayer();
//            mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
//            mediaPlayer.prepare();
//            mediaPlayer.start();
//        } catch(Exception e) {
//            Log.e(DEBUG_TAG, "Couldn't load MP3 to play sound.", e);
//        }

        try {
            AssetFileDescriptor assetFileDescriptor = getAssets().openFd(rawIdentifier);
            soundPool.load(assetFileDescriptor, 1);
        } catch(Exception e) {
            Log.e(DEBUG_TAG, "Could not get file descriptor of mp3.", e);
        }

        Log.d(DEBUG_TAG, rawIdentifier);
        //int rawId = resources.getIdentifier(rawIdentifier, "raw", this.getPackageName());
//        int rawId = getResources().getIdentifier(rawIdentifier, null, this.getPackageName());
        //int soundId = soundPool.load(this, R.raw.k0, 1);
//        int soundId = soundPool.load(this, R.raw.a, 1);
//        int soundId = soundPool.load(this, rawId, 1);
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
        if(viewID!=4) {
            showAnswerButton.setVisibility(View.VISIBLE);
        }
        Log.d(DEBUG_TAG, Integer.toString(reviewCardList.size()));
        correctAnswerButton.setClickable(false);
        wrongAnswerButton.setClickable(false);
        if(reviewCardList.size() > 0 && studyReviewMode == false) {
            //reviewmode for cards due at the beginning of the exercise
            koreanWordButton.setText("♫");
            studyModeText.setText("Reviewing, " + reviewCardList.size() + " cards left");
            playSound("a" + Integer.toString(tempCard.getIndexKey()) + ".mp3");
        } else {
            studyModeText.setText("Studying New Cards");
            if (tempCard.getStreak() < 4) {
                playSound("a" + Integer.toString(tempCard.getIndexKey()) + ".mp3");
                //            updatedPlaySound(tempCard.getForeignWord());
            }
            if (tempCard.getStreak() <= 3) {
                koreanWordButton.setText(tempCard.getForeignWord());
            } else if (tempCard.getStreak() == 4 || studyReviewMode == true) {
                koreanWordButton.setText("♫");
                //            updatedPlaySound(tempCard.getForeignWord());
                playSound("a" + Integer.toString(tempCard.getIndexKey()) + ".mp3");
            } else {
                //todo will this run? Are reviewcards reset to 0 streak?
                Log.d(DEBUG_TAG, "ReviewCard Streak: " + Integer.toString(tempCard.getStreak()));
                koreanWordButton.setText(tempCard.getNativeWord());
            }
            if(tempCard.getStreak()==0) {
                showAnswerButton.setVisibility(View.INVISIBLE);
                correctAnswerButton.setClickable(true);
                koreanWordButton.setText(koreanWordButton.getText() + " - " + tempCard.getNativeWord());
            } else {
//                koreanWordButton.setText(koreanWordButton.getText() + " - " + tempCard.getStreak());
                koreanWordButton.setText(koreanWordButton.getText());
            }
        }
    }
    public void updatedChangeLayout(int session) {
        viewID = session;
        if(waitingTimer) {
            session = 4;
        }
        if(session==0) {
            //main screen
            setContentView(R.layout.test_layout);
            wordsLearnedText=findViewById(R.id.wordsLearnedText);
            studyButton=findViewById(R.id.StudyButton);
            dictionaryButton=findViewById(R.id.DictionaryButton);
            settingsButton=findViewById(R.id.SettingsButton);
            infoText=findViewById(R.id.infoText);
            infoText.setText(infoText.getText() + Integer.toString(reviewCardList.size()) + "\n" + "New Words: " + Integer.toString(newCards.size()));
            studyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for(CardTable cT: newCards) {
                        Log.d(DEBUG_TAG, "CardData: "+ Integer.toString(cT.getIndexKey()) + "-" + cT.getForeignWord() + "-" + cT.getNativeWord() + " Factor: " + Double.toString(cT.getFactor()) +
            " Time: " + Long.toString(cT.getTime()) + " Days: " + cT.getDays() + " Streak: " + cT.getStreak());
                    }
                    for(CardTable cT : reviewCardList) {
                        Log.d(DEBUG_TAG, "CardData: "+ Integer.toString(cT.getIndexKey()) + "-" + cT.getForeignWord() + "-" + cT.getNativeWord() + " Factor: " + Double.toString(cT.getFactor()) +
                                " Time: " + Long.toString(cT.getTime()) + " Days: " + cT.getDays() + " Streak: " + cT.getStreak());
                    }
                    //todo change canStudy to something useful later, for now keep as true
                    if (newCards.size() > 0 || reviewCardList.size() > 0) {
                        Log.d(DEBUG_TAG, "ReviewList size: " + Integer.toString(reviewCardList.size()));
//                    Log.d(DEBUG_TAG, "BundleList size: " + bundleList.size());
                        updatedChangeLayout(1);
                        //todo check if anything changes between getting cards from database and here
                        for (CardTable cT : newCards) {
                            initialNewCardList.add(cT.createCopy());
                        }
                        for (CardTable cT : reviewCardList) {
                            initialReviewList.add(cT.createCopy());
                        }
                        if (reviewCardList.size() > 0) {
                            tempCard = reviewCardList.get(0);
                        } else {
                            tempCard = newCards.get(0);
                        }
                        nextRound();
//                        koreanWordButton.setText(tempCard.getForeignWord());
//                        //todo why check?
//                        checkDatabase();
//                        playSound("a" + (tempCard.getIndexKey()) + ".mp3");
                    }
                }
            });
            dictionaryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updatedChangeLayout(2);
                }
            });
            settingsButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updatedChangeLayout(3);
                }
            });
            wordsLearnedText.setText("Words Learned: " + Integer.toString(MainActivity.cardDatabase.cardDao().getLearnedWords().size()));
        } else if(session==1) {
            //study screen
            setContentView(R.layout.study_layout);
            waitingText=findViewById(R.id.waitingText);
            studyModeText=findViewById(R.id.ModeTextView);
            koreanWordButton = findViewById(R.id.KoreanWordText);
            showAnswerButton = findViewById(R.id.ShowAnswerButton);
            wrongAnswerButton = findViewById(R.id.WrongAnswerButton);
            correctAnswerButton = findViewById(R.id.CorrectAnswerButton);
            correctAnswerButton.setTextColor(this.getResources().getColor(R.color.Green));
            undoButton = findViewById(R.id.UndoButton);
            timerText = findViewById(R.id.TimerText);
            koreanWordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(DEBUG_TAG, tempCard.getIndexKey() + "_" + tempCard.getForeignWord());
                    if(tempCard.getStreak() < 5) {
                        playSound("a" + Integer.toString(tempCard.getIndexKey()) + ".mp3");
                    }

                }
            });
            showAnswerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (tempCard.getStreak() <= 3) {
                        showAnswerButton.setText(tempCard.getNativeWord());
                    } else if (tempCard.getStreak() == 4 || studyReviewMode == true) {
                        showAnswerButton.setText(tempCard.getNativeWord() + " - " + tempCard.getForeignWord());
                    } else {
                        showAnswerButton.setText(tempCard.getForeignWord());
                        playSound("a" + Integer.toString(tempCard.getIndexKey()) + ".mp3");
                    }
                    correctAnswerButton.setClickable(true);
                    if(tempCard.getStreak() > 0) {
                        wrongAnswerButton.setClickable(true);
                    }
                }
            });
            wrongAnswerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updatedCardAnswer(false);
                    for(CardTable cT : newCards) {
                        if(cT.getIndexKey() == tempCard.getIndexKey()) {
                            cT.setDays(tempCard.getDays());
                            cT.setStreak(tempCard.getStreak());
                        }
                    }
                    for(CardTable cT : reviewCardList) {
                        if (cT.getIndexKey() == tempCard.getIndexKey()) {
                            cT.setDays(tempCard.getDays());
                            cT.setStreak(tempCard.getStreak());
                        }
                    }
                    Log.d(DEBUG_TAG, "Tempcard after: " + tempCard.getForeignWord() + ", days-" + tempCard.getDays() + ", streak-" + tempCard.getStreak() + "\tTime: " + tempCard.getTime());
                    checkDeckStatus();
                    int i = 0;
                    for (CardTable cT : newCards) {
                        Log.d(DEBUG_TAG, i + " - " + cT.getForeignWord() + "\tDays: " + cT.getDays() + "\tStreak: " + cT.getStreak() + "\tTime: " + cT.getTime());
                        i += 1;
                    }
                    int j = 0;
                    for (CardTable cT : reviewCardList) {
                        Log.d(DEBUG_TAG, j + " - " + cT.getForeignWord() + "\tDays: " + cT.getDays() + "\tStreak: " + cT.getStreak() + "\tTime: " + cT.getTime());
                        j += 1;
                    }
                    Log.d(DEBUG_TAG, "AnswerHistory Size: " + Integer.toString(tempCardHistory.size()));
                    Log.d(DEBUG_TAG, "NewCardList size: " + Integer.toString(newCards.size()));
                    Log.d(DEBUG_TAG, "ReviewList Size: " + Integer.toString(reviewCardList.size()));
                    Log.d(DEBUG_TAG, "Boolean status: " + Boolean.toString(studyReviewMode));
                    if (newCards.size() > 0 || reviewCardList.size() > 0) { //todo this will change, most likely reviewcardList and boolean check
                        tempCard = updatedNextCard();
                        nextRound();
                        showAnswerButton.setText("?");
                    }


                }
            });
            undoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int i = 0;
                    undoButtonPressed();
                    for (CardTable cT : newCards) {
                        Log.d(DEBUG_TAG, i + " - " + cT.getForeignWord() + "\tDays: " + cT.getDays() + "\tStreak: " + cT.getStreak() + "\tTime: " + cT.getTime());
                        i += 1;
                    }
                    int j = 0;
                    for (CardTable cT : reviewCardList) {
                        Log.d(DEBUG_TAG, j + " - " + cT.getForeignWord() + "\tDays: " + cT.getDays() + "\tStreak: " + cT.getStreak() + "\tTime: " + cT.getTime());
                        j += 1;
                    }
                    Log.d(DEBUG_TAG, "AnswerHistory Size: " + Integer.toString(tempCardHistory.size()));
                    Log.d(DEBUG_TAG, "NewCardList size: " + Integer.toString(newCardHistory.size()));
                    Log.d(DEBUG_TAG, "ReviewList Size: " + Integer.toString(reviewCardHistory.size()));
                    Log.d(DEBUG_TAG, "Boolean status: " + Boolean.toString(studyReviewMode));
                    nextRound();
                    showAnswerButton.setText("?");
                    //check status of deck?
                }
            });
            correctAnswerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int i = 0;
                    for (CardTable cT : newCards) {
                        Log.d(DEBUG_TAG, i + " - " + cT.getForeignWord() + "\tDays: " + cT.getDays() + "\tStreak: " + cT.getStreak());
                        i += 1;
                    }
                    int j = 0;
                    for (CardTable cT : reviewCardList) {
                        Log.d(DEBUG_TAG, j + " - " + cT.getForeignWord() + "\tDays: " + cT.getDays() + "\tStreak: " + cT.getStreak());
                        j += 1;
                    }
                    Log.d(DEBUG_TAG, "AnswerHistory Size: " + Integer.toString(tempCardHistory.size()));
                    Log.d(DEBUG_TAG, "NewCardList size: " + Integer.toString(newCards.size()));
                    Log.d(DEBUG_TAG, "ReviewList Size: " + Integer.toString(reviewCardList.size()));
                    Log.d(DEBUG_TAG, "Boolean status: " + Boolean.toString(studyReviewMode));
                    updatedCardAnswer(true);//todo BROKE
                    for(CardTable cT : newCards) {
                        if(cT.getIndexKey() == tempCard.getIndexKey()) {
                            cT.setDays(tempCard.getDays());
                            cT.setStreak(tempCard.getStreak());
                        }
                    }
                    for(CardTable cT : reviewCardList) {
                        if (cT.getIndexKey() == tempCard.getIndexKey()) {
                            cT.setDays(tempCard.getDays());
                            cT.setStreak(tempCard.getStreak());
                        }
                    }
                    Log.d(DEBUG_TAG, "Tempcard after: " + tempCard.getForeignWord() + ", days-" + tempCard.getDays() + ", streak-" + tempCard.getStreak());
                    checkDeckStatus();
                    if (newCards.size() > 0 || reviewCardList.size() > 0) {
                        tempCard = updatedNextCard();
                        nextRound();
                        showAnswerButton.setText("?");
                    }
                }
            });
            if(studyReviewMode) {
                studyModeText.setText("Review Mode");
            } else {
                studyModeText.setText("Study Mode");
            }
            correctAnswerButton.setClickable(false);
            wrongAnswerButton.setClickable(false);
        } else if(session==2) {
            //dictionary screen
            setContentView(R.layout.dictionary_layout);
            dictionaryListView = (ListView)findViewById(R.id.dictionaryListView);
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
        } else if(session==3) {
            //setting screen
            setContentView(R.layout.settings_layout);
            pickTime = (Button)findViewById(R.id.pick_time);
            settingText = (TextView)findViewById(R.id.settingText);
            cardsPerDayText = (EditText)findViewById(R.id.cardsPerDay);
            cardsPerDayText.setText(Integer.toString(cardsPerDay));
            loadButton = findViewById(R.id.LoadButton);
            saveButton = findViewById(R.id.SaveButton);

            pickTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragment timePicker = new TimePickerFragment();
                    timePicker.show(getFragmentManager(), "time picker");
                    //todo import vs v4
                }
            });
            cardsPerDayText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(actionId==EditorInfo.IME_ACTION_DONE) {

//                    Log.d(DEBUG_TAG, "New cardsPerDay: " + cardsPerDayText.toString().trim());
                        cardsPerDay = Integer.parseInt(cardsPerDayText.getText().toString().trim());
                        if(cardsPerDay <= 0) {
                            cardsPerDay=1;
                        } else if(cardsPerDay > allCardList.size()) {
                            cardsPerDay = allCardList.size();
                        }
                        Log.d(DEBUG_TAG, "New cardsPerDay: " + Integer.toString(cardsPerDay));
                        editor.putInt(CARDS_PER_DAY_KEY, cardsPerDay);
                        editor.apply();
                        loadNewCards();
                        return true;
                    }
                    return false;
                }
            });
            loadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    readDatabaseSave();
                }
            });
            saveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    writeDatabaseSave();
                }
            });
        } else if(session==4) {
            //study timer started
            studyModeText.setVisibility(View.GONE);
            undoButton.setVisibility(View.GONE);
            koreanWordButton.setVisibility(View.GONE);
            showAnswerButton.setVisibility(View.GONE);
            wrongAnswerButton.setVisibility(View.GONE);
            correctAnswerButton.setVisibility(View.GONE);
            waitingText.setVisibility(View.VISIBLE);
            timerText.setVisibility(View.VISIBLE);
        } else if(session==5) {
            //study timer ended
            studyModeText.setVisibility(View.VISIBLE);
            undoButton.setVisibility(View.VISIBLE);
            koreanWordButton.setVisibility(View.VISIBLE);
            showAnswerButton.setVisibility(View.VISIBLE);
            wrongAnswerButton.setVisibility(View.VISIBLE);
            correctAnswerButton.setVisibility(View.VISIBLE);
            waitingText.setVisibility(View.GONE);
            timerText.setVisibility(View.GONE);
        } else if(session==6) {
            loadFileListView = findViewById(R.id.loadFileListView);
            loadFileListView.setVisibility(View.VISIBLE);
            pickTime.setVisibility(View.GONE);
            settingText.setVisibility(View.GONE);
            cardsPerDayText.setVisibility(View.GONE);
            loadButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
        } else if(session==7) {
            loadFileListView.setVisibility(View.GONE);
            pickTime.setVisibility(View.VISIBLE);
            settingText.setVisibility(View.VISIBLE);
            cardsPerDayText.setVisibility(View.VISIBLE);
            loadButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
        }
    }
    public void setupDatabase() {
        BufferedReader bufferedReader = null;
        try {
//            bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("WordPairs.txt"), "UTF-8"));
            bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("Anki_List.txt"), "UTF-8"));

            String tempLine;
            String[] splitTempLine;
            int loopIndex = 0;
            while((tempLine = bufferedReader.readLine()) != null) {
                splitTempLine = tempLine.split(",", 2);
//                Log.d(DEBUG_TAG, tempLine + "\tsplitTempLine Length: " + Integer.toString(splitTempLine.length));
                MainActivity.cardDatabase.cardDao().addCard(new CardTable(loopIndex, splitTempLine[1].trim(), splitTempLine[0].trim(), 2.5, Long.MAX_VALUE, 0, 0));
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
    public void initialTasks() {
        //index, cardsperday, time, timezone?, actual, time?
        Log.d(DEBUG_TAG, "InitialTasks called");
        loadSharedPreferences();
        loadDeck();
        loadStartedCards();
        checkDueTime(); //todo check started cards beforehand and only pull enough new cards to equal the new per day
        checkDatabase();
    }
    public void loadStartedCards() {
        //this shouldn't load more than a day's worth
        newCards.addAll(MainActivity.cardDatabase.cardDao().getReviewCards());
    }
    public void loadSharedPreferences() {
        if(!sharedPrefs.contains(INDEX_KEY)) {
            timeStudied = 0;
            index = 0;
            cardsPerDay = 20;
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
            editor.putInt(TIME_STUDIED, timeStudied);
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
//            newDayMilli = (System.currentTimeMillis() + 360000L); //ten minutes
            newDayMilli = (System.currentTimeMillis() - (System.currentTimeMillis() % 86400000L) + 10800000L);
//            newDayMilli = newDayMilli + 86400000L; //todo why the above and not this
            editor.putLong(NEW_DAY_MILLI_KEY, newDayMilli);
            editor.apply();
            loadNewCards();
        }
    }
    public void loadNewCards() {

        hideKeyboard(this);

        int newCardListSize = newCards.size();
        if(newCardListSize < cardsPerDay) {
            for(int i = 0; i < (cardsPerDay - newCardListSize); i++) { //this will not run if we have a days worth due, or will only fill what is needed if a partial study session was performed
                newCards.add(MainActivity.cardDatabase.cardDao().getCard(index));
                newCards.get(newCards.size() - 1).setTime(0);
                MainActivity.cardDatabase.cardDao().updateCard(newCards.get(newCards.size() - 1)); //cards now marked as study cards
                index += 1;
            }
        } else if(newCardListSize > cardsPerDay) {

            for(int i = 0; i < newCardListSize - cardsPerDay; i++) {
                CardTable tempCard = newCards.remove(newCards.size() - 1);
                tempCard.setFactor(2.5);
                tempCard.setTime(Long.MAX_VALUE);
                tempCard.setDays(0);
                tempCard.setTime(0);
                MainActivity.cardDatabase.cardDao().updateCard(tempCard);
                index--;
            }
        }
//        index += cardsPerDay;
        editor.putInt(INDEX_KEY, index);
        editor.apply();


    }
    public void undoButtonPressed() {
        //todo remove
        if(!tempCardHistory.empty()) {
            tempCard = tempCardHistory.pop();
            Log.d(DEBUG_TAG, "Tempcard days: " + tempCard.getDays() + ", streak: " + tempCard.getStreak());
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
            for(CardTable cT : newCards) {
                if(cT.getIndexKey() == tempCard.getIndexKey()) {
                    cT.setDays(tempCard.getDays());
                    cT.setStreak(tempCard.getStreak());
                }
            }
            for(CardTable cT : reviewCardList) {
                if (cT.getIndexKey() == tempCard.getIndexKey()) {
                    cT.setDays(tempCard.getDays());
                    cT.setStreak(tempCard.getStreak());
                }
            }
            //if in study mode check put back in newcards
            //check to see if anything changes at the time of assignment or if tempcard can just change
        }

    }
    public void updatedCardAnswer(boolean result) {
        //only method to change the card data
        Log.d(DEBUG_TAG, "Tempcard before: " + tempCard.getForeignWord() + ", days-" + tempCard.getDays() + ", streak-" + tempCard.getStreak());
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
                if(reviewCardList.size()>0) {
                    reviewCardList.remove(0); //TODO BROKE HERE
                }
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
//                short tempShort = tempCard.getStreak();
//                if(tempShort < 6) {
//                    tempShort += ((short) 1);
//                }
//                tempCard.setStreak(tempShort);
                Log.d(DEBUG_TAG, "Changeing tempcard before: " + tempCard.getForeignWord() + ", days-" + tempCard.getDays() + ", streak-" + tempCard.getStreak());
                if(tempCard.getStreak() < 6) {
                    tempCard.setStreak(tempCard.getStreak() + 1);
                }
                tempCard.setDays(gapMap[tempCard.getStreak()-1]);
                Log.d(DEBUG_TAG, "Changeing tempcard after: " + tempCard.getForeignWord() + ", days-" + tempCard.getDays() + ", streak-" + tempCard.getStreak());
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
//                    short tempShort = tempCard.getStreak();
//                    tempShort-=1;
//                    tempCard.setStreak(tempShort);
                    tempCard.setStreak(tempCard.getStreak() - 1);
                    tempCard.setDays(gapMap[tempCard.getStreak()]);
                } else {
                    tempCard.setDays(0);
                }
            }
        }
    }
    public CardTable updatedNextCard() {
        //fixing

        //fixing
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
                //get the card most due, based on the lowest days value and for equal values choose the high streak`
                int index = -1;
                int tempStreak = -1; //this okay? todo
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
                    updatedChangeLayout(0);
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
                updatedChangeLayout(4);
                startStudyTimer(600000, 1000);
                checkDatabase();
            }
        }
    }
    public void cardDone(CardTable cardTable, double answerQuality) {
        //todo change layout
//        short tempShort = cardTable.getStreak();
        int tempStreak = 0;
        int tempDays = 0;
        for(CardTable cT : initialNewCardList) {
            if(cT.getIndexKey() == cardTable.getIndexKey()) {
                tempStreak = cT.getStreak();
                tempDays = cT.getDays();
                //todo same with streak?
            }
        }
        for(CardTable cT : initialReviewList) {
            if(cT.getIndexKey() == cardTable.getIndexKey()) {
                tempStreak = cT.getStreak();
                tempDays = cT.getDays();
            }
        }
//        tempStreak +=  1;
        cardTable.setStreak(tempStreak + 1);
        cardTable.setFactor(calEasinessFactor(cardTable.getFactor(), answerQuality)); //probably wrong
        cardTable.setDays(calRepetitionInterval(tempDays, cardTable.getFactor()));
        cardTable.setTime(newDayMilli+((86400000)*(cardTable.getDays()-1)));
        MainActivity.cardDatabase.cardDao().updateCard(cardTable);
        editor.putInt(TIME_STUDIED, timeInterval);
        timeInterval = 0;
        editor.apply();
    }
    public void startStudyTimer(int duration, int tick) {
        waitingTimer = true;
        new CountDownTimer(duration, tick) {

            public void onTick(long millisUntilFinished) {
//                timerText.setText(Long.toString(millisUntilFinished/1000));
                String tempMinutes = Long.toString(millisUntilFinished/1000/60);
                long tempSeconds = millisUntilFinished/1000%60;
                String tempSecondsText = Long.toString(tempSeconds);
                if(tempSeconds < 10) {
                    tempSecondsText = "0" + Long.toString(tempSeconds);
                }
                timerText.setText(tempMinutes + ":" + tempSecondsText);
//                timerText.setText(Long.toString(millisUntilFinished/1000/60) + ":" + millisUntilFinished/1000%60);
            }
            public void onFinish() {
                waitingTimer = false;
                sendOnChannel();
                updatedChangeLayout(5);
            }
        }.start();
    }

    public void checkDatabase() {
        //temporary method for debugging to see the values in the database
//        for(int i = 0; i<20; i++) {
////            CardTable tempCard = MainActivity.cardDatabase.cardDao().getCard(i);
//            Log.d(DEBUG_TAG, "CardData: "+ Integer.toString(tempCard.getIndexKey()) + "-" + tempCard.getForeignWord() + "-" + tempCard.getNativeWord() + " Factor: " + Double.toString(tempCard.getFactor()) +
//            " Time: " + Long.toString(tempCard.getTime()) + " Days: " + tempCard.getDays() + " Streak: " + tempCard.getStreak());
//        }
    }
    public void writeDatabaseSave() {
        getWritePermission();
       String state = Environment.getExternalStorageState();
       if(!Environment.MEDIA_MOUNTED.equals(state)) {
           //if not mounted we cannot write
           Log.d(DEBUG_TAG,"Write failed.");
           return;
       }
       Calendar tempCalender = Calendar.getInstance();
       File file = new File(Environment.getExternalStorageDirectory(), Integer.toString(index) + "Words_" + tempCalender.get(Calendar.MONTH) + "-" + tempCalender.get(Calendar.DAY_OF_MONTH) + "-" + tempCalender.get(Calendar.YEAR) + "_" + tempCalender.get(Calendar.HOUR) + "-" + tempCalender.get(Calendar.MINUTE) + "-" + tempCalender.get(Calendar.SECOND));

       FileOutputStream fos = null;
       try {
           file.createNewFile();
           fos = new FileOutputStream(file, true);
           ObjectOutputStream os = new ObjectOutputStream(fos);

           ArrayList<CardTable> cardsModified = new ArrayList<>(MainActivity.cardDatabase.cardDao().getModifiedCards(Long.MAX_VALUE));
           cardsModified.add(new CardTable(index * -1, null, null, 0.0, 0L, 0, 0));

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

        String state = Environment.getExternalStorageState();
        if(!Environment.MEDIA_MOUNTED.equals(state)) {
            //if not mounted we cannot write
            Log.d(DEBUG_TAG,"Write failed.");
            return;
        }
        String path = Environment.getExternalStorageDirectory().toString();
        Log.d(DEBUG_TAG, "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d(DEBUG_TAG, "Size: " + files.length);

        updatedChangeLayout(6);
        final ArrayList<String> loadFileChoices = new ArrayList<>();
        for(int i = 0; i < files.length; i++) {
            if(files[i].isFile()) {
                loadFileChoices.add(files[i].getName());
//                loadFileListView
//                Log.d(DEBUG_TAG, "Filename: " + files[i].getName());
            }
        }
//        setupDatabase();//reset it
//        MainActivity.cardDatabase.cardDao().addCard(new CardTable(loopIndex, splitTempLine[0].trim(), splitTempLine[1].trim(), 2.5, Long.MAX_VALUE, 0, (short) 0));

        ArrayAdapter loadAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, loadFileChoices);
        loadFileListView.setAdapter(loadAdapter);
        loadFileListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(DEBUG_TAG, loadFileChoices.get(i)); //todo some of these varaibles might need to become global
                try {
                    ArrayList<CardTable> allCards = new ArrayList<>(MainActivity.cardDatabase.cardDao().getCards());
                    for(CardTable cT : allCards) {
                        cT.setFactor(2.5);
                        cT.setTime(Long.MAX_VALUE);
                        cT.setDays(0);
                        cT.setTime(0);
                        MainActivity.cardDatabase.cardDao().updateCard(cT);
                    }

                    File file = new File(Environment.getExternalStorageDirectory(), loadFileChoices.get(i));
                    ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(file));
                    boolean readObjects= true;
                    while(readObjects) {
                        CardTable cardTable = (CardTable) objectInputStream.readObject();
                        if (cardTable.getIndexKey() > -1) { //todo negative 0??? load one that has just started?
                            Log.d(DEBUG_TAG, cardTable.getIndexKey() + "\t" + cardTable.getForeignWord() + "\t" + cardTable.getNativeWord() + "\t" + cardTable.getStreak());
                            MainActivity.cardDatabase.cardDao().updateCard(cardTable);
                        } else {
                            index = cardTable.getIndexKey() * -1;
                            Log.d(DEBUG_TAG, Integer.toString(index));
                            editor.putInt(INDEX_KEY, index);
                            editor.apply();
                            Log.d(DEBUG_TAG, Integer.toString(cardTable.getIndexKey()));
                            break;
                        }
                    }
                } catch(Exception e) {
                    Log.e(DEBUG_TAG, "Database load on click event failed.", e); //todo this good?
                    e.printStackTrace();
                }
            }
        });
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
    public void sendOnChannel() {
        Notification notification = new NotificationCompat.Builder(this, AppChannel.STUDY_WAIT_CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Trance Cards")
                .setContentText("Timer finished, time to review.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        notificationManagerCompat.notify(1, notification);
    }
    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
