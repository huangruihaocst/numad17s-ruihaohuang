package edu.neu.madcourse.ruihaohuang.scroggle;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import edu.neu.madcourse.ruihaohuang.R;
import edu.neu.madcourse.ruihaohuang.about.AboutActivity;

public class ScroggleAcknowledgementsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroggle_acknowledgements);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        TextView acknowledgementsTextView = ((TextView) findViewById(R.id.text_acknowledgements));
        acknowledgementsTextView.setClickable(true);
        acknowledgementsTextView.setMovementMethod(LinkMovementMethod.getInstance());
        acknowledgementsTextView.setText(AboutActivity.fromHtml(getString(R.string.acknowledgements_scroggle)));

    }

}
