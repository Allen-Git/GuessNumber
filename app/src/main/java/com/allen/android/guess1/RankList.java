package com.allen.android.guess1;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class RankList extends Activity {
	
	private static final String TAG = Const.APP_TAG;
	private TableLayout tableRank;
	
	RankSQLiteHelper rankSqlHelper;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rank_list);
        findViews();
        rankSqlHelper = new RankSQLiteHelper(this, null, 1);
        List<Record> result = rankSqlHelper.queryAll();
        Log.d(TAG, "rank list:" + result.size());
        for (Record r : result) {
        	appendRow(tableRank, r);
        }
    }
    
    private void appendRow(TableLayout table, Record r) {
        TableRow row = new TableRow(this);

        TextView rank = new TextView(this);
        rank.setText(String.valueOf(r.getRank()));
        rank.setTextSize(20);
        
        TextView name = new TextView(this);
        name.setText(r.getPlayer());
        name.setTextSize(20);
        
        TextView count = new TextView(this);
        count.setText(String.valueOf(r.getCount()));
        count.setTextSize(20);
        
        TextView timer = new TextView(this);
        timer.setText(String.valueOf(r.getTotalTime()));
        timer.setTextSize(20);
        
        TableRow.LayoutParams params1 = new TableRow.LayoutParams();
        params1.weight = 1;
        TableRow.LayoutParams params2 = new TableRow.LayoutParams();
        params2.weight = 2;
        row.addView(rank, params1);
        row.addView(name, params2);
        row.addView(count, params1);
        row.addView(timer, params1);

        table.addView(row, new TableLayout.LayoutParams());
    }
    
    private void findViews() {
    	tableRank = (TableLayout) findViewById(R.id.tableRank);
	}

    
}