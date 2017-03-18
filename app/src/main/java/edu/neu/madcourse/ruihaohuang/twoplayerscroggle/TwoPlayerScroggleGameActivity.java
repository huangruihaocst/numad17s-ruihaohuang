package edu.neu.madcourse.ruihaohuang.twoplayerscroggle;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import edu.neu.madcourse.ruihaohuang.R;

public class TwoPlayerScroggleGameActivity extends AppCompatActivity {

    TextView phaseText;
    TextView timerText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_player_scroggle_game);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        phaseText = (TextView) findViewById(R.id.text_phase);
        timerText = (TextView) findViewById(R.id.text_timer);

        phaseText.setText(String.format(getString(R.string.text_phase), "ONE"));
        phaseText.setTypeface(null, Typeface.BOLD);
        timerText.setText(String.format(getString(R.string.text_timer), 10));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
