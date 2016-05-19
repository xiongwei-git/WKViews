package com.android.wkzf.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;
import com.wkzf.library.view.WKClickView;

/**
 * Created by Ted on 2015/11/17.
 * test
 */
public class WKClickViewActivity extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_click_view);
        WKClickView wkClickView = (WKClickView)findViewById(R.id.click_view_semicircle);
        wkClickView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Toast.makeText(WKClickViewActivity.this, "熊伟", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
