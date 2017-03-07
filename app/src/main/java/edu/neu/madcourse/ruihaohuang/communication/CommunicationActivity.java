package edu.neu.madcourse.ruihaohuang.communication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import edu.neu.madcourse.ruihaohuang.R;

public class CommunicationActivity extends AppCompatActivity {

    private final static String tag = "CommunicationActivity";
    private final static String SERVER_KEY = "key=AAAAfWGUWtM:APA91bFNCTZfeLBBYem4PEhwq3FW-VQzTdoMcdbPzrn8kOQnHs0SRkYnyTle22pjE_cMAQNmk-5ssizDGAlamjvoKR-l51ZZS1YvIbwAklmmFR0lEsAjR02IyCiPrXAxX5WjIJnI_cxX";

    private String username;
    private String token;
    private String pairToken;  // chat with
    private String pairUsername;

    private DatabaseReference databaseReference;

    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        databaseReference = FirebaseDatabase.getInstance().getReference();

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ((TextView) findViewById(R.id.text_history))
                        .append(pairUsername + ": "
                                + intent.getStringExtra(CommunicationMessagingService.COPA_MESSAGE) + "\n");
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        final View rootView = inflater.inflate(R.layout.dialog_enter_username, null);

        builder.setView(rootView)
                .setTitle(getString(R.string.text_enter_username))
                .setPositiveButton(getString(R.string.button_confirm), null)
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        // reference: http://stackoverflow.com/questions/2620444/how-to-prevent-a-dialog-from-closing-when-a-button-is-clicked
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(final DialogInterface dialog) {
                Button positiveButton = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    EditText usernameEditText = (EditText) rootView.findViewById(R.id.edit_enter_username);
                    @Override
                    public void onClick(View v) {
                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String u = usernameEditText.getText().toString();
                                if (!u.isEmpty() && !dataSnapshot.child("users").hasChild(u)) {
                                    username = u;
                                    token = FirebaseInstanceId.getInstance().getToken();
                                    databaseReference.child("users").child(username).setValue(token);
                                    (new PairTask(username, token,
                                            CommunicationActivity.this, CommunicationActivity.this, databaseReference)).execute();
                                    dialog.dismiss();
                                } else if (u.isEmpty()) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.toast_no_username), Toast.LENGTH_LONG).show();
                                } else if (dataSnapshot.child("users").hasChild(u)) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.toast_username_already_registered), Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        });
        dialog.show();

        final EditText sendEditText = (EditText) findViewById(R.id.edit_send);

        findViewById(R.id.button_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = sendEditText.getText().toString();
                if (!content.isEmpty()) {
                    sendMessage(content);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_content_empty), Toast.LENGTH_LONG).show();
                }
                // reference: http://stackoverflow.com/questions/5308200/clear-text-in-edittext-when-entered
                sendEditText.getText().clear();
                ((TextView) findViewById(R.id.text_history)).append(username + ": " + content + "\n");
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(CommunicationMessagingService.COPA_RESULT)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        // reference: http://stackoverflow.com/questions/26537720/how-to-delete-remove-nodes-on-firebase
        databaseReference.child("users").child(username).removeValue();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    void setPaired(String pairUsername, String pairToken) {
        this.pairUsername = pairUsername;
        this.pairToken = pairToken;
        ((TextView) findViewById(R.id.text_chat_with))
                .setText(String.format(getString(R.string.text_chat_with), pairUsername));
    }

    private void sendMessage(final String content) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject jPayload = new JSONObject();
                JSONObject jNotification = new JSONObject();
                try {
                    jNotification.put("title", null);
                    jNotification.put("body", content);
                    jNotification.put("sound", "default");
                    jNotification.put("badge", "1");
                    jNotification.put("click_action", "OPEN_ACTIVITY_1");

                    jPayload.put("to", pairToken);
                    jPayload.put("priority", "high");
                    jPayload.put("notification", jNotification);

                    URL url = new URL("https://fcm.googleapis.com/fcm/send");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Authorization", SERVER_KEY);
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setDoOutput(true);

                    OutputStream outputStream = conn.getOutputStream();
                    outputStream.write(jPayload.toString().getBytes());
                    outputStream.close();

                    conn.getInputStream();
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}