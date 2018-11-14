package com.allen.android.guess1;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class About extends Activity implements OnClickListener {

    private static final String TAG = Const.APP_TAG;
    
    private TextView textView1;
    private Button button1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate about...");
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.about);
        
        findViews();
        
        textView1.setText("玩法說明：\n" + 
        		"底牌：4位不重覆的數字。\n" + 
        		"先將底牌設定好，讓對手猜，猜出的 4 位數字分別與底牌比對，若有猜中某一位數，且其位置正確，則算 1 A，位置不對則為 1 B。統計後回報給對手。\n" + 
        		"例如底牌設為 5810，對手猜5083。則回報 1 A 2 B (5位置對算1A，8跟0位置不對為2B)。\n" + 
        		"猜完後，換對手猜自己的底牌。看誰先猜到的次數最少即為贏家。\n" + 
        		"\n" + 
        		"1. 個人挑戰：電腦會設定底牌讓您猜，練功用。\n" + 
        		"2. 電腦對戰：您可以設定底牌與電腦對猜。");
    }
    
    @Override
    public void onStart() {
        Log.d(TAG, "onStart...");
        super.onStart();
        
        
    }
    
    private void findViews() {
        button1 = (Button) findViewById(R.id.aboutButton1);
        textView1 = (TextView) findViewById(R.id.aboutTextView1);
        
        button1.setOnClickListener(this);
    }
    
    @Override
    public void onClick(View v) {
        this.finish();
    }
    
    
    @Override
    protected void onStop() {
        Log.d(TAG, "onStop...");
        super.onStop();
    }
    
    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart...");
        super.onRestart();
    }
    
    @Override
    protected void onResume() {
        Log.d(TAG, "onResume...");
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        Log.d(TAG, "onPause...");
        super.onPause();
    }
    
    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy...");
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged...");
        super.onConfigurationChanged(newConfig);
    }
    
}
