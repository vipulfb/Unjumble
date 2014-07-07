package com.vipulfb.Unjumble;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.*;
import com.crittercism.app.Crittercism;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ShowScore extends BaseGameActivity implements View.OnClickListener {
    Button bHome,bShareScore,bLeaderBoard;
    TextView tvScore,tvHighScore,tvNew;
    ImageView ivGameMode;
    private MarathonGame mMarathonGame;
    private SprintGame mSprintGame;
    private SoundMusicVibration mSoundMusicVibration;
    public static Boolean isSprintMode;
    int tempCurrentScore;
    final int RC_UNUSED = 5001;
    MediaPlayer game_end;
    Tracker t;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showscore);

        try {
            // Get tracker.
            t = ((MyApplication) getApplication()).getTracker(
                    MyApplication.TrackerName.APP_TRACKER);
        }catch (Exception e){
            Crittercism.logHandledException(e);
        }

        mMarathonGame = new MarathonGame(this.getApplicationContext());
        mSprintGame = new SprintGame(this.getApplicationContext());
        mSoundMusicVibration = new SoundMusicVibration(this.getApplicationContext());

        try {
            AdView adView = (AdView) findViewById(R .id.adViewScore);
            AdRequest adRequest = new AdRequest.Builder()
                    .build();
            adView.loadAd(adRequest);
        }catch (Exception e){
            Crittercism.logHandledException(e);
        }

        if (mSoundMusicVibration.isSoundActive()){
            game_end = MediaPlayer.create(getApplicationContext(),R.raw.game_end);
            game_end.start();
        }
        bHome = (Button) findViewById(R.id.bHome);
        bHome.setOnClickListener(this);

        tvNew = (TextView) findViewById(R.id.tvNew);
        ivGameMode = (ImageView) findViewById(R.id.ivGameMode);

        tvScore=(TextView) findViewById((R.id.tvScore));
        tvHighScore=(TextView) findViewById((R.id.tvHighScore));
        bShareScore = (Button) findViewById(R.id.bShareScore);
        bShareScore.setOnClickListener(this);

        bLeaderBoard = (Button) findViewById(R.id.bLeaderBoard);
        bLeaderBoard.setOnClickListener(this);

        if(isSprintMode){
            ivGameMode.setBackgroundResource(R.drawable.sprint);
            tempCurrentScore = mSprintGame.getCurrentScore();
            if (tempCurrentScore > mSprintGame.getHighScore()){
                tvNew.setText(Constants.newString);
                tvNew.setVisibility(1);
                Toast.makeText(this, R.string.toastHighScoreAchieved , Toast.LENGTH_SHORT).show();
                mSprintGame.updateHighScore(tempCurrentScore);
            }
            tvHighScore.setText(Constants.highScoreString + String.valueOf(mSprintGame.getHighScore()));

        }else{
            ivGameMode.setBackgroundResource(R.drawable.marathon);
            tempCurrentScore = mMarathonGame.getCurrentScore();
            if (tempCurrentScore > mMarathonGame.getHighScore()){
                tvNew.setText(Constants.newString);
                tvNew.setVisibility(1);
                Toast.makeText(this, R.string.toastHighScoreAchieved , Toast.LENGTH_SHORT).show();
                mMarathonGame.updateHighScore(tempCurrentScore);
            }
            tvHighScore.setText(Constants.highScoreString + String.valueOf(mMarathonGame.getHighScore()));

            //Updating Marathon current score to 0
            mMarathonGame.updateCurrentScore(0);
        }
        updateAnalyticsScoreUpdate(tempCurrentScore);
        tvScore.setText(Constants.youScoredString + String.valueOf(String.valueOf(tempCurrentScore)));

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

    @Override
    public void onSignInSucceeded() {

        updateScoreLeaderBoard(tempCurrentScore);
    }

    private void updateScoreLeaderBoard(int score) {
        if(isSprintMode){
            if(getApiClient().isConnected()){
                Games.Leaderboards.submitScore(getApiClient(),
                        getString(R.string.leaderboard_sprint),
                        score);
            }
        }else{
            if(getApiClient().isConnected()){
                Games.Leaderboards.submitScore(getApiClient(),
                        getString(R.string.leaderboard_marathon),
                        score);
            }
        }
    }
    @Override
    public void onSignInFailed() {
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bHome:
                showDialogBoxBackButtonPressed();
                break;

            case R.id.bShareScore:
                t.send(new HitBuilders.EventBuilder()
                        .setCategory("Share score click")
                        .setAction("click")
                        .build());
                shareAsScreenShot();
                break;

            case R.id.bLeaderBoard:
                onShowLeaderBoardsRequested();
                break;

        }
    }

    private void onShowLeaderBoardsRequested() {
        if (isSignedIn()) {

            if(isSprintMode){
                startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
                        getApiClient(), getString(R.string.leaderboard_sprint)),
                        2);
            }else{
                startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
                        getApiClient(), getString(R.string.leaderboard_marathon)),
                        2);
            }

        } else {

            beginUserInitiatedSignIn();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_BACK:
                // if back is pressed, finish the activity
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
                .setMessage(R.string.dialogConfirmExit)
//                .setCancelable(false)
                .setPositiveButton(R.string.dialogExit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, go to score show activity
                        finish();
                    }
                })
                .setNegativeButton(R.string.dialogNewGame,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, cancel the dialog and go to score show activity
                        startNewGame();
                    }
                })
                .setNeutralButton(R.string.dialogHome,new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, finish current activity and go to home activity

//                        t.send(new HitBuilders.EventBuilder()
//                                .setCategory("Rate")
//                                .setAction("click")
//                                .setLabel("Rate")
//                                .build());

                        showHomePage();
                    }
                })
        ;


        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void startNewGame() {
        if(isSprintMode){
            //Start Sprint Game
            mSprintGame.initialiseWordList();
            mSprintGame.updateCurrentScore(0);
            mSprintGame.UpdateSharedPrefInt(Constants.SPRINT_BUBBLE_COUNT_KEY,Constants.SPRINT_BUBBLE_COUNT);
            startActivity(new Intent("com.vipulfb.Unjumble.SprintScreen"));
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            finish();
        }else{
            //Start Marathon Game
            mMarathonGame.initialiseWordList();
            startActivity(new Intent("com.vipulfb.Unjumble.MarathonScreen"));
            overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
            finish();
        }





    }

    private void showHomePage() {
        startActivity(new Intent(this,HomeScreen.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }


    private void shareAsScreenShot() {
        String strFileLocation
                = MediaStore.Images.Media.insertImage(getContentResolver(), takeScreenShot(), "UnJumbleScore", null);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.setType("image/*");
        sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(strFileLocation));
        startActivity(Intent.createChooser(sendIntent, "Share your score"));

    }

    private Bitmap takeScreenShot()
    {
        View rootView = findViewById(android.R.id.content).getRootView();
        rootView.setDrawingCacheEnabled(true);
        return rootView.getDrawingCache();
    }

    private void updateAnalyticsScoreUpdate(int score) {
         if (isSprintMode){
//Sprint score (5-15]
             if (score >=5 && score <15)
                 t.send(new HitBuilders.EventBuilder().setCategory("Sprint score 5-15").setAction("condition").build());
//Sprint score (15-20]
             if (score >=15 && score <20)
                 t.send(new HitBuilders.EventBuilder().setCategory("Sprint score 15-20").setAction("condition").build());
//Sprint score (20-30]
             if (score >=20 && score <30)
                 t.send(new HitBuilders.EventBuilder().setCategory("Sprint score 20-30").setAction("condition").build());
//Sprint score >30
             if (score >=30)
                 t.send(new HitBuilders.EventBuilder().setCategory("Sprint score 30").setAction("condition").build());
         }else{
//Marathon score (50-100]
             if (score >=50 && score <100)
                 t.send(new HitBuilders.EventBuilder().setCategory("Marathon score 50-100").setAction("condition").build());
//Marathon score (100-150]
             if (score >=100 && score <150)
                 t.send(new HitBuilders.EventBuilder().setCategory("Marathon score 100-150").setAction("condition").build());
//Marathon score >150
             if (score >=150)
                 t.send(new HitBuilders.EventBuilder().setCategory("Marathon score 150").setAction("condition").build());
         }

    }


}
