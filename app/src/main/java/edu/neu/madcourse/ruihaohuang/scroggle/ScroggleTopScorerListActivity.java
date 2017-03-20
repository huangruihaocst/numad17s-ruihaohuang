package edu.neu.madcourse.ruihaohuang.scroggle;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import edu.neu.madcourse.ruihaohuang.R;

public class ScroggleTopScorerListActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroggle_top_scorer_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        databaseReference = FirebaseDatabase.getInstance().getReference();

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        final ListView scorerListView = (ListView) findViewById(R.id.list_top_scorer);
        final ArrayList<Integer> scorerList = new ArrayList<>();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // reference: http://stackoverflow.com/questions/5804043/convert-long-into-integer
                scorerList.add((int) (long) dataSnapshot.child("score").child("first").getValue());
                scorerList.add((int) (long) dataSnapshot.child("score").child("second").getValue());
                scorerList.add((int) (long) dataSnapshot.child("score").child("third").getValue());
                final ArrayList<String> showScoreList = new ArrayList<>();
                for (int i = 0; i < 3; ++i) {
                    showScoreList.add(getResources().getStringArray(R.array.scorer_list)[i] + ": " + scorerList.get(i));
                }
                // reference: http://stackoverflow.com/questions/3663745/what-is-android-r-layout-simple-list-item-1
                @SuppressWarnings("unchecked")
                ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1,
                        showScoreList) {
                    @Override
                    @NonNull
                    public View getView(int position, View convertView, ViewGroup parent) {
                        TextView textView = new TextView(getApplicationContext());
                        textView.setTextColor(Color.BLACK);
                        textView.setText(showScoreList.get(position));
                        textView.setTextSize(getResources().getDimension(R.dimen.top_scorer_list_text_size));
                        return textView;
                    }
                };
                scorerListView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
