package com.allen.android.guess1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class Game extends Activity implements OnClickListener {

    private static final String TAG = Const.APP_TAG;
    static final String FILE_NAME = "record.xml";
    
    private static final int WHAT_RESET = 1;
    private static final int WHAT_UPDATE = 2;
    private static final int WHAT_PAUSE = 3;
    private static final int WHAT_RESUME = 4;
    private static final int WHAT_SET = 5;
    private static final int WHAT_STOP = 6;
    
    private Button button0;
    private Button button1;
    private Button button2;
    private Button button3;
    private Button button4;
    private Button button5;
    private Button button6;
    private Button button7;
    private Button button8;
    private Button button9;
    
    private Button btnEnter;
    private Button btnNewGame;
    private Button btnAnswer;
    
    private TextView text1;
    private TextView text2;
    private TextView text3;
    private TextView text4;

    private TextView textTimer;
    private TextView textHistory;
    
    private LinearLayout matcherLayout;
    private TextView textSelfAnswer;
    private TextView textMatchHistory;
    
    private ScrollView scrollView;
    private ScrollView scrollView2;
    
    private TextView[] texts = new TextView[4];
    
    private int currIdx = 0;    //目前在第幾個輸入框
    
    private String answer = "";
    private int guessCount = 0;
    
    private String selfAnswer = ""; //自己設定的答案
    
    private TimerHandler timerHandler;
    private List<String> history;
    private List<String> matchHistory;
    private boolean viewedAnswer;
    private boolean guessFinished;
    
    private int computeRate = 100;  //電腦智力, 1~100 越大越聰明
    
    private Set<String> NUMBER_BOX = new HashSet<String>();
    private Set<String> numberBox = new HashSet<String>();
    
    RankSQLiteHelper rankSqlHelper;
    
    private int gameType = 1;   //1:自我挑戰        2:與電腦對戰     3:網路對戰
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate new...");
        super.onCreate(savedInstanceState);
        
        boolean continueGame = this.getIntent().getExtras().getBoolean("continue");
        gameType = this.getIntent().getExtras().getInt("gameType");
        computeRate = this.getIntent().getExtras().getInt("computeRate");
        
        setContentView(R.layout.game);
        
        findViews();
        
        if (gameType == 1) {
            setTitle(R.string.single_game);
            matcherLayout.setVisibility(LinearLayout.INVISIBLE);
        }
        if (gameType == 2) {
            setTitle(R.string.compute_game);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(getResources().getAssets().open("numbers.txt"), "big5"));
                String line = "";
                while ((line = br.readLine()) != null) {
                    NUMBER_BOX.add(line.trim());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        if (gameType == 3) {
            //網路對戰要打到最後.
            btnNewGame.setVisibility(Button.INVISIBLE);
            btnAnswer.setVisibility(Button.INVISIBLE);
        }
        
        timerHandler = new TimerHandler();
        
        rankSqlHelper = new RankSQLiteHelper(this, null, 1);
        new Thread() {
            public void run() {
                while (true) {
                    if (isInterrupted()) {
                        return;
                    }
                    Message m = new Message();  
                    m.what = WHAT_UPDATE;  
                    timerHandler.sendMessage(m);
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }.start();
        if (continueGame) {
            continueGame();
        } else {
            reset();
        }
    }
    
    @Override
    public void onStart() {
        Log.d(TAG, "onStart...");
        super.onStart();
        
        
    }
    
    private void findViews() {
        button0 = (Button) findViewById(R.id.button0);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        button3 = (Button) findViewById(R.id.button3);
        button4 = (Button) findViewById(R.id.button4);
        button5 = (Button) findViewById(R.id.button5);
        button6 = (Button) findViewById(R.id.button6);
        button7 = (Button) findViewById(R.id.button7);
        button8 = (Button) findViewById(R.id.button8);
        button9 = (Button) findViewById(R.id.button9);
        button0.setTag(Const.BTN_TYPE_NUMBER);
        button1.setTag(Const.BTN_TYPE_NUMBER);
        button2.setTag(Const.BTN_TYPE_NUMBER);
        button3.setTag(Const.BTN_TYPE_NUMBER);
        button4.setTag(Const.BTN_TYPE_NUMBER);
        button5.setTag(Const.BTN_TYPE_NUMBER);
        button6.setTag(Const.BTN_TYPE_NUMBER);
        button7.setTag(Const.BTN_TYPE_NUMBER);
        button8.setTag(Const.BTN_TYPE_NUMBER);
        button9.setTag(Const.BTN_TYPE_NUMBER);
        
        text1 = (TextView) findViewById(R.id.textView1);
        text1.setTag(Const.BTN_TYPE_TEXT_1);
        text2 = (TextView) findViewById(R.id.textView2);
        text2.setTag(Const.BTN_TYPE_TEXT_2);
        text3 = (TextView) findViewById(R.id.textView3);
        text3.setTag(Const.BTN_TYPE_TEXT_3);
        text4 = (TextView) findViewById(R.id.textView4);
        text4.setTag(Const.BTN_TYPE_TEXT_4);
        
        texts[0] = text1;
        texts[1] = text2;
        texts[2] = text3;
        texts[3] = text4;
        
        for (int i=0; i < texts.length; i++) {
            texts[i].setOnClickListener(this);
        }
        
        button0.setOnClickListener(this);
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        button3.setOnClickListener(this);
        button4.setOnClickListener(this);
        button5.setOnClickListener(this);
        button6.setOnClickListener(this);
        button7.setOnClickListener(this);
        button8.setOnClickListener(this);
        button9.setOnClickListener(this);
        
        btnEnter = (Button) findViewById(R.id.btnSingleEnter);
        btnNewGame = (Button) findViewById(R.id.btnNewSingleGame);
        btnAnswer = (Button) findViewById(R.id.btnSingleAnswer);
        
        btnEnter.setTag(Const.BTN_TYPE_ENTER);
        btnNewGame.setTag(Const.BTN_TYPE_NEW_GAME);
        btnAnswer.setTag(Const.BTN_TYPE_ANSWER);
        btnEnter.setOnClickListener(this);
        btnNewGame.setOnClickListener(this);
        btnAnswer.setOnClickListener(this);
        
        textTimer = (TextView) findViewById(R.id.textSingleTimer);
        textHistory = (TextView) findViewById(R.id.textSingleHistory);
        scrollView = (ScrollView)findViewById(R.id.scrollView1);
        scrollView2 = (ScrollView)findViewById(R.id.scrollView2);
        
        textSelfAnswer = (TextView) findViewById(R.id.textSelfAnswer);
        textMatchHistory = (TextView) findViewById(R.id.textMatchHistory);
        matcherLayout = (LinearLayout)findViewById(R.id.matcherLayout);
    }
    
    public void newGame() {
        //若還沒看答案, 且有猜過, 則需確認
        if (!viewedAnswer && guessCount > 0) {
            new AlertDialog.Builder(Game.this)
            .setTitle(R.string.confirm_title)
            .setMessage(R.string.confirm_renew_body)
            .setPositiveButton(R.string.yes_label,
                new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialoginterface, int i){
                    reset();
                }})
            .setNegativeButton(R.string.no_label,
                new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialoginterface, int i){
                    return;
                }})
            .show();
            return;     //return 的原因是 show AlertDialog 為 Thread 執行.
        }
        reset();
    }
    
    private void setupAnswer(String defaultAnswer) {
        LayoutInflater factory = getLayoutInflater();
        final View textEntryView = factory.inflate(R.layout.set_answer, null);
        final EditText editAnswer = (EditText)textEntryView.findViewById(R.id.editAnswer);
        if (defaultAnswer == null) {
            defaultAnswer = "";
        }
        editAnswer.setText(defaultAnswer);
        new AlertDialog.Builder(Game.this)
            .setTitle(R.string.set_answer_title)
            .setView(textEntryView)
            .setCancelable(false) 
            .setPositiveButton(R.string.set_answer_ok, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    boolean valid = false;
                    String s = editAnswer.getText().toString();
                    if (s.length() == 4) {
                        int[] tmp = new int[10];
                        for (int i=0; i<4; i++) {
                            tmp[Integer.parseInt(String.valueOf(s.charAt(i)))] = 1;
                        }
                        int c = 0;
                        for (int i : tmp) {
                            c += i;
                        }
                        if (c == 4) {
                            valid = true;
                        }
                    }
                    if (!valid) {
                        setupAnswer(editAnswer.getText().toString());
                    } else {
                        Message m = new Message();  
                        m.what = WHAT_RESET;  
                        timerHandler.sendMessage(m);
                        selfAnswer = s;
                        textSelfAnswer.setText(selfAnswer);
                    }
                }
            })
            .setNegativeButton(R.string.set_answer_cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    Game.this.finish();
                }
            })
            .show();
        return;
    }
    
    private void reset() {
        //若為對戰型, 需設底牌
        if (gameType == 2) {
            setupAnswer(null);
            Random r = new Random();
            //computeRate = r.nextInt(80) + 21; //至少 20, 以免太笨 //改讓使用者選難度
            Log.d(TAG, "computer rate:" + computeRate);
        }
        Random r = new Random();
        String tmp = "";
        while (true) {
            String s = String.valueOf(r.nextInt(10));
            if (tmp.contains(s)) {
                continue;
            }
            tmp += s;
            if (tmp.length() == 4) {
                break;
            }
        }
        answer = tmp;
        Log.v(TAG, "the answer:" + answer);
        textHistory.setText("");
        textMatchHistory.setText("");
        resetGuessNum();
        guessCount = 0;
        
        Message m = new Message();  
        m.what = WHAT_RESET;  
        timerHandler.sendMessage(m);
        history = new ArrayList<String>();
        matchHistory = new ArrayList<String>();
        textSelfAnswer.setText("");
        viewedAnswer = false;
        guessFinished = false;
        numberBox.clear();
        numberBox.addAll(NUMBER_BOX);
    }
    
    private void continueGame() {
        try {
            Record r = new Record();
            r.load(openFileInput(FILE_NAME));
            Log.d(TAG, "load totalTime:" + r.getTotalTime());
            Log.d(TAG, "load answer:" + r.getAnswer());
            Log.d(TAG, "load history:" + r.getHistory());
            
            answer = r.getAnswer();
            
            history = new ArrayList<String>();
            for (String num : r.getHistory()) {
                guess(num);
            }
            
            Message m = new Message();
            m.what = WHAT_SET;  
            m.obj = r.getTotalTime();
            timerHandler.sendMessage(m);
            
            deleteFile(FILE_NAME);
        } catch (Exception e) {
            Log.d(TAG, "error when load:" + e, e);
            reset();
        }
    }
    
    private void viewAnswer() {
        if (viewedAnswer) {
            //若已看過答案, 直接秀答案.
            showAnswer(R.string.answer_title);
        } else {
            new AlertDialog.Builder(Game.this)
            .setTitle(R.string.confirm_title)
            .setMessage(R.string.confirm_body)
            .setPositiveButton(R.string.yes_label,
                new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialoginterface, int i){
                    showAnswer(R.string.answer_title);
                }})
            .setNegativeButton(R.string.no_label,
                new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialoginterface, int i){
                    
                }})
            .show();
        }
    }
    
    private void showAnswer(int title) {
        viewedAnswer = true;
        new AlertDialog.Builder(Game.this)
        .setTitle(title)
        .setMessage("答案為:" + answer + ", 要重玩嗎?")        //TODO 看如何解決 hard code 問題.
        .setPositiveButton(R.string.yes_label,
            new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialoginterface, int i){
                reset();
            }})
        .setNegativeButton(R.string.no_label,
            new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialoginterface, int i){
                
            }})
        .show();
    }
    
    private void guess() {
        boolean ok = guess(getGuessNum());
        if (ok && !guessFinished && gameType == 2) {
            //換電腦猜
            if (numberBox.isEmpty()) {
                //電腦已經猜出來了
                return;
            }
            Random r = new Random();
            int t = r.nextInt(numberBox.size());
            String[] tmp = numberBox.toArray(new String[0]);
            String guessNum = tmp[t];
            int[] result = guess(guessNum, selfAnswer);
            int a = result[0];
            int b = result[1];
            textMatchHistory.setText(textMatchHistory.getText() + (guessCount < 10 ? "0" : "") + guessCount + ". " + guessNum + "  " + a + "A" + b + "B\n");
            matchHistory.add(guessNum);
            scrollView2.fullScroll(ScrollView.FOCUS_DOWN);
            if (a == 4) {
                //電腦贏了
                numberBox.clear();
                new AlertDialog.Builder(Game.this)
                .setTitle(R.string.compute_title)
                .setMessage("您的底牌為" + guessNum + ",要再玩一次嗎?")
                .setPositiveButton(R.string.yes_label,
                    new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialoginterface, int i){
                        reset();
                    }})
                .setNegativeButton(R.string.continue_btn,
                    new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialoginterface, int i){
                        
                    }})
                .show();
                return;
            } else {
                //暴力法去除不合的條件..
                Iterator<String> it = numberBox.iterator();
                while (it.hasNext()) {
                    String num = it.next();
                    if (num.equals(guessNum)) { //避免電腦較笨的情況下, 猜到一樣的
                        it.remove();
                        continue;
                    }
                    result = guess(num, guessNum);
                    if (result[0] != a || result[1] != b) {
                        int random = r.nextInt(100);
                        if (computeRate > random) {
                            it.remove();
                        }
                    }
                }
            }
        }
    }
    
    private boolean guess(String guess) {
        if (viewedAnswer) {
            showAnswer(R.string.viewed_answer_title);
            return false;
        }
        if (guessFinished) {
            showSuccessDialog(false);
            return false;
        }
        if (guess.length() < 4) {
            return false;
        }
        int[] result = guess(guess, answer);
        int a = result[0];
        int b = result[1];
        if (history.contains(guess)) {
            new AlertDialog.Builder(Game.this)
            .setMessage("已經猜過此數字("+guess+"), " + a + "A" + b + "B")
            .setPositiveButton(R.string.continue_label,
                new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialoginterface, int i){
                    
                }})
            .show();
            return false;
        }
        guessCount++;
        textHistory.setText(textHistory.getText() + (guessCount < 10 ? "0" : "") + guessCount + ". " + guess + "  " + a + "A" + b + "B\n");
        history.add(guess);
        scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        
        resetGuessNum();
        
        if (a == 4) {
            showSuccessDialog(true);
        }
        return true;
    }
    
    private int[] guess(String guessNum, String answer) {
        int a = 0;
        int b = 0;
        for (int i=0; i<4; i++) {
            for (int j=0; j<4; j++) {
                if (guessNum.charAt(i) == answer.charAt(j)) {
                    if (i == j) {
                        a++;
                    } else {
                        b++;
                    }
                }
            }
        }
        return new int[] {a, b};
    }
    
    private void showSuccessDialog(boolean fromGuess) {
        guessFinished = true;
        if (fromGuess && gameType == 1) {
            final long guessTimer = timerHandler.getTotalTimeMillis();
            final int rank = rankSqlHelper.getRank(guessCount, guessTimer);
            Message m = new Message();
            m.what = WHAT_STOP;
            timerHandler.sendMessage(m);
            if (rank > -1) {
                //進入排行榜
                try {
                    LayoutInflater factory = getLayoutInflater();
                    final View textEntryView = factory.inflate(R.layout.rank, null);
                    final TextView textRank = (TextView)textEntryView.findViewById(R.id.textRank);
                    final EditText editName = (EditText)textEntryView.findViewById(R.id.editName);
                    textRank.setText("您此次排名為:" + rank); //TODO 需解決 hard
                    final SharedPreferences settings = getSharedPreferences(Const.PREF_FILE_NAME, Context.MODE_PRIVATE);
                    String name = settings.getString(Const.PREF_USER_NAME, "");
                    editName.setText(name);
                    new AlertDialog.Builder(Game.this)
        //                .setIcon(R.drawable.alert_dialog_icon)
                        .setTitle(R.string.guess_success_rank)
                        .setView(textEntryView)
                        .setPositiveButton(R.string.rank_button_ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //TODO 研究若沒輸入, 要如何跳出對話框提醒 user 後, 保持此對話框? 是再重來?
                                settings.edit()
                                  .putString(Const.PREF_USER_NAME, editName.getText().toString())
                                  .commit();
                                rankSqlHelper.insertRank(rank, editName.getText().toString(), guessCount, guessTimer);
                                showSuccessDialog(false);
                            }
                        })
                        .setNegativeButton(R.string.rank_button_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                rankSqlHelper.insertRank(rank, "<<no name>>", guessCount, guessTimer);
                                showSuccessDialog(false);
                            }
                        })
                        .show();
                    return;
                } catch (Exception e) {
                    Log.d(TAG, "error:" + e, e);
                }
            }
        }
        int body = R.string.guess_success_body;
        if (gameType == 2 && !numberBox.isEmpty()) {
            //贏了
            body = R.string.guess_success_body_2;
        } 
        new AlertDialog.Builder(Game.this)
        .setTitle(R.string.guess_success_title)
        .setMessage(body)
        .setPositiveButton(R.string.yes_label,
            new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialoginterface, int i){
                reset();
            }})
        .setNegativeButton(R.string.no_label,
            new DialogInterface.OnClickListener(){
            public void onClick(DialogInterface dialoginterface, int i){
                
            }})
        .show();
    }

    private void resetGuessNum() {
        for (TextView t : texts) {
            t.setText("");
            toIndex(0);
        }
    }

    @Override
    public void onClick(View v) {
        Button btn = null;
        if (v instanceof Button) {
            btn = (Button)v;
        }
        switch ((Integer)v.getTag()) {
        case Const.BTN_TYPE_NUMBER:
            setNumber(Integer.parseInt(btn.getText().toString()));
            break;
        case Const.BTN_TYPE_NEW_GAME:
            newGame();
            break;
        case Const.BTN_TYPE_ANSWER:
            viewAnswer();
            break;
        case Const.BTN_TYPE_ENTER:
            guess();
            break;
        case Const.BTN_TYPE_TEXT_1:
            toIndex(0);
            break;
        case Const.BTN_TYPE_TEXT_2:
            toIndex(1);
            break;
        case Const.BTN_TYPE_TEXT_3:
            toIndex(2);
            break;
        case Const.BTN_TYPE_TEXT_4:
            toIndex(3);
            break;
        }
    }
    
    private void setNumber(int v) {
        texts[currIdx].setText("");
        for (TextView t : texts) {
            if (t.getText().equals(String.valueOf(v))) {
                t.setText("");
            }
        }
        texts[currIdx].setText(String.valueOf(v));
        toNext();
    }
    
    private void toNext() {
        toIndex(currIdx + 1);
        if (getGuessNum().length() != 4) {
            //到下一個空白
            if (texts[currIdx].getText().length() == 1) {
                toNext();
            }
        }
    }
    
    private void toIndex(int idx) {
        currIdx = idx % 4;
        for (int i=0; i<texts.length; i++) {
            if (i == currIdx) {
                texts[i].setTextColor(0xffffffff);
                texts[i].setBackgroundResource(R.color.focus_color);
            } else {
                texts[i].setTextColor(Color.GRAY);
                texts[i].setBackgroundResource(R.drawable.bg);
            }
        }
    }
    
    private String getGuessNum() {
        String s = "";
        for (TextView t : texts) {
            s += t.getText();
        }
        return s;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //若是有鍵盤的, 可以直接按鍵盤上的數字
        Log.v(TAG, "the keyCode:" + keyCode);
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            setNumber(keyCode-7);
        }
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
            guess();
        }
        return super.onKeyDown(keyCode, event);
    }
    
    class TimerHandler extends Handler {
        
        private long startTime = System.currentTimeMillis();
        private long totalTimeMillis = 0;
        private boolean paused = false;
        private boolean stop = false;
        
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {  
            case WHAT_UPDATE:
                if (paused || stop) {
                    break;
                }
                long time = getTotalTimeMillis()/1000;
                long min = time / 60;
                long sec = time % 60;
                DecimalFormat df = new DecimalFormat("00");
                textTimer.setText(df.format(min) + ":" + df.format(sec));
                break;
            case WHAT_RESET:
                startTime = System.currentTimeMillis();
                totalTimeMillis = 0;
                paused = false;
                stop = false;
                break;
            case WHAT_PAUSE:
                totalTimeMillis += (System.currentTimeMillis() - startTime);
                paused = true;
                break;
            case WHAT_RESUME:
                startTime = System.currentTimeMillis();
                paused = false;
                break;
            case WHAT_STOP:
                stop = true;
                break;
            case WHAT_SET:
                startTime = System.currentTimeMillis();
                totalTimeMillis = (Long)msg.obj;
                break;
            }  
            super.handleMessage(msg);  
        }
        
        public long getTotalTimeMillis() {
            return totalTimeMillis + System.currentTimeMillis() - startTime;
        }
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
        Message m = new Message();  
        m.what = WHAT_RESUME;  
        timerHandler.sendMessage(m);
        super.onResume();
    }
    
    @Override
    protected void onPause() {
        Log.d(TAG, "onPause...");
        Message m = new Message();  
        m.what = WHAT_PAUSE;  
        timerHandler.sendMessage(m);
        
        //將目前紀錄存起來, 存成檔案
        if (gameType == 1) {
            if (!viewedAnswer && !guessFinished && guessCount > 0) {
                try {
                    Record r = new Record();
                    r.setTotalTime(timerHandler.getTotalTimeMillis());
                    r.setHistory(history);
                    r.setAnswer(answer);
                    r.save(openFileOutput(FILE_NAME, Context.MODE_PRIVATE));
                } catch (Exception e) {Log.d(TAG, "error:" + e, e); }
            }
        }
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
