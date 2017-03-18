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
import edu.neu.madcourse.ruihaohuang.twoplayerscroggle.TwoPlayerScroggleGameActivity;

/**
 * Created by huangruihao on 2017/3/6.
 */

public class PairTask extends AsyncTask<Void, Void, Void> {
    private final static String tag = "PairTask";
    private final static int CONTAINS_GO_FIRST_INFO_MIN_LENGTH = 2;
    private final static char OPPONENT_GO_FIRST = '1';
    private final static char PLAYER_GO_FIRST = '0';

    private String username;
    private String token;
    private Activity activity;
    private Context context;
    private DatabaseReference databaseReference;

    private ProgressDialog dialog;
    private boolean paired = false;

    private String pairToken;
    private String pairUsername;

    private String callerTag;

    private int goFirst;

    // goFirst: 1 for yes, 0 for no, -1 for don't know
    public PairTask(String username, String token, Activity activity, Context context,
                    DatabaseReference databaseReference, String callerTag) {
        this.username = username;
        this.token = token;
        this.activity = activity;
        this.context = context;
        this.databaseReference = databaseReference;
        this.callerTag = callerTag;
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

    public PairTask(String username, String token, Activity activity, Context context,
                    DatabaseReference databaseReference, String callerTag, int goFirst) {
        this(username, token, activity, context, databaseReference, callerTag);
        this.goFirst = goFirst;
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
        switch (callerTag) {
            case CommunicationActivity.tag:
                ((CommunicationActivity) activity).setPaired(pairUsername, pairToken);
                dialog.dismiss();
                break;
            case TwoPlayerScroggleGameActivity.tag:
                ((TwoPlayerScroggleGameActivity) activity).initGame(pairUsername, pairToken, goFirst);
                dialog.dismiss();
                break;
        }

    }

    private void findPair(DataSnapshot dataSnapshot) {
        String pairToken = (String) dataSnapshot.getValue();
        switch (callerTag) {
            case CommunicationActivity.tag:
                if (!pairToken.equals(token)) {
                    this.pairToken = pairToken;
                    this.pairUsername = dataSnapshot.getKey();
                    paired = true;
                }
                break;
            case TwoPlayerScroggleGameActivity.tag:
                if (!pairToken.equals(token)) {
                    this.pairToken = pairToken;
                    // contains go first information
                    String tempPairUsername = dataSnapshot.getKey();
                    if (tempPairUsername.length() >= CONTAINS_GO_FIRST_INFO_MIN_LENGTH
                            && tempPairUsername.charAt(tempPairUsername.length() - 2) == ' ') {
                        if (tempPairUsername.charAt(tempPairUsername.length() - 1) == OPPONENT_GO_FIRST) {
                            goFirst = 0;
                        } else if (tempPairUsername.charAt(tempPairUsername.length() - 1) == PLAYER_GO_FIRST) {
                            goFirst = 1;
                        }
                        this.pairUsername = tempPairUsername.substring(0, tempPairUsername.length() - 2);
                    } else {
                        this.pairUsername = dataSnapshot.getKey();
                    }
                    paired = true;
                }
                break;
        }

    }
}
