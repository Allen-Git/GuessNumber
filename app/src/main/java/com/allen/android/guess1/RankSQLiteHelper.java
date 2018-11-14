package com.allen.android.guess1;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class RankSQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG = Const.APP_TAG;
    private final int MAX_RANK = 10;
    private static final String dbName = "rank.db";
    
    public RankSQLiteHelper(Context context, CursorFactory factory, int version) {
        super(context, dbName, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "SQLiteHelper onCreate.");
        String sql = "create table if not exists rank_info(" +
                "id integer primary key, " +    //SQLite 會自動產生流水號 insert
                "name varchar, " +
                "guess_cnt integer, " +
                "guess_timer integer, " +
                "rank integer" +
                ")";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        
    }

    public int getRank(int guessCount, long guessTimer) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("rank_info", new String[] {"rank", "guess_cnt", "guess_timer"}, "guess_cnt <= ?", new String[] {String.valueOf(guessCount)}, null, null, "rank asc");
            int rankIndex = cursor.getColumnIndex("rank");
            int countIndex = cursor.getColumnIndex("guess_cnt");
            int timerIndex = cursor.getColumnIndex("guess_timer");
            
    //      for(cursor.moveToFirst();!(cursor.isAfterLast());cursor.moveToNext()){
            int maxRank = 0;
            for (int i=0; i<cursor.getCount(); i++) {
                cursor.moveToNext();
                maxRank = cursor.getInt(rankIndex);
                if (cursor.getInt(countIndex) == guessCount && cursor.getInt(timerIndex) > guessTimer) {
                    return maxRank;
                }
            }
            maxRank++;
            if (maxRank > MAX_RANK) {
                return -1;
            }
            return maxRank;
        } finally {
            if (cursor != null) {
                try { cursor.close(); } catch (Exception e) {}
            }
            db.close();
        }
    }
    
    public void insertRank(int rank, String name, int guessCount, long guessTimer) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            //將 rank 後之紀錄 + 1
            db.execSQL("update rank_info set rank = rank + 1 where rank >= ?", new Object[] {rank});
            
            //insert
            ContentValues values = new ContentValues();
            values.put("rank", rank);
            values.put("name", name);
            values.put("guess_cnt", guessCount);
            values.put("guess_timer", guessTimer);
            db.insert("rank_info", null, values);
            
            //刪除 MAX_RANK 之後的紀錄
            db.delete("rank_info", "rank > ?", new String[] {String.valueOf(MAX_RANK)});
            
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            db.close();
        }
    }
    
    public List<Record> queryAll() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query("rank_info", null, null, null, null, null, "rank asc");
            int rankIndex = cursor.getColumnIndex("rank");
            int nameIndex = cursor.getColumnIndex("name");
            int countIndex = cursor.getColumnIndex("guess_cnt");
            int timerIndex = cursor.getColumnIndex("guess_timer");
            
            List<Record> result = new ArrayList<Record>();
            for (int i=0; i<cursor.getCount(); i++) {
                cursor.moveToNext();
                Record r = new Record();
                r.setPlayer(cursor.getString(nameIndex));
                r.setRank(cursor.getInt(rankIndex));
                r.setCount(cursor.getInt(countIndex));
                r.setTotalTime(cursor.getLong(timerIndex) / 1000);
                result.add(r);
            }
            return result;
        } finally {
            if (cursor != null) {
                try { cursor.close(); } catch (Exception e) {}
            }
            db.close();
        }
    }
}
