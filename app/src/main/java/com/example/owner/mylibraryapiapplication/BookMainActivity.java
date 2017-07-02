package com.example.owner.mylibraryapiapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created by Mark on 15/10/2016.
 */
public class BookMainActivity extends AppCompatActivity {
    public static final String BOOK_NAME_KEY = "book";
    public static final String BOOK_ID_KEY = "book";
    private EditText searchId;
    private Button btnBookId;
    private EditText searchTitle;
    private Button btnSearch;
    private TextView batteryPercent;
    private Button btnBattery;
    private ProgressBar progBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchId = (EditText) findViewById(R.id.bookById);
        btnBookId = (Button) findViewById(R.id.btnBookId);
        btnBookId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bookId = searchId.getText().toString();
                if( bookId.length() == 0 ) {
                    searchId.setError("ISBN Number is required!");
                    return;
                }
                // Launch the detail view passing the query as an extra
                Intent intent = new Intent(BookMainActivity.this, DisplayByIdActivity.class);
                intent.putExtra(BOOK_ID_KEY, bookId);
                startActivity(intent);
            }
        });

        searchTitle = (EditText) findViewById(R.id.bookSearch);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchTitle.getText().toString();
                if( query.length() == 0 ) {
                    searchTitle.setError("Search word is required!");
                    return;
                }
                // Launch the detail view passing the query as an extra
                Intent intent = new Intent(BookMainActivity.this, BookListActivity.class);
                intent.putExtra(BOOK_NAME_KEY, query);
                startActivity(intent);
            }
        });

        batteryPercent = (TextView) this.findViewById(R.id.batteryLevel);
        btnBattery = (Button) findViewById(R.id.btnBattery);
        progBar =(ProgressBar)findViewById(R.id.batProgBar);
        btnBattery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getBatteryLevels();
            }

        });
    }

    private void getBatteryLevels() {
            BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    context.unregisterReceiver(this);
                    int pbLevel=intent.getIntExtra("level", 0);
                    int currentLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    int level = -1;
                    if (currentLevel >= 0 && scale > 0) {
                        level = (currentLevel * 100) / scale;
                    }
                    String batLevel = "Battery Level Remaining: " + level + "%";
                    batteryPercent.setText(batLevel);
                    // Play around with progress bar settings.
                    if (progBar != null) {
                        progBar.setScaleY(5f);
                        progBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                        progBar.setProgress(pbLevel);
                    }
                }
            };
            IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            registerReceiver(batteryLevelReceiver, batteryLevelFilter);
    }
}
