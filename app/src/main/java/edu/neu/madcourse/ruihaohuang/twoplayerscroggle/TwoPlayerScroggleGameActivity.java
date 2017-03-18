package edu.neu.madcourse.ruihaohuang.twoplayerscroggle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
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

import edu.neu.madcourse.ruihaohuang.R;
import edu.neu.madcourse.ruihaohuang.communication.CommunicationMessagingService;
import edu.neu.madcourse.ruihaohuang.utils.BoardAssignHelper;
import edu.neu.madcourse.ruihaohuang.utils.PairTask;
import edu.neu.madcourse.ruihaohuang.utils.Tile;

public class TwoPlayerScroggleGameActivity extends AppCompatActivity {

    public static final String tag = "TwoPlayerScroggleGameActivity";

    private static final int MILLISECONDS_PER_SECOND = 1000;

    private static final int BOARD_SIZE = Tile.BOARD_SIZE;

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

    TwoPlayerScroggleHelper helper;

    private DatabaseReference databaseReference;

    private String username;
    private String token;
    private String opponentToken;
    private String opponentUsername;
    private boolean willGoFirst;

    private BroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_player_scroggle_game);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        phaseText = (TextView) findViewById(R.id.text_phase);
        timerText = (TextView) findViewById(R.id.text_timer);

        phaseText.setText(String.format(getString(R.string.text_phase), "ONE"));
        phaseText.setTypeface(null, Typeface.BOLD);
        timerText.setText(String.format(getString(R.string.text_timer), 10));

        databaseReference = FirebaseDatabase.getInstance().getReference();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: handle submission
            }
        });

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String content = intent.getStringExtra(TwoPlayerMessagingService.COPA_MESSAGE);
                Toast.makeText(getApplicationContext(), content, Toast.LENGTH_LONG).show();
            }
        };

        pair();
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
        initBoard();
        helper = new TwoPlayerScroggleHelper();
        removeAllPossibleData(opponentUsername);
        willGoFirst = (goFirst == 1);
        Toast.makeText(getApplicationContext(), String.valueOf(willGoFirst), Toast.LENGTH_LONG).show();
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
                        helper.selectSmallTile(l, s);
                    }
                });
            }
        }

        // set content for all the tiles following the rule
        // that each BOARD_SIZE * BOARD_SIZE tile can form a word
        BoardAssignHelper boardAssignHelper = new BoardAssignHelper(getApplicationContext());
        boardAssignHelper.assignBoard(board);
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

}
