package edu.fandm.research.vpnplus.Application.AppInterface;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import edu.fandm.research.vpnplus.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
