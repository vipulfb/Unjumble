package com.vipulfb.Unjumble;

import android.content.Context;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

public class MarathonGame {
    static ArrayList<String> wordListArray = new ArrayList<String>();
    Context mContext;
    boolean isWordsFinished;

    MarathonGame(Context context) {
        mContext = context;
    }

    int getBubbleCount() {
        return mContext.getSharedPreferences(Constants.COMMON_SHARED_PREF, Context.MODE_PRIVATE).
               getInt(Constants.MARATHON_BUBBLE_COUNT_KEY, 1);
        }

    void updateBubbleCount(int bubbles, boolean isPenalty) {
           int bubbleCount;
        if (isPenalty)
            bubbleCount = getBubbleCount() - bubbles;
        else
            bubbleCount = getBubbleCount() + bubbles;
        UpdateSharedPrefInt(Constants.MARATHON_BUBBLE_COUNT_KEY, bubbleCount);
    }

    int getCurrentScore() {
        return mContext.getSharedPreferences(Constants.COMMON_SHARED_PREF, Context.MODE_PRIVATE).
                getInt(Constants.MARATHON_CURRENT_SCORE_KEY, 1);
    }

    void updateConsecutiveWordKnownCount(int value) {
        UpdateSharedPrefInt(Constants.CONSECUTIVE_WORD_KNOWN_COUNT_KEY, value);
    }

    int getConsecutiveWordKnownCount() {
        return mContext.getSharedPreferences(Constants.COMMON_SHARED_PREF, Context.MODE_PRIVATE).
                getInt(Constants.CONSECUTIVE_WORD_KNOWN_COUNT_KEY, 1);
    }

    void updateCurrentScore(int score) {
        UpdateSharedPrefInt(Constants.MARATHON_CURRENT_SCORE_KEY, score);
    }


    int getHighScore() {
        return mContext.getSharedPreferences(Constants.COMMON_SHARED_PREF, Context.MODE_PRIVATE).
                getInt(Constants.MARATHON_HIGH_SCORE_KEY, 1);
    }

    void updateHighScore(int score) {
        UpdateSharedPrefInt(Constants.MARATHON_HIGH_SCORE_KEY, score);
    }

    void UpdateSharedPrefInt(String key, int value) {
        mContext.getSharedPreferences(Constants.COMMON_SHARED_PREF, Context.MODE_PRIVATE).
                edit().putInt(key, value).commit();
    }

    boolean canKnowCompleteWord(){
        return getBubbleCount() >= Constants.FULL_WORD_PENALTY;
    }

    boolean canKnowFirstLetter(){
        return getBubbleCount() >= Constants.FIRST_WORD_HINT_PENALTY;
    }

    String getRandomWordLine() {  //extracts a currentWordLine from txt file randomly
        int wordListLength = wordListArray.size();
        if (wordListLength==2) isWordsFinished = true; // 1 as backup for bonus

        int rndNumber =randomNumber(wordListLength-1,0);
        String randomWord = wordListArray.get(rndNumber).trim();
        wordListArray.remove(rndNumber);
        return randomWord;
    }

    int randomNumber(int high, int low) { //generating random number from range
        Random r = new Random();
        return low + r.nextInt(high-low+1); //+1 if high is inclusive [low,high]
    }

    void initialiseWordList() {  //gets words as string from words.txt
        byte[] buffer = null;
        InputStream is;
        try {
            is = mContext.getResources().openRawResource(R.raw.words); //reading from raw folder
            //is = getAssets().open(words.txt); // reading from assets folder
            int size = is.available(); //size of the file in bytes
            buffer = new byte[size]; //declare the size of the byte array with size of the file
            is.read(buffer); //read file
            is.close(); //close file

        } catch (IOException e) {
            e.printStackTrace();

        }
        // Store text file data in the string variable
        String str_data = new String(buffer);
        String[] mWordList = str_data.split("\n");

        // Nullifying words which are not in allowed length range
        for (int index = 1; index < mWordList.length; index++) {
        //Starting from index 1 as 1st word not properly added with split function
            String line = mWordList[index];
            String word = line.substring(0,line.indexOf(" ")).trim();
            if (word.length() >= Constants.WORD_MIN_LENGTH && word.length() <= Constants.WORD_MAX_LENGTH){
                wordListArray.add(line);
            }
        } //for loop


    }

}
