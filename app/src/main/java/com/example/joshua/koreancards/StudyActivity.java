package com.example.joshua.koreancards;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;

/**
 * Created by Joshua on 5/27/2018.
 */

public class StudyActivity extends Activity {

    private Calendar calendar = Calendar.getInstance(); //set this every time the activity is created or resumed

    private final String DEBUG_TAG = "DEBUG_TAG";
    private ArrayList<String> koreanWords=new ArrayList<>();
    private ArrayList<String> englishWords=new ArrayList<>();

    private ArrayList<Cell> newWordsList = new ArrayList<>();//new word list
    private ArrayList<Cell> learningList = new ArrayList<>();//learning set
    private ArrayList<Cell> retrievalList = new ArrayList<>();//retrieval set
    private ArrayList<Cell> finishedList = new ArrayList<>();//finished set

    private ArrayList<Cell> reviewWordsList = new ArrayList<>(); //words from previous sessions that need to be retrieved by the user

    private Cell currentCell = null;

    private int index=0;
    private ArrayList<Button> buttonList = new ArrayList<>();
    private Button koreanWordButton, showAnswerButton, wrongAnswerButton, correctAnswerButton, timerButton;
    private Button item0, item1, item2, item3, item4, item5, item6, item7, item8, item9;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.study_layout);
        calendar.setTimeInMillis(System.currentTimeMillis());
//        koreanWords.addAll(getIntent().getStringArrayListExtra("KOREAN"));
//        englishWords.addAll(getIntent().getStringArrayListExtra("ENGLISH"));
//
//        Log.d(DEBUG_TAG,"K-Length: " + koreanWords.size() + "\t" + "E-length: " + englishWords.size());
        koreanWordButton=findViewById(R.id.KoreanWordButton);
        showAnswerButton=findViewById(R.id.ShowAnswerButton);
        wrongAnswerButton=findViewById(R.id.WrongAnswerButton);
        correctAnswerButton=findViewById(R.id.CorrectAnswerButton);
        timerButton=findViewById(R.id.TimerButton);
        item0=findViewById(R.id.Item0);
        item1=findViewById(R.id.Item1);
        item2=findViewById(R.id.Item2);
        item3=findViewById(R.id.Item3);
        item4=findViewById(R.id.Item4);
        item5=findViewById(R.id.Item5);
        item6=findViewById(R.id.Item6);
        item7=findViewById(R.id.Item7);
        item8=findViewById(R.id.Item8);
        item9=findViewById(R.id.Item9);
        buttonList.add(item0);
        buttonList.add(item1);
        buttonList.add(item2);
        buttonList.add(item3);
        buttonList.add(item4);
        buttonList.add(item5);
        buttonList.add(item6);
        buttonList.add(item7);
        buttonList.add(item8);
        buttonList.add(item8);

        //add in the resuming aspect later or just looped through new words and move the ones that have streaks
        handleBundle(savedInstanceState);
//        for(int i=0;i<7;i++) {
//            Cell tempCell = newWordsList.remove(0);
//            tempCell.setList(1);
//            tempCell.setMiliSeconds(System.currentTimeMillis() + (1000 * tempCell.getWaitTime(tempCell.getStreak())));
//            subSetList.add(tempCell);
//        }
        currentCell = learningList.get(0);
        koreanWordButton.setText(currentCell.getKorean());
//        Log.d(DEBUG_TAG, currentCell.getIndex() + "\t" + currentCell.getKorean() + "\t" + currentCell.getEnglish());
        showAnswerButton.setText("?");
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
                showAnswerButton.setText(currentCell.getEnglish());
            }
        });
        wrongAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                answerMade(false, currentCell);
                showAnswerButton.setText("?");
                updateDisplay(false, currentCell);
                currentCell = nextCard(currentCell);
                koreanWordButton.setText(currentCell.getKorean());
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
                answerMade(true, currentCell);
                showAnswerButton.setText("?");
                //change text displayed or set to audio i.e. hide
                updateDisplay(true, currentCell);
                currentCell = nextCard(currentCell);
                koreanWordButton.setText(currentCell.getKorean() + "\t" + currentCell.getSessionStreak());

                Log.d(DEBUG_TAG, "NewWordList");
                for(int i=0;i<newWordsList.size();i++) {
                    Log.d(DEBUG_TAG, i + "\t" + newWordsList.get(i).getKorean() + "\t" + Integer.toString(newWordsList.get(i).getCardsBetween()) + "\t" + newWordsList.get(i).getRetrievalStreak() + "\t" + (newWordsList.get(i).getMilliSeconds() - System.currentTimeMillis()));
                }
                Log.d(DEBUG_TAG, "\n" + "LearningList");
                for(int i=0;i<learningList.size();i++) {
                    Log.d(DEBUG_TAG, i + "\t" + learningList.get(i).getKorean() + "\t" + Integer.toString(learningList.get(i).getCardsBetween()) + "\t" + learningList.get(i).getRetrievalStreak() + "\t" + (learningList.get(i).getMilliSeconds() - System.currentTimeMillis()));
                }
                Log.d(DEBUG_TAG, "\n" + "RetrievalList");
                for(int i=0;i<retrievalList.size();i++) {
                    Log.d(DEBUG_TAG, i + "\t" + retrievalList.get(i).getKorean() + "\t" + Integer.toString(retrievalList.get(i).getCardsBetween()) + "\t" + retrievalList.get(i).getRetrievalStreak() + "\t" + (retrievalList.get(i).getMilliSeconds() - System.currentTimeMillis()));
                }
                Log.d(DEBUG_TAG, "\n" + "FinishedList");
                for(int i=0;i<finishedList.size();i++) {
                    Log.d(DEBUG_TAG, i + "\t" + finishedList.get(i).getKorean() + "\t" + Integer.toString(finishedList.get(i).getCardsBetween()) + "\t" + finishedList.get(i).getRetrievalStreak() + "\t" + (finishedList.get(i).getMilliSeconds() - System.currentTimeMillis()));
                }
            }
        });
    }
    public void handleBundle(Bundle bundle) {
        newWordsList.addAll((ArrayList<Cell>)getIntent().getSerializableExtra("NEW_WORDS"));
//        newWordsList.addAll((ArrayList<Cell>) bundle.getSerializable("NEW_WORDS"));
        for(Cell c: newWordsList) {
            switch(c.getListID()) {
                case 1:learningList.add(newWordsList.remove(newWordsList.indexOf(c)));
                learningList.get(learningList.size() -1).setListID(1);
                break;
                case 2:retrievalList.add(newWordsList.remove(newWordsList.indexOf(c)));
                retrievalList.get(retrievalList.size() -1).setListID(2);
                break;
            }
        }
        int wordsInPlay = 7;
        //change later to handle dynamic input of how many words need to be learned
        //per day as decided by user in their settings
        int difference = wordsInPlay-learningList.size();
        if(difference>0) {
            for(int i=0; i<difference; i++) {
                learningList.add(newWordsList.remove(0));
                learningList.get(learningList.size() -1).setListID(1);
            }
        }
        Log.d(DEBUG_TAG, "NewWordList");
        for(int i=0;i<newWordsList.size();i++) {
            Log.d(DEBUG_TAG, i + "\t" + newWordsList.get(i).getKorean() + "\t" + Integer.toString(newWordsList.get(i).getCardsBetween()) + "\t" + newWordsList.get(i).getEnglish() + "\t" + newWordsList.get(i).getRetrievalStreak());
        }
        Log.d(DEBUG_TAG, "\n" + "LearningList");
        for(int i=0;i<learningList.size();i++) {
            Log.d(DEBUG_TAG, i + "\t" + learningList.get(i).getKorean() + "\t" + Integer.toString(learningList.get(i).getCardsBetween()) + "\t" + learningList.get(i).getEnglish() + "\t" + learningList.get(i).getRetrievalStreak());
        }
        Log.d(DEBUG_TAG, "\n" + "RetrievalList");
        for(int i=0;i<retrievalList.size();i++) {
            Log.d(DEBUG_TAG, i + "\t" + retrievalList.get(i).getKorean() + "\t" + Integer.toString(retrievalList.get(i).getCardsBetween()) + "\t" + retrievalList.get(i).getEnglish() + "\t" + retrievalList.get(i).getRetrievalStreak());
        }
        Log.d(DEBUG_TAG, "\n" + "FinishedList");
        for(int i=0;i<finishedList.size();i++) {
            Log.d(DEBUG_TAG, i + "\t" + finishedList.get(i).getKorean() + "\t" + Integer.toString(finishedList.get(i).getCardsBetween()) + "\t" + finishedList.get(i).getEnglish() + "\t" + finishedList.get(i).getRetrievalStreak());
        }
//        reviewWordsList.addAll((ArrayList<Cell>) bundle.getSerializable("REVIEW_WORDS"));
        //handle additional bundle scenario when onDestroy has been called and the lists are completely empty
//        subSetList.addAll((ArrayList<Cell>) bundle.getSerializable("SUB_SET_LIST"));
//        subSetAlphaList.addAll((ArrayList<Cell>) bundle.getSerializable("SUB_SET_LIST_ALPHA"));
//        consolidatedList.addAll((ArrayList<Cell>) bundle.getSerializable("CONSOLIDATED_LIST"));
//        finishedList.addAll((ArrayList<Cell>) bundle.getSerializable("FINISHED_LIST"));
        //can this be done with empty lists?
    }
//    public Cell nextCell() { //current time
////        long time = Long.MAX_VALUE;
//        long currentTime = System.currentTimeMillis();
//        Cell tempCell = null;
//        if(subSetAlphaList.size()>1) {
//            tempCell=subSetAlphaList.get(0);
//        } else {
//            tempCell=subSetList.get(0);
//        }
//        long time = tempCell.getMilliSeconds();
////        for(Cell c : consolidatedList) { //will this fail if empty?
////            if(c.getMilliSeconds() < time) {
////                tempCell = c;
////                time = c.getMilliSeconds();
////            }
////        }
//        for(Cell c : subSetAlphaList) {
//            if((c.getMilliSeconds() - currentTime) < (time - currentTime)) {
//                tempCell = c;
//                time = c.getMilliSeconds();
//            }
//        }
//        //at this time we have the smallest difference of scheduled time compared to current time
////        if(time < 0) {
//        if((time - currentTime) < 0) {
//            //if we have a negative value then a word is ready to be seen again, return this value
//            return tempCell;
//        } else {
//            //no card is ready, add a card out of play (not new) or else use closest time
//            if(subSetList.size() > 0) {
//                //there are cards we can introduce to buy time, choose the first one and add it to the other list
//                tempCell = subSetList.remove(0);
//                tempCell.setList(2);
//                subSetAlphaList.add(tempCell);
//                //move to words in play
//                return tempCell;
//                //new word is now in play and at the end of the list
//            } else {
//                //new words are empty, even though a new word isn't ready, use the closest one
//                return tempCell;
//            }
//        }
//    }
//    public void answerMade(boolean correct, Cell c) {
//        Log.d(DEBUG_TAG, "NewWordList Size: " + "\t" + newWordsList.size());
//        Log.d(DEBUG_TAG, "SubSetList Size: " + "\t" + subSetList.size());
//        Log.d(DEBUG_TAG, "SubSetAlphaList Size: " + "\t" + subSetAlphaList.size());
//        Log.d(DEBUG_TAG, "ConsolidatedList Size: " + "\t" + consolidatedList.size());
//        if (correct) {
//            c.setStreak(c.getStreak()+1);
//            Log.d(DEBUG_TAG, c.getKorean() + "\t" + c.getStreak());
//            c.setMiliSeconds(System.currentTimeMillis() + (1000 * c.getWaitTime(c.getStreak())));
//            if (c.getList() == 1) {
//                c.setList(2);
//                subSetAlphaList.add(subSetList.remove(subSetList.indexOf(c)));
//            } else if (c.getList() == 2) {
//                if(c.getStreak() == 5) {
//                    //for now just remove from list and add new
//                    c.setList(3);
//                    consolidatedList.add(subSetAlphaList.remove(subSetAlphaList.indexOf(c)));
//                    //now add new one
//                    //handle case of running out of new words
//                    Cell tempCell = newWordsList.remove(0);
//                    tempCell.setList(1);
//                    tempCell.setMiliSeconds(System.currentTimeMillis() + (1000 * tempCell.getWaitTime(tempCell.getStreak())));
//                    subSetList.add(tempCell);
//                }
//            } else { //consolidated will never be added so doesn't matter
////                c.setList(3);
////                c.setStreak(5);
////                //set interval of days
////                consolidatedList.add(subSetAlphaList.remove(subSetAlphaList.indexOf(c)));
////                Cell tempCell = newWordsList.remove(newWordsList.indexOf(c));
////                tempCell.setList(1);
////                subSetList.add(tempCell);
////                //change here too
//            }
//        } else {
//            if(c.getStreak()>0) {
//                c.setStreak(c.getStreak() - 1);
//            }
//            c.setMiliSeconds(System.currentTimeMillis() + (1000 * c.getWaitTime(c.getStreak())));
//        }
//    }
    public void createDisplayDeck() {
        int[] topCoord = new int[2];
        timerButton.getLocationOnScreen(topCoord);
        Log.d(DEBUG_TAG, topCoord[0] + " " + topCoord[1]);
        int[] bottomCoord = new int[2];
        koreanWordButton.getLocationOnScreen(bottomCoord);
        Log.d(DEBUG_TAG, bottomCoord[0] + " " + bottomCoord[1]);
        //calc difference
        int[] screenSpace = new int[]{bottomCoord[0]-topCoord[0], bottomCoord[1]-topCoord[1]};
        int ySpread;
        Log.d(DEBUG_TAG, screenSpace[0] + " " + screenSpace[1]);
        if(learningList.size() >= 10) {
            ySpread = screenSpace[1]/6;
        } else {
            if(learningList.size() % 2 == 0) {
                //even number of buttons learned per day
                ySpread = (screenSpace[1]/((learningList.size()/2)+1));
            } else {
                //odd number
                ySpread = (screenSpace[1]/((learningList.size()/2)+2));
            }
        }
        Log.d(DEBUG_TAG, Integer.toString(ySpread));
        //max will be ten buttons on the screen, half on one side and half on the other
        //therefore we split the total buttons
        int topMarginSpread = ySpread;
        int topMarginSpreadHalf = topMarginSpread/2;
        for(int i = 0; i < learningList.size(); i++){
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) buttonList.get(i).getLayoutParams();
            params.topMargin = ySpread;
            buttonList.get(i).setVisibility(View.VISIBLE);
            buttonList.get(i).setText(learningList.get(i).getKorean());
            //set this index value to the card so it the button position can be referenced easily later
            learningList.get(i).setButtonIndex(i);
            buttonList.get(i).setLayoutParams(params);
//            buttonList.get(i).setY(ySpread);
//            ySpread+=ySpread;
            if((learningList.size() % 2) == 0) {
                if ((i % 2) == 1) {
                    ySpread += topMarginSpread;
                }
            } else {
                ySpread+= topMarginSpreadHalf;
            }
        }
    }

    //TODO function called at the start to speak

    public double calEasinessFactor(double oldEasyFactor, double quality) {
        return oldEasyFactor-(0.8+(0.28*quality)-(0.02*quality*quality));
    }
    public int calRepititionInterval(int oldInterval, double easyFactor, int streak) {
        if(streak==1) {
            return 1;
        } else if(streak==2) {
            return 3;
        } else {
            return (int)Math.ceil((double)oldInterval*easyFactor);
        }
    }
    public void answerMade(boolean result, Cell cell) { //add EF calcs later
        //search listID in descending order since correct answers can only move up
        //new and finished listed will never be addressed here
        //when new cards are to be used they are to be first moved into the learning set
        //eventually will have a review list, correct will be moved to finished else moved to learning set

        //set new dates for finished cards
        //handle review cards

        Log.d(DEBUG_TAG, cell.getKorean() + " ListID: " + cell.getListID());

        if(result) {
//            if(cell.getListID()==2) {
            if(retrievalList.contains(cell)){
                //retrieval stage
                //increase streak
                Log.d(DEBUG_TAG, "Retrieval cell: " + cell.getKorean() + " has retrievalStreak: " + cell.getRetrievalStreak());
                cell.setRetrievalStreak(cell.getRetrievalStreak()+1);
                Log.d(DEBUG_TAG, "Retrieval cell: " + cell.getKorean() + " has NEW retrievalStreak: " + cell.getRetrievalStreak());
                if(cell.getRetrievalStreak()==2) {
                    //second retrieval succeeded
                    //move to finished list
                    cell.setListID(3);
                    finishedList.add(retrievalList.remove(retrievalList.indexOf(cell)));
                } else {
                    //set a new time since it is not finished yet
                    Long tempLong = 10000L;
                    cell.setMiliSeconds(System.currentTimeMillis()+ tempLong);
                }
            } else if(cell.getListID()==1) {
                //learning stage
                //increase streak
                cell.setSessionStreak(cell.getSessionStreak()+1);
                if(cell.getSessionStreak()>=7) { //was 4
                    //learning stage over
                    //move cell only if available new word to replace this word



//                    if (newWordsList.size() > 0) {


                        cell.setListID(2);
                        Long tempLong = 10000L;
                        cell.setMiliSeconds(System.currentTimeMillis() + tempLong);
                        retrievalList.add(learningList.remove(learningList.indexOf(cell)));
                        //add a new card from new list
                    if(newWordsList.size()>0) {
                        Cell tempCell = newWordsList.remove(0);
                        tempCell.setListID(1);
                        //which one is irrelevant so just grab the first one
                        learningList.add(tempCell);
                    }
//                    } else {
                        //can't move out, keep the learning list full
//                        cell.setSessionStreak(2);
//                        cell.setCardsBetween(cell.getGap(cell.getSessionStreak()));



//                    }


                } else {
                    //session streak less than 3, has been incremented
                    //just set a new gap
                    cell.setCardsBetween(cell.getGap(cell.getSessionStreak()));
                }
            }
        } else {
            //list order is irrelevant however if we plan to move words from the retrieval back to study list then
            //having it go in increase order will be necessary
            if(cell.getListID()==1) {
                //learning list, incorrect response
                //decrease only if the streak isn't 0
                if(cell.getSessionStreak()>0) {
                    cell.setSessionStreak(cell.getSessionStreak()-1);
                }
                //set new gap
                cell.setCardsBetween(cell.getGap(cell.getSessionStreak()));
            } else if(cell.getListID()==2) {
                //retrieval list
                //decrease retrieval streak only if not 0
                //do we move it back down a list? I think no
                if(cell.getRetrievalStreak()>0) {
                    //decrease streak
                    cell.setRetrievalStreak(cell.getRetrievalStreak()-1);
                }
                //set new time of at least 10 min gap
                Long tempLong = 10000L;
                cell.setMiliSeconds(System.currentTimeMillis() + tempLong);
            }
        }
        //iterate through the learning list and reduce the gaps by 1
        for(Cell c: learningList) {
            if(c.getCardsBetween()!=Integer.MAX_VALUE) {
                c.setCardsBetween(c.getCardsBetween() - 1);
            }
            //it's fine if it goes negative
        }
    }

    public void updateDisplay(boolean result, Cell c) {
        //TODO where are other words added?
        //0-2 korean
        //3-4 audio
        //5-6 english
        if(result) {
            switch(c.getSessionStreak()) {
                case 3:
                    //set to audio
                    buttonList.get(c.getButtonIndex()).setText("{ADD_NOTE_SYMBOL}");
                    break;
                case 5:
                    //swap to english
                    buttonList.get(c.getButtonIndex()).setText(c.getEnglish());
                    break;
                case 7:
                    //remove here instead?
                }
        }else {
            switch(c.getSessionStreak()) {
                case 2:
                    //korean
                    buttonList.get(c.getButtonIndex()).setText(c.getKorean());
                    break;
                case 4:
                    //audio
                    buttonList.get(c.getButtonIndex()).setText("{ADD_NOTE_SYMBOL}");
                    break;
            }
        }
    }

    public Cell nextCard(Cell cell) {
        Cell returnCell;
        switch(cell.getListID()) {
            case 1:learningList.remove(learningList.indexOf(cell));
                break;
            case 2:retrievalList.remove(retrievalList.indexOf(cell));
        }
        if(cell.getNextCell()!=null) {
            //remove the old next cell to prevent patterns
            switch(cell.getNextCell().getListID()) {
                case 1:learningList.remove(learningList.indexOf(cell.getNextCell()));
                    break;
                case 2:retrievalList.remove(retrievalList.indexOf(cell.getNextCell()));
            }
        }
        //make sure that this handles the previous card problem
        //make sure that we add a check for when newlist is empty and everything has finally been learned to transition to audio mode
        long indexTime = System.currentTimeMillis();
        int learningIndex = -1; //was 0
        int overdue=1;
        //betaList should never be empty
        int retrievalIndex = -1;
        //iterate through the retrievalList to see if any cards are ready to be retrieved
        for(int i=0;i< retrievalList.size();i++) {
            //the card that should be played next is 1) larger than indexTime so
            //every consecutive throw is because a new time is larger i.e. has been due for longer
            if(retrievalList.get(i).getMilliSeconds() < indexTime) {
                retrievalIndex = i;
                indexTime=retrievalList.get(i).getMilliSeconds();
                //change this to be most overdue
            }
        }
        if(retrievalIndex>=0) {
            //we have a time that is up
            //even thought there is always a value given to indexTime, retrievalTime
            //is only above zero if there is a value less than the current time
            returnCell=retrievalList.get(retrievalIndex);
            switch(cell.getListID()) {
                case 1:learningList.add(cell);
                    break;
                case 2:retrievalList.add(cell);
            }
            if(cell.getNextCell()!=null) {
                switch (cell.getNextCell().getListID()) {
                    //add the cell back in
                    case 1:learningList.add(cell.getNextCell());
                        //set the new nextCell value
                        break;
                    case 2:retrievalList.add(cell.getNextCell());
                }
            }
            cell.setNextCell(returnCell);
            return returnCell;
        } else {
            //next card gap
            //value is in ints so worst case two are two at the same time
            //calculate the weight of each to decide that case

//            for(int i=0;i<learningList.size();i++) {
//                if(learningList.get(i).getCardsBetween()<overdue) {
//                    overdue=learningList.get(i).getCardsBetween();
//                    learningIndex=i;
//                } else if(learningList.get(i).getCardsBetween()==overdue && learningList.get(i).getSessionStreak()>learningList.get(learningIndex).getSessionStreak()) {
//                    //in the case that there is the same amount overdue, choose the one that has the longest streak aka the more known card
//                    overdue=learningList.get(i).getCardsBetween();
//                    learningIndex=i;
//                }
//            }
            for(int i=0;i<learningList.size();i++) {
                if(learningList.get(i).getCardsBetween()<overdue) {
                    overdue=learningList.get(i).getCardsBetween();
                    learningIndex=i;
                    //add check for same value situations
                }
            }
            if(learningIndex<0) {
                //no card ready, search again for max values
                for(int i=0;i<learningList.size();i++) {
                    if(learningList.get(i).getCardsBetween()==Integer.MAX_VALUE) {
                        learningList.get(i).getCardsBetween();
                            learningIndex = i;
                            break;
                            //just natural to stop at the first new card
                    }
                }
            }
            if(learningIndex<0) {
                //no card ready and no new cards, this should be rare, just find the lowest value aka the next card to be played
                overdue=learningList.get(0).getCardsBetween();
                learningIndex=0;
                for(int i=1;i<learningList.size();i++) {
                    if(learningList.get(i).getCardsBetween()<overdue) {
                        overdue=learningList.get(i).getCardsBetween();
                        learningIndex=i;
                    }
                }
            }
//            if(overdueIndex<1) {
//                return betaList.get(betaIndex);
//            }
            //check if near the end of the learning session because all cards kept in play have a streak of at least 4
//            boolean doneFlag = true;
//            for(Cell c: learningList) {
//                if(c.getSessionStreak()<4) {
//                    doneFlag = false;
//                }
//            }
            //will be true if all done
//            if(doneFlag) {
//                finish();
//            }
            if(newWordsList.size()==0 && learningList.size()==0 && retrievalList.size()==0) {
                finish();
            }
        }
        //if no new value, either choose the next best one or just the next one
        returnCell=learningList.get(learningIndex);
        switch(cell.getListID()) {
            case 1:learningList.add(cell);
                break;
            case 2:retrievalList.add(cell);
        }
        if(cell.getNextCell()!=null) {
            switch(cell.getNextCell().getListID()) {
                case 1: learningList.add(cell.getNextCell());
                    break;
                case 2: retrievalList.add(cell.getNextCell());
            }
        }
        cell.setNextCell(returnCell);
        return returnCell;
        //in the case where no retrieval card is up
        //and no learning card via cardGap is ready
        //do I return a retrieval or cardGap one? I think cardGap
        //newlist

    }
    @Override
    public void onWindowFocusChanged (boolean hasFocus) {
        createDisplayDeck();
    }
    @Override
    public void onDestroy() {

        Intent resultIntent = new Intent();
        //TODO add extras or a date URI
        Bundle bundle = new Bundle();
        bundle.putSerializable("FINISHED_LIST", finishedList);
        resultIntent.putExtras(bundle);
        setResult(Activity.RESULT_OK, resultIntent);

        super.onDestroy();
    }
}
