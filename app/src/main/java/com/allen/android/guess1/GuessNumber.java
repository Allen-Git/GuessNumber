package com.allen.android.guess1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

//import com.google.ads.AdRequest;
//import com.google.ads.AdSize;
//import com.google.ads.AdView;

public class GuessNumber extends Activity implements OnClickListener {
    
    private static final String TAG = Const.APP_TAG;
    
    private Button btnNewGame;
    private Button btnComputeGame;
    private Button btnContinueGame;
    private Button btnList;

    private Button btnAbout;
//    private AdView adView;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        findViews();
        checkContinue();
        
//        adView = new AdView(this, AdSize.BANNER, "ca-app-pub-3208820061030563/4885647533");
        
        LinearLayout layout = (LinearLayout)findViewById(R.id.adLayout);

        // 在其中加入 adView
//        layout.addView(adView);

        // 啟用泛用請求，並隨廣告一起載入
//        AdRequest adRequest = new AdRequest();
//        adRequest.addTestDevice(AdRequest.TEST_EMULATOR);
//        adRequest.addTestDevice("D7987FEE57E95D986D0C3B3CB6CD3539");    //samsung gio s5660
//        adView.loadAd(adRequest);
    }
    
    @Override
    protected void onResume() {
        Log.d(TAG, "onResume GuessNumber...");
        super.onResume();
        checkContinue();
    }
    
    @Override
    protected void onStart() {
        Log.d(TAG, "onStart GuessNumber...");
        super.onStart();
    }
    
    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart GuessNumber...");
        super.onRestart();
    }
    
    @Override
    protected void onDestroy() {
//        adView.destroy();
        super.onDestroy();
    }
    
    private void findViews() {
        btnNewGame = (Button) findViewById(R.id.btnNewGame);
        btnContinueGame = (Button) findViewById(R.id.btnContinueGame);
        btnComputeGame = (Button) findViewById(R.id.btnComputeGame);
        btnList = (Button) findViewById(R.id.btnList);
        btnAbout = (Button) findViewById(R.id.btnAbout);
        
        btnNewGame.setOnClickListener(this);
        btnComputeGame.setOnClickListener(this);
        btnContinueGame.setOnClickListener(this);
        btnList.setOnClickListener(this);
        btnAbout.setOnClickListener(this);
    }
    
    private void checkContinue() {
        btnContinueGame.setVisibility(Button.GONE);
        String[] files = fileList();
        if (files != null) {
            for (String f : files) {
                if (f.equals(Game.FILE_NAME)) {
                    btnContinueGame.setVisibility(Button.VISIBLE);
                }
            }
        }
    }
    
    @Override
    public void onClick(View v) {
        if (v == btnNewGame) {
            //自我挑戰
            startGameActivity(1, false, 0);
        } else if (v == btnComputeGame) {
            //跟電腦對戰
            new AlertDialog.Builder(GuessNumber.this)
            .setTitle(R.string.level_title)
            .setPositiveButton(R.string.level_easy_label,
                new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialoginterface, int i){
                    startGameActivity(2, false, 70);
                }})
            .setNeutralButton(R.string.level_middle_label,
                new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialoginterface, int i){
                    startGameActivity(2, false, 85);
                }})
            .setNegativeButton(R.string.level_hard_label,
                new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialoginterface, int i){
                    startGameActivity(2, false, 100);
                }})
            .show();
            return;
        } else if (v == btnContinueGame) {
            //單人遊戲才存檔.
            startGameActivity(1, true, 0);
        } else if (v == btnList) {
            Intent intent = new Intent();
            intent.setClass(this, RankList.class);
            startActivity(intent);
        } else if (v == btnAbout) {
            Intent intent = new Intent();
            intent.setClass(this, About.class);
            startActivity(intent);
        }
    }
    
    private void startGameActivity(int gameType, boolean isContinue, int computeRate) {
        Intent intent = new Intent();
        intent.setClass(this, Game.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("continue", isContinue);
        bundle.putInt("gameType", gameType);
        bundle.putInt("computeRate", computeRate);
        intent.putExtras(bundle);
        startActivity(intent);
    }
    
}