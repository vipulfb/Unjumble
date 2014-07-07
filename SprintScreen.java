package com.vipulfb.Unjumble;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.*;
import com.crittercism.app.Crittercism;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.Timer;
import java.util.TimerTask;

public class SprintScreen extends Activity implements View.OnClickListener {

    int timerCount, timerCursorCount;
    boolean isSkipWordClicked, isShowFirstLetterClicked;
    boolean isResetChangedToNextWord;
    String randomChar, currentWord, wordTemp, wordHint, wordKnownNextLetterClicked;
    Integer currentWordLength, row, lengthTemp,currentScoreValue;
    TextView tvDisplay,tvCursor,tvBubbles,tvTimer,tvScore,tvMeaning,tvScoreAnimate;
    ImageView ivMeaningImage,ivMarathonSprint;
    Button bReset, bSkipWord, bKnowFirstLetter, bBackspace;
    TableLayout tableJumbledButtons;
    Button[] jumbledCharButtonList;
    private SprintGame mSprintGame;
    private SoundMusicVibration mSoundMusicVibration;
    private Timer myTimer, myTimer1;
    ProgressBar progressBarTop;
    int screenWidth, screenHeight, jumbledButtonDimension, nextLetterTimer_Value, nextLetterPosition;
    MediaPlayer[] letter_tap;
    MediaPlayer game_start,bubble_hint_click,
            reset_click, next_click, skip_click;
    Tracker t;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        mSprintGame = new SprintGame(this.getApplicationContext());
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
        setContentView(R.layout.gamescreen);

        try {
            // Get tracker.
            t = ((MyApplication) getApplication()).getTracker(
                    MyApplication.TrackerName.APP_TRACKER);
        }catch (Exception e){
            Crittercism.logHandledException(e);
        }

        try {
            AdView adView = (AdView) findViewById(R .id.adViewGame);
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            adView.loadAd(adRequest);
        }catch (Exception e){
            Crittercism.logHandledException(e);
        }

        if (mSoundMusicVibration.isSoundActive()){
            game_start = MediaPlayer.create(getApplicationContext(),R.raw.game_start);
            game_start.start();
        }
        bubble_hint_click = MediaPlayer.create(getApplicationContext(),R.raw.bubble_hint_click);
        reset_click = MediaPlayer.create(getApplicationContext(),R.raw.reset_click);
        next_click = MediaPlayer.create(getApplicationContext(),R.raw.next_click);
        skip_click = MediaPlayer.create(getApplicationContext(),R.raw.skip_click);

        newWord();
        //Timer Code
        initializeTimer();
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

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_sprint_actionbar,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.newGame:
                showDialogBoxNewGame();
                break;
            case R.id.endGame:
                showDialogBoxBackButtonPressed();
                break;
            case R.id.setting:
                startActivity(new Intent(this,Preference.class));
                break;
            case R.id.instructions:
                startActivity(new Intent(this,Instructions.class));
                break;
        }

        return true;
    }

    private void startNewSprintGame() {
        mSprintGame.updateCurrentScore(0);

        myTimer.cancel();
        myTimer1.cancel();
        tvCursor.setText(" ");
        startActivity(new Intent("com.vipulfb.Unjumble.SprintScreen"));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void initializeTimer() {
        timerCount = Constants.TIMER_HIGH_VALUE_SPRINT;
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
            String value = Integer.toString(timerCount);
            tvTimer.setText(value);
            progressBarTop.setProgress(Constants.TIMER_HIGH_VALUE_SPRINT - timerCount);
            if(timerCount <= Constants.TIMER_ALERT_VALUE_SPRINT)
            {tvTimer.setTextColor(Constants.COLOR_RED);}

            if (tvDisplay.getText().toString().equals(currentWord))
                 isShowFirstLetterClicked = false;

            if (isShowFirstLetterClicked){
                bKnowFirstLetter.setText(String.valueOf(nextLetterTimer_Value));

                if (--nextLetterTimer_Value < 0 ){
                    isShowFirstLetterClicked=false;
                    bKnowFirstLetter.setText("");
                    setEnabledDisabledHintButtons();
                }

            }

            if(timerCount-- <= 0)
            {myTimer.cancel();
                myTimer1.cancel();
                tvCursor.setText(" ");

                t.send(new HitBuilders.EventBuilder()
                        .setCategory("Sprint time up")
                        .setAction("condition")
                        .build());
                //show dialog box to show time up
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

    private void showScore() {
        ShowScore.isSprintMode=true;
        myTimer.cancel();
        myTimer1.cancel();
        tvCursor.setText(" ");
        startActivity(new Intent("com.vipulfb.Unjumble.ShowScore"));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    private void initializeNewWord() {
        String wordLine = mSprintGame.getRandomWordLine();
        int wordHintSeparatorPosition = wordLine.indexOf(" ");
        wordTemp = currentWord = wordLine.substring(0,wordHintSeparatorPosition).trim().toUpperCase();
        wordHint = wordLine.substring(wordHintSeparatorPosition).trim();
        lengthTemp = currentWordLength = currentWord.length();
        jumbledButtonDimension = Math.min(screenWidth,screenHeight)/(Constants.WORD_MAX_LENGTH-2);

        // buttons representing string
        generateJumbledWordButtons();

        //Showing Word Meaning
        tvMeaning.setText(wordHint);
        ivMeaningImage.setVisibility(View.VISIBLE);
        tvMeaning.startAnimation(Constants.ANIMATION.fade_in2(this));
        ivMeaningImage.startAnimation(Constants.ANIMATION.fade_in2(this));
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
        for (row = 0; row < currentWordLength; row++) {
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
        int rnd = mSprintGame.randomNumber(lengthTemp, 1);
        randomChar = wordTemp.substring(rnd - 1, rnd);
        wordTemp = wordTemp.substring(0, rnd - 1) + wordTemp.substring(rnd, lengthTemp--);
    }

    public void tableButtonClicked(String text) {
        bReset.setEnabled(true);
        bReset.setBackgroundResource(R.drawable.reset_enabled);
        tvDisplay.setTextColor(Constants.COLOR_WHITE);
        tvDisplay.setText(tvDisplay.getText().toString() + text);

        if (tvDisplay.getText().toString().equals(currentWord)) {
            discloseCompleteWord();
        }
    }

    private void discloseCompleteWord() {   //when the full currentWord is known
        bReset.startAnimation(Constants.ANIMATION.fade_out(this));
        bReset.setBackgroundResource(R.drawable.next_word);
        bReset.startAnimation(Constants.ANIMATION.fade_in(this));
        isResetChangedToNextWord=true;
        bReset.setEnabled(true);
        if (mSoundMusicVibration.isVibrationActive()) mSoundMusicVibration.vibrate.vibrate(500);

        //Disabling elements
        for (int i = 0; i < currentWordLength; i++) {
            jumbledCharButtonList[i].setEnabled(false);
            jumbledCharButtonList[i].setTextColor(Constants.COLOR_LIGHT_GREY);
        }
        isSkipWordClicked=true;
        isShowFirstLetterClicked=true;
        setEnabledDisabledHintButtons();

        tvDisplay.setTextColor(Constants.COLOR_GREEN);
        tvDisplay.setText(currentWord);
        tvCursor.setText(" ");

        currentScoreValue=mSprintGame.getCurrentScore();
        if (++currentScoreValue % Constants.SCORE_THREE_BUBBLE_GENERATE == 0){
            Toast.makeText(this, R.string.toastThreeMoreBubbles, Toast.LENGTH_SHORT).show();
            mSprintGame.updateBubbleCount(3,false); //Add 3 to bubble count as prize
            tvBubbles.setText(String.valueOf(mSprintGame.getBubbleCount()));
        }
        tvScoreAnimate.setText("+1");
        tvScoreAnimate.startAnimation(Constants.ANIMATION.slide_up(this));
        tvScore.setText("Score : " + String.valueOf(currentScoreValue));
        mSprintGame.updateCurrentScore(currentScoreValue);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bReset:
                if (isResetChangedToNextWord) {
                    if (mSoundMusicVibration.isSoundActive())next_click.start();
                    tableJumbledButtons.startAnimation(Constants.ANIMATION.fade_out(this));
                    newWord();
                }else {
                    if (mSoundMusicVibration.isSoundActive())reset_click.start();
                    bReset.setEnabled(false);
                    bReset.startAnimation(Constants.ANIMATION.fade_out(this));
                    bReset.setBackgroundResource(R.drawable.reset_disabled);
                    bReset.startAnimation(Constants.ANIMATION.fade_in(this));
                    tvDisplay.setTextColor(Constants.COLOR_WHITE);
                    tvDisplay.setText(wordKnownNextLetterClicked);
                    jumbledButtonEnableDisable();
                }

                break;
            case R.id.buttonShowCompleteWord:
                if (mSoundMusicVibration.isSoundActive())skip_click.start();
                onSkipWordClicked();
                break;
            case R.id.buttonKnowFirstLetter:
                onKnowFirstLetterClick();
                break;
            case R.id.bBackspace:
                onBackspaceClicked();
                break;
        }
    }

    private void onSkipWordClicked() {
        tableJumbledButtons.startAnimation(Constants.ANIMATION.fade_out(this));
        newWord();
    }

    private void onBackspaceClicked() {
        String displayText =  tvDisplay.getText().toString();
        int len = displayText.length();
        //if tvDisplay is (blank or known word by next letter clicked) or current word is guessed, then no operation
        if (len != 0 && !displayText.equals(currentWord) && !displayText.equals(wordKnownNextLetterClicked)){
            if(len == (wordKnownNextLetterClicked.length()+1)){bReset.setBackgroundResource(R.drawable.reset_disabled);}
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

    private void onKnowFirstLetterClick() {  //on clicking Know first letter
        if (mSoundMusicVibration.isSoundActive())bubble_hint_click.start();
        mSprintGame.updateBubbleCount(Constants.FIRST_WORD_HINT_PENALTY, true);
        tvBubbles.setText(String.valueOf(mSprintGame.getBubbleCount()));

        //Knowing next letter
        tvDisplay.setTextColor(Constants.COLOR_WHITE);
        wordKnownNextLetterClicked = currentWord.substring(0, ++nextLetterPosition);
        tvDisplay.setText(wordKnownNextLetterClicked);

        if (tvDisplay.getText().toString().equals(currentWord)) {
            discloseCompleteWord();
        }else{

            nextLetterTimer_Value = Constants.TIMER_NEXT_LETTER_SPRINT;
            bKnowFirstLetter.setText(String.valueOf(nextLetterTimer_Value));

            isShowFirstLetterClicked=true;
            setEnabledDisabledHintButtons();

            jumbledButtonEnableDisable();

        }

    }

    private void jumbledButtonEnableDisable() {
        for (int i=0; i<currentWordLength; i++){
            jumbledCharButtonList[i].setEnabled(true);
            jumbledCharButtonList[i].setTextColor(Constants.COLOR_WHITE);
        } //for loop

        for (int j=0; j<wordKnownNextLetterClicked.length(); j++){
            String chr = wordKnownNextLetterClicked.substring(j,j+1);
            for (int i = 0; i < currentWordLength; i++) {
                if (jumbledCharButtonList[i].getText().equals(chr) &&jumbledCharButtonList[i].isEnabled()) {
                    jumbledCharButtonList[i].setEnabled(false);
                    jumbledCharButtonList[i].setTextColor(Constants.COLOR_LIGHT_GREY);
                    i=currentWordLength;
                }

            } //for loop
        } //for loop

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

    private void showDialogBoxNewGame() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(R.string.dialogTitle);

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.dialogNewGameMessage)
//                .setCancelable(false)
                .setPositiveButton(R.string.dialogYes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, start a new game
                        startNewSprintGame();
                    }
                })
                .setNegativeButton(R.string.dialogNo,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, cancel the dialog
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void showDialogBoxBackButtonPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(R.string.dialogTitle);

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.dialogBackMessage)
//                .setCancelable(false)
                .setPositiveButton(R.string.dialogYes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, go to score show activity
                        showScore();
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

        //String message = Constants.dialogTimerUpMessage + currentWord;
        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.dialogTimerUpMessage)
                .setCancelable(false)
                .setPositiveButton(R.string.dialogOk,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, go to score show activity
                        dialog.cancel();
                        showScore();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void showDialogWordsOver() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(R.string.dialogTitle);

        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.dialogGameOverMessage)
                .setCancelable(false)
                .setPositiveButton(R.string.dialogOk,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, go to score show activity
                        dialog.cancel();
                        showScore();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void initialCustomisation() {
        // If word list is over
        if (mSprintGame.isWordsFinished){
            mSprintGame.isWordsFinished = false;
            showDialogWordsOver();
        }

        nextLetterPosition = 0;
        isSkipWordClicked=false;
        isShowFirstLetterClicked=false;
        wordKnownNextLetterClicked = "";

        timerCursorCount = 0;
        //Getting screen width and height
        DisplayMetrics display = this.getResources().getDisplayMetrics();

        screenWidth = display.widthPixels;
        screenHeight = display.heightPixels;

        //Customising bKMW- Know my word------Skip Word
        bSkipWord = (Button) findViewById(R.id.buttonShowCompleteWord);
        bSkipWord.setOnClickListener(this);

        //Customising bKFL- Know first letter
        bKnowFirstLetter = (Button) findViewById(R.id.buttonKnowFirstLetter);
        bKnowFirstLetter.setOnClickListener(this);
        bKnowFirstLetter.setText("");

        //Setting enabled or disabled hint buttons
        setEnabledDisabledHintButtons();

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
        tvTimer.setTypeface(Constants.TYPEFACE.Arial(this));

        // Customising and populating tvBubbles
        tvBubbles = (TextView) findViewById(R.id.tvBubbles);
        tvBubbles.setText(String.valueOf(mSprintGame.getBubbleCount()));
        tvBubbles.setTextColor(Constants.COLOR_WHITE);
        tvBubbles.setTypeface(Constants.TYPEFACE.Arial(this));

        // Customising and populating tvScore
        tvScore = (TextView) findViewById(R.id.tvScore);
        tvScore.setText("Score : " + String.valueOf(mSprintGame.getCurrentScore()));
        tvScore.setTextColor(Constants.COLOR_WHITE);
        tvScore.setTypeface(Constants.TYPEFACE.Arial(this));

        //Customising tvScoreAnimate
        tvScoreAnimate = (TextView) findViewById(R.id.tvScoreAnimate);
        tvScoreAnimate.setTypeface(Constants.TYPEFACE.Arial(this));

        //Customising tvMeaning
        tvMeaning = (TextView) findViewById(R.id.tvMeaning);
        tvMeaning.setText("");
        tvMeaning.setTypeface(Constants.TYPEFACE.Arial(this));
        tvMeaning.setTextColor(Constants.COLOR_BLUE_TEXT);

        //Customising ivMeaningImage
        ivMeaningImage = (ImageView) findViewById(R.id.ivMeaningImage);
        ivMeaningImage.setVisibility(View.INVISIBLE);

        //Customising ivMarathonSprint
        ivMarathonSprint = (ImageView) findViewById(R.id.ivMarathonSprint);
        ivMarathonSprint.setBackgroundResource(R.drawable.sprint);

        tableJumbledButtons = (TableLayout) findViewById(R.id.tblJumbledButtons);

        progressBarTop = (ProgressBar) findViewById(R.id.progressbar);
        progressBarTop.setBackgroundColor(Constants.COLOR_WHITE);
        progressBarTop.setMax(Constants.TIMER_HIGH_VALUE_SPRINT);

    }

    private void setEnabledDisabledHintButtons() {
        if (isSkipWordClicked){
            bSkipWord.setBackgroundResource(R.drawable.skip_disabled);
            bSkipWord.setEnabled(false);
        }else{
                bSkipWord.setBackgroundResource(R.drawable.skip_enabled);
                bSkipWord.setEnabled(true);
        }

        if (isShowFirstLetterClicked){

            if (bKnowFirstLetter.getText().toString().equals(""))
            {bKnowFirstLetter.setBackgroundResource(R.drawable.one_bubble_disabled);}
            else {bKnowFirstLetter.setBackgroundResource(R.drawable.one_bubble_blank);}

            bKnowFirstLetter.setEnabled(false);
        }else{
            if (mSprintGame.canKnowFirstLetter()){
                bKnowFirstLetter.setBackgroundResource(R.drawable.one_bubble_enabled);
                bKnowFirstLetter.setEnabled(true);
            }else{

                if (bKnowFirstLetter.getText().toString().equals(""))
                {bKnowFirstLetter.setBackgroundResource(R.drawable.one_bubble_disabled);}
                else {bKnowFirstLetter.setBackgroundResource(R.drawable.one_bubble_blank);}

                bKnowFirstLetter.setEnabled(false);
            }
        }

    }

}
