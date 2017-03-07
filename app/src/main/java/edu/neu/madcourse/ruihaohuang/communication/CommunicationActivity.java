package edu.neu.madcourse.ruihaohuang.communication;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

import edu.neu.madcourse.ruihaohuang.R;

public class CommunicationActivity extends AppCompatActivity {

    private final static String SERVER_KEY = "key=AAAAfWGUWtM:APA91bFNCTZfeLBBYem4PEhwq3FW-VQzTdoMcdbPzrn8kOQnHs0SRkYnyTle22pjE_cMAQNmk-5ssizDGAlamjvoKR-l51ZZS1YvIbwAklmmFR0lEsAjR02IyCiPrXAxX5WjIJnI_cxX";
    private String username;
    private String token;
    private String pairToken;  // chat with
    private String pairUsername;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        databaseReference = FirebaseDatabase.getInstance().getReference();

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
    }

    @Override
    protected void onStop() {
        super.onStop();
        // reference: http://stackoverflow.com/questions/26537720/how-to-delete-remove-nodes-on-firebase
        databaseReference.child("users").child(username).removeValue();
    }

    void setPaired(String pairUsername, String pairToken) {
        this.pairUsername = pairUsername;
        this.pairToken = pairToken;
        ((TextView) findViewById(R.id.text_chat_with))
                .setText(String.format(getString(R.string.text_chat_with), pairUsername));
    }
}
