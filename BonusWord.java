package com.vipulfb.Unjumble;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.crittercism.app.Crittercism;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Timer;
import java.util.TimerTask;

public class BonusWord extends Activity implements View.OnClickListener {

    int timerCountBonus, timerCursorCount;
    boolean isResetChangedToNextWord;
    String randomChar, currentWord, wordTemp, wordHint;
    Integer currentWordLength, lengthTemp;
    TextView tvDisplay, tvCursor, tvTimer, tvMeaning;
    ImageView ivMeaningImage;
    Button bReset, bSkipWord, bBackspace;
    TableLayout tableJumbledButtons;
    Button[] jumbledCharButtonList;
    private MarathonGame mMarathonGame;
    private SoundMusicVibration mSoundMusicVibration;
    private Timer myTimer, myTimer1;
    ProgressBar progressBarTop;
    int screenWidth, screenHeight, jumbledButtonDimension;
    MediaPlayer[] letter_tap;
    MediaPlayer bonus_start,
            reset_click, next_click, skip_click;
    Tracker t;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mMarathonGame = new MarathonGame(this.getApplicationContext());
        mSoundMusicVibration = new SoundMusicVibration(this.getApplicationContext());

        if (mSoundMusicVibration.isOrientationActive()){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }else{
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bonusword);

        try {
            // Get tracker.
            t = ((MyApplication) getApplication()).getTracker(
                    MyApplication.TrackerName.APP_TRACKER);
        }catch (Exception e){
            Crittercism.logHandledException(e);
        }

        try {
            AdView adView = (AdView) findViewById(R .id.adViewBonus);
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            adView.loadAd(adRequest);
        }catch (Exception e){
            Crittercism.logHandledException(e);
        }

        if (mSoundMusicVibration.isSoundActive()){
            bonus_start = MediaPlayer.create(getApplicationContext(),R.raw.game_start);
            bonus_start.start();
        }
        reset_click = MediaPlayer.create(getApplicationContext(),R.raw.reset_click);
        next_click = MediaPlayer.create(getApplicationContext(),R.raw.next_click);
        skip_click = MediaPlayer.create(getApplicationContext(),R.raw.skip_click);

        newWord();

    }

    @Override
    public void onStart(){
        super.onStart();
        //Stop the analytics tracking
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    public void onStop(){
        super.onStop();
        //Stop the analytics tracking
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    private void newWord() {

        //Customising Buttons and Text Views
        initialCustomisation();

        initializeNewWord();

        //Timer Code
        initializeTimer();

    }

    private void initializeTimer() {
        timerCountBonus = Constants.TIMER_HIGH_VALUE_BONUS;
        timerCursorCount = 0;
        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                timerMethod();
            }
        }, 0, 1000);
        myTimer1 = new Timer();
        myTimer1.schedule(new TimerTask() {
            @Override
            public void run() {
                timerMethod1();
            }
        }, 0, 300);
    }

    private void timerMethod()
    {
        //This method is called directly by the timer
        //and runs in the same thread as the timer.

        //We call the method that will work with the UI
        //through the runOnUiThread method.
        this.runOnUiThread(Timer_Tick);
    }

    private void timerMethod1()
    {
        //This method is called directly by the timer
        //and runs in the same thread as the timer.

        //We call the method that will work with the UI
        //through the runOnUiThread method.
        this.runOnUiThread(Timer_Tick1);
    }

    private Runnable Timer_Tick = new Runnable() {
        public void run() {
            String value = Integer.toString(timerCountBonus);
            tvTimer.setText(value);
            progressBarTop.setProgress(Constants.TIMER_HIGH_VALUE_BONUS - timerCountBonus);
            if(timerCountBonus <= Constants.TIMER_ALERT_VALUE_MARATHON)
            {tvTimer.setTextColor(Constants.COLOR_RED);}

            if(timerCountBonus-- <= 0)
            {myTimer.cancel();
                myTimer1.cancel();
                tvCursor.setText(" ");
                showDialogTimeUp();
            }
        }
    };


    private Runnable Timer_Tick1 = new Runnable() {
        public void run() {
            if (tvDisplay.getText().toString().length()==currentWordLength){
                tvCursor.setText(" ");
                if (!tvDisplay.getText().toString().equals(currentWord))
                {
                    if (timerCursorCount%2==0){
                        tvDisplay.setTextColor(Constants.COLOR_RED);
                    }else{
                        tvDisplay.setTextColor(Constants.COLOR_WHITE);
                    }

                }

            }else{
                if (timerCursorCount%2==0){
                    tvCursor.setText("_");
                }else{
                    tvCursor.setText(" ");
                }
            }

            timerCursorCount++;
        }
    };

    private void initializeNewWord() {
        String wordLine = mMarathonGame.getRandomWordLine();
        int wordHintSeparatorPosition = wordLine.indexOf(" ");
        wordTemp = currentWord = wordLine.substring(0,wordHintSeparatorPosition).trim().toUpperCase();
        wordHint = wordLine.substring(wordHintSeparatorPosition).trim();
        tvMeaning.setText(wordHint);
        lengthTemp = currentWordLength = currentWord.length();
        jumbledButtonDimension = Math.min(screenWidth,screenHeight)/(Constants.WORD_MAX_LENGTH-2);

        // buttons representing string
        generateJumbledWordButtons();
    }

    public void generateJumbledWordButtons() {     // buttons representing string
        tableJumbledButtons.startAnimation(Constants.ANIMATION.fade_in(this));
        //to delete previous table view if Any
        tableJumbledButtons.removeAllViews();
        TableRow tableRow = new TableRow(this);
        tableRow.setLayoutParams(new TableLayout .LayoutParams(
                TableLayout.LayoutParams.MATCH_PARENT,
                TableLayout.LayoutParams.MATCH_PARENT,
                1.0f));
        tableJumbledButtons.addView(tableRow);

        letter_tap = new MediaPlayer[currentWordLength];
        jumbledCharButtonList = new Button[currentWordLength];
        for (int row = 0; row < currentWordLength; row++) {
            letter_tap[row] = MediaPlayer.create(getApplicationContext(),R.raw.letter_tap);
            jumbledCharButtonList[row] = new Button(this);
            jumbledCharButtonList[row].setLayoutParams(new TableRow.LayoutParams(
                    jumbledButtonDimension,
                    jumbledButtonDimension,
                    1.0f));
            getChar(); //get single random character from string in uppercase
            final int IDD = row;
            jumbledCharButtonList[row].setSoundEffectsEnabled(false);
            jumbledCharButtonList[row].setTypeface(Constants.TYPEFACE.Arial(this));
            jumbledCharButtonList[row].setText(randomChar);
            jumbledCharButtonList[row].setTextColor(Constants.COLOR_WHITE);
            jumbledCharButtonList[row].setPadding(0, 0, 0, 0);
            jumbledCharButtonList[row].setBackgroundResource(R.drawable.jumbled_btn_bg);
            jumbledCharButtonList[row].setTextSize(24);
            jumbledCharButtonList[row].startAnimation(Constants.ANIMATION.fade_in(this));

            final Button[] BTN = jumbledCharButtonList;
            BTN[row].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BTN[IDD].setEnabled(false);
                    BTN[IDD].setTextColor(Constants.COLOR_LIGHT_GREY);
                    tableButtonClicked(BTN[IDD].getText().toString());
                    if (mSoundMusicVibration.isSoundActive())letter_tap[IDD].start();
                }
            }
            );
            tableRow.addView(jumbledCharButtonList[row]);

        } //for loop
    }

    private void getChar() {    //get single random character from string
        int rnd = mMarathonGame.randomNumber(lengthTemp, 1);
        randomChar = wordTemp.substring(rnd - 1, rnd);
        wordTemp = wordTemp.substring(0, rnd - 1) + wordTemp.substring(rnd, lengthTemp--);
    }

    public void tableButtonClicked(String text) {
        bReset.setEnabled(true);
        bReset.setBackgroundResource(R.drawable.reset_enabled);
        tvDisplay.setTextColor(Constants.COLOR_WHITE);
        tvDisplay.setText(tvDisplay.getText().toString() + text);
        String tt = tvDisplay.getText().toString();
        if (tt.equals(currentWord)) {
            discloseCompleteWord();
        }
    }

    private void discloseCompleteWord() {   //when the full currentWord is known
        t.send(new HitBuilders.EventBuilder()
                .setCategory("Bonus word known")
                .setAction("condition")
                .build());
        Toast.makeText(this, R.string.toastBonusWordKnown, Toast.LENGTH_SHORT).show();

        // Setting Reset button image to next
        bReset.startAnimation(Constants.ANIMATION.fade_out(this));
        bReset.setBackgroundResource(R.drawable.next_word);
        bReset.startAnimation(Constants.ANIMATION.fade_in(this));
        isResetChangedToNextWord=true;
        bReset.setEnabled(true);
        tvMeaning.setText(wordHint);
        ivMeaningImage.setVisibility(View.VISIBLE);
        if (mSoundMusicVibration.isVibrationActive()) mSoundMusicVibration.vibrate.vibrate(500);

        //Disabling elements
        for (int i = 0; i < currentWordLength; i++) {
            jumbledCharButtonList[i].setEnabled(false);
            jumbledCharButtonList[i].setTextColor(Constants.COLOR_LIGHT_GREY);
        }

        myTimer.cancel();
        myTimer1.cancel();
        tvDisplay.setTextColor(Constants.COLOR_GREEN);
        tvDisplay.setText(currentWord);
        tvCursor.setText(" ");

        mMarathonGame.updateCurrentScore(mMarathonGame.getCurrentScore() + Constants.BONUS_SCORE_ADD);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bReset:
                if (isResetChangedToNextWord) {
                    if (mSoundMusicVibration.isSoundActive())next_click.start();
                    tableJumbledButtons.startAnimation(Constants.ANIMATION.fade_out(this));
                    continueGame();
                } else {
                    if (mSoundMusicVibration.isSoundActive())reset_click.start();
                    bReset.setEnabled(false);
                    bReset.startAnimation(Constants.ANIMATION.fade_out(this));
                    bReset.setBackgroundResource(R.drawable.reset_disabled);
                    bReset.startAnimation(Constants.ANIMATION.fade_in(this));
                    tvDisplay.setTextColor(Constants.COLOR_WHITE);
                    tvDisplay.setText("");
                    for (int i = 0; i < currentWordLength; i++) {
                        jumbledCharButtonList[i].setEnabled(true);
                        jumbledCharButtonList[i].setTextColor(Constants.COLOR_WHITE);
                    }  //for loop
                }

                break;
            case R.id.buttonShowCompleteWord:
                if (mSoundMusicVibration.isSoundActive())skip_click.start();
                showDialogBoxBackButtonPressed();
                break;
            case R.id.bBackspace:
                onBackspaceClicked();
                break;

        }

    }

    private void onBackspaceClicked() {
        String displayText =  tvDisplay.getText().toString();
        int len = displayText.length();
        //if tvDisplay is blank or current word is guessed, then no operation
        if (len != 0 && !displayText.equals(currentWord)){
            if(len == 1){bReset.setBackgroundResource(R.drawable.reset_disabled);}
            tvCursor.setText("_");
            tvDisplay.setTextColor(Constants.COLOR_WHITE);
            tvDisplay.setText(displayText.substring(0,len-1));
            String lastChar = displayText.substring(len-1,len);

            for (int i = 0; i < currentWordLength; i++) {
                if (jumbledCharButtonList[i].getText().toString().equals(lastChar)
                        && !jumbledCharButtonList[i].isEnabled()){
                    jumbledCharButtonList[i].setEnabled(true);
                    jumbledCharButtonList[i].setTextColor(Constants.COLOR_WHITE);
                    if (mSoundMusicVibration.isSoundActive())letter_tap[i].start();
                    i=currentWordLength;
                }
            }  //for loop

        }
    }

    private void continueGame() {
        myTimer.cancel();
        myTimer1.cancel();
        tvCursor.setText(" ");
        startActivity(new Intent("com.vipulfb.Unjumble.MarathonScreen"));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_BACK:
                // if back is pressed
                showDialogBoxBackButtonPressed();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showDialogBoxBackButtonPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(R.string.dialogTitle);

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.dialogBackPressSkipBonusMessage)
//                .setCancelable(false)
                .setPositiveButton(R.string.dialogYes,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, go to score show activity
                        myTimer.cancel();
                        continueGame();
                    }
                })
                .setNegativeButton(R.string.dialogNo,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, cancel the dialog and go to score show activity
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void showDialogTimeUp() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(R.string.dialogTitle);

        String message = Constants.dialogTimerUpMessage + currentWord;
        // set dialog message
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.dialogOk,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, go to score show activity
                        dialog.cancel();
                        continueGame();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void initialCustomisation() {
        //Getting screen width and height
        DisplayMetrics display = this.getResources().getDisplayMetrics();

        screenWidth = display.widthPixels;
        screenHeight = display.heightPixels;

        //Customising bKMW- Know my word------Skip Word
        bSkipWord = (Button) findViewById(R.id.buttonShowCompleteWord);
        bSkipWord.setBackgroundResource(R.drawable.skip_enabled);
        bSkipWord.setOnClickListener(this);

        isResetChangedToNextWord=false;

        //Customising bReset
        bReset = (Button) findViewById(R.id.bReset);
        bReset.setEnabled(false);
        bReset.startAnimation(Constants.ANIMATION.fade_out(this));
        bReset.setBackgroundResource(R.drawable.reset_disabled);
        bReset.startAnimation(Constants.ANIMATION.fade_in(this));
        bReset.setOnClickListener(this);

        //Customising bBackspace
        bBackspace = (Button) findViewById(R.id.bBackspace);
        bBackspace.setOnClickListener(this);

        //Customising tvDisplay
        tvDisplay = (TextView) findViewById(R.id.tvDisplay);
        tvDisplay.setText("");
        tvDisplay.setTextColor(Constants.COLOR_WHITE);
        tvDisplay.setTypeface(Constants.TYPEFACE.Arial(this));

        //Customising tvCursor
        tvCursor = (TextView) findViewById(R.id.tvCursor);
        tvCursor.setTextColor(Constants.COLOR_WHITE);
        tvDisplay.setTypeface(Constants.TYPEFACE.Arial(this));
        // Customising and populating tvTimer

        tvTimer = (TextView) findViewById(R.id.tvTimer);
        tvTimer.setTextColor(Constants.COLOR_WHITE);
        tvTimer.setText(String.valueOf(Constants.TIMER_HIGH_VALUE_BONUS));
        tvTimer.setTypeface(Constants.TYPEFACE.Arial(this));
        //Customising tvMeaning

        tvMeaning = (TextView) findViewById(R.id.tvMeaning);
        tvMeaning.setText("");
        tvMeaning.setTypeface(Constants.TYPEFACE.Arial(this));
        tvMeaning.setTextColor(Constants.COLOR_BLUE_TEXT);

        //Customising ivMeaningImage
        ivMeaningImage = (ImageView) findViewById(R.id.ivMeaningImage);
        ivMeaningImage.setVisibility(View.VISIBLE);


        tableJumbledButtons = (TableLayout) findViewById(R.id.tblJumbledButtons);

        progressBarTop = (ProgressBar) findViewById(R.id.progressbar);
        progressBarTop.setBackgroundColor(Constants.COLOR_WHITE);
        progressBarTop.setMax(Constants.TIMER_HIGH_VALUE_BONUS);

    }

}
