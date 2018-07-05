package com.example.joshua.koreancards;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;


public class MainActivity extends Activity {
    private final String DEBUG_TAG = "DEBUG_TAG";
    private Button studyButton, dataButton, dictionaryButton, settingsButton;
    private int wordIndex = 10;
    private int wordsPerDay = 20;
    private final String KOREAN_TABLE_NAME = "KoreanTable.tmp";
    private final String ENGLISH_TABLE_NAME = "EnglishTable.tmp";

    private final int STUDY_RESULT_CODE = 3;

    private FileOutputStream fos;
    private ObjectOutputStream oos;
    private FileInputStream fis;
    private ObjectInputStream ois;

    private Hashtable<Integer, String> koreanTable = new Hashtable<>();
    private Hashtable<Integer, String> englishTable = new Hashtable<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        studyButton=findViewById(R.id.StudyButton);
        dataButton=findViewById(R.id.DataButton);
        dictionaryButton=findViewById(R.id.DictionaryButton);
        settingsButton=findViewById(R.id.SettingsButton);
        createKoreanTable(KOREAN_TABLE_NAME);
        Log.d(DEBUG_TAG, Integer.toString(koreanTable.size()));
        createEnglishTable(ENGLISH_TABLE_NAME);
        Log.d(DEBUG_TAG, Integer.toString(englishTable.size()));
        studyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i=0;i<5667;i++) {
                    Log.d(DEBUG_TAG, i + "\t" + koreanTable.get(i) + "\t" + englishTable.get(i) + "\t");
                }
                ArrayList<Cell> bundleList = new ArrayList<>();
                for(int i=1;i<21;i++) {
                    bundleList.add(new Cell(i, koreanTable.get(i), englishTable.get(i)));
                    Log.d(DEBUG_TAG, koreanTable.get(i) + "\t" + englishTable.get(i));
                }
                Intent intent = new Intent(MainActivity.this, StudyActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("NEW_WORDS", bundleList);
                intent.putExtras(bundle);
//                newWordsList.addAll((ArrayList<Cell>) bundle.getSerializable("NEW_WORDS")
//                intent.putExtra("ENGLISH", englishWords(wordIndex, wordsPerDay));
//                intent.putExtra("KOREAN", koreanWords(wordIndex, wordsPerDay));
                MainActivity.this.startActivityForResult(intent, STUDY_RESULT_CODE);
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
            Log.d(DEBUG_TAG, Integer.toString(koreanTable.size()));
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
            Log.d(DEBUG_TAG, Integer.toString(englishTable.size()));
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
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case(STUDY_RESULT_CODE) : {
                //deserialze  data
                if(resultCode== Activity.RESULT_OK) {
                    ArrayList<Cell> returnWords = new ArrayList<>();
                    returnWords.addAll((ArrayList<Cell>)data.getSerializableExtra("FINISHED_LIST"));
                    for(int i=0;i<returnWords.size();i++) {
                        Log.d(DEBUG_TAG, i + "\t" + returnWords.get(i).getKorean() + "\t" + returnWords.get(i) + "\t");
                    }
                }
            }
        }
    }
}
