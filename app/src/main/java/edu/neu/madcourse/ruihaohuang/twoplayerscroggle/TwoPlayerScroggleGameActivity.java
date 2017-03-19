package edu.neu.madcourse.ruihaohuang.twoplayerscroggle;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import edu.neu.madcourse.ruihaohuang.dictionary.DictionaryHelper;
import edu.neu.madcourse.ruihaohuang.utils.BoardAssignHelper;
import edu.neu.madcourse.ruihaohuang.utils.MyMessagingService;
import edu.neu.madcourse.ruihaohuang.utils.PairTask;
import edu.neu.madcourse.ruihaohuang.utils.Tile;

public class TwoPlayerScroggleGameActivity extends AppCompatActivity {

    public static final String tag = "TwoPlayerScroggleGameActivity";

    private final static String SERVER_KEY = "key=AAAAfWGUWtM:APA91bFNCTZfeLBBYem4PEhwq3FW-VQzTdoMcdbPzrn8kOQnHs0SRkYnyTle22pjE_cMAQNmk-5ssizDGAlamjvoKR-l51ZZS1YvIbwAklmmFR0lEsAjR02IyCiPrXAxX5WjIJnI_cxX";

    private static final int MILLISECONDS_PER_SECOND = 1000;
    private static final int BOARD_SIZE = Tile.BOARD_SIZE;

    public static final String TITLE_BOARD = "twoplayerscroggle.board";

    private Tile board;
    // BOARD_SIZE * BOARD_SIZE large tiles
    private Tile[] largeTiles = new Tile[BOARD_SIZE * BOARD_SIZE];
    // BOARD_SIZE * BOARD_SIZE small tiles in each large tile
    private Tile[][] smallTiles = new Tile[BOARD_SIZE * BOARD_SIZE][BOARD_SIZE * BOARD_SIZE];

    private static int smallTileIds[] = {R.id.scroggle_small_0, R.id.scroggle_small_1,
            R.id.scroggle_small_2, R.id.scroggle_small_3, R.id.scroggle_small_4,
            R.id.scroggle_small_5, R.id.scroggle_small_6, R.id.scroggle_small_7,
            R.id.scroggle_small_8};
    private static int largeTileIds[] = {R.id.scroggle_large_0, R.id.scroggle_large_1,
            R.id.scroggle_large_2, R.id.scroggle_large_3, R.id.scroggle_large_4,
            R.id.scroggle_large_5, R.id.scroggle_large_6, R.id.scroggle_large_7,
            R.id.scroggle_large_8};

    TextView phaseText;
    TextView timerText;
    TextView myScoreText;
    TextView opponentScoreText;

    TwoPlayerScroggleHelper scroggleHelper;
    BoardAssignHelper boardAssignHelper;

    private DatabaseReference databaseReference;

    private String username;
    private String token;
    private String opponentToken;
    private String opponentUsername;
    private boolean willGoFirst;

    private BroadcastReceiver receiver;

    ProgressDialog assigningBoardDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_player_scroggle_game);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        phaseText = (TextView) findViewById(R.id.text_phase);
        timerText = (TextView) findViewById(R.id.text_timer);
        myScoreText = (TextView) findViewById(R.id.text_my_score);
        opponentScoreText = (TextView) findViewById(R.id.text_opponent_score);

        phaseText.setText(String.format(getString(R.string.text_phase), "ONE"));
        phaseText.setTypeface(null, Typeface.BOLD);
        timerText.setText(String.format(getString(R.string.text_timer), 10));
        myScoreText.setText(String.format(getString(R.string.text_my_score), 0));
        opponentScoreText.setText(String.format(getString(R.string.text_opponent_score),
                getString(R.string.text_opponent), 0));

        databaseReference = FirebaseDatabase.getInstance().getReference();

        boardAssignHelper = new BoardAssignHelper(getApplicationContext());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.button_submit);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scroggleHelper.checkWord();
                myScoreText.setText(String.format(getString(R.string.text_score), scroggleHelper.getMyScore()));
            }
        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String content = intent.getStringExtra(MyMessagingService.COPA_MESSAGE);
                String title = content.split(MyMessagingService.splitter)[0];
                String body = content.split(MyMessagingService.splitter)[1];
                switch (title) {
                    case TITLE_BOARD:
                        initBoard();
                        boardAssignHelper.assignBoard(board, body);
                        assigningBoardDialog.dismiss();
                        break;
                }
            }
        };

        pair();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (Build.VERSION.SDK_INT >= 23) {
            switch (requestCode) {
                case DictionaryHelper.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                    if (grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        scroggleHelper.initializeDb();
                    }
                    return;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((receiver),
                new IntentFilter(MyMessagingService.COPA_RESULT)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (databaseReference != null && username != null) {
            removeAllPossibleData(username);
        }
        if (receiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        }
        if (opponentToken != null) {
            sendLeaveMessage();
        }
    }

    private void pair() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        final View rootView = inflater.inflate(R.layout.dialog_enter_username, null);

        builder.setView(rootView)
                .setTitle(getString(R.string.text_enter_username))
                .setPositiveButton(getString(R.string.button_confirm), null);
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
                                if (!u.isEmpty() && !u.contains(" ") && !dataSnapshot.child("users").hasChild(u)) {
                                    username = u;
                                    token = FirebaseInstanceId.getInstance().getToken();
                                    int goFirst = -1;
                                    // wait for his opponent. Decide who will go first
                                    if (dataSnapshot.child("users").getChildrenCount() == 0) {
                                        goFirst = generateGoFirst();
                                        willGoFirst = (goFirst == 1);
                                        databaseReference.child("users")
                                                .child(username + " " + String.valueOf(goFirst))
                                                .setValue(token);
                                    } else {
                                        databaseReference.child("users")
                                                .child(username).setValue(token);
                                    }
                                    (new PairTask(username, token,
                                            TwoPlayerScroggleGameActivity.this,
                                            TwoPlayerScroggleGameActivity.this, databaseReference, tag, goFirst))
                                            .execute();
                                    dialog.dismiss();
                                } else if (u.isEmpty()) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.toast_no_username), Toast.LENGTH_LONG).show();
                                } else if (u.contains(" ")) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.toast_username_contains_space), Toast.LENGTH_LONG).show();
                                } else if (dataSnapshot.child("users").hasChild(u)
                                        || dataSnapshot.child("users").hasChild(u + " 0")
                                        || dataSnapshot.child("users").hasChild(u + " 1")) {
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
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void initGame(String opponentUsername, String opponentToken, int goFirst) {
        this.opponentUsername = opponentUsername;
        this.opponentToken = opponentToken;
        removeAllPossibleData(opponentUsername);
        willGoFirst = (goFirst == 1);
        if (willGoFirst) {
            initBoard();  // the one who goes first decides the board
            // set content for all the tiles following the rule
            // that each BOARD_SIZE * BOARD_SIZE tile can form a word
            boardAssignHelper.assignBoard(board);
            String boardArrangement = getBoardArrangement();
            sendMessage(TITLE_BOARD, boardArrangement);
        } else {
            assigningBoardDialog = new ProgressDialog(TwoPlayerScroggleGameActivity.this);
            assigningBoardDialog.setCancelable(false);
            assigningBoardDialog.setCanceledOnTouchOutside(false);
            assigningBoardDialog.setTitle(getString(R.string.text_assigning_board));
            assigningBoardDialog.show();
            initBoard();
        }
        opponentScoreText.setText(String.format(getString(R.string.text_opponent_score),
                opponentUsername, 0));
        Toast.makeText(getApplicationContext(), String.format(getString(R.string.toast_show_opponent),
                opponentUsername, willGoFirst ? getString(R.string.toast_you): opponentUsername),
                Toast.LENGTH_LONG).show();
        scroggleHelper = new TwoPlayerScroggleHelper(getApplicationContext(), TwoPlayerScroggleGameActivity.this,
                board);
    }

    private void initBoard() {
        // initialize all the Tiles by calling their constructors
        board = new Tile();
        for (int large = 0; large < BOARD_SIZE * BOARD_SIZE; ++large) {
            largeTiles[large] = new Tile();
            for (int small = 0; small < BOARD_SIZE * BOARD_SIZE; ++small) {
                smallTiles[large][small] = new Tile();
            }
            largeTiles[large].setSubTiles(smallTiles[large]);
        }
        board.setSubTiles(largeTiles);

        // set views for all the Tiles
        View rootView = findViewById(R.id.scroggle_board);
        board.setView(rootView);
        for (int large = 0; large < BOARD_SIZE * BOARD_SIZE; ++large) {
            View outer = findViewById(largeTileIds[large]);
            largeTiles[large].setView(outer);
            for (int small = 0; small < BOARD_SIZE * BOARD_SIZE; ++small) {
                Button inner = (Button) outer.findViewById(smallTileIds[small]);
                smallTiles[large][small].setView(inner);
                final int l = large;
                final int s = small;
                inner.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        scroggleHelper.selectSmallTile(l, s);
                    }
                });
            }
        }
    }

    private void sendLeaveMessage() {
        // TODO: send message to opponent if you player leaves
    }

    private int generateGoFirst() {
        // reference: http://stackoverflow.com/questions/28401093/problems-generating-a-math-random-number-either-0-or-1
        return Math.random() < 0.5 ? 0: 1;
    }

    private void removeAllPossibleData(String username) {
        // reference: http://stackoverflow.com/questions/26537720/how-to-delete-remove-nodes-on-firebase
        databaseReference.child("users").child(username).removeValue();
        databaseReference.child("users").child(username + " 0").removeValue();
        databaseReference.child("users").child(username + " 1").removeValue();
    }

    private String getBoardArrangement() {
        String boardAssignment = "";
        for (int i = 0; i < BOARD_SIZE * BOARD_SIZE; ++i) {
            Tile largeTile = board.getSubTiles()[i];
            for (int j = 0; j < BOARD_SIZE * BOARD_SIZE; ++j) {
                boardAssignment += largeTile.getSubTiles()[j].getContent();
            }
        }
        return boardAssignment;
    }

    // send any message to the opponent
    private void sendMessage(final String title, final String body) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject jPayload = new JSONObject();
                JSONObject jNotification = new JSONObject();
                try {
                    jNotification.put("title", title);
                    jNotification.put("body", body);
                    jNotification.put("sound", "default");
                    jNotification.put("badge", "1");
                    jNotification.put("click_action", "OPEN_ACTIVITY_1");

                    jPayload.put("to", opponentToken);
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
