package edu.neu.madcourse.ruihaohuang.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import edu.neu.madcourse.ruihaohuang.R;
import edu.neu.madcourse.ruihaohuang.communication.CommunicationActivity;

/**
 * Created by huangruihao on 2017/3/6.
 */

public class PairTask extends AsyncTask<Void, Void, Void> {
    private final static String tag = "PairTask";

    private String username;
    private String token;
    private Activity activity;
    private Context context;
    private DatabaseReference databaseReference;

    private ProgressDialog dialog;
    private boolean paired = false;

    private String pairToken;
    private String pairUsername;

    public PairTask(String username, String token, Activity activity, Context context, DatabaseReference databaseReference) {
        this.username = username;
        this.token = token;
        this.activity = activity;
        this.context = context;
        this.databaseReference = databaseReference;
        dialog = new ProgressDialog(context);
        dialog.setTitle(context.getString(R.string.text_pairing));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        databaseReference.child("users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                findPair(dataSnapshot);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                findPair(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPreExecute() {
        dialog.show();
    }

    @Override
    protected Void doInBackground(Void[] params) {
        while (!paired) {
            if (paired) {
                break;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        ((CommunicationActivity) activity).setPaired(pairUsername, pairToken);
        dialog.dismiss();
    }

    private void findPair(DataSnapshot dataSnapshot) {
        String pairToken = (String) dataSnapshot.getValue();
        if (!pairToken.equals(token)) {
            paired = true;
            this.pairToken = pairToken;
            this.pairUsername = dataSnapshot.getKey();
            databaseReference.child("users").child(username).removeValue();
        }
    }
}
