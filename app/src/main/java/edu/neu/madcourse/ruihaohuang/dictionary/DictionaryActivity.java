package edu.neu.madcourse.ruihaohuang.dictionary;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import edu.neu.madcourse.ruihaohuang.R;

public class DictionaryActivity extends AppCompatActivity {
    private final String tag = "DictionaryActivity";
    DictionaryHelper helper;
    private ArrayList<String> wordList;
    private WordListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        wordList = new ArrayList<>();
        adapter = new WordListAdapter(getApplicationContext(), wordList);
        ((ListView) findViewById(R.id.word_list)).setAdapter(adapter);

        helper = DictionaryHelper.getInstance(DictionaryActivity.this, this, tag);
        helper.initializeHelper();

        final EditText wordInput = (EditText) findViewById(R.id.edit_word);
        wordInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<String> removeList = new ArrayList<>();
                for (String word: wordList) {
                    if (word.length() > s.length()) {  // deleting
                        removeList.add(word);
                    }
                }
                wordList.removeAll(removeList);  // avoid the Iterator problem
                if (s.length() >= getResources().getInteger(R.integer.min_word_length)
                        && helper.wordExists(s.toString())) {
                    if (!wordList.contains(s.toString())) {
                        wordList.add(s.toString());
                    }
                    adapter.notifyDataSetChanged();
                    // reference: http://stackoverflow.com/questions/12154940/how-to-make-a-beep-in-android
                    new ToneGenerator(AudioManager.STREAM_ALARM, 100)
                            .startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        findViewById(R.id.button_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wordInput.setText("");
                adapter.notifyDataSetChanged();
            }
        });

        findViewById(R.id.button_return).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        findViewById(R.id.button_acknowledgements).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DictionaryActivity.this, DictionaryAcknowledgementsActivity.class));
            }
        });

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (Build.VERSION.SDK_INT >= 23) {
            switch (requestCode) {
                case DictionaryHelper.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        helper.initializeDb();
                    }
                    return;
                default:
            }
        }
    }
}
