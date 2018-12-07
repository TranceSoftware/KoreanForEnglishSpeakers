package com.example.joshua.koreancards;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Resources;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Calendar;

public class StudyFragment extends Fragment {
    private Calendar calendar = Calendar.getInstance();
    private final String DEBUG_TAG = "DEBUG_TAG";
    private ArrayList<Cell> everyList = new ArrayList<>();
    private Cell currentCell = null;
    private Button koreanWordButton, showAnswerButton, wrongAnswerButton, correctAnswerButton, timerButton;
    private LocalBroadcastManager localBroadcastManager;
    //TODO api catch
    private Resources resources;
    private SoundPool soundPool;
    private int mSoundId;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(DEBUG_TAG, "onCreate() called.");
        //get cells past to fragment
        everyList.addAll((ArrayList<Cell>) getArguments().getSerializable("NEW_WORDS"));
        for(Cell c : everyList) {
            c.setListID(0);
        }
        localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        calendar.setTimeInMillis(System.currentTimeMillis());
        resources = this.getResources();
    }
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "onCreateView() called");

        View studyView = inflator.inflate(R.layout.study_layout, container, false);
        ConstraintLayout constraintLayout = (ConstraintLayout) inflator.inflate(R.layout.study_layout, container,false);

        koreanWordButton = (Button)constraintLayout.findViewById(R.id.KoreanWordButton);
        showAnswerButton = (Button) constraintLayout.findViewById(R.id.ShowAnswerButton);
        wrongAnswerButton = (Button) constraintLayout.findViewById(R.id.WrongAnswerButton);
        correctAnswerButton = (Button) constraintLayout.findViewById(R.id.CorrectAnswerButton);
        timerButton = (Button) constraintLayout.findViewById(R.id.TimerButton);
        //initial view
        currentCell = everyList.get(0);
        currentCell.setListID(1);
        koreanWordButton.setText(currentCell.getKorean());
        showAnswerButton.setText("?");
        //set click listeners
        koreanWordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo play audio
            }
        });
        showAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(currentCell.getSessionStreak() ==4 ) {
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
                currentCell =  updatedNextCard(currentCell);
                koreanWordButton.setText(currentCell.getKorean());
            }
        });
        correctAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatedAnswerMade(true, currentCell);
                showAnswerButton.setText("?");
                currentCell = updatedNextCard(currentCell);
                koreanWordButton.setText(currentCell.getKorean());
            }
        });
//        studyView.setBackgroundColor(getResources().getColor(android.R.color.background_dark));
        return studyView;
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
    public void onPause() {
        //finishSession(false);
        super.onPause();
    }
    public void updatedAnswerMade(boolean result, Cell cell) {
        //everyList = new ArrayList<>();
        //everyList.addAll(learningList);
        //everyList.addAll(retrievalList);
        //decrease all cards between values
        //do not update
        if (cell.isReviewCard() == 1) {
            if (result) {
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
                        finishSession(finished); //this should call onDestroy
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
    public void finishSession(boolean finished) {
        Log.d(DEBUG_TAG, "StudyActivity calling finishedSession");
        for(Cell c : everyList) {
            // Log.d(DEBUG_TAG, "StudyActivity calling finishedSession" +c.getKorean() + Integer.toString(c.getListID()));
            if(c.getListID()==3 || c.isReviewCard()==2) {
                //only finished cards(ID=3), which includes failed review cards that depreciated, and successful review cards will be given new dates
                //all others will be as zero
                c.setDaysBetweenReviews(calRepititionInterval(c.getDaysBetweenReviews(), c.getDifficulty()));
                //Log.d(DEBUG_TAG, Integer.toString(c.getDaysBetweenReviews()));
            }
        }
        Intent resultIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("STUDY_WORDS", everyList);
        resultIntent.putExtras(bundle);
        if(finished) {
            setArguments(bundle);
            getFragmentManager().popBackStack();
        } else {
            Log.d(DEBUG_TAG, "LocalBroadcast sending.");
            localBroadcastManager.sendBroadcast(resultIntent);
        }
    }
//
}
