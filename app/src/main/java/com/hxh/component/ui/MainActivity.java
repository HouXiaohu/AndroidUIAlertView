package com.hxh.component.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.hxh.component.ui.alertview.AlertView;
import com.hxh.component.ui.alertview.OnItemClickListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AlertView.Builder builder = new AlertView.Builder(this)
                .setTitle(null)
                .setMessage("123123123")
                .setCancelText(null)
                .setConfirmText("hahah ")
                .setOthers(null)
                .setOnItemClickListenerTest(new OnItemClickListener() {
                    @Override
                    public void onItemClick(String item, int position) {

                    }
                });

        builder.build().show();
    }
}
