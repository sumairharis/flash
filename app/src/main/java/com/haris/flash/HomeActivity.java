package com.haris.flash;

import android.content.Intent;
import android.support.v7.app.ActionBar;
        import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;;


public class HomeActivity extends AppCompatActivity {
ImageButton send;
    ImageButton receive;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        View decorView = getWindow().getDecorView();    //hidid status bar ..
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
                                                     //hide actionbar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        send=(ImageButton)findViewById(R.id.imageButton6);
        receive=(ImageButton)findViewById(R.id.imageButton2);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(HomeActivity.this,Sender.class);
                startActivity(intent);
            }
        });


        receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(HomeActivity.this,Receiver.class);

                startActivity(intent);
            }
        });
    }
}
