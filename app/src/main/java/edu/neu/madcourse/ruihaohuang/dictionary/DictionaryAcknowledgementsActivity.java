package edu.neu.madcourse.ruihaohuang.dictionary;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import edu.neu.madcourse.ruihaohuang.R;
import edu.neu.madcourse.ruihaohuang.about.AboutActivity;

public class DictionaryAcknowledgementsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary_acknowledgements);
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
        // reference: http://stackoverflow.com/questions/12119800/android-set-link-with-a-href-in-textview
        acknowledgementsTextView.setClickable(true);
        acknowledgementsTextView.setMovementMethod(LinkMovementMethod.getInstance());
        acknowledgementsTextView.setText(AboutActivity.fromHtml(getString(R.string.acknowledgements_dictionary)));
    }

}
