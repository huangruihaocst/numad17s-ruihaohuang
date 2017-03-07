package edu.neu.madcourse.ruihaohuang.communication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
//        databaseReference.child("users").setValue()

    }
}
