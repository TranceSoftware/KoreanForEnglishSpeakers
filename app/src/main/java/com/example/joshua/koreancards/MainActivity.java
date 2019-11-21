package com.example.joshua.koreancards;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Notification;
import android.app.TimePickerDialog;
import android.arch.persistence.room.Room;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
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
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Stack;
import java.util.TimeZone;

public class MainActivity extends Activity implements TimePickerDialog.OnTimeSetListener {
    private final String DEBUG_TAG = "DEBUG_TAG";
    private Button studyButton, dictionaryButton, settingsButton, saveButton, loadButton, resetDatabaseButton;
    private Button redButton, orangeButton, yellowButton, greenButton, blueButton, purpleButton, pinkButton, greyButton;
    private final String CARDS_PER_DAY_KEY = "cards_per_day_key";
    private int cardsPerDay; //10 is default
    private final String REMINDER_MILLI_KEY = "reminder_milli_key";
    private final String INDEX_KEY = "index_key";
    private int index = 0;
    private final String NEW_DAY_MILLI_KEY = "new_day_miLli_key";
    private final String TIME_STUDIED = "time_studied";
    private long newDayMilli;
    private long reminderMilli; //default is 8pm
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
    private Button showAnswerButton, wrongAnswerButton, correctAnswerButton, undoButton, reviewButton, conjugationButton;
    private SoundPool soundPool;
    private Button pickTime;
    private TextView settingText, koreanWordButton, studyModeText, wordsLearnedText, infoText, waitingText, newDayText, wrongAnswerText;
    private EditText cardsPerDayText;

    private static CardDatabase cardDatabase;
    private int[] gapMap = new int[]{1,2,3,4,5};
    private boolean studyReviewMode = false;

    private TextView timerText;

    private Stack<Integer> studyHistory = new Stack<>();
    private Stack<Integer> reviewHistory = new Stack<>();
    private Stack<Integer> conjugationHistory;
    private Stack<Integer> conjAnswerHistory = new Stack<>();
    private Stack<ArrayList<CardTable>> newCardHistory = new Stack<>();
    private Stack<ArrayList<CardTable>> reviewCardHistory = new Stack<>();

    private ArrayList<CardTable> initialReviewList = new ArrayList<>();
    private ArrayList<CardTable> initialNewCardList = new ArrayList<>();

    private int viewID = -1;

    private NotificationManagerCompat notificationManagerCompat;

    private boolean waitingTimer = false;
    private int timeStudied = 0;
    private int timeInterval = 0;
    private int wordsFinishedToday = 0;
    private final String WORDS_FINISHED_TODAY = "words_finished_today";
    private int oldCardsPerDay = 0;
    private long newDayMilliExtra = 0L;
    private final String NEW_DAY_MILLI_EXTRA_KEY = "new_day_milli_extra_key";

    private CountDownTimer countDownTimer;

    private ConstraintLayout constraintLayout;
    private int backgroundID;
    private final String BACKGROUND_ID = "background_id_key";
    private String finishedWords;
    private final String FINISHED_WORDS_ID = "finished_words_id";
    private boolean dayDone;
    private String DAYDONE_ID = "daydone_id";
    private ArrayList<CardTable> finishedVerbs = new ArrayList<>();
    private int sdk = android.os.Build.VERSION.SDK_INT;
    private Button conjugationStartButton, studyPreviewStart;
    private ListView conjugationListView, studyPreviewListView;
    private int studyIndex, wrongStudyAnswers, studyPasses, reviewIndex;
    private int conjugationIndex, conjugationAnswers, conjugationPasses, conjugationWrongAnswers;
    private NeoStack neoStack;
    private Button conjugationCorrectAnswerButton, conjugationWrongAnswerButton, conjugationShowAnswerButton, conjugationUndoButton;
    private TextView conjugationKoreanWordText, conjugationModeTextView;
    private int tomorrowCardsPerDay;
    private final String TOMORROW_CARDS_PER_DAY_ID = "cards_per_day_id";

    private AlertDialog.Builder builder;
    private boolean studyStarted = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if(viewID == -1) {
            super.onCreate(savedInstanceState);

            notificationManagerCompat = NotificationManagerCompat.from(this);


            allCardList = new ArrayList<>();
            newCards = new ArrayList<>();
            reviewCardList = new ArrayList<>();

            cardDatabase = Room.databaseBuilder(getApplicationContext(), CardDatabase.class, "carddb").allowMainThreadQueries().build();
            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            editor = sharedPrefs.edit();

            SharedPreferences.OnSharedPreferenceChangeListener spChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    // your stuff here
                    Log.d(DEBUG_TAG, key + " - was changed ");
                }
            };
            updatedInitialTasks();
            setContentView(R.layout.test_layout);

            constraintLayout=(ConstraintLayout)findViewById(R.id.testLayout);
            chooseBackground(backgroundID);
            /*
             *
             * END OF ON CREATE FOR MAIN ACTIVITY
             *
             */
            calendar.setTimeInMillis(System.currentTimeMillis());

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
        if(viewID==6) {
            updatedChangeLayout(7);
        } else {
            updatedChangeLayout(0);
        }
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
        try {
            AssetFileDescriptor assetFileDescriptor = getAssets().openFd(rawIdentifier);
            soundPool.load(assetFileDescriptor, 1);
        } catch(Exception e) {
            Log.e(DEBUG_TAG, rawIdentifier);
            Log.e(DEBUG_TAG, "Could not get file descriptor of mp3.", e);
        }
        Log.d(DEBUG_TAG, rawIdentifier);
    }

    public double calEasinessFactor(double oldEasyFactor, double quality) { //quality is 0-5 originally
        double newFactor = oldEasyFactor - (0.8 + (0.28 * quality) - (0.02 * quality * quality));
        if(newFactor<1.3) {
            newFactor=1.3;
        }
        return newFactor;
    }

    public int calRepetitionInterval(int oldInterval, double easyFactor) {
        if(oldInterval==0) {
            return 1;
        } else if(oldInterval==1) {
            return 2;
        } else {
            return (int) Math.ceil((double) oldInterval * easyFactor);
        }
    }
    public void nextRound() {

    }
    public void updatedChangeLayout(int session) {
        viewID = session;
        Log.d(DEBUG_TAG, "initialNewCardList size: " + Integer.toString(initialNewCardList.size()));
        Log.d(DEBUG_TAG, "initialReviewList size: " + Integer.toString(initialReviewList.size()));

        if(session==0) {
            //main screen
            setContentView(R.layout.test_layout);
            constraintLayout=(ConstraintLayout)findViewById(R.id.testLayout);
            chooseBackground(backgroundID);
            wordsLearnedText=findViewById(R.id.wordsLearnedText);
            studyButton=findViewById(R.id.StudyButton);
            conjugationButton=findViewById(R.id.conjugationButton);
            dictionaryButton=findViewById(R.id.DictionaryButton);
            settingsButton=findViewById(R.id.SettingsButton);
            reviewButton=findViewById(R.id.reviewButton);
            infoText=findViewById(R.id.infoText);
            if(!dayDone) {
                infoText.setText(infoText.getText() + Integer.toString(reviewCardList.size()) + "\n" + "New Words: " + Integer.toString(newCards.size()));
            } else {
                infoText.setText(infoText.getText() +"0\n" + "New Words: 0");
            }
            studyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (waitingTimer) {
                        updatedChangeLayout(4);
                    } else if(!dayDone){
                        for (CardTable cT : newCards) {
                            Log.d(DEBUG_TAG, "CardData: " + Integer.toString(cT.getIndexKey()) + "-" + cT.getForeignWord() + "-" + cT.getNativeWord() + " Factor: " + Double.toString(cT.getFactor()) +
                                    " Time: " + Long.toString(cT.getTime()) + " Days: " + cT.getDays() + " Streak: " + cT.getStreak());
                        }
                        for (CardTable cT : reviewCardList) {
                            Log.d(DEBUG_TAG, "CardData: " + Integer.toString(cT.getIndexKey()) + "-" + cT.getForeignWord() + "-" + cT.getNativeWord() + " Factor: " + Double.toString(cT.getFactor()) +
                                    " Time: " + Long.toString(cT.getTime()) + " Days: " + cT.getDays() + " Streak: " + cT.getStreak());
                        }
                        if (newCards.size() > 0 || reviewCardList.size() > 0) {
                            Log.d(DEBUG_TAG, "ReviewList size: " + Integer.toString(reviewCardList.size()));
//                    Log.d(DEBUG_TAG, "BundleList size: " + bundleList.size());

                            if(!studyStarted) {
                                if (reviewCardList.isEmpty()) {
                                    neoStack = new NeoStack(newCards);
                                } else {
                                    reviewIndex = 0;
                                }
                                updatedChangeLayout(9);
                            } else {
                                updatedChangeLayout(1);
                            }
                        }
                    }
                }
            });
            conjugationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(DEBUG_TAG, "Day is done: " + dayDone);
                    if(dayDone) {
                        if(finishedVerbs.isEmpty()) {
                            //either starting from scratch or the app was closed
                            for(CardTable cT: initialNewCardList) {
                                Log.d(DEBUG_TAG, cT.getPos());
                                if(cT.getPos().equals("v")) {
                                    Log.d(DEBUG_TAG, "index - " + cT.getIndexKey() + ", native word - " + cT.getNativeWord() + ", foreign word - " + cT.getForeignWord() + ", pos - " + cT.getPos() + ", factor - " + cT.getFactor() + ", streak - " + cT.getStreak());
                                    finishedVerbs.add(cT);
                                }
                            }
                            //preview conjugation
                            conjugationIndex = 0;
                            conjugationAnswers = 0;
                            conjugationPasses = 0;
                            conjugationWrongAnswers = 0;
                            conjugationHistory = new Stack<>();
                            updatedChangeLayout(8);
                        } else {
                            //just resume
                            updatedChangeLayout(10);
                        }
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
            reviewButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(dayDone) {
                        for(CardTable cT : initialNewCardList) {
                            cT.setTime(0L);
                            cT.setStreak(0);
                            cT.setDays(0);
                            newCards.add(cT);
                            //todo set defaults
                        }
                        neoStack = new NeoStack(newCards);
                        studyPasses=1;
                        updatedChangeLayout(1);
                    }
                }
            });
        } else if(session==1) {
            //study screen
            studyStarted = true;
            setContentView(R.layout.study_layout);
            constraintLayout=(ConstraintLayout)findViewById(R.id.studyLayout);
            chooseBackground(backgroundID);
            waitingText=findViewById(R.id.waitingText);
            studyModeText=findViewById(R.id.ModeTextView);
            koreanWordButton = findViewById(R.id.KoreanWordText);
            showAnswerButton = findViewById(R.id.ShowAnswerButton);
            wrongAnswerButton = findViewById(R.id.WrongAnswerButton);
            correctAnswerButton = findViewById(R.id.CorrectAnswerButton);
            correctAnswerButton.setTextColor(this.getResources().getColor(R.color.Green));
            undoButton = findViewById(R.id.UndoButton);
            timerText = findViewById(R.id.TimerText);
            if(reviewIndex < reviewCardList.size()) {
                koreanWordButton.setText("♪♫♪");
            } else {
                if (studyPasses < 2) {
                    koreanWordButton.setText(newCards.get(studyIndex).getForeignWord());//todo change this for other two versions
                } else if (studyPasses < 4) {
                    koreanWordButton.setText(newCards.get(studyIndex).getNativeWord());
                } else {
                    koreanWordButton.setText("♪♫♪");
                }
            }
            koreanWordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    playSound("en" + Integer.toString(tempCard.getIndexKey()) + "_" + tempCard.getForeignWord() + ".mp3");
                }
            });
            if(reviewIndex < reviewCardList.size()) {
                studyModeText.setText("Reviewing: " + (reviewCardList.size() - reviewIndex) + " cards left.");
            } else if(studyPasses < 1){
                studyModeText.setText("Learning Phase, " + (newCards.size() - studyIndex) + " cards left.\nIncorrect Answers: " + wrongAnswerText);
            } else if(studyPasses < 3) {
                studyModeText.setText("Reading Phase, " + (newCards.size() - studyIndex) + " cards left.\nIncorrect Answers: " + wrongAnswerText);
            } else if(studyPasses < 5) {
                studyModeText.setText("Memory Phase, " + (newCards.size() - studyIndex) + " cards left.\nIncorrect Answers: " + wrongAnswerText);
            } else {
                studyModeText.setText("Hearing Phase, " + (newCards.size() - studyIndex) + " cards left.\nIncorrect Answers: " + wrongAnswerText);
            }
            showAnswerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(reviewIndex < reviewCardList.size()) {
                        showAnswerButton.setText(newCards.get(studyIndex).getForeignWord());
                    } else {
                        if(studyPasses < 1) {
                            //todo learning phase
                            showAnswerButton.setText(neoStack.peak().getNativeWord());
                        }
                        if (studyPasses < 3) {
                            showAnswerButton.setText(newCards.get(studyIndex).getNativeWord()); //todo change this based on studypasses
                        } else if (studyPasses < 5) {
                            showAnswerButton.setText(newCards.get(studyIndex).getForeignWord());
                        } else {
                            //                        showAnswerButton.setText("♪♫♪");
                            showAnswerButton.setText(newCards.get(studyIndex).getForeignWord() + " - " + newCards.get(studyIndex).getNativeWord());
                        }
                        //                    playSound("en" + Integer.toString(tempCard.getIndexKey()) + "_" + tempCard.getForeignWord() + ".mp3");
                    }
                    wrongAnswerButton.setClickable(true);
                    correctAnswerButton.setClickable(true);
                }
            });
            wrongAnswerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(reviewIndex < reviewCardList.size()) {
                        reviewHistory.push(0);
                        //modifycard so that days and streak become 0, calculate new e factor
                        //todo
                        //add card to newlist
                        //todo modify card here in case app restarts?
                        newCards.add(reviewCardList.get(reviewIndex));
                        newCards.get(newCards.size() - 1).setFactor(calEasinessFactor(reviewCardList.get(reviewIndex).getFactor(), 0.0));
                        newCards.get(newCards.size() - 1).setDays(0);
                        newCards.get(newCards.size() - 1).setStreak(0);

                        MainActivity.cardDatabase.cardDao().updateCard(newCards.get(newCards.size() - 1));

                        initialNewCardList.add(newCards.get(newCards.size() - 1).createCopy());
                        //increment
                        reviewIndex += 1;
                        if(reviewIndex >= reviewCardList.size()) {
                            //review done
                            neoStack = new NeoStack(newCards); //todo handle back button
                            if (studyPasses < 3) {
                                koreanWordButton.setText(newCards.get(studyIndex).getForeignWord());//todo change this for other two versions
                            } else if (studyPasses < 5) {
                                koreanWordButton.setText(newCards.get(studyIndex).getNativeWord());
                            } else {
                                koreanWordButton.setText("♪♫♪");
                            }
                        }
                    } else {
                        if(studyPasses == 0) {
                            //todo study logic
                            studyHistory.push(neoStack.peak().getIndexKey());
                            studyHistory.push(1); //1 for correct and 0 for incorrect, index followed by this value in pairs
                            if(neoStack.peak().getStreak() >= 1) {
                                neoStack.peak().setStreak(neoStack.peak().getStreak() - 1);
                            }
                            neoStack.push(gapMap[neoStack.peak().getStreak()], neoStack.pop()); //todo what if there are less than five cards?
                            if(neoStack.checkStatus()) {
                                //done learning
                                studyPasses += 1;
                            }
                        } else {
                            studyHistory.push(0);
                            studyIndex += 1;
                            wrongStudyAnswers += 1;
                            if (studyIndex >= newCards.size()) {
                                if (wrongStudyAnswers == 0) {
                                    studyPasses += 1;
                                    //todo check for if it's done
                                }
                                Collections.shuffle(newCards);
                                studyIndex = 0;
                                wrongStudyAnswers = 0;
                                //done with a pass
                                //scramble
                            }
                        }
                    }
                    if (studyPasses < 3) {
                        koreanWordButton.setText(newCards.get(studyIndex).getForeignWord());//todo change this for other two versions
                    } else if (studyPasses < 5) {
                        koreanWordButton.setText(newCards.get(studyIndex).getNativeWord());
                    } else {
                        koreanWordButton.setText("♪♫♪");
                    }
                    if (reviewIndex < reviewCardList.size()) {
                        studyModeText.setText("Reviewing: " + (reviewCardList.size() - reviewIndex) + " cards left.");
                    } else if (studyPasses < 1) {
                        studyModeText.setText("Learning Phase, " + (newCards.size() - studyIndex) + " cards left.\nIncorrect Answers: " + wrongAnswerText);
                    } else if (studyPasses < 3) {
                        studyModeText.setText("Reading Phase, " + (newCards.size() - studyIndex) + " cards left.\nIncorrect Answers: " + wrongAnswerText);
                    } else if (studyPasses < 5) {
                        studyModeText.setText("Memory Phase, " + (newCards.size() - studyIndex) + " cards left.\nIncorrect Answers: " + wrongAnswerText);
                    } else {
                        studyModeText.setText("Hearing Phase, " + (newCards.size() - studyIndex) + " cards left.\nIncorrect Answers: " + wrongAnswerText);
                    }
                    showAnswerButton.setText("?");
                    correctAnswerButton.setClickable(false);
                    wrongAnswerButton.setClickable(false);
                }
            });
            undoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int tempAnswer;
                    if(reviewIndex <= reviewCardList.size() && reviewIndex != 0 && studyIndex == 0 && studyPasses == 0) {
                        //review process, regardless of edge case (just began, in the middle, at the end) the process is the same
                        tempAnswer = reviewHistory.pop();
                        if (tempAnswer == 1) {
                            //card was answered correctly
                            //undo the write to database
                            undoCardWrite(reviewCardList.get(reviewIndex));
                        } else {
                            //remove from new list it wsa added to
                            for (int i = 0; i < newCards.size(); i++) {
                                if (newCards.get(i).getIndexKey() == reviewCardList.get(reviewIndex).getIndexKey()) {
                                    newCards.remove(i);
                                    break;
                                }
                            }
                        }
                        koreanWordButton.setText("♪♫♪"); //this is irrelevant unless study mode i.e. we are undoing the last line
                        //better than doing a check
                        reviewIndex -= 1;
                    } else if(studyPasses == 0 && !studyHistory.isEmpty()) {
                        neoStack.studyUndo(studyHistory.pop(), studyHistory.pop());
                        koreanWordButton.setText(neoStack.peak().getForeignWord());
                    } else if(studyIndex < newCards.size() && studyIndex != 0) {
                        //can't undo to a previous iteration so no edge case scenario
                        studyIndex -= 1;
                        tempAnswer = studyHistory.pop();
                        if (tempAnswer == 1) {
                            wrongStudyAnswers -= 1;
                        } else {
                            //nothing
                        }
                    }
                    //todo do we need a final else?
                    if (studyPasses < 3) {
                        koreanWordButton.setText(newCards.get(studyIndex).getForeignWord());//todo change this for other two versions
                    } else if (studyPasses < 5) {
                        koreanWordButton.setText(newCards.get(studyIndex).getNativeWord());
                    } else {
                        koreanWordButton.setText("♪♫♪");
                    }
                    if (reviewIndex < reviewCardList.size()) {
                        studyModeText.setText("Reviewing: " + (reviewCardList.size() - reviewIndex) + " cards left.");
                    } else if (studyPasses < 1) {
                        studyModeText.setText("Learning Phase, " + (newCards.size() - studyIndex) + " cards left.\nIncorrect Answers: " + wrongAnswerText);
                    } else if (studyPasses < 3) {
                        studyModeText.setText("Reading Phase, " + (newCards.size() - studyIndex) + " cards left.\nIncorrect Answers: " + wrongAnswerText);
                    } else if (studyPasses < 5) {
                        studyModeText.setText("Memory Phase, " + (newCards.size() - studyIndex) + " cards left.\nIncorrect Answers: " + wrongAnswerText);
                    } else {
                        studyModeText.setText("Hearing Phase, " + (newCards.size() - studyIndex) + " cards left.\nIncorrect Answers: " + wrongAnswerText);
                    }
                }
            });
            correctAnswerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(reviewIndex < reviewCardList.size()) {
                        reviewHistory.push(1);
                        //todo
                        //card done, no need to do card modify if correct done on its own
                        cardDone(reviewCardList.get(reviewIndex), 5);
                        //increment
                        reviewIndex += 1;
                        if(reviewIndex >= reviewCardList.size()) {
                            neoStack = new NeoStack(newCards); //todo handle back button
                            if (studyPasses < 3) {
                                koreanWordButton.setText(newCards.get(studyIndex).getForeignWord());//todo change this for other two versions
                            } else if (studyPasses < 5) {
                                koreanWordButton.setText(newCards.get(studyIndex).getNativeWord());
                            } else {
                                koreanWordButton.setText("♪♫♪");
                            }
                        }
                    } else {
                        if(studyPasses==0) {
                            //todo study logic
                            studyHistory.push(neoStack.peak().getIndexKey());
                            studyHistory.push(-1); //-1 for correct and 1 for incorrect, index followed by this value in pairs
                            if(neoStack.peak().getStreak() < 4) {
                                neoStack.peak().setStreak(neoStack.peak().getStreak() + 1);
                            }
                            neoStack.push(gapMap[neoStack.peak().getStreak()], neoStack.pop()); //todo what if there are less than five cards?
                            if(neoStack.checkStatus()) {
                                //done learning
                                studyPasses += 1;
                            }
                        } else {
                            studyHistory.push(1);
                            studyIndex += 1;
                            if (studyIndex >= newCards.size()) {
                                if (wrongStudyAnswers == 0) {
                                    studyPasses += 1;
                                    if (studyPasses >= 7) {
                                        for (CardTable cT : newCards) {
                                            Log.d(DEBUG_TAG, "Card done: " + cT.getIndexKey() + "-" + cT.getNativeWord());
                                            cardDone(cT, 5);
                                        }
                                        Log.d(DEBUG_TAG, "Day is done.");
                                        dayDone = true;
                                        editor.putBoolean(DAYDONE_ID, dayDone);
                                        editor.apply();

                                        updatedChangeLayout(0);
                                    }
                                }
                                Collections.shuffle(newCards);
                                studyIndex = 0;
                                wrongStudyAnswers = 0;
                                //done with a pass
                                //scramble
                            }
                        }
                    }
                    if (studyPasses < 2) {
                        koreanWordButton.setText(newCards.get(studyIndex).getForeignWord());//todo change this for other two versions
                    } else if (studyPasses < 4) {
                        koreanWordButton.setText(newCards.get(studyIndex).getNativeWord());
                    } else {
                        koreanWordButton.setText("♪♫♪");
                    }
                    if (reviewIndex < reviewCardList.size()) {
                        studyModeText.setText("Reviewing: " + (reviewCardList.size() - reviewIndex) + " cards left.");
                    } else if (studyPasses < 1) {
                        studyModeText.setText("Learning Phase, " + (newCards.size() - studyIndex) + " cards left.\nIncorrect Answers: " + wrongAnswerText);
                    } else if (studyPasses < 3) {
                        studyModeText.setText("Reading Phase, " + (newCards.size() - studyIndex) + " cards left.\nIncorrect Answers: " + wrongAnswerText);
                    } else if (studyPasses < 5) {
                        studyModeText.setText("Memory Phase, " + (newCards.size() - studyIndex) + " cards left.\nIncorrect Answers: " + wrongAnswerText);
                    } else {
                        studyModeText.setText("Hearing Phase, " + (newCards.size() - studyIndex) + " cards left.\nIncorrect Answers: " + wrongAnswerText);
                    }
                    showAnswerButton.setText("?");
                    correctAnswerButton.setClickable(false);
                    wrongAnswerButton.setClickable(false);
                }
            });
            correctAnswerButton.setClickable(false);
            wrongAnswerButton.setClickable(false);
        } else if(session==2) {
            //dictionary screen
            setContentView(R.layout.dictionary_layout);
            constraintLayout=(ConstraintLayout)findViewById(R.id.dictionaryLayout);
            chooseBackground(backgroundID);

            dictionaryListView = (ListView)findViewById(R.id.dictionaryListView);
            searchText = (EditText)findViewById(R.id.searchButton);
            dictionaryList = new ArrayList<>();
            Log.d(DEBUG_TAG, "DictionaryList: " + Integer.toString(dictionaryList.size()));
            customAdapter = new CustomAdapter(this, R.layout.customlayout, allCardList, sdk);
            dictionaryListView.setAdapter(customAdapter);
            dictionaryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(!waitingTimer) {
                        playSound("en" + Integer.toString(tempCard.getIndexKey()) + "_" + tempCard.getForeignWord() + ".mp3");
                    }
                }
            });
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
            constraintLayout=(ConstraintLayout)findViewById(R.id.settingLayout);
            chooseBackground(backgroundID);
            builder = new AlertDialog.Builder(this, R.style.MyTimePickerDialogTheme);
            builder.setTitle("Words Per Day");
            builder.setPositiveButton("Tomorrow", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //change the new variable tomorrowCardsPerDay
                    tomorrowCardsPerDay = cardsPerDay;
                    editor.putInt(TOMORROW_CARDS_PER_DAY_ID, tomorrowCardsPerDay);
                    cardsPerDay = oldCardsPerDay;
                }
            });
            builder.setNeutralButton("Back", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //return
                }
            });
            builder.setNegativeButton("Today", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //
//                    newCards.clear();
                    updateModifyDeck();
                }
            });
            pickTime = (Button)findViewById(R.id.pick_time);
            settingText = (TextView)findViewById(R.id.settingText);
            cardsPerDayText = (EditText)findViewById(R.id.cardsPerDay);
            cardsPerDayText.setText(Integer.toString(cardsPerDay));
            loadButton = findViewById(R.id.LoadButton);
            saveButton = findViewById(R.id.SaveButton);
            resetDatabaseButton = findViewById(R.id.resetDatabaseButton);

            newDayText = (TextView) findViewById(R.id.newDayText);

            Calendar tempCalendar = Calendar.getInstance();
            tempCalendar.setTimeInMillis(newDayMilli);

            int hourOfDay = tempCalendar.get(Calendar.HOUR_OF_DAY);
            int minute = tempCalendar.get(Calendar.MINUTE);
            String am_or_pm = "am";
            if(hourOfDay > 13) {
                hourOfDay = hourOfDay - 12;
                am_or_pm = "pm";
            }
            if(hourOfDay==0) {
                hourOfDay = 12;
            }
            String tempMinuteText = Integer.toString(minute);
            if(minute < 10) {
                tempMinuteText = "0" + tempMinuteText;
            }
            pickTime.setText(hourOfDay + ":" + tempMinuteText + " " + am_or_pm);


            pickTime.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragment timePicker = new TimePickerFragment();
                    timePicker.show(getFragmentManager(), "time picker");
                }
            });
            cardsPerDayText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if(actionId==EditorInfo.IME_ACTION_DONE) {

//                    Log.d(DEBUG_TAG, "New cardsPerDay: " + cardsPerDayText.toString().trim());
                        String tempString = cardsPerDayText.getText().toString().trim();
                        if(tempString.length() == 0) {
                            tempString = "1";
                        }
                        oldCardsPerDay = cardsPerDay;
                        cardsPerDay = Integer.parseInt(tempString);
                        if(cardsPerDay <= 0) {
                            cardsPerDay=1;
                        } else if(cardsPerDay > allCardList.size()) {
                            cardsPerDay = allCardList.size();
                        }
                        cardsPerDayText.clearFocus();
                        if(!dayDone && !studyStarted) {
                            //new day hasn't started yet
                            updateModifyDeck();
                        } else if(!dayDone) {
                            //mid studying, ask if they want to restart or apply for tomorrow
                            builder.setMessage("Would you like to restart today or apply this tomorrow?");
                            final AlertDialog dialog = builder.create();
                            dialog.show();
                        } else if(dayDone) {
                            //day is done
                            builder.setMessage("Would you like to study new words today or apply this tomorrow?");
                            final AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                        editor.putInt(CARDS_PER_DAY_KEY, cardsPerDay);
                        editor.apply();
                        Log.d(DEBUG_TAG, "InitialNewCardList Size: " + initialNewCardList.size());
                        return true;
                    }
//                    updateModifyDeck();
//                    cardsPerDayText.clearFocus();
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
            resetDatabaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openDialog();
                }
            });
            redButton=(Button)findViewById(R.id.redButton);
            redButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chooseBackground(0);
                }
            });
            orangeButton=(Button)findViewById(R.id.orangeButton);
            orangeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chooseBackground(1);
                }
            });
            yellowButton=(Button)findViewById(R.id.yellowButton);
            yellowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chooseBackground(2);
                }
            });
            greenButton=(Button)findViewById(R.id.greenButton);
            greenButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chooseBackground(3);
                }
            });
            blueButton=(Button)findViewById(R.id.blueButton);
            blueButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chooseBackground(4);
                }
            });
            purpleButton=(Button)findViewById(R.id.purpleButton);
            purpleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chooseBackground(5);
                }
            });
            pinkButton=(Button)findViewById(R.id.pinkButton);
            pinkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chooseBackground(6);
                }
            });
            greyButton=(Button)findViewById(R.id.greyButton);
            greyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chooseBackground(7);
                }
            });

        } else if(session==4) {
            //study timer started
            setContentView(R.layout.study_layout);
            constraintLayout=(ConstraintLayout)findViewById(R.id.studyLayout);
            chooseBackground(backgroundID);

            waitingText=findViewById(R.id.waitingText);
            studyModeText=findViewById(R.id.ModeTextView);
            koreanWordButton = findViewById(R.id.KoreanWordText);
            showAnswerButton = findViewById(R.id.ShowAnswerButton);
            wrongAnswerButton = findViewById(R.id.WrongAnswerButton);
            correctAnswerButton = findViewById(R.id.CorrectAnswerButton);
            undoButton = findViewById(R.id.UndoButton);
            timerText = findViewById(R.id.TimerText);

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
            newDayText.setVisibility(View.GONE);
            pickTime.setVisibility(View.GONE);
            settingText.setVisibility(View.GONE);
            cardsPerDayText.setVisibility(View.GONE);
            loadButton.setVisibility(View.GONE);
            saveButton.setVisibility(View.GONE);
            resetDatabaseButton.setVisibility(View.GONE);
            redButton.setVisibility(View.GONE);
            orangeButton.setVisibility(View.GONE);
            yellowButton.setVisibility(View.GONE);
            greenButton.setVisibility(View.GONE);
            blueButton.setVisibility(View.GONE);
            purpleButton.setVisibility(View.GONE);
            pinkButton.setVisibility(View.GONE);
            greyButton.setVisibility(View.GONE);
        } else if(session==7) {
            loadFileListView.setVisibility(View.GONE);
            newDayText.setVisibility(View.VISIBLE);
            pickTime.setVisibility(View.VISIBLE);
            settingText.setVisibility(View.VISIBLE);
            cardsPerDayText.setVisibility(View.VISIBLE);
            loadButton.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            resetDatabaseButton.setVisibility(View.VISIBLE);
            redButton.setVisibility(View.VISIBLE);
            orangeButton.setVisibility(View.VISIBLE);
            yellowButton.setVisibility(View.VISIBLE);
            greenButton.setVisibility(View.VISIBLE);
            blueButton.setVisibility(View.VISIBLE);
            purpleButton.setVisibility(View.VISIBLE);
            pinkButton.setVisibility(View.VISIBLE);
            greyButton.setVisibility(View.VISIBLE);
        } else if(session==8) {
            //conjugation
            setContentView(R.layout.conjugation_preview_layout);
            constraintLayout=(ConstraintLayout)findViewById(R.id.conjugation_preview_layout);
            chooseBackground(backgroundID);

            conjugationListView = findViewById(R.id.conjugationListView);
            conjugationStartButton = findViewById(R.id.conjugationStartButton);

            customAdapter = new CustomAdapter(this, R.layout.customlayout, finishedVerbs, sdk);
            conjugationListView.setAdapter(customAdapter);
            conjugationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(!waitingTimer) {
                        playSound("en" + Integer.toString(tempCard.getIndexKey()) + "_" + tempCard.getForeignWord() + ".mp3");
                    }
                }
            });
            conjugationStartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updatedChangeLayout(10);
                }
            });
        } else if(session==9) {
            setContentView(R.layout.study_preview_layout);
            constraintLayout=(ConstraintLayout)findViewById(R.id.study_preview_layout);
            chooseBackground(backgroundID);

            studyPreviewStart = findViewById(R.id.studyPreviewStart);
            studyPreviewListView = findViewById(R.id.studyPreviewListView);

            customAdapter = new CustomAdapter(this, R.layout.customlayout, newCards, sdk);
            studyPreviewListView.setAdapter(customAdapter);
            studyPreviewListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if(!waitingTimer) {
                        playSound("en" + Integer.toString(tempCard.getIndexKey()) + "_" + tempCard.getForeignWord() + ".mp3");
                    }
                }
            });
            studyPreviewStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updatedChangeLayout(1);
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
                }
            });
        } else if(session==10) {
            setContentView(R.layout.conjugation_layout);
            constraintLayout = (ConstraintLayout) findViewById(R.id.conjugation_layout);
            chooseBackground(backgroundID);

            conjugationCorrectAnswerButton = findViewById(R.id.ConjugationCorrectAnswerButton);
            conjugationWrongAnswerButton = findViewById(R.id.ConjugationWrongAnswerButton);
            conjugationShowAnswerButton = findViewById(R.id.ConjugationShowAnswerButton);
            conjugationUndoButton = findViewById(R.id.ConjugationUndoButton);

            conjugationKoreanWordText = findViewById(R.id.ConjugationKoreanWordText);
            conjugationModeTextView = findViewById(R.id.ConjugationModeTextView);

            conjugationModeTextView.setText("Incorrect Answers: " + conjugationWrongAnswers +"\nPasses: " + conjugationPasses);
            conjugationKoreanWordText.setText(finishedVerbs.get(conjugationIndex).getForeignWord());
            conjugationWrongAnswerButton.setClickable(false);
            conjugationCorrectAnswerButton.setClickable(false);
            switch(conjugationAnswers) {
                case 0:
                    conjugationKoreanWordText.setText(finishedVerbs.get(conjugationIndex).getForeignWord() + "\nPast Tense");
                    break;
                case 1:
                    conjugationKoreanWordText.setText(finishedVerbs.get(conjugationIndex).getForeignWord() + "\nContinuous Tense");
                    break;
                case 2:
                    conjugationKoreanWordText.setText(finishedVerbs.get(conjugationIndex).getForeignWord() + "\nFuture Tense");
                    break;
            }
            conjugationShowAnswerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch(conjugationAnswers) {
                        case 0:
                            conjugationShowAnswerButton.setText(finishedVerbs.get(conjugationIndex).getPastTense());
                            break;
                        case 1:
                            conjugationShowAnswerButton.setText(finishedVerbs.get(conjugationIndex).getContinuousTense());
                            break;
                        case 2:
                            conjugationShowAnswerButton.setText(finishedVerbs.get(conjugationIndex).getPerfectContinuous());
                            break;
                    }
                    conjugationShowAnswerButton.setClickable(false);
                    conjugationWrongAnswerButton.setClickable(true);
                    conjugationCorrectAnswerButton.setClickable(true);
                }
            });
            conjugationCorrectAnswerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    conjugationHistory.push(1);
                    conjugationAnswers += 1;
                    switch(conjugationAnswers) {
                        case 0:
                            conjugationKoreanWordText.setText(finishedVerbs.get(conjugationIndex).getForeignWord() + "\nPast Tense");
                            break;
                        case 1:
                            conjugationKoreanWordText.setText(finishedVerbs.get(conjugationIndex).getForeignWord() + "\nContinuous Tense");
                            break;
                        case 2:
                            conjugationKoreanWordText.setText(finishedVerbs.get(conjugationIndex).getForeignWord() + "\nFuture Tense");
                            break;
                        case 3:
                            //word is done, iterate
                            conjugationAnswers = 0;
                            conjugationIndex += 1;
                            if(conjugationIndex == finishedVerbs.size()) {
                                //list is done, iterate the pass an start anew
                                conjugationIndex = 0;
                                if(conjugationWrongAnswers == 0) {
                                    conjugationPasses += 1;
                                    if(conjugationPasses == 2){
                                        conjugationIndex = 0;
                                        conjugationAnswers = 0;
                                        conjugationPasses = 0;
                                        conjugationWrongAnswers = 0;
                                        conjugationHistory.clear();
                                        finishedVerbs.clear();
                                        updatedChangeLayout(0);
                                        break;
                                    }
                                }
                                conjugationWrongAnswers = 0;
                                conjugationHistory.clear();
                            }
                            conjugationModeTextView.setText("Incorrect Answers: " + conjugationWrongAnswers +"\nPasses: " + conjugationPasses);
                            conjugationKoreanWordText.setText(finishedVerbs.get(conjugationIndex).getForeignWord() + "\nPast Tense");
                    }
                    conjugationShowAnswerButton.setClickable(true);
                    conjugationShowAnswerButton.setText("?");
                    conjugationWrongAnswerButton.setClickable(false);
                    conjugationCorrectAnswerButton.setClickable(false);
                }
            });
            conjugationWrongAnswerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    conjugationHistory.push(-1);
                    conjugationWrongAnswers += 1;
                    conjugationAnswers += 1;
                    conjugationModeTextView.setText("Incorrect Answers: " + conjugationWrongAnswers +"\nPasses: " + conjugationPasses);
                    switch(conjugationAnswers) {
                        case 0:
                            conjugationKoreanWordText.setText(finishedVerbs.get(conjugationIndex).getForeignWord() + "\nPast Tense");
                            break;
                        case 1:
                            conjugationKoreanWordText.setText(finishedVerbs.get(conjugationIndex).getForeignWord() + "\nContinuous Tense");
                            break;
                        case 2:
                            conjugationKoreanWordText.setText(finishedVerbs.get(conjugationIndex).getForeignWord() + "\nFuture Tense");
                            break;
                        case 3:
                            //word is done, iterate
                            conjugationAnswers = 0;
                            conjugationIndex += 1;
                            if(conjugationIndex == finishedVerbs.size()) {
                                //list is done
                                conjugationIndex = 0;
                                conjugationWrongAnswers = 0;
                                conjugationHistory.clear();
                            }
                            conjugationModeTextView.setText("Incorrect Answers: " + conjugationWrongAnswers +"\nPasses: " + conjugationPasses);
                            conjugationKoreanWordText.setText(finishedVerbs.get(conjugationIndex).getForeignWord() + "\nPast Tense");
                    }
                    conjugationShowAnswerButton.setClickable(true);
                    conjugationShowAnswerButton.setText("?");
                    conjugationWrongAnswerButton.setClickable(false);
                    conjugationCorrectAnswerButton.setClickable(false);
                }
            });
            conjugationUndoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (conjugationHistory.size() > 0) {
                        conjugationAnswers -= 1;
                        conjugationShowAnswerButton.setText("?");
                        switch(conjugationAnswers) {
                            case -1:
                                //decrement to previous word
                                if(conjugationIndex > 0) {
                                    conjugationIndex -= 1;
                                    conjugationAnswers = 3;
                                    conjugationKoreanWordText.setText(finishedVerbs.get(conjugationIndex).getForeignWord() + "\nPast Tense");
                                }
                                break;
                            case 0:
                                conjugationKoreanWordText.setText(finishedVerbs.get(conjugationIndex).getForeignWord() + "\nPast Tense");
                                break;
                            case 1:
                                conjugationKoreanWordText.setText(finishedVerbs.get(conjugationIndex).getForeignWord() + "\nContinuous Tense");
                                break;
                            case 2:
                                conjugationKoreanWordText.setText(finishedVerbs.get(conjugationIndex).getForeignWord() + "\nFuture Tense");
                                break;
                        }
                        switch (conjugationHistory.pop()) {
                            case (1):
                                //undoing a correct answer
                                break;
                            case (-1):
                                conjugationWrongAnswers -= 1;
                                conjugationModeTextView.setText("Incorrect Answers: " + conjugationWrongAnswers +"\nPasses: " + conjugationPasses);
                                //undoing a wrong answer
                                break;
                        }
                    }
                }
            });
        }
    }
    public void setupDatabase() {
        BufferedReader bufferedReader = null;
        try {
//            bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("WordPairs.txt"), "UTF-8"));
            bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("Sorted_Final.txt"), "UTF-8"));

            String tempLine;
            String[] splitTempLine;
            int loopIndex = 0;
            while((tempLine = bufferedReader.readLine()) != null) {
                splitTempLine = tempLine.split(",", 10);
////                Log.d(DEBUG_TAG, tempLine + "\tsplitTempLine Length: " + Integer.toString(splitTempLine.length));
//                Log.d(DEBUG_TAG, splitTempLine[0].trim());
//                Log.d(DEBUG_TAG, splitTempLine[1].trim());
                if(splitTempLine[1].trim().equals("v")){
                    Log.d(DEBUG_TAG, "Verb found" + splitTempLine);
                    MainActivity.cardDatabase.cardDao().addCard(new CardTable(Integer.parseInt(splitTempLine[0].trim()), splitTempLine[1].trim(), splitTempLine[2].trim(), splitTempLine[3].trim(), 2.5, Long.MAX_VALUE, 0, 0, splitTempLine[4].trim(), splitTempLine[5].trim(), splitTempLine[6].trim(), splitTempLine[7].trim()));
                } else {
                    MainActivity.cardDatabase.cardDao().addCard(new CardTable(Integer.parseInt(splitTempLine[0].trim()), splitTempLine[1].trim(), splitTempLine[2].trim(), splitTempLine[3].trim(), 2.5, Long.MAX_VALUE, 0, 0, null, null, null, null));
                }
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

    public void reloadDatabase() {
//        BufferedReader bufferedReader = null;
//        try {
//            bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("Anki_List.txt"), "UTF-8"));
//
//            String tempLine;
//            String[] splitTempLine;
//            int loopIndex = 0;
//            while((tempLine = bufferedReader.readLine()) != null) {
//                splitTempLine = tempLine.split(",", 2);
////                Log.d(DEBUG_TAG, tempLine + "\tsplitTempLine Length: " + Integer.toString(splitTempLine.length));
//                MainActivity.cardDatabase.cardDao().addCard(new CardTable(Integer.parseInt(splitTempLine[0].trim()), splitTempLine[1].trim(), splitTempLine[2].trim(), splitTempLine[3].trim(), 2.5, Long.MAX_VALUE, 0, 0));
//                loopIndex += 1;
//            }
        BufferedReader bufferedReader = null;
        try {
//            bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("WordPairs.txt"), "UTF-8"));
            bufferedReader = new BufferedReader(new InputStreamReader(getAssets().open("Sorted_Final.txt"), "UTF-8"));

            String tempLine;
            String[] splitTempLine;
            int loopIndex = 0;
            while((tempLine = bufferedReader.readLine()) != null) {
                splitTempLine = tempLine.split(",", 10);
////                Log.d(DEBUG_TAG, tempLine + "\tsplitTempLine Length: " + Integer.toString(splitTempLine.length));
//                Log.d(DEBUG_TAG, splitTempLine[0].trim());
//                Log.d(DEBUG_TAG, splitTempLine[1].trim());
                if(splitTempLine[1].trim().equals('v')){
                    MainActivity.cardDatabase.cardDao().addCard(new CardTable(Integer.parseInt(splitTempLine[0].trim()), splitTempLine[1].trim(), splitTempLine[2].trim(), splitTempLine[3].trim(), 2.5, Long.MAX_VALUE, 0, 0, splitTempLine[4].trim(), splitTempLine[5].trim(), splitTempLine[6].trim(), splitTempLine[7].trim()));
                } else {
                    MainActivity.cardDatabase.cardDao().addCard(new CardTable(Integer.parseInt(splitTempLine[0].trim()), splitTempLine[1].trim(), splitTempLine[2].trim(), splitTempLine[3].trim(), 2.5, Long.MAX_VALUE, 0, 0, null, null, null, null));
                }
                loopIndex += 1;
            }
            Log.d(DEBUG_TAG, "Database initialized. Size: " + Integer.toString(loopIndex));
            newCards.clear();
            reviewCardList.clear();
            newCardHistory.clear();
            reviewCardHistory.clear();
            oldCardsPerDay=0;
            newDayMilli = 0L;
            editor.putLong(NEW_DAY_MILLI_KEY, newDayMilli);
            editor.apply();
            updatedInitialTasks();
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
    public void updatedInitialTasks() {
        Log.d(DEBUG_TAG, "InitialTasks called");
        loadSharedPreferences();//load shared preferences
        checkDueTime();
        loadDeck();//load entire deck for dictionary
        cardsDue();
        updatedLoadNewCards();
//        if(finishedWords.length() > 0) { //todo check this
        if(dayDone) {
//            dayDone = true;
            Log.d(DEBUG_TAG, finishedWords);
            String[] finishedWordsArray = finishedWords.split(",");
            Log.d(DEBUG_TAG, "FinishedWordsArray Size: " + finishedWordsArray.length);
            for(String tempString : finishedWordsArray) {
                Log.d(DEBUG_TAG, tempString);
//                newCards.add(MainActivity.cardDatabase.cardDao().getCard(Integer.parseInt(tempString)));
                initialNewCardList.add(MainActivity.cardDatabase.cardDao().getCard(Integer.parseInt(tempString)));
            }
        }
    }
    public void loadSharedPreferences() {
        if(!sharedPrefs.contains(INDEX_KEY)) {
            timeStudied = 0;
            index = 0;
            cardsPerDay = 10;
            tomorrowCardsPerDay = 0;
            wordsFinishedToday = 0;
            newDayMilliExtra = 0L;
            backgroundID = 7;
            finishedWords = "";
            dayDone = false;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeZone(TimeZone.getDefault());
            calendar.set(Calendar.HOUR_OF_DAY, 3);
            newDayMilli = calendar.getTimeInMillis();
//            newDayMilli = System.currentTimeMillis() - 60000;
            //todo hour
            Log.d(DEBUG_TAG, Long.toString(newDayMilli));
            editor.putInt(WORDS_FINISHED_TODAY, wordsFinishedToday);
            editor.putInt(INDEX_KEY, index);
            editor.putInt(CARDS_PER_DAY_KEY, cardsPerDay);
            editor.putInt(TOMORROW_CARDS_PER_DAY_ID, tomorrowCardsPerDay);
            editor.putLong(NEW_DAY_MILLI_KEY, newDayMilli);
            editor.putLong(NEW_DAY_MILLI_EXTRA_KEY, newDayMilliExtra);
            editor.putInt(TIME_STUDIED, timeStudied);
            editor.putInt(BACKGROUND_ID, backgroundID);
            editor.putString(FINISHED_WORDS_ID, finishedWords);
            editor.putBoolean(DAYDONE_ID, dayDone);
            editor.apply();
            setupDatabase();
        } else {
            //sharedPrefs.getLong(NEW_DAY_MILLI_KEY, newDayMilli);
            wordsFinishedToday = sharedPrefs.getInt(WORDS_FINISHED_TODAY, wordsFinishedToday);
            index = sharedPrefs.getInt(INDEX_KEY, index);
            cardsPerDay = sharedPrefs.getInt(CARDS_PER_DAY_KEY, cardsPerDay);
            tomorrowCardsPerDay = sharedPrefs.getInt(TOMORROW_CARDS_PER_DAY_ID, tomorrowCardsPerDay);
            if(tomorrowCardsPerDay > 1) {
                cardsPerDay = tomorrowCardsPerDay;
                tomorrowCardsPerDay = 0;

                editor.putInt(CARDS_PER_DAY_KEY, cardsPerDay);
                editor.putInt(TOMORROW_CARDS_PER_DAY_ID, tomorrowCardsPerDay);
                editor.apply();
            }
//            oldCardsPerDay = cardsPerDay;
            newDayMilli = sharedPrefs.getLong(NEW_DAY_MILLI_KEY, newDayMilli);
            reminderMilli = sharedPrefs.getLong(REMINDER_MILLI_KEY, reminderMilli);
            backgroundID = sharedPrefs.getInt(BACKGROUND_ID, backgroundID);
            finishedWords = sharedPrefs.getString(FINISHED_WORDS_ID, finishedWords);
            dayDone = sharedPrefs.getBoolean(DAYDONE_ID, dayDone);
            Log.d(DEBUG_TAG, "New Day Milli: " + "\t" + Long.toString(newDayMilli));
        }
    }
    public void checkDueTime() {
        Log.d(DEBUG_TAG, "Difference: " + Long.toString(newDayMilli - System.currentTimeMillis()));
        if(System.currentTimeMillis() > newDayMilli) {
            Calendar tempCalendar = Calendar.getInstance();
            tempCalendar.setTimeInMillis(newDayMilli);
            Log.d(DEBUG_TAG, "Old, new day time - " + tempCalendar.get(Calendar.HOUR_OF_DAY) + ":" + tempCalendar.get(Calendar.MINUTE));
            Log.d(DEBUG_TAG, "New Day Loaded");
            //new day to study
            //increment newDayMilli
//            newDayMilli = (System.currentTimeMillis() + 360000L); //ten minutes
//            newDayMilli = (System.currentTimeMillis() - (System.currentTimeMillis() % 86400000L) + 86400000L + newDayMilliExtra); //current time reduced to its original start time
            tempCalendar = Calendar.getInstance();
            tempCalendar.set(Calendar.HOUR_OF_DAY, 0);
            newDayMilli = tempCalendar.getTimeInMillis();
            newDayMilli = newDayMilli + 86400000L + newDayMilliExtra; //current time reduced to its original start time
            Log.d(DEBUG_TAG, "New, new day time - " + tempCalendar.get(Calendar.HOUR_OF_DAY) + ":" + tempCalendar.get(Calendar.MINUTE));
//            newDayMilli = newDayMilli + 86400000L;
            wordsFinishedToday = 0;
            dayDone = false;
            //clear newlist?
            editor.putInt(WORDS_FINISHED_TODAY, wordsFinishedToday);
            editor.putLong(NEW_DAY_MILLI_KEY, newDayMilli);
            editor.putBoolean(DAYDONE_ID, dayDone);
            finishedWords = "";
            editor.putString(FINISHED_WORDS_ID, finishedWords);
            editor.apply();
//            updatedLoadNewCards();
        }
    }
    public void updateModifyDeck() {
        Log.d(DEBUG_TAG, "oldCardsPerDay - " + oldCardsPerDay);
        Log.d(DEBUG_TAG, "cardsPerDay - " + cardsPerDay);
        Log.d(DEBUG_TAG, "wordsFinishedToday - " + wordsFinishedToday);
        hideKeyboard(this);
        //cardsperday already changed at this point

//        if(!dayDone) {
//            newCards.clear();
//            reviewCardList.clear();
//            updatedInitialTasks();
//            //should refill the values as if it's a new day
//            //should handle an increase or decrease since all words are finished at once
//            //no "partial" completes
//        } else {
//            //day is done
//            //if less words then it'll automatically add to the next day
//            if(cardsPerDay < oldCardsPerDay) {
//
//            } else {
//                dayDone = false;
//                updatedLoadNewCards();
//            }
//            //else add some words
//        }
//
//
        if(cardsPerDay > oldCardsPerDay) {
            if(dayDone) {
                dayDone = false;
//                newCards.clear();
//                reviewCardList.clear();
//                initialReviewList.clear();
//                initialNewCardList.clear();
            }
            updatedLoadNewCards();
        } else if(cardsPerDay < oldCardsPerDay && !dayDone) {
            updatedRemoveCards();
        }
        oldCardsPerDay = cardsPerDay;

        int tempI = 0;
        for (CardTable cT : newCards) {
            Log.d(DEBUG_TAG, tempI + " - " + cT.getForeignWord() + "\tDays: " + cT.getDays() + "\tStreak: " + cT.getStreak() + "\tTime: " + cT.getTime());
            tempI += 1;
        }
    }
    public void updatedLoadNewCards() {
        //atm this is ONLY for adding cards
        //load newcards with the new way  of not using index
        //assumes oldcardsperday will be set to 0 when app opens and newcardlist is empty
        //make sure the to is less than the size and greater than
        if(oldCardsPerDay - wordsFinishedToday  >= 0) {
//            ArrayList<CardTable> cardsToAddTemp = new ArrayList<>(new ArrayList<>(MainActivity.cardDatabase.cardDao().getNewCards(Long.MAX_VALUE)).subList(oldCardsPerDay, cardsPerDay - wordsFinishedToday));
            ArrayList<CardTable> cardsToAddTemp = new ArrayList<>(new ArrayList<>(MainActivity.cardDatabase.cardDao().getNewCards(Long.MAX_VALUE)).subList(oldCardsPerDay - wordsFinishedToday, cardsPerDay - wordsFinishedToday));
            for (CardTable cT : cardsToAddTemp) {
                cT.setTime(0L);
                newCards.add(cT);
                initialNewCardList.add(cT);
            }
            //todo add in old cards? Only if the day is done so newwords is empty
//            addCardsToDecks(cardsToAddTemp);
        }
        oldCardsPerDay = cardsPerDay;
    }
    public void updatedRemoveCards() {
        //remove the cards by searching for highest primary key repeatedly

        ArrayList<Integer> cardsToRemove = new ArrayList<>();

        int tempIndex = -1;
        for(int i = 0; i < (oldCardsPerDay - cardsPerDay); i++) {
            for(CardTable cT: newCards) {
                if(cT.getIndexKey() > tempIndex && !cardsToRemove.contains(cT.getIndexKey())) {
                    tempIndex = cT.getIndexKey();
                }
            }
            //this following might be bad
            for(CardTable cT: reviewCardList) {
                if(cT.getIndexKey() > tempIndex && !cardsToRemove.contains(cT.getIndexKey())) {
                    tempIndex = cT.getIndexKey();
                }
            }
            //the card with the highest index has been found, add to the appropriate list
            cardsToRemove.add(tempIndex);
            tempIndex = -1;
        }
        //by now we have the N highest index value cards where N is how many cards we are removing
        //send them to remove
        for(int i: cardsToRemove) {
            for (int j = 0; j < newCards.size(); j++) {
                if (newCards.get(j).getIndexKey() == i) {
                    newCards.remove(j);
                    break;
                }
            }
            for (int j = 0; j < reviewCardList.size(); j++) {
                if (reviewCardList.get(j).getIndexKey() == i) {
                    reviewCardList.remove(j);
                    break;
                }
            }
            for(int j = 0; j < initialNewCardList.size(); j++) {
                if(initialNewCardList.get(j).getIndexKey() == i) {
                    initialNewCardList.remove(j);
                    break;
                }
            }
            Log.d(DEBUG_TAG, "initialNewCardList size: " + Integer.toString(initialNewCardList.size()));
        }
//        removeCardsFromDeck(cardsToRemove);
        checkDeckStatus();
    }

    public void removeCardsFromDeck(ArrayList<Integer> cardsToRemove) {
        for(ArrayList<CardTable> oldNewCards : newCardHistory) {
            for (int i : cardsToRemove) {
                for (int j = 0; j < oldNewCards.size(); j++) {
                    if (oldNewCards.get(j).getIndexKey() == i) {
                        oldNewCards.remove(j);
                        break;
                    }
                }
            }
        }
        for(ArrayList<CardTable> oldReviewCards : reviewCardHistory) { //necessary?
            for(int i : cardsToRemove) {
                for(int j = 0; j < oldReviewCards.size(); j++) {
                    if(oldReviewCards.get(j).getIndexKey() == i) {
                        oldReviewCards.remove(j);
                        break;
                    }
                }
            }
        }
    }
    public void undoButtonPressed() {

    }
    public void updatedCardAnswer(boolean result) {

    }
    public CardTable updatedNextCard() {
        return tempCard;
    }
    public void checkDeckStatus() {

    }
    public void cardDone(CardTable cardTable, double answerQuality) { //todo doesn't this undo messed up review cards?
        if(!dayDone) {
//            int tempStreak = 0;
//            int tempDays = 0;
//            for (CardTable cT : initialNewCardList) {
//                if (cT.getIndexKey() == cardTable.getIndexKey()) {
//                    tempStreak = cT.getStreak();
//                    tempDays = cT.getDays();
//                }
//            }
//            for (CardTable cT : initialReviewList) {
//                if (cT.getIndexKey() == cardTable.getIndexKey()) {
//                    tempStreak = cT.getStreak();
//                    tempDays = cT.getDays();
//                }
//            }
            if(finishedWords.length()==0) {
                finishedWords = Integer.toString(cardTable.getIndexKey());
            } else {
                finishedWords = finishedWords + "," + Integer.toString(cardTable.getIndexKey());
            }
            Log.d(DEBUG_TAG, "finished words: " + finishedWords);

            cardTable.setStreak(cardTable.getStreak() + 1);
            cardTable.setFactor(calEasinessFactor(cardTable.getFactor(), answerQuality)); //probably wrong
            cardTable.setDays(calRepetitionInterval(cardTable.getDays(), cardTable.getFactor()));
            cardTable.setTime(newDayMilli + ((86400000) * (cardTable.getDays() - 1)));
            MainActivity.cardDatabase.cardDao().updateCard(cardTable);
            wordsFinishedToday += 1;
            editor.putInt(WORDS_FINISHED_TODAY, wordsFinishedToday);
            editor.putInt(TIME_STUDIED, timeInterval);
            editor.putString(FINISHED_WORDS_ID, finishedWords);
            timeInterval = 0;
            editor.apply();
        }
    }
    public void startStudyTimer(int duration, int tick) {
        waitingTimer = true;
        countDownTimer = new CountDownTimer(duration, tick) {

            public void onTick(long millisUntilFinished) {
                String tempMinutes = Long.toString(millisUntilFinished/1000/60);
                long tempSeconds = millisUntilFinished/1000%60;
                String tempSecondsText = Long.toString(tempSeconds);
                if(tempSeconds < 10) {
                    tempSecondsText = "0" + Long.toString(tempSeconds);
                }
                timerText.setText(tempMinutes + ":" + tempSecondsText);
            }
            public void onFinish() {
                waitingTimer = false;
                sendOnChannel();
                updatedChangeLayout(1);
                tempCard=updatedNextCard();
                koreanWordButton.setText("♫");
                nextRound();
            }
        }.start();
    }
    public void undoCardWrite(CardTable undoCard) {
        //when a card was written to the database and needs to be undone
        //todo it will only be review cards
        for (CardTable cT : initialReviewList) {
            if (cT.getIndexKey() == undoCard.getIndexKey()) {
                MainActivity.cardDatabase.cardDao().updateCard(cT);
            }
        }
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
            Log.d(DEBUG_TAG,"Write failed.");
            return;
        }
       Calendar tempCalender = Calendar.getInstance();
       File file = new File(Environment.getExternalStorageDirectory(), Integer.toString(index) + "Words_" + tempCalender.get(Calendar.MONTH) + "-" + tempCalender.get(Calendar.DAY_OF_MONTH) + "-" + tempCalender.get(Calendar.YEAR) + "-" + tempCalender.get(Calendar.HOUR) + ":" + tempCalender.get(Calendar.MINUTE) + ":" + tempCalender.get(Calendar.SECOND));

       FileOutputStream fos = null;
       try {
           if (!file.exists()) {
               try {
                   file.createNewFile();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
//           file.createNewFile();
           fos = new FileOutputStream(file, true);
           ObjectOutputStream os = new ObjectOutputStream(fos);

           ArrayList<CardTable> cardsModified = new ArrayList<>(MainActivity.cardDatabase.cardDao().getModifiedCards(Long.MAX_VALUE));
           cardsModified.add(new CardTable(index * -1, "x", null, null, 0.0, 0L, 0, 0, null, null, null, null));
           //index is negative and days represents the cards learned per day maybe

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
            }
        }

        if(loadFileChoices.isEmpty()) {
            loadFileChoices.add("No files found.");
        }

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
//                        cT.setTime(0);
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
                            Log.d(DEBUG_TAG, "READING DATABASE SAVE");
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

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        Calendar tempCalender = Calendar.getInstance();
        tempCalender.setTimeInMillis(newDayMilli);
        tempCalender.set(Calendar.HOUR_OF_DAY, hourOfDay);
        tempCalender.set(Calendar.MINUTE, minute);
        newDayMilli = tempCalender.getTimeInMillis();
        if (System.currentTimeMillis() > newDayMilli) {
            newDayMilli+=86400000L;
        }
        editor.putLong(NEW_DAY_MILLI_KEY, newDayMilli);
        editor.apply();
        String am_or_pm = "am";
        if(hourOfDay > 13) {
            hourOfDay = hourOfDay - 12;
            am_or_pm = "pm";
        }
        if(hourOfDay==0) {
            hourOfDay = 12;
        }
        String tempMinuteText = Integer.toString(minute);
        if(minute < 10) {
            tempMinuteText = "0" + tempMinuteText;
        }
        pickTime.setText(hourOfDay + ":" + tempMinuteText + " " + am_or_pm);
        Log.d(DEBUG_TAG, "Hour: " + hourOfDay + ", Minute: " + tempMinuteText + " " + am_or_pm);
        newDayMilliExtra = (hourOfDay * 1000 * 60 * 60) + (minute * 1000 * 60);
        editor.putLong(NEW_DAY_MILLI_EXTRA_KEY, newDayMilliExtra);
        editor.putLong(NEW_DAY_MILLI_KEY, newDayMilli);
        editor.apply();
    }

    public void addCardsToDecks(ArrayList<CardTable> cardsToAdd) {
        //newcard list already done
        for(ArrayList<CardTable> historyList : newCardHistory) {
            for(CardTable cT: cardsToAdd) {
                historyList.add(cT);
                Log.d(DEBUG_TAG, "Adding card: " + cT.getForeignWord());
            }
        }
    }
    public void openDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyTimePickerDialogTheme);
        builder.setMessage("Are you sure you want to reset your deck and delete your progress? Hold 'Yes' to confirm.")
                .setCancelable(false)
//                .setPositiveButton("No", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        //
//                    }
//                })
                .setPositiveButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.cancel();
                    }
                })
                .setNegativeButton("Yes - delete everything", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        reloadDatabase();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    public void chooseBackground(int tempIdentifier) {
        switch (tempIdentifier) {
            case 0:
                //red
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    constraintLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.red_background));
//                    actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.red_background));
                } else {
                   constraintLayout.setBackground(getResources().getDrawable(R.drawable.red_background));
//                   actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.red_background));
                }
                backgroundID = tempIdentifier;
                editor.putInt(BACKGROUND_ID, 0);
                editor.apply();
                break;
            case 1:
                //orange
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    constraintLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.orange_background));
                } else {
                    constraintLayout.setBackground(getResources().getDrawable(R.drawable.orange_background));
                }
                backgroundID = tempIdentifier;
                editor.putInt(BACKGROUND_ID, 1);
                editor.apply();
                break;
            case 2:
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    constraintLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.yellow_background));
                } else {
                    constraintLayout.setBackground(getResources().getDrawable(R.drawable.yellow_background));
                }
                backgroundID = tempIdentifier;
                editor.putInt(BACKGROUND_ID, 2);
                editor.apply();
                break;
            case 3:
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    constraintLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.green_background));
                } else {
                    constraintLayout.setBackground(getResources().getDrawable(R.drawable.green_background));
                }
                backgroundID = tempIdentifier;
                editor.putInt(BACKGROUND_ID, 3);
                editor.apply();
                break;
            case 4:
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    constraintLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.blue_background));
                } else {
                    constraintLayout.setBackground(getResources().getDrawable(R.drawable.blue_background));
                }
                backgroundID = tempIdentifier;
                editor.putInt(BACKGROUND_ID, 4);
                editor.apply();
                break;
            case 5:
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    constraintLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.purple_background));
                } else {
                    constraintLayout.setBackground(getResources().getDrawable(R.drawable.purple_background));
                }
                backgroundID = tempIdentifier;
                editor.putInt(BACKGROUND_ID, 5);
                editor.apply();
                break;
            case 6:
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    constraintLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.pink_background));
                } else {
                    constraintLayout.setBackground(getResources().getDrawable(R.drawable.pink_background));
                }
                backgroundID = tempIdentifier;
                editor.putInt(BACKGROUND_ID, 6);
                editor.apply();
                break;
            case 7:
                if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    constraintLayout.setBackgroundDrawable(getResources().getDrawable(R.drawable.grey_background));
                } else {
                    constraintLayout.setBackground(getResources().getDrawable(R.drawable.grey_background));
                }
                backgroundID = tempIdentifier;
                editor.putInt(BACKGROUND_ID, 7);
                editor.apply();
                break;
        }
    }
    public class NeoStack{
        public ArrayList<CardTable> cards;
        public NeoStack(ArrayList<CardTable> cards) {
            this.cards = cards;
        }
        public void push(int index, CardTable cT) {
            if(index == 4) {
               this.cards.add(cards.size() - 1, cT);
            } else {
                if(index >= this.cards.size()) {
                    this.cards.add(cT);
                } else {
                    this.cards.add(index, cT);
                }
            }
        }
        public CardTable pop() {
            return this.cards.remove(0);
        }
        public CardTable peak() {
            return this.cards.get(0);
        }
        public boolean checkStatus() {
            for(CardTable cT : this.cards) {
                if(cT.getStreak() < 4) {
                    return false;
                }
            }
            return true;
        }
        public void studyUndo(int answer, int index) {
            for(int i = 0; i < this.cards.size(); i++) {
                if(this.cards.get(i).getIndexKey() == index) {
                    this.cards.get(i).setStreak(this.cards.get(i).getStreak() + answer);
                    this.cards.add(0, this.cards.remove(i));
                    break;
                }
            }
        }
    }
    public void cardsPerDayChoice() {

    }
}

