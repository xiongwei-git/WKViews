package com.android.wkzf;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.android.wkzf.player.PlayerTestActivity;
import com.android.wkzf.view.R;
import com.android.wkzf.view.WKClickViewActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    public void onVideoPlayer(View view) {
        startActivity(new Intent(MainActivity.this, PlayerTestActivity.class));
    }

    public void onClickView(View view) {
        startActivity(new Intent(MainActivity.this, WKClickViewActivity.class));
    }

    public void onClickRecycleView(View view) {
        startActivity(new Intent(MainActivity.this, com.android.wkzf.recycle.MainActivity.class));
    }

}
